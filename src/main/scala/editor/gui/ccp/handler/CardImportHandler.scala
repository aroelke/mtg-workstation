package editor.gui.ccp.handler

import editor.collection.StandaloneEntry
import editor.database.card.Card
import editor.gui.ccp.data.DataFlavors
import editor.gui.editor.EditorFrame

import java.awt.datatransfer.UnsupportedFlavorException
import java.io.IOException
import javax.swing.TransferHandler
import java.time.LocalDate

/**
 * Import handler for [[Card]]s, which adds one copy of each to the deck.
 * 
 * @param editor frame containing the deck to modify
 * @param id list to add cards to
 * 
 * @author Alec Roelke
 */
class CardImportHandler(editor: EditorFrame, id: Int) extends TransferHandler with ImportHandler {
  override def supportedFlavor = DataFlavors.cardFlavor
  override def canImport(supp: TransferHandler.TransferSupport) = supp.isDataFlavorSupported(supportedFlavor)
  override def importData(supp: TransferHandler.TransferSupport) = if (!canImport(supp)) false else {
    try {
      val data = supp.getTransferable.getTransferData(supportedFlavor) match {
        case a: Array[?] => a.toSeq.collect{ case c: Card => c }
        case x => throw ClassCastException(s"needed ${classOf[Card]}, got ${x.getClass}")
      }
      editor.lists(id) ++= data.map(StandaloneEntry(_, 1, LocalDate.now))
      true
    } catch {
      case _: UnsupportedFlavorException => false
      case _: IOException => false
    }
  }
}