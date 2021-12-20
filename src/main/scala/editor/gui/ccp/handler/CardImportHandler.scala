package editor.gui.ccp.handler

import javax.swing.TransferHandler
import editor.gui.editor.EditorFrame
import editor.gui.ccp.data.DataFlavors
import editor.database.card.Card
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.IOException

class CardImportHandler(editor: EditorFrame, id: Int) extends TransferHandler with ImportHandler {
  override def supportedFlavor = DataFlavors.cardFlavor
  override def canImport(supp: TransferHandler.TransferSupport) = supp.isDataFlavorSupported(supportedFlavor)
  override def importData(supp: TransferHandler.TransferSupport) = if (!canImport(supp)) false else {
    try {
      val data = supp.getTransferable.getTransferData(supportedFlavor) match {
        case a: Array[?] => a.collect{ case e if e.isInstanceOf[Card] => e.asInstanceOf[Card] }
        case x => throw ClassCastException(s"needed ${classOf[Card]}, got ${x.getClass}")
      }
      editor.lists(id) ++= data.toSeq -> 1
    } catch {
      case _: UnsupportedFlavorException => false
      case _: IOException => false
    }
  }
}