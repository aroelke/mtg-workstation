package editor.gui.ccp.handler

import editor.gui.ccp.data.DataFlavors
import editor.gui.ccp.data.EntryTransferData
import editor.gui.editor.EditorFrame

import java.awt.datatransfer.UnsupportedFlavorException
import java.io.IOException
import javax.swing.TransferHandler
import scala.collection.immutable.ListMap

/**
 * Handler for importing data from a list contained by an [[EditorFrame]] into another one. Data can't be movied or copied to and from the same
 * list.
 * 
 * @constructor create a new import handler for a list in a particular [[EditorFrame]]
 * @param editor frame containing the list to import into
 * @param id ID of the list to import into
 */
class EntryImportHandler(editor: EditorFrame, id: Int) extends TransferHandler with ImportHandler {
  override def supportedFlavor = DataFlavors.entryFlavor

  override def canImport(supp: TransferHandler.TransferSupport) = if (supp.isDataFlavorSupported(supportedFlavor)) {
    try {
      supp.getTransferable.getTransferData(supportedFlavor) match {
        case data: EntryTransferData => !(data.source == editor && data.from == id && supp.isDrop)
        case _ => false
      }
    } catch {
      case _: UnsupportedFlavorException => false
      case _: IOException => false
    }
  } else false

  override def importData(supp: TransferHandler.TransferSupport) = if (canImport(supp)) {
    try {
      supp.getTransferable.getTransferData(supportedFlavor) match {
        case data: EntryTransferData =>
          if (supp.isDrop) {
            // Actually handle all list modification in the source handler; just tell it where the cards should go
            data.target = editor
            data.to = id
            true
          } else {
            editor.lists(id) %%= ListMap.from(data.cards)
          }
        case _ => false
      }
    } catch {
      case _: UnsupportedFlavorException => false
      case _: IOException => false
    }
  } else false
}
