package editor.gui.filter

import _root_.editor.filter.Filter
import _root_.editor.filter.FilterGroup
import _root_.editor.filter.leaf.FilterLeaf
import _root_.editor.gui.generic.ChangeTitleListener
import _root_.editor.gui.settings.SettingsDialog
import _root_.editor.unicode.{_, given}

import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.GridLayout
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.DefaultComboBoxModel
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JMenu
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JSeparator
import _root_.editor.util.PopupMenuListenerFactory

/**
 * Convenience constructors for [[FilterGroupPanel]].
 * @author Alec Roelke
 */
object FilterGroupPanel {
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
  def apply(panels: Seq[FilterPanel[?]] = Seq.empty) = new FilterGroupPanel(panels)
}

/**
 * A panel corresponding to a [[FilterGroup]], containing [[FilterPanel]] children to allow editing filters within
 * the group. Group controls are located at the top of the group; the filter group mode box is on the top left,
 * the add, remove, and group buttons are on the top right, and double-clicking the title (or top-left of the border)
 * of the panel allows for naming the group. If a group is removed, its contents are added to its parent group
 * if any, or the entire filter is cleared if there isn't one.
 * 
 * @constructor create a new filter group panel with a list of panels in it
 * @param panels panels to add to the new filter group panel
 * 
 * @author Alec Roelke
 */
class FilterGroupPanel(panels: Seq[FilterPanel[?]] = Seq.empty) extends FilterPanel[Filter] {
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
    this += FilterSelectorPanel()
    firePanelsChanged()
  })
  editPanel.add(addButton)
  private val removeButton = JButton(Minus)
  removeButton.addActionListener(_ => {
    group.map((g) => {
      g -= this
      g.firePanelsChanged()
    }).getOrElse({
      clear()
      this += FilterSelectorPanel()
      firePanelsChanged()
    })
  })
  editPanel.add(removeButton)
  private val groupButton = JButton(Ellipsis)
  groupButton.addActionListener(_ => {
    group.map(_.engroup(this)).getOrElse({
      val newGroup = FilterGroupPanel()
      newGroup.clear()
      newGroup.modeBox.setSelectedIndex(modeBox.getSelectedIndex)
      children.foreach(newGroup += _)
      clear()
      this += newGroup
    })
    firePanelsChanged()
  })
  editPanel.add(groupButton)
  topPanel.add(editPanel)

  // Panel containing child filters
  private val filtersPanel = Box(BoxLayout.Y_AXIS)
  add(filtersPanel, BorderLayout.CENTER)

  if (panels.isEmpty) this += FilterSelectorPanel() else panels.foreach(this += _)

  addMouseListener(new ChangeTitleListener(this, border, (t) => {
    border.setTitle(t)
    revalidate()
    repaint()
    firePanelsChanged()
  }, _ => Gap, (s) => if (s.isEmpty) 0 else Gap))

  val popup = JPopupMenu()
  val addMenu = JMenu("Add")
  SettingsDialog.settings.editor.categories.presets.foreach((c) => {
    val default = JMenuItem(c.name)
    default.addActionListener(_ => {
      this += FilterGroupPanel(c.filter)
      firePanelsChanged()
    })
    addMenu.add(default)
  })
  if (!SettingsDialog.settings.editor.categories.presets.isEmpty)
    addMenu.add(JSeparator())
  val addGroup = JMenuItem("New group")
  addGroup.addActionListener(_ => {
    this += FilterGroupPanel()
    firePanelsChanged()
  })
  addMenu.add(addGroup)
  val addTerm = JMenuItem("New term")
  addTerm.addActionListener(_ => {
    this += FilterSelectorPanel()
    firePanelsChanged()
  })
  addMenu.add(addTerm)
  popup.add(addMenu)
  val replaceMenu = JMenu("Replace with")
  SettingsDialog.settings.editor.categories.presets.foreach((c) => {
    val replacement = JMenuItem(c.name)
    replacement.addActionListener(_ => {
      setContents(c.filter)
      firePanelsChanged()
    })
    replaceMenu.add(replacement)
  })
  popup.add(replaceMenu)
  val clearItem = JMenuItem("Clear")
  clearItem.addActionListener(_ => {
    clear()
    this += FilterSelectorPanel()
    firePanelsChanged()
  })
  popup.add(clearItem)
  val groupSeparator = JSeparator()
  val ungroupItem = JMenuItem("Ungroup")
  ungroupItem.addActionListener(_ => group.foreach((g) => {
    g -= this
    g.firePanelsChanged()
  }))
  popup.add(ungroupItem)
  val deleteItem = JMenuItem("Delete")
  deleteItem.addActionListener(_ => group.foreach((g) => {
    clear()
    g -= this
    g.firePanelsChanged()
  }))
  popup.add(deleteItem)
  setComponentPopupMenu(popup)
  popup.addPopupMenuListener(PopupMenuListenerFactory.createPopupListener(visible = _ => {
    groupSeparator.setVisible(group.isDefined)
    ungroupItem.setVisible(group.isDefined)
    deleteItem.setVisible(group.exists(_.children.size > 1))
  }))

  /**
   * Add a new filter to the group.
   * @param panel filter panel to add
   */
  def +=(panel: FilterPanel[?]) = {
    children += panel
    filtersPanel.add(panel)
    panel.group = Some(this)
  }

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

  override def filter = FilterGroup(children.map(_.filter), modeBox.getItemAt(modeBox.getSelectedIndex), border.getTitle)

  override def setContents(filter: Filter) = {
    clear()
    val group = filter match {
      case g: FilterGroup => g
      case f => FilterGroup(Seq(f))
    }
    modeBox.setSelectedItem(group.mode)
    border.setTitle(group.comment)
    group.foreach((f) => this += (f match {
      case g: FilterGroup => FilterGroupPanel(g)
      case l: FilterLeaf => FilterSelectorPanel(l)
    }))
  }
}