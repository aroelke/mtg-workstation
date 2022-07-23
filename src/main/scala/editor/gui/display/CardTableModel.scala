package editor.gui.display

import editor.collection.CardList
import editor.collection.mutable.Deck
import editor.database.attributes.CardAttribute
import editor.gui.editor.EditorFrame
import editor.gui.editor.IncludeExcludePanel

import javax.swing.table.AbstractTableModel

/**
 * A model telling a [[CardTable]] how to display cards in a [[CardList]].
 * 
 * @constructor create a new card table model
 * @param cards list of cards to display, arranged in rows
 * @param attributes list of attributes of those cards to display, arranged in columns, in order of display
 * @param editor frame housing the table and being used to edit the list, if there is one; set to None to disable editing via table
 * @note The [[CardList]] shown by the table can be modified using outside actions, but [[fireTableDataChanged]] needs to be called to get the table
 * to update in that case.
 * 
 * @author Alec Roelke
 */
class CardTableModel(private var cards: CardList, private var attributes: IndexedSeq[CardAttribute[?, ?]], editor: Option[EditorFrame] = None) extends AbstractTableModel {
  override def getColumnCount = attributes.size
  override def getColumnName(column: Int) = attributes(column).toString
  override def getColumnClass(column: Int) = attributes(column).dataType
  override def getRowCount = cards.size
  override def getValueAt(row: Int, column: Int) = attributes(column)(cards(row))
  override def isCellEditable(row: Int, column: Int) = editor.isDefined && (attributes(column) == CardAttribute.Count || attributes(column) == CardAttribute.Categories)

  override def setValueAt(value: Object, row: Int, column: Int) = if (isCellEditable(row, column)) {
    (cards, attributes(column), value) match {
      case (deck: EditorFrame#DeckData, CardAttribute.Count, i: java.lang.Integer) => deck(row).count = i
      case (deck: EditorFrame#DeckData, CardAttribute.Categories, ie: IncludeExcludePanel) => editor.foreach(_.categories.update(ie.updates.map((c) => c.name -> c).toMap))
      case _ => throw IllegalArgumentException(s"cannot edit data type ${attributes(column)} to $value (${cards.getClass} | ${value.getClass}")
    }
    fireTableDataChanged()
  } else throw UnsupportedOperationException("cells cannot be edited")

  /** @return the attributes of cards to display, in the order they are displayed */
  def columns = attributes

  /**
   * Change the attributes to be displayed by the table.
   * @param c new attributes to show
   */
  def columns_=(c: IndexedSeq[CardAttribute[?, ?]]) = {
    attributes = c
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