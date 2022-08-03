package editor.gui.ccp.data

import editor.collection.Categorization
import editor.database.card.Card
import editor.gui.deck.EditorFrame

import java.awt.datatransfer.DataFlavor

/**
 * Data used for transferring categories using cut/copy/paste.  Drag-and-drop of catgories is not supported.
 * 
 * @constructor create a new set of category transfer data to transfer a category between elements
 * @param spec categorization to transfer
 * @param editor reserved for future use with transferring cards along with categories
 * @param cards reserved for future use with transferring cards along with categories
 * 
 * @author Alec Roelke
 */
class CategoryTransferData(val spec: Categorization, editor: EditorFrame = null, cards: Map[Card, Int] = Map.empty) extends EntryTransferData(editor, EditorFrame.MainDeck, cards) {
  override def getTransferDataFlavors = DataFlavors.categoryFlavor +: super.getTransferDataFlavors
  override def isDataFlavorSupported(flavor: DataFlavor) = getTransferDataFlavors.contains(flavor)
  override def getTransferData(flavor: DataFlavor) = if (flavor == DataFlavors.categoryFlavor) this else super.getTransferData(flavor)
}