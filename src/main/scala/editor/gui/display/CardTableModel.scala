package editor.gui.display

import editor.collection.CardList
import editor.collection.mutable.Deck
import editor.database.attributes.CardAttribute
import editor.gui.ElementAttribute
import editor.gui.editor.EditorFrame
import editor.gui.editor.IncludeExcludePanel

import javax.swing.table.AbstractTableModel
import scala.annotation.targetName

/**
 * Companion object containing convenience constructors for [[CardTableModel]].
 * @author Alec Roelke
 */
object CardTableModel {
  /**
   * Create a new [[CardTableModel]] using [[ElementAttribute]]s and an [[EditorFrame]].
   * 
   * @param cards list of cards to display
   * @param attributes inital attributes to display
   * @param editor parent frame
   * @return a new [[CardTableModel]] for the frame
   */
  @targetName("element_some_apply")
  def apply(cards: CardList, attributes: IndexedSeq[ElementAttribute[?, ?]], editor: EditorFrame): CardTableModel = new CardTableModel(cards, attributes, Some(editor))

  /**
   * Create a new [[CardTableModel]] using [[ElementAttribute]]s without an [[EditorFrame]].
   * 
   * @param cards list of cards to display
   * @param attributes initial attributes to display
   * @return a new, frameless [[CardTableModel]]
   */
  @targetName("element_none_apply")
  def apply(cards: CardList, attributes: IndexedSeq[ElementAttribute[?, ?]]): CardTableModel = new CardTableModel(cards, attributes, None)

  /**
   * Create a new [[CardTableModel]] using [[CardAttribute]]s and an [[EditorFrame]].
   * 
   * @param cards list of cards to display
   * @param attributes initial attributes to display
   * @param editor parent frame
   * @return a new [[CardTableModel]] for the frame
   */
  @targetName("card_some_apply")
  def apply(cards: CardList, attributes: IndexedSeq[CardAttribute[?, ?]], editor: EditorFrame): CardTableModel = new CardTableModel(cards, attributes.map(ElementAttribute.fromAttribute), Some(editor))

  /**
   * Create a new [[CardTableModel]] using [[CardAttribute]]s without an [[EditorFrame]].
   * 
   * @param cards list of cards to display
   * @param attributes initial attributes to display
   * @return a new, frameless [[CardTableModel]]
   */
  @targetName("card_none_apply")
  def apply(cards: CardList, attributes: IndexedSeq[CardAttribute[?, ?]]): CardTableModel = new CardTableModel(cards, attributes.map(ElementAttribute.fromAttribute), None)
}

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
class CardTableModel(private var cards: CardList, private var attributes: IndexedSeq[ElementAttribute[?, ?]], editor: Option[EditorFrame] = None) extends AbstractTableModel {
  override def getColumnCount = attributes.size
  override def getColumnName(column: Int) = attributes(column).attribute.toString
  override def getColumnClass(column: Int) = attributes(column).attribute.dataType
  override def getRowCount = cards.size
  override def getValueAt(row: Int, column: Int) = attributes(column).attribute(cards(row))
  override def isCellEditable(row: Int, column: Int) = editor.isDefined && (attributes(column) == CardAttribute.Count || attributes(column) == CardAttribute.Categories)

  override def setValueAt(value: Object, row: Int, column: Int) = if (isCellEditable(row, column)) {
    (cards, attributes(column), value) match {
      case (deck: EditorFrame#DeckData, ElementAttribute.CountElement, i: java.lang.Integer) => deck(row).count = i
      case (deck: EditorFrame#DeckData, ElementAttribute.CategoriesElement, ie: IncludeExcludePanel) => editor.foreach(_.categories.update(ie.updates.map((c) => c.name -> c).toMap))
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
  @targetName("element_columns_=") def columns_=(c: IndexedSeq[ElementAttribute[?, ?]]): Unit = {
    attributes = c
    fireTableStructureChanged()
  }

  @targetName("card_columns_=") def columns_=(c: IndexedSeq[CardAttribute[?, ?]]): Unit = columns = c.map(ElementAttribute.fromAttribute)

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