package editor.gui.ccp.handler;

import java.awt.datatransfer.DataFlavor;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.TransferHandler;

@SuppressWarnings("serial")
public class EditorTransferHandler extends TransferHandler
{
    private LinkedHashMap<DataFlavor, ImportHandler> handlers;

    public EditorTransferHandler(List<ImportHandler> importers) throws IllegalArgumentException
    {
        for (ImportHandler a : importers)
            for (ImportHandler b : importers)
                if (a != b && a.supportedFlavor().equals(b.supportedFlavor()))
                    throw new IllegalArgumentException("Import handlers both support data flavor " + a.supportedFlavor());

        handlers = new LinkedHashMap<>();
        for (ImportHandler importer : importers)
            handlers.put(importer.supportedFlavor(), importer);
    }

    public EditorTransferHandler(ImportHandler... importers) throws IllegalArgumentException
    {
        this(Arrays.asList(importers));
    }

    @Override
    public boolean canImport(TransferSupport supp)
    {
        for (var e : handlers.entrySet())
            if (supp.isDataFlavorSupported(e.getKey()))
                return e.getValue().canImport(supp);
        return false;
    }

    @Override
    public boolean importData(TransferSupport supp)
    {
        for (var e : handlers.entrySet())
            if (supp.isDataFlavorSupported(e.getKey()))
                return e.getValue().importData(supp);
        return false;
    }
}