package editor.gui.display

import editor.database.attributes.CardAttribute
import editor.database.attributes.ManaCost
import editor.database.attributes.OptionalAttribute
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

object CardTable {
  def createCellEditor(frame: EditorFrame, attr: CardAttribute) = attr match {
    case CardAttribute.COUNT => SpinnerCellEditor()
    case CardAttribute.CATEGORIES => InclusionCellEditor(frame)
    case _ => throw IllegalArgumentException(s"values of type $attr can't be edited")
  }
}

class CardTable(model: TableModel) extends JTable(model) {
  private var _stripe = Color.LIGHT_GRAY
  setFillsViewportHeight(true)
  setShowGrid(false)
  private val renderer = CardTableCellRenderer()
  CardAttribute.displayableValues.foreach((a) => setDefaultRenderer(a.dataType, renderer))
  setRowSorter(EmptyTableRowSorter(model))

  def getRowColor(row: Int) = if (row % 2 == 0) Color(getBackground.getRGB) else stripe

  def stripe = _stripe
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
              if (m.columns(col) == CardAttribute.MANA_COST)
                s"""<html>${getValueAt(row, col) match {
                  case s: java.util.List[?] => s.asScala.collect{ case cost: ManaCost => cost.toHTMLString }.mkString(Card.FACE_SEPARATOR)
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
      c.setBackground(getRowColor(row))
    c
  }

  override def setModel(model: TableModel) = {
    super.setModel(model)
    setRowSorter(EmptyTableRowSorter(model))
  }
}

private class EmptyTableRowSorter(model: TableModel) extends TableRowSorter[TableModel](model) {
  private val NoString = Set(
    CardAttribute.MANA_COST,
    CardAttribute.MANA_VALUE,
    CardAttribute.COLORS,
    CardAttribute.COLOR_IDENTITY,
    CardAttribute.POWER,
    CardAttribute.TOUGHNESS,
    CardAttribute.LOYALTY,
    CardAttribute.CATEGORIES
  )

  override def getComparator(column: Int) = model match {
    case m: CardTableModel =>
      val ascending = getSortKeys.get(0).getSortOrder == SortOrder.ASCENDING
      val attribute = m.columns(column)
      attribute match {
        case CardAttribute.POWER | CardAttribute.TOUGHNESS | CardAttribute.LOYALTY => (a: AnyRef, b: AnyRef) => {
          val first = a match {
            case s: Seq[?] => s.collect{ case o: OptionalAttribute if o.exists => o }.headOption.getOrElse(OptionalAttribute.empty)
            case _ => OptionalAttribute.empty
          }
          val second = b match {
            case s: Seq[?] => s.collect{ case o: OptionalAttribute if o.exists => o }.headOption.getOrElse(OptionalAttribute.empty)
            case _ => OptionalAttribute.empty
          }
          if (!first.exists && !second.exists)
            0
          else if (!first.exists)
            if (ascending) 1 else -1
          else if (!second.exists)
            if (ascending) -1 else 1
          else
            attribute.compare(first, second)
        }
        case _ => m.columns(column)
      }
    case _ => super.getComparator(column)
  }

  override protected def useToString(column: Int) = model match {
    case m: CardTableModel => !NoString.contains(m.columns(column)) && super.useToString(column)
    case _ => super.useToString(column)
  }
}