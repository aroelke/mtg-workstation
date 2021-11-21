package editor.gui.display

import javax.swing.JList
import editor.collection.deck.Category
import javax.swing.ListSelectionModel
import javax.swing.DefaultListModel
import editor.util.MouseListenerFactory
import editor.gui.editor.CategoryEditorPanel
import editor.gui.ccp.handler.CategoryTransferHandler
import javax.swing.JPopupMenu
import java.awt.Component
import java.awt.Point
import editor.gui.ccp.CCPItems
import editor.util.PopupMenuListenerFactory
import java.awt.Toolkit
import editor.gui.ccp.data.DataFlavors
import java.awt.event.MouseEvent
import javax.swing.JOptionPane
import scala.jdk.CollectionConverters._

class CategoryList(hint: String, c: Seq[Category] = Seq.empty) extends JList[String] {
  setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

  private val _categories = collection.mutable.ArrayBuffer[Category](c:_*)
  def categories = _categories.toSeq

  private object model extends DefaultListModel[String] {
    override def getElementAt(index: Int) = {
      if (index < _categories.size)
        _categories(index).getName
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
    _ => false, // duplicate categories aren't allowed
    (cat) => {
      val row = getSelectedIndex
      if (row < 0 || row >= _categories.size)
        _categories += cat
      else {
        _categories.insert(row, cat)
        model.add(row, cat.getName)
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

  menu.addPopupMenuListener(PopupMenuListenerFactory.createVisibleListener(_ => {
    val clipboard = Toolkit.getDefaultToolkit.getSystemClipboard
    ccp.paste.setEnabled(clipboard.isDataFlavorAvailable(DataFlavors.categoryFlavor))
  }))

  private def confirmListClean(spec: Category) = {
    if (!spec.getWhitelist.isEmpty || !spec.getBlacklist.isEmpty) {
      JOptionPane.showConfirmDialog(this,
        s"Category ${spec.getName} contains cards in its whitelist or blacklist which will not be present in the preset category. Continue?",
        "Add to Presets",
        JOptionPane.YES_NO_OPTION
      ) == JOptionPane.YES_OPTION
    } else true
  }

  def getCount = categories.size

  def getCategoryAt(index: Int) = _categories(index)

  def addCategory(spec: Category) = if (confirmListClean(spec)) {
    val copy = Category(spec)
    copy.getBlacklist.asScala.foreach(copy.include(_))
    copy.getWhitelist.asScala.foreach(copy.exclude(_))
    _categories += copy
    model.addElement(copy.getName)
  }

  def removeCategoryAt(index: Int) = {
    _categories.remove(index)
    model.remove(index)
  }

  def setCategoryAt(index: Int, spec: Category) = if (confirmListClean(spec)) {
    val copy = Category(spec)
    copy.getBlacklist.asScala.foreach(copy.include(_))
    copy.getWhitelist.asScala.foreach(copy.exclude(_))
    _categories(index) = copy
    model.setElementAt(copy.getName, index)
  }

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