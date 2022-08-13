package editor.gui.ccp.handler

import java.awt.datatransfer.DataFlavor
import javax.swing.TransferHandler

/**
 * Transfer handler that supports importing data of a single flavor.
 * @author Alec Roelke
 */
trait ImportHandler extends TransferHandler {
  /** @return the data flavor supported by the handler */
  def supportedFlavor: DataFlavor
}