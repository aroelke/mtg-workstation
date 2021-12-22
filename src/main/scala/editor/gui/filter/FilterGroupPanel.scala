package editor.gui.filter

import _root_.editor.filter.Filter
import javax.swing.BorderFactory
import java.awt.BorderLayout
import javax.swing.JPanel
import java.awt.GridLayout
import _root_.editor.filter.FilterGroup
import _root_.editor.filter.leaf.FilterLeaf
import java.awt.FlowLayout
import javax.swing.JComboBox
import javax.swing.DefaultComboBoxModel
import javax.swing.JButton
import _root_.editor.util.UnicodeSymbols
import javax.swing.Box
import javax.swing.BoxLayout
import _root_.editor.gui.generic.ChangeTitleListener
import scala.jdk.CollectionConverters._

class FilterGroupPanel extends FilterPanel[Filter] {
  private val Gap = 10

  private val children = collection.mutable.Buffer[FilterPanel[?]]()

  private val border = BorderFactory.createTitledBorder("")
  setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(Gap, Gap, Gap, Gap), border))
  setLayout(BorderLayout())

  // Panel containing the mode selector and edit buttons
  private val topPanel = JPanel(GridLayout(1, 2))
  add(topPanel, BorderLayout.NORTH)

  // Mode selection combo box
  private val modePanel = JPanel(FlowLayout(FlowLayout.LEFT))
  private val modeBox = JComboBox[FilterGroup.Mode]()
  modeBox.setModel(DefaultComboBoxModel(FilterGroup.Mode.values))
  modePanel.add(modeBox)
  topPanel.add(modePanel)
  
  // Add, remove, and group buttons
  private val editPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
  private val addButton = JButton("+")
  addButton.addActionListener(_ => {
    add(FilterSelectorPanel())
    firePanelsChanged()
  })
  editPanel.add(addButton)
  private val removeButton = JButton(UnicodeSymbols.MINUS.toString)
  removeButton.addActionListener(_ => {
    if (group == null) {
      clear()
      add(FilterSelectorPanel())
      firePanelsChanged()
    } else {
      group.remove(this)
      group.firePanelsChanged()
    }
  })
  editPanel.add(removeButton)
  private val groupButton = JButton(UnicodeSymbols.ELLIPSIS.toString)
  groupButton.addActionListener(_ => {
    if (group == null) {
      val newGroup = FilterGroupPanel()
      newGroup.clear()
      newGroup.modeBox.setSelectedIndex(modeBox.getSelectedIndex)
      children.foreach(newGroup.add)
      clear()
      add(newGroup)
    }
    else
      group.engroup(this)
    firePanelsChanged()
  })
  editPanel.add(groupButton)
  topPanel.add(editPanel)

  // Panel containing child filters
  private val filtersPanel = Box(BoxLayout.Y_AXIS)
  add(filtersPanel, BorderLayout.CENTER)

  add(FilterSelectorPanel())

  addMouseListener(new ChangeTitleListener(this, border, (t) => {
    border.setTitle(t)
    revalidate()
    repaint()
    firePanelsChanged()
  }, _ => Gap, (s) => if (s.isEmpty) 0 else Gap))

  def +=(panel: FilterPanel[?]) = {
    children += panel
    filtersPanel.add(panel)
    panel.group = this
  }
  @deprecated def add(panel: FilterPanel[?]) = this += panel

  def -=(panel: FilterPanel[?]) = {
    if (children.contains(panel)) {
      panel match {
        case g: FilterGroupPanel =>
          // Make this insert in place of the old group
          filtersPanel.remove(g)
          children -= g
          g.children.foreach(this += _)
        case p =>
          if (children.size > 1) {
            filtersPanel.remove(p)
            children -= p
          }
      }
    }
  }
  @deprecated def remove(panel: FilterPanel[?]) = this -= panel

  def clear() = {
    children.clear()
    filtersPanel.removeAll()
    modeBox.setSelectedIndex(0)
    border.setTitle("")
  }

  def engroup(panel: FilterPanel[?]): Unit = {
    if (panel.group != this)
      add(panel)

    val index = children.indexOf(panel)
    if (index >= 0) {
      filtersPanel.removeAll()
      val newGroup = FilterGroupPanel()
      newGroup.clear()
      newGroup.add(panel)
      children(index) = newGroup
      newGroup.group = this
      children.foreach(filtersPanel.add)
    }
  }

  override def filter = {
    val group = FilterGroup()
    group.mode = modeBox.getItemAt(modeBox.getSelectedIndex)
    group.comment = border.getTitle
    children.foreach((c) => group.addChild(c.filter))
    group
  }

  override def setContents(filter: Filter) = {
    clear()
    val group = filter match {
      case g: FilterGroup => g
      case f => FilterGroup(f)
    }
    modeBox.setSelectedItem(group.mode)
    border.setTitle(group.comment)
    group.asScala.foreach((f) => add(f match {
      case g: FilterGroup =>
        val panel = FilterGroupPanel()
        panel.setContents(g)
        panel
      case l: FilterLeaf[?] =>
        val panel = FilterSelectorPanel()
        panel.setContents(l)
        panel
    }))
  }
}