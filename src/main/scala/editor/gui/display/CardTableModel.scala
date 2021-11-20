package editor.gui.display

import editor.collection.CardList
import editor.database.attributes.CardAttribute
import editor.gui.editor.EditorFrame
import editor.gui.editor.IncludeExcludePanel

import javax.swing.table.AbstractTableModel

/**
 * A model telling a [[CardTable]] how to display cards in a [[CardList]].
 * 
 * @constructor create a new card table model
 * @param cards list of cards to display, arranged in rows
 * @param characteristics list of characteristics of those cards to display, arranged in columns, in order of display
 * @param editor frame housing the table and being used to edit the list, if there is one; set to None to disable editing via table
 * @note The [[CardList]] shown by the table can be modified using outside actions, but [[fireTableDataChanged]] needs to be called to get the table
 * to update in that case.
 * 
 * @author Alec Roelke
 */
class CardTableModel(private var cards: CardList, private var characteristics: Seq[CardAttribute], editor: Option[EditorFrame] = None) extends AbstractTableModel {
  override def getColumnCount = characteristics.size
  override def getColumnName(column: Int) = characteristics(column).toString
  override def getColumnClass(column: Int) = characteristics(column).dataType
  override def getRowCount = cards.size
  override def getValueAt(row: Int, column: Int) = cards.getEntry(row).get(characteristics(column))
  override def isCellEditable(row: Int, column: Int) = editor.isDefined && (characteristics(column) == CardAttribute.COUNT || characteristics(column) == CardAttribute.CATEGORIES)

  override def setValueAt(value: Object, row: Int, column: Int) = if (isCellEditable(row, column)) {
    (characteristics(column), value) match {
      case (CardAttribute.COUNT, i: Integer) => cards.set(cards.get(row), i)
      case (CardAttribute.CATEGORIES, ie: IncludeExcludePanel) => editor.foreach(_.editInclusion(ie.included, ie.excluded))
      case _ => throw IllegalArgumentException(s"Cannot edit data type ${characteristics(column)} to $value")
    }
    fireTableDataChanged()
  } else throw UnsupportedOperationException("cells cannot be edited")

  /** @return the characteristics of cards to display, in the order they are displayed */
  def columns = characteristics

  /**
   * Change the characteristics to be displayed by the table.
   * @param c new characteristics to show
   */
  def columns_=(c: Seq[CardAttribute]) = {
    characteristics = c
    fireTableStructureChanged()
  }

  /** @return the list of cards shown by the table. */
  def list = cards

  /**
   * Change the list of cards to be displayed by the table.  Updates the table to show the new cards.
   * @param l new list to show
   */
  def list_=(l: CardList) = {
    cards = l
    fireTableDataChanged()
  }
}