package editor.gui.ccp.handler;

import javax.swing.TransferHandler;

public interface ImportHandler
{
    boolean canImport(TransferHandler.TransferSupport supp);
    
    boolean importData(TransferHandler.TransferSupport supp);
}