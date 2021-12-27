package editor.gui.filter

import _root_.editor.filter.Filter
import _root_.editor.filter.FilterGroup
import _root_.editor.filter.leaf.FilterLeaf
import _root_.editor.gui.generic.ChangeTitleListener
import _root_.editor.util.UnicodeSymbols

import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.GridLayout
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.DefaultComboBoxModel
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JPanel
import scala.jdk.CollectionConverters._

/**
 * Convenience constructors for [[FilterGroupPanel]].
 * @author Alec Roelke
 */
object FilterGroupPanel {
  /** @return an empty [[FilterGroupPanel]] */
  def apply() = new FilterGroupPanel

  /**
   * Create a new [[FilterGroupPanel]] with preset contents.
   * 
   * @param filter contents of the filter
   * @return a new [[FilterGroupPanel]] pre-populated with filters based on the given filter
   */
  def apply(filter: Filter) = {
    val panel = new FilterGroupPanel
    panel.setContents(filter)
    panel
  }

  /**
   * Create a new [[FilterGroupPanel]] with preset contents.
   * 
   * @param panels children of the new [[FilterGroupPanel]]
   * @return a new [[FilterGroupPanel]] with the specified panels as children
   */
  def apply(panels: Seq[FilterPanel[?]]) = {
    val panel = new FilterGroupPanel
    panel.clear()
    panels.foreach(panel += _)
    panel
  }
}

/**
 * A panel corresponding to a [[FilterGroup]], containing [[FilterPanel]] children to allow editing filters within
 * the group. Group controls are located at the top of the group; the filter group mode box is on the top left,
 * the add, remove, and group buttons are on the top right, and double-clicking the title (or top-left of the border)
 * of the panel allows for naming the group. If a group is removed, its contents are added to its parent group
 * if any, or the entire filter is cleared if there isn't one.
 * @author Alec Roelke
 */
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
    group.map((g) => {
      g -= this
      g.firePanelsChanged()
    }).getOrElse({
      clear()
      add(FilterSelectorPanel())
      firePanelsChanged()
    })
  })
  editPanel.add(removeButton)
  private val groupButton = JButton(UnicodeSymbols.ELLIPSIS.toString)
  groupButton.addActionListener(_ => {
    group.map(_.engroup(this)).getOrElse({
      val newGroup = FilterGroupPanel()
      newGroup.clear()
      newGroup.modeBox.setSelectedIndex(modeBox.getSelectedIndex)
      children.foreach(newGroup.add)
      clear()
      add(newGroup)
    })
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

  /**
   * Add a new filter to the group.
   * @param panel filter panel to add
   */
  def +=(panel: FilterPanel[?]) = {
    children += panel
    filtersPanel.add(panel)
    panel.group = Some(this)
  }
  @deprecated def add(panel: FilterPanel[?]) = this += panel

  /**
   * Remove a filter from the group. If that filter is a [[FilterGroup]], add its children to this panel's group.
   * @param panel filter to remove
   */
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

  /** Remove all filters from the group and reset its mode and name. */
  def clear() = {
    children.clear()
    filtersPanel.removeAll()
    modeBox.setSelectedIndex(0)
    border.setTitle("")
  }

  /**
   * Create a new group containing the given [[FilterPanel]], and replace the panel with the new group.
   * @param panel filter to group
   */
  def engroup(panel: FilterPanel[?]) = {
    if (!panel.group.exists(_ == this))
      this += panel

    val index = children.indexOf(panel)
    if (index >= 0) {
      filtersPanel.removeAll()
      val newGroup = FilterGroupPanel(Seq(panel))
      newGroup.group = Some(this)
      children(index) = newGroup
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
      case g: FilterGroup => FilterGroupPanel(g)
      case l: FilterLeaf[?] => FilterSelectorPanel(l)
    }))
  }
}