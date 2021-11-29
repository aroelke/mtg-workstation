package editor.gui.generic

import java.awt.event.MouseAdapter
import javax.swing.JTable
import javax.swing.JPopupMenu
import java.awt.event.MouseEvent
import java.awt.event.InputEvent

class TableMouseAdapter(table: JTable, menu: JPopupMenu) extends MouseAdapter {
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
