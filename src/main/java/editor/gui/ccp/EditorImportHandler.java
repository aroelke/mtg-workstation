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

    @Override
    public boolean importData(TransferSupport supp)
    {
        try
        {
            if (!canImport(supp))
                return false;
            else if (supp.isDataFlavorSupported(CardList.entryFlavor))
            {
                DeckTransferData data = (DeckTransferData)supp.getTransferable().getTransferData(CardList.entryFlavor);
                boolean success = false;
                switch (supp.getDropAction())
                {
                case TransferHandler.MOVE:
                    if (data.source == editor)
                    {
                        success = data.source.moveCards(data.id, id, data.cards);
                        break;
                    }
                    else
                    {
                        success |= data.source.modifyCards(data.id, data.cards.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (e) -> -e.getValue())));
                        if (!success)
                            break;
                    }
                case TransferHandler.COPY:
                    success |= editor.modifyCards(id, data.cards);
                    break;
                }
                return success;
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