package editor.gui.ccp.handler

import javax.swing.TransferHandler
import scala.collection.mutable.ListMap

/**
 * Handler for transferring data between elements. Supported import flavors and how to import data is determined using a list of [[ImportHandler]]s,
 * each of which must support a single distinct flavor and the order of which determines the priority that each flavor is used, in case the
 * data being transferred supports multiple flavors. If two handlers support the same flavor, but the higher-priority handler can't
 * import it for some reason, that handler is used anyway and the data isn't imported.  Data export isn't supported; to support it,
 * extend this class.
 * 
 * @constructor create a new transfer handler that handles data flavors using importers in the given order
 * @param importers importers used to import data
 * 
 * @author Alec Roelke
 */
class EditorTransferHandler(importers: Seq[ImportHandler]) extends TransferHandler {
  if (importers.map(_.supportedFlavor).distinct.size != importers.size)
    throw IllegalArgumentException("importers must support distinct flavors")

  override def canImport(supp: TransferHandler.TransferSupport) = importers.exists((i) => supp.isDataFlavorSupported(i.supportedFlavor) && i.canImport(supp))

  override def importData(supp: TransferHandler.TransferSupport) = importers.filter((i) => supp.isDataFlavorSupported(i.supportedFlavor)).headOption.exists(_.importData(supp))
}