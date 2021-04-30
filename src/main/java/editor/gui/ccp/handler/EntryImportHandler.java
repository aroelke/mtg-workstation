package editor.gui.ccp.handler;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.TransferHandler;

import editor.gui.ccp.data.DataFlavors;
import editor.gui.ccp.data.EntryTransferData;
import editor.gui.editor.EditorFrame;

/**
 * Importer for card "entries," allowing the import of multiple copies of cards
 * at a time.
 * 
 * @author Alec Roelke
 */
public class EntryImportHandler extends TransferHandler implements ImportHandler
{
    /** Editor frame containing the deck to import cards into. */
    private final EditorFrame editor;
    /** ID of the list to import cards into. */
    private final int id;

    /**
     * Create a new entry import handler.
     * 
     * @param e editor frame containing the deck to import into
     * @param i ID of the list to import cards into
     */
    public EntryImportHandler(EditorFrame e, int i)
    {
        editor = e;
        id = i;
    }

    @Override
    public DataFlavor supportedFlavor()
    {
        return DataFlavors.entryFlavor;
    }

    @Override
    public boolean canImport(TransferSupport supp)
    {
        if (supp.isDataFlavorSupported(supportedFlavor()))
        {
            try
            {
                EntryTransferData data = (EntryTransferData)supp.getTransferable().getTransferData(supportedFlavor());
                if (data.source == editor && data.from == id && supp.isDrop())
                    return false;
            }
            catch (UnsupportedFlavorException | IOException e)
            {
                return false;
            }
        }
        return supp.isDataFlavorSupported(supportedFlavor());
    }

    @Override
    public boolean importData(TransferSupport supp)
    {
        if (canImport(supp))
        {
            try
            {
                EntryTransferData data = (EntryTransferData)supp.getTransferable().getTransferData(supportedFlavor());
                if (supp.isDrop())
                {
                    // Actually handle all list modification in the source handler; just tell it where the cards should go
                    data.target = editor;
                    data.to = id;
                    return true;
                }
                else
                    return editor.modifyCards(id, data.entries);
            }
            catch (UnsupportedFlavorException | IOException e)
            {
                return false;
            }
        }
        else
            return false;
    }
}