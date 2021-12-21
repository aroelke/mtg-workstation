package editor.gui.ccp.handler

import javax.swing.TransferHandler
import scala.collection.mutable.ListMap

class EditorTransferHandler(importers: Seq[ImportHandler]) extends TransferHandler {
  if (importers.map(_.supportedFlavor).distinct.size != importers.size)
    throw IllegalArgumentException("importers must support distinct flavors")

  override def canImport(supp: TransferHandler.TransferSupport) = importers.exists((i) => supp.isDataFlavorSupported(i.supportedFlavor) && i.canImport(supp))

  override def importData(supp: TransferHandler.TransferSupport) = importers.filter((i) => supp.isDataFlavorSupported(i.supportedFlavor) && i.canImport(supp)).headOption.exists(_.importData(supp))
}