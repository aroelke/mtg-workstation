package editor.gui.ccp.handler

import javax.swing.TransferHandler
import editor.database.card.Card
import javax.swing.JComponent
import editor.gui.ccp.data.CardTransferData
import scala.jdk.CollectionConverters._

/**
 * Handler for copying cards from the inventory. Cards cannot be removed from it, so it doesn't
 * support cut.
 * 
 * @constructor create a new transfer handler for transferring a collection of cards.
 * @param cards carsd to transfer
 * 
 * @author Alec Roelke
 */
class InventoryExportHandler(cards: => Iterable[Card]) extends TransferHandler {
  override def getSourceActions(c: JComponent) = TransferHandler.COPY
  override def createTransferable(c: JComponent) = CardTransferData(cards.toArray)
}