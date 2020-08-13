package editor.gui.ccp.handler;

import java.awt.datatransfer.DataFlavor;

import javax.swing.TransferHandler;

/**
 * Interface describing a handler for importing a single flavor of data.
 * 
 * @author Alec Roelke
 */
public interface ImportHandler
{
    /**
     * @return The data flavor supported by this ImportHandler.
     */
    DataFlavor supportedFlavor();

    /**
     * @see TransferHandler#canImport
     */
    boolean canImport(TransferHandler.TransferSupport supp);
    
    /**
     * @see TransferHandler#importData
     */
    boolean importData(TransferHandler.TransferSupport supp);
}