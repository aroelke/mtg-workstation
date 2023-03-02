package editor.gui

import editor.collection.CardList
import editor.gui.display.CardTable

import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

/**
 * Listener for selection changes on [[CardTable]]s in the editor to make sure only one table has a selection at
 * a time and to keep track of what the current card selection is.
 * 
 * @param frame parent frame that contains the card selection
 * @param table table that is listening to events
 * @param list list providing data to the table
 * 
 * @author Alec Roelke
 */
class TableSelectionListener(frame: MainFrame, table: CardTable, list: CardList) extends ListSelectionListener with MouseListener {
  override def valueChanged(e: ListSelectionEvent) = if (!e.getValueIsAdjusting) {
    if (table.getSelectedRow >= 0)
      frame.setDisplayedCard(list(table.convertRowIndexToModel(table.getSelectedRow)).card)
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
