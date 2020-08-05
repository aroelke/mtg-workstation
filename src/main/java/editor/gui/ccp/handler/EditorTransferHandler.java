package editor.gui.ccp.handler;

import java.awt.datatransfer.DataFlavor;
import java.util.LinkedHashMap;

import javax.swing.TransferHandler;

import editor.gui.ccp.data.DataFlavors;
import editor.gui.editor.EditorFrame;

@SuppressWarnings("serial")
public class EditorTransferHandler extends TransferHandler
{
    private LinkedHashMap<DataFlavor, ImportHandler> importers;

    public EditorTransferHandler(EditorFrame e, int i)
    {
        importers = new LinkedHashMap<>();
        importers.put(DataFlavors.entryFlavor, new EntryImportHandler(e, i));
        importers.put(DataFlavors.cardFlavor, new CardImportHandler(e, i));
    }

    @Override
    public boolean canImport(TransferSupport supp)
    {
        for (var e : importers.entrySet())
            if (supp.isDataFlavorSupported(e.getKey()))
                return e.getValue().canImport(supp);
        return false;
    }

    @Override
    public boolean importData(TransferSupport supp)
    {
        for (var e : importers.entrySet())
            if (supp.isDataFlavorSupported(e.getKey()))
                return e.getValue().importData(supp);
        return false;
    }
}