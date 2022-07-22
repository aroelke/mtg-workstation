package editor.gui.display

import editor.database.attributes.CardAttribute
import editor.database.attributes.CombatStat
import editor.database.attributes.Loyalty
import editor.database.attributes.ManaCost
import editor.database.card.Card
import editor.gui.editor.EditorFrame
import editor.gui.editor.InclusionCellEditor
import editor.gui.generic.SpinnerCellEditor

import java.awt.Color
import java.awt.Component
import java.awt.event.MouseEvent
import javax.swing.JTable
import javax.swing.SortOrder
import javax.swing.table.TableCellRenderer
import javax.swing.table.TableModel
import javax.swing.table.TableRowSorter
import scala.jdk.CollectionConverters._
import scala.reflect.ClassTag
import editor.util.SeqOrdering
import editor.util.OptionOrdering

/**
 * Companion used for global card table operations.
 * @author Alec Roelke
 */
object CardTable {
  /**
   * Create a new instance of a cell editor for a particular card attribute if values of that
   * attribute can be edited in a card table (by double-clicking).
   * 
   * @param frame [[EditorFrame]] containing the deck that contains the card whose attribute should
   * be edited
   * @param attr attribute to be edited by the editor
   * @return a component that can edit the value of the specified attribute
   */
  def createCellEditor(frame: EditorFrame, attr: CardAttribute[?, ?]) = attr match {
    case CardAttribute.Count => SpinnerCellEditor()
    case CardAttribute.Categories => InclusionCellEditor(frame)
    case _ => throw IllegalArgumentException(s"values of type $attr can't be edited")
  }
}

/**
 * Table for displaying information about cards.  Which cards are displayed and what data
 * to show in each column is customizable via the model.  Rows alternate in color between
 * white and another customizable color.
 * 
 * @constructor create a new card table using the given model
 * @param model model to use to display card data
 * 
 * @author Alec Roelke
 */
class CardTable(model: TableModel) extends JTable(model) {
  private var _stripe = Color.LIGHT_GRAY
  setFillsViewportHeight(true)
  setShowGrid(false)
  private val renderer = CardTableCellRenderer()
  CardAttribute.displayableValues.foreach((a) => setDefaultRenderer(a.dataType, renderer))
  setRowSorter(EmptyTableRowSorter(model))

  /** 
   * @param row row to get the color of
   * @return [[Color.WHITE]] if the row is odd and the stripe color otherwise
   */
  def rowColor(row: Int) = if (row % 2 == 0) Color(getBackground.getRGB) else stripe

  /** @return the stripe color */
  def stripe = _stripe

  /** @param s new stripe color */
  def stripe_=(s: Color) = {
    _stripe = s
    repaint()
  }

  override def getScrollableTracksViewportWidth = getPreferredSize.width < getParent.getWidth

  override def getToolTipText(e: MouseEvent) = {
    Option(super.getToolTipText(e)).getOrElse({
      val row = rowAtPoint(e.getPoint)
      val col = columnAtPoint(e.getPoint)
      if (col >= 0 && row >= 0) {
        val bounds = getCellRect(row, col, false)
        prepareRenderer(getCellRenderer(row, col), row, col) match {
          case c: Component if c.getPreferredSize.width > bounds.width => model match {
            case m: CardTableModel =>
              if (m.columns(col) == CardAttribute.ManaCost)
                s"""<html>${getValueAt(row, col) match {
                  case s: Seq[?] => s.collect{ case cost: ManaCost => cost.toHTMLString }.mkString(Card.FaceSeparator)
                  case _ => ""
                }}</html>"""
              else
                s"<html>${getValueAt(row, col)}</html>"
            case _ => null
          }
          case _ => null
        }
      } else null
    })
  }

  override def prepareRenderer(r: TableCellRenderer, row: Int, column: Int) = {
    val c = super.prepareRenderer(r, row, column)
    if (!isRowSelected(row) || !getRowSelectionAllowed())
      c.setBackground(rowColor(row))
    c
  }

  override def setModel(model: TableModel) = {
    super.setModel(model)
    setRowSorter(EmptyTableRowSorter(model))
  }
}

/**
 * Row sorter that sorts rows with empty values last regardless of how the empty string compares
 * with other values the row could have.
 */
private class EmptyTableRowSorter(model: TableModel) extends TableRowSorter[TableModel](model) {
  override def getComparator(column: Int) = model match {
    case m: CardTableModel =>
      val ascending = getSortKeys.asScala.exists(_.getSortOrder == SortOrder.ASCENDING)
      m.columns(column) match {
        case CardAttribute.Power | CardAttribute.Toughness => SeqOrdering[Option[CombatStat]](OptionOrdering[CombatStat](ascending))
        case CardAttribute.Loyalty => SeqOrdering[Option[Loyalty]](OptionOrdering[Loyalty](ascending))
        case attribute => attribute
      }
    case _ => super.getComparator(column)
  }

  override protected def useToString(column: Int) = false
}