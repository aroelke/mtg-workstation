package editor.gui.display

import editor.collection.Categorization
import editor.collection.mutable.Deck
import editor.database.attributes.CardAttribute
import editor.database.attributes.ManaCost
import editor.database.attributes.ManaType
import editor.database.card.Card
import editor.database.symbol.ManaSymbolInstances.ColorSymbol
import editor.gui.generic.ComponentUtils
import editor.util.UnicodeSymbols

import java.awt.Color
import java.awt.Container
import java.awt.FlowLayout
import java.awt.Graphics
import java.time.LocalDate
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.UIManager
import javax.swing.border.Border
import javax.swing.table.DefaultTableCellRenderer
import scala.jdk.CollectionConverters._

private object CardTableCellRenderer {
  val cache = collection.mutable.Map[Seq[ManaCost], Seq[Seq[Icon]]]()
}

/**
 * Cell renderer for a [[CardTable]] cell. Mostly just displays the contents as a string,
 * but if it's a mana cost or set of colors, displays it as a list of mana symbols, and if it's
 * a set of categories in a deck, displays squares corresponding to the categories' colors (and
 * adds a tooltip for more detailed information).
 * 
 * @constructor create a new [[CardTable]] cell renderer
 * 
 * @author Alec Roelke
 */
class CardTableCellRenderer extends DefaultTableCellRenderer {
  import CardTableCellRenderer._

  override def getTableCellRendererComponent(table: JTable, value: AnyRef, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int) = {
    val c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
    table.getModel match {
      case m: CardTableModel =>
        def setColors[C <: JComponent](component: C) = {
          component.setForeground(c.getForeground)
          component.setBackground(c.getBackground)
          component
        }
        def tableLabel(contents: String | Icon) = setColors(contents match {
          case text: String => JLabel(text)
          case icon: Icon => JLabel(icon)
        })
        def tablePanel(panel: JPanel = JPanel(), border: Border = BorderFactory.createEmptyBorder(1, 1, 1, 1)) = {
          panel.setBorder(if (hasFocus) UIManager.getBorder("Table.focusCellHighlightBorder") else border)
          setColors(panel)
        }
        m.columns(column) match {
          case CardAttribute.ManaCost =>
            val costs = value match {
              case s: Seq[?] => s.collect{ case cost: ManaCost => cost }
              case _ => Seq.empty
            }
            val icons = cache.getOrElseUpdate(costs, costs.map(_.map(_.getIcon(ComponentUtils.TextSize))))
            val panel = tablePanel()
            panel.setLayout(BoxLayout(panel, BoxLayout.X_AXIS))
            for (i <- 0 until icons.size) {
              if (!icons(i).isEmpty) {
                if (i > 0)
                  panel.add(tableLabel(Card.FaceSeparator))
                icons(i).foreach((icon) => panel.add(tableLabel(icon)))
              }
            }
            panel
          case CardAttribute.RealManaValue =>
            val mv = Option(value) match {
              case Some(v: Double) => v
              case Some(v: Int) => v.toDouble
              case _ => 0.0
            }
            val panel = tablePanel(JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)))
            panel.add(setColors(tableLabel(if (mv == mv.toInt) mv.toInt.toString else mv.toString)))
            panel
          case CardAttribute.EffManaValue =>
            val mvs = value match {
              case s: Seq[?] => s.collect{ case mv: Double => mv }
              case _ => Seq.empty
            }
            val panel = tablePanel(JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)))
            panel.add(setColors(tableLabel(mvs.map((mv) => if (mv == mv.toInt) mv.toInt.toString else mv.toString).mkString(Card.FaceSeparator))))
            panel
          case CardAttribute.Colors | CardAttribute.ColorIdentity =>
            val panel = tablePanel()
            panel.setLayout(BoxLayout(panel, BoxLayout.X_AXIS))
            value match {
              case s: Seq[?] => s.foreach{ case t: ManaType => panel.add(tableLabel(ColorSymbol(t).scaled(ComponentUtils.TextSize))) }
              case _ =>
            }
            panel
          case CardAttribute.TypeLine =>
            val panel = tablePanel(JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)))
            value match {
              case s: Seq[?] => panel.add(setColors(tableLabel(s.mkString(Card.FaceSeparator))))
              case _ => ""
            }
            panel
          case CardAttribute.Power | CardAttribute.Toughness | CardAttribute.Loyalty =>
            val panel = tablePanel(JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)))
            value match {
              case s: Seq[?] => panel.add(setColors(tableLabel(s.mkString(Card.FaceSeparator))))
              case s: java.util.List[?] => panel.add(setColors(tableLabel(s.asScala.mkString(Card.FaceSeparator))))
              case _ =>
            }
            panel
          case CardAttribute.Categories =>
            val categories = value match {
              case s: Set[?] => s.toSeq.collect{ case category: Categorization => category }.sortBy(_.name)
              case _ => Seq.empty
            }
            val panel = tablePanel(new JPanel {
              override def paintComponent(g: Graphics) = {
                super.paintComponent(g)
                for (i <- 0 until categories.size) {
                  val x = i*(getHeight + 1) + 1
                  val y = 1
                  g.setColor(categories(i).color)
                  g.fillRect(x, y, getHeight - 3, getHeight - 3)
                  g.setColor(Color.BLACK)
                  g.drawRect(x, y, getHeight - 3, getHeight - 3)
                }
              }
            })
            if (!categories.isEmpty) {
              panel.setToolTipText(categories.map(_.name).mkString(
                s"<html>Categories:<br>${UnicodeSymbols.Bullet} ",
                s"<br>${UnicodeSymbols.Bullet} ",
                "</html>"
              ))
            }
            panel
          case CardAttribute.DateAdded =>
            val panel = tablePanel(JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)))
            value match {
              case d: LocalDate => panel.add(setColors(tableLabel(Deck.DateFormatter.format(d))))
              case _ =>
            }
            panel
          case _ => c
        }
      case _ => c
    }
  }
}
