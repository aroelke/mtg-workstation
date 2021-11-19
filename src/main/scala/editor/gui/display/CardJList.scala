package editor.gui.display

import javax.swing.JList
import editor.database.card.Card
import javax.swing.DefaultListSelectionModel
import javax.swing.ListSelectionModel
import javax.swing.DefaultListModel
import editor.gui.settings.SettingsDialog

/**
 * UI element for displaying a list of cards.  Does not support selection.
 * 
 * @constructor create a new card list showing a list of cards
 * @param cards cards to initially display; can be changed later
 * 
 * @author Alec Roelke
 */
class CardJList(var cards: Seq[Card] = Seq.empty) extends JList[String] {
  setSelectionModel(new DefaultListSelectionModel {
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
    override def setSelectionInterval(index0: Int, index1: Int) = super.setSelectionInterval(-1, -1)
  })
  setModel(new DefaultListModel[String] {
    override def getElementAt(index: Int) = cards(index).unifiedName
    override def getSize = cards.size
  })
  setVisibleRowCount(SettingsDialog.settings.editor.categories.explicits)
}
