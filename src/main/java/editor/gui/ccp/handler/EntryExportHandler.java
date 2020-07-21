package editor.gui.ccp.handler;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

public class EntryExportHandler extends TransferHandler implements ExportHandler
{
    @Override
    public int getSourceActions(JComponent c)
    {
        return TransferHandler.MOVE | TransferHandler.COPY;
    }
}