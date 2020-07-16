package editor.gui.ccp.handler;

import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;

public interface ExportHandler
{
    int getSourceActions(JComponent c);

    Transferable createTransferable(JComponent c);

    void exportDone(JComponent source, Transferable data, int action);
}