package editor.gui.display

import javax.swing.JList
import editor.database.card.Card
import javax.swing.DefaultListSelectionModel
import javax.swing.ListSelectionModel
import javax.swing.DefaultListModel
import editor.gui.settings.SettingsDialog

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
