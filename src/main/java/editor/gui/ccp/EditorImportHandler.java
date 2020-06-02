package editor.gui.ccp;

import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.swing.TransferHandler;

import editor.database.card.Card;
import editor.gui.editor.EditorFrame;

/**
 * This class represents a transfer handler for transferring cards to and from a
 * table in the editor frame.
 *
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class EditorImportHandler extends TransferHandler
{
    /**
     * Editor containing the data to move.
     */
    protected EditorFrame editor;
    /**
     * ID of the list to make changes to.
     */
    final protected int id;

    /**
     * Create a new EditorImportHandler that imports from the given list.
     *
     * @param n ID of the list to make changes to
     */
    public EditorImportHandler(int id, EditorFrame e)
    {
        super();
        editor = e;
        this.id = id;
    }

    /**
     * {@inheritDoc}
     * Data can only be imported if it is of the card or entry flavors.
     */
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
        return supp.isDataFlavorSupported(DataFlavors.entryFlavor) || supp.isDataFlavorSupported(DataFlavors.cardFlavor);
    }

    @Override
    public boolean importData(TransferSupport supp)
    {
        try
        {
            if (!canImport(supp))
                return false;
            else if (supp.isDataFlavorSupported(DataFlavors.entryFlavor))
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
            else if (supp.isDataFlavorSupported(DataFlavors.cardFlavor))
            {
                var data = Arrays.stream((Card[])supp.getTransferable().getTransferData(DataFlavors.cardFlavor)).collect(Collectors.toSet());
                return editor.addCards(id, data, 1);
            }
            else
                return false;
        }
        catch (UnsupportedFlavorException | IOException e)
        {
            return false;
        }
    }
}