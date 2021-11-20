package editor.gui.display

import javax.swing.table.DefaultTableCellRenderer
import editor.database.attributes.ManaCost
import javax.swing.Icon
import javax.swing.JTable
import javax.swing.JPanel
import java.awt.FlowLayout
import javax.swing.BorderFactory
import editor.database.attributes.CardAttribute
import javax.swing.BoxLayout
import scala.jdk.CollectionConverters._
import editor.database.card.Card
import javax.swing.JLabel
import javax.swing.UIManager
import javax.swing.Box
import editor.database.symbol.ColorSymbol
import editor.database.attributes.ManaType
import editor.collection.deck.Category
import java.awt.Graphics
import java.awt.Color
import editor.util.UnicodeSymbols
import java.time.LocalDate
import editor.collection.deck.Deck
import java.awt.Container
import javax.swing.border.Border
import javax.swing.JComponent

private object CardTableCellRenderer {
  val cache = collection.mutable.Map[Seq[ManaCost], Seq[Seq[Icon]]]()
}

class CardTableCellRenderer extends DefaultTableCellRenderer {
  import CardTableCellRenderer._

  override def getTableCellRendererComponent(table: JTable, value: AnyRef, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int) = {
    val c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
    table.getModel match {
      case m: CardTableModel =>
        def finishPanel(p: JComponent, b: Border = BorderFactory.createEmptyBorder(1, 1, 1, 1)) = {
          p.setBorder(if (hasFocus) UIManager.getBorder("Table.focusCellHighlightBorder") else b)
          p.setForeground(c.getForeground)
          p.setBackground(c.getBackground)
          p
        }
        m.columns(column) match {
          case CardAttribute.MANA_COST =>
            val panel = JPanel()
            panel.setLayout(BoxLayout(panel, BoxLayout.X_AXIS))
            val costs = value match {
              case s: java.util.List[?] => s.asScala.toSeq.collect{ case cost: ManaCost => cost }
              case _ => Seq.empty
            }
            val icons = cache.getOrElseUpdate(costs, costs.map(_.asScala.toSeq.map(_.getIcon(13))))
            for (i <- 0 until icons.size) {
              if (!icons(i).isEmpty) {
                if (i > 0)
                  panel.add(JLabel(Card.FACE_SEPARATOR))
                icons(i).foreach{ icon => panel.add(JLabel(icon)) }
              }
            }
            finishPanel(panel, BorderFactory.createEmptyBorder(0, 1, if (icons.length == 1) -1 else 0, 0))
          case CardAttribute.MANA_VALUE =>
            val mv = Option(value) match {
              case Some(v: Double) => v
              case Some(v: Int) => v.toDouble
              case _ => 0.0
            }
            val panel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
            panel.add(JLabel(if (mv == mv.toInt) mv.toInt.toString else mv.toString))
            finishPanel(panel)
          case CardAttribute.COLORS | CardAttribute.COLOR_IDENTITY =>
            val panel = JPanel()
            panel.setLayout(BoxLayout(panel, BoxLayout.X_AXIS))
            value match {
              case s: java.util.List[?] => s.asScala.toSeq.foreach{ case t: ManaType => panel.add(JLabel(ColorSymbol.SYMBOLS.get(t).getIcon(13))) }
              case _ =>
            }
            finishPanel(panel)
          case CardAttribute.POWER | CardAttribute.TOUGHNESS | CardAttribute.LOYALTY =>
            val panel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
            value match {
              case s: java.util.List[?] => panel.add(JLabel(s.asScala.mkString(Card.FACE_SEPARATOR)))
              case _ =>
            }
            finishPanel(panel)
          case CardAttribute.CATEGORIES =>
            val categories = value match {
              case s: java.util.Set[?] => s.asScala.toSeq.collect{ case category: Category => category }.sortBy(_.getName)
              case _ => Seq.empty
            }
            val panel = new JPanel {
              override def paintComponent(g: Graphics) = {
                super.paintComponent(g)
                for (i <- 0 until categories.size) {
                  val x = i*(getHeight + 1) + 1
                  val y = 1
                  g.setColor(categories(i).getColor)
                  g.fillRect(x, y, getHeight - 3, getHeight - 3)
                  g.setColor(Color.BLACK)
                  g.drawRect(x, y, getHeight - 3, getHeight - 3)
                }
              }
            }
            if (!categories.isEmpty) {
              panel.setToolTipText(categories.map(_.getName).mkString(
                s"<html>Categories:<br>${UnicodeSymbols.BULLET} ",
                s"<br>${UnicodeSymbols.BULLET} ",
                "</html>"
              ))
            }
            finishPanel(panel)
          case CardAttribute.DATE_ADDED =>
            val panel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
            value match {
              case d: LocalDate => panel.add(JLabel(Deck.DATE_FORMATTER.format(d)))
              case _ =>
            }
            finishPanel(panel)
          case _ => c
        }
      case _ => c
    }
  }
}
