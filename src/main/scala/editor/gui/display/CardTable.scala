package editor.gui.display

import editor.database.attributes.CardAttribute
import editor.database.attributes.CombatStat
import editor.database.attributes.CounterStat
import editor.database.attributes.ManaCost
import editor.database.card.Card
import editor.gui.ElementAttribute
import editor.gui.deck.EditorFrame
import editor.gui.deck.InclusionCellEditor
import editor.gui.generic.ComponentUtils
import editor.gui.generic.SpinnerCellEditor
import editor.math.{_, given}

import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.event.MouseEvent
import javax.swing.JTable
import javax.swing.SortOrder
import javax.swing.table.TableCellRenderer
import javax.swing.table.TableModel
import javax.swing.table.TableRowSorter
import scala.jdk.CollectionConverters._
import scala.reflect.ClassTag

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
        (prepareRenderer(getCellRenderer(row, col), row, col), model) match {
          case (c: Component, m: CardTableModel) if c.getPreferredSize.width > bounds.width || m.columns(col) == ElementAttribute.CategoriesElement =>
            m.columns(col).getToolTipText(getValueAt(row, col))
          case _ => null
        }
      } else null
    })
  }

  override def prepareRenderer(r: TableCellRenderer, row: Int, column: Int) = {
    val c = super.prepareRenderer(r, row, column)
    ComponentUtils.propagateColors(c, c.getForeground, if (!isRowSelected(row) || !getRowSelectionAllowed) rowColor(row) else c.getBackground)
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
      given ascending: Boolean = getSortKeys.asScala.exists(_.getSortOrder == SortOrder.ASCENDING)
      m.columns(column) match {
        case ElementAttribute.PowerElement | ElementAttribute.ToughnessElement => SeqOrdering[Option[CombatStat]]
        case ElementAttribute.LoyaltyElement => SeqOrdering[Option[CounterStat]]
        case attribute => attribute.attribute
      }
    case _ => super.getComparator(column)
  }

  override protected def useToString(column: Int) = false
}