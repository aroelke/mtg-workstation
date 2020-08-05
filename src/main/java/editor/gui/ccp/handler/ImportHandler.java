package editor.gui.ccp.handler;

import java.awt.datatransfer.DataFlavor;

import javax.swing.TransferHandler;

public interface ImportHandler
{
    DataFlavor supportedFlavor();

    boolean canImport(TransferHandler.TransferSupport supp);
    
    boolean importData(TransferHandler.TransferSupport supp);
}