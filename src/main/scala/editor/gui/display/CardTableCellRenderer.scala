package editor.gui.display

import editor.gui.GuiAttribute
import editor.gui.generic.ComponentUtils

import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.UIManager
import javax.swing.table.DefaultTableCellRenderer

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
  override def getTableCellRendererComponent(table: JTable, value: AnyRef, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int) = {
    val c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
    table.getModel match {
      case m: CardTableModel =>
        val panel = JPanel(BorderLayout())
        panel.add(GuiAttribute.fromAttribute(m.columns(column)).render(value), BorderLayout.CENTER)
        ComponentUtils.propagateColors(panel, c.getForeground, c.getBackground)
        panel.setBorder(if (hasFocus) UIManager.getBorder("Table.focusCellHighlightBorder") else BorderFactory.createEmptyBorder(0, 1, 0, 0))
        panel
      case _ => c
    }
  }
}