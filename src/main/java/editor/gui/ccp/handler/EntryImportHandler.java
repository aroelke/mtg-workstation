package editor.gui.ccp.handler;

import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.TransferHandler;

import editor.gui.ccp.DataFlavors;
import editor.gui.ccp.DeckTransferData;
import editor.gui.editor.EditorFrame;

@SuppressWarnings("serial")
public class EntryImportHandler extends TransferHandler implements ImportHandler
{
    private final EditorFrame editor;
    private final int id;

    public EntryImportHandler(EditorFrame e, int i)
    {
        editor = e;
        id = i;
    }

    @Override
    public boolean canImport(TransferSupport supp)
    {
        if (supp.isDataFlavorSupported(DataFlavors.entryFlavor))
        {
            try
            {
                DeckTransferData data = (DeckTransferData)supp.getTransferable().getTransferData(DataFlavors.entryFlavor);
                if (data.source == editor && data.from == id && supp.isDrop())
                    return false;
            }
            catch (UnsupportedFlavorException | IOException e)
            {
                return false;
            }
        }
        return supp.isDataFlavorSupported(DataFlavors.entryFlavor);
    }

    @Override
    public boolean importData(TransferSupport supp)
    {
        if (canImport(supp))
        {
            try
            {
                DeckTransferData data = (DeckTransferData)supp.getTransferable().getTransferData(DataFlavors.entryFlavor);
                if (supp.isDrop())
                {
                    // Actually handle all list modification in the source handler; just tell it where the cards should go
                    data.target = editor;
                    data.to = id;
                    return true;
                }
                else
                    return editor.modifyCards(id, data.cards);
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