package editor.gui.ccp;

import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.TransferHandler;

import editor.collection.CardList;
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
        return supp.isDataFlavorSupported(CardList.entryFlavor) || supp.isDataFlavorSupported(Card.cardFlavor);
    }

    /**
     * {@inheritDoc}
     * If the data can be imported, copy the cards from the source to the target deck.
     */
    @Override
    public boolean importData(TransferSupport supp)
    {
        try
        {
            if (!canImport(supp))
                return false;
            else if (supp.isDataFlavorSupported(CardList.entryFlavor))
            {
                @SuppressWarnings("unchecked")
                var data = (Map<Card, Integer>)supp.getTransferable().getTransferData(CardList.entryFlavor);
                return editor.modifyCards(id, data);
            }
            else if (supp.isDataFlavorSupported(Card.cardFlavor))
            {
                var data = Arrays.stream((Card[])supp.getTransferable().getTransferData(Card.cardFlavor)).collect(Collectors.toSet());
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