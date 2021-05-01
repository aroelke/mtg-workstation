package editor.gui.ccp.handler;

import java.awt.datatransfer.DataFlavor;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.TransferHandler;

/**
 * Handler for importing and exporting data between components in the editor.
 * By default, only importing is supported based on a list of provided handlers.
 * Each handler must only support one data flavor and they must all supporte
 * differnet flavors. The list indicates the priority of each supported data
 * flavor; if a flavor is supported, its handler will be used even if it can't
 * import the data.
 * 
 * @author Alec Roelke
 */
public class EditorTransferHandler extends TransferHandler
{
    /** import handlers telling the handler how to import different kinds of data. */
    private LinkedHashMap<DataFlavor, ImportHandler> handlers;

    /**
     * Create a new editor transfer handler with the given importers that tell it
     * how to import supported data.
     * 
     * @param importers list of handlers for importing data
     * @throws IllegalArgumentException
     */
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

    /**
     * Convenience constructor allowing the use of an array of import handlers or
     * just specifying them in-line without having to create a List.
     * 
     * @param importers handlers for importing data
     * @throws IllegalArgumentException
     */
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