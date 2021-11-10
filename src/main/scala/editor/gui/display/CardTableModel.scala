package editor.gui.display

import javax.swing.table.AbstractTableModel
import editor.collection.CardList
import editor.database.attributes.CardAttribute
import editor.gui.editor.EditorFrame
import editor.gui.editor.IncludeExcludePanel

class CardTableModel(private var list: CardList, private var characteristics: Seq[CardAttribute], editor: Option[EditorFrame] = None) extends AbstractTableModel {
  override def getColumnCount = characteristics.size
  override def getColumnName(column: Int) = characteristics(column).toString
  override def getColumnClass(column: Int) = characteristics(column).dataType
  override def getRowCount = list.size
  override def getValueAt(row: Int, column: Int) = list.getEntry(row).get(characteristics(column))
  override def isCellEditable(row: Int, column: Int) = editor.isDefined && (characteristics(column) == CardAttribute.COUNT || characteristics(column) == CardAttribute.CATEGORIES)

  override def setValueAt(value: Object, row: Int, column: Int) = if (isCellEditable(row, column)) {
    (characteristics(column), value) match {
      case (CardAttribute.COUNT, i: Integer) => list.set(list.get(row), i)
      case (CardAttribute.CATEGORIES, ie: IncludeExcludePanel) => editor.foreach(_.editInclusion(ie.getIncluded, ie.getExcluded))
      case _ => throw IllegalArgumentException(s"Cannot edit data type ${characteristics(column)} to $value")
    }
    fireTableDataChanged()
  } else throw UnsupportedOperationException("cells cannot be edited")

  def columns = characteristics
  def columns_=(c: Seq[CardAttribute]) = {
    characteristics = c
    fireTableStructureChanged()
  }

  @deprecated def getColumnData(column: Int) = characteristics(column)
  @deprecated def setColumns(c: Seq[CardAttribute]) = columns = c
  @deprecated def setList(l: CardList) = list = l
}