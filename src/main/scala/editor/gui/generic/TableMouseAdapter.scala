package editor.gui.generic

import java.awt.event.InputEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPopupMenu
import javax.swing.JTable

/**
 * Mouse listener for a [[JTable]] that allows for discontiguous selection by holding the Ctrl
 * key down while clicking.
 * 
 * @constructor create a new mouse adapter for a table with a menu
 * @param table table to listen for mouse events
 * @param menu menu to open when the menu button is pressed
 * 
 * @author Alec Roelke
 */
class TableMouseAdapter(table: JTable, menu: JPopupMenu) extends MouseAdapter {
  /**
   * Process an event that should generate a popup menu.
   * @param e mouse event that should generate the popup
   */
  def popupClick(e: MouseEvent) = if (e.isPopupTrigger) {
    val r = table.rowAtPoint(e.getPoint)
    if (r >= 0 && !table.isRowSelected(r)) {
      if ((e.getModifiersEx & InputEvent.CTRL_DOWN_MASK) == 0)
        table.setRowSelectionInterval(r, r)
      else
        table.addRowSelectionInterval(r, r)
    }
    menu.show(e.getComponent, e.getX, e.getY)
  }

  override def mousePressed(e: MouseEvent) = popupClick(e)

  override def mouseReleased(e: MouseEvent) = {
    popupClick(e)
    if (table.rowAtPoint(e.getPoint) < 0)
      table.clearSelection()
  }
}
