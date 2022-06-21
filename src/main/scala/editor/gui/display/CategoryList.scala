package editor.gui.display

import editor.collection.Categorization
import editor.gui.ccp.CCPItems
import editor.gui.ccp.data.DataFlavors
import editor.gui.ccp.handler.CategoryTransferHandler
import editor.gui.editor.CategoryEditorPanel
import editor.util.MouseListenerFactory
import editor.util.PopupMenuListenerFactory

import java.awt.Component
import java.awt.Point
import java.awt.Toolkit
import java.awt.event.MouseEvent
import javax.swing.DefaultListModel
import javax.swing.JList
import javax.swing.JOptionPane
import javax.swing.JPopupMenu
import javax.swing.ListSelectionModel
import scala.jdk.CollectionConverters._

/**
 * UI element displaying a list of categorizations by name. Optionally, a line at the bottom can
 * also be displayed to indicate any message in italics, such as how to edit categorizations in place.
 * Categorizations displayed by this list must not contain entries in their white- or blacklists.
 * 
 * @constructor create a new list for displaying categories
 * @param hint italicized string to display at the bottom of the list
 * @param c initial list of categorizations to display
 * 
 * @author Alec Roelke
 */
class CategoryList(hint: String, c: Seq[Categorization] = Seq.empty) extends JList[String] {
  setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

  private val _categories = collection.mutable.ArrayBuffer[Categorization](c:_*)

  /** @return the categorizations being displayed */
  def categories = _categories.toSeq

  private object model extends DefaultListModel[String] {
    override def getElementAt(index: Int) = {
      if (index < _categories.size)
        _categories(index).name
      else if (!hint.isEmpty && index == _categories.size)
        hint
      else
        throw IndexOutOfBoundsException(index)
    }

    override def getSize = _categories.size + (if (hint.isEmpty) 0 else 1)
  }
  setModel(model)

  if (!hint.isEmpty) {
    addMouseListener(MouseListenerFactory.createDoubleClickListener((e) => {
      val index = locationToIndex(e.getPoint)
      val rec = Option(getCellBounds(index, index))
      CategoryEditorPanel.showCategoryEditor(this, rec.filter(_.contains(e.getPoint)).map(_ => _categories(index))).foreach{ s => if (index < 0) addCategory(s) else setCategoryAt(index, s) }
    }))
  }

  setTransferHandler(CategoryTransferHandler(
    () => if (getSelectedIndex < 0 || getSelectedIndex >= _categories.size) null else _categories(getSelectedIndex),
    _ => false, // duplicate categorizations aren't allowed
    (cat) => {
      val row = getSelectedIndex
      if (row < 0 || row >= _categories.size)
        _categories += cat
      else {
        _categories.insert(row, cat)
        model.add(row, cat.name)
      }
      true
    },
    (cat) => removeCategoryAt(_categories.indexOf(cat))
  ))

  private var mouseTriggeredPopup = false
  private val menu = new JPopupMenu {
    override def show(invoker: Component, x: Int, y: Int) = {
      if (mouseTriggeredPopup) {
        val row = locationToIndex(Point(x, y))
        if (row >= 0)
          setSelectedIndex(row)
        else
          clearSelection()
      }
      super.show(invoker, x, y)
    }
  }
  setComponentPopupMenu(menu)

  val ccp = CCPItems(this, true)
  menu.add(ccp.cut)
  menu.add(ccp.copy)
  menu.add(ccp.paste)

  menu.addPopupMenuListener(PopupMenuListenerFactory.createPopupListener(visible = _ => {
    val clipboard = Toolkit.getDefaultToolkit.getSystemClipboard
    ccp.paste.setEnabled(clipboard.isDataFlavorAvailable(DataFlavors.categoryFlavor))
  }))

  private def confirmListClean(spec: Categorization) = {
    if (!spec.whitelist.isEmpty || !spec.blacklist.isEmpty) {
      JOptionPane.showConfirmDialog(this,
        s"Categorization ${spec.name} contains cards in its whitelist or blacklist which will not be present in the preset category. Continue?",
        "Add to Presets",
        JOptionPane.YES_NO_OPTION
      ) == JOptionPane.YES_OPTION
    } else true
  }

  /** @return the number of catgories being displayed */
  def getCount = categories.size

  /**
   * Add a new categorization to the list. If the categorization has entries in its white- or
   * blacklists, confirm with the user that they should be removed first. If not confirmed, no
   * addition is performed.
   * 
   * @param spec categorization to add
   */
  def addCategory(spec: Categorization) = if (confirmListClean(spec)) {
    _categories += spec
    model.addElement(spec.name)
  }

  /**
   * Remove a catgorization from the list.
   * @param index index where the categorization is displayed
   */
  def removeCategoryAt(index: Int): Unit = {
    _categories.remove(index)
    model.remove(index)
  }

  /**
   * Change a categorization in place. If the new categorization has entries in its white- or
   * blacklists, confirm with the user about removing them first. If not confirmed, no change
   * takes place.
   * 
   * @param index index to set to a new categorization
   * @param spec new categorization to display there
   */
  def setCategoryAt(index: Int, spec: Categorization) = if (confirmListClean(spec)) {
    _categories(index) = spec
    model.setElementAt(spec.name, index)
  }

  /** Remove all categorizations from the list. */
  def clear() = {
    _categories.clear()
    model.clear()
  }

  override def locationToIndex(p: Point) = {
    val index = super.locationToIndex(p)
    if (index < _categories.size) index else -1
  }

  override def getPopupLocation(event: MouseEvent) = {
    mouseTriggeredPopup = event != null
    super.getPopupLocation(event)
  }
}