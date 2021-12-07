package editor.gui

import javax.swing.event.ListSelectionListener
import java.awt.event.MouseListener
import _root_.editor.gui.display.CardTable
import _root_.editor.collection.CardList
import java.awt.event.MouseEvent
import javax.swing.event.ListSelectionEvent

class TableSelectionListener(frame: MainFrame, table: CardTable, list: CardList) extends ListSelectionListener with MouseListener {
  override def valueChanged(e: ListSelectionEvent) = if (!e.getValueIsAdjusting) {
    if (table.getSelectedRow >= 0)
      frame.setDisplayedCard(list.get(table.convertRowIndexToModel(table.getSelectedRow)))
    else if (frame.getSelectedTable.exists(_ == table))
      frame.clearSelectedCard()
  }

  override def mouseReleased(e: MouseEvent) = {
    if (table.rowAtPoint(e.getPoint) < 0)
      frame.clearSelectedList()
    if (!frame.getSelectedList.exists(_ == list))
      frame.setSelectedComponents(table, list)
  }

  override def mouseClicked(e: MouseEvent) = {}
  override def mousePressed(e: MouseEvent) = {}
  override def mouseEntered(e: MouseEvent) = {}
  override def mouseExited(e: MouseEvent) = {}
}
