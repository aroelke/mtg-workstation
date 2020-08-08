package editor.gui.ccp.handler;

import java.awt.datatransfer.Transferable;
import java.util.Collection;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import editor.database.card.Card;
import editor.gui.ccp.data.CardTransferData;

/**
 * This class is a handler for expording copied data from the inventory. It only
 * supports copying single copies of cards.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class InventoryExportHandler extends TransferHandler
{
    /** Function for generating the list of cards to export. */
    private final Supplier<? extends Collection<? extends Card>> cards;

    /**
     * Create a new inventory export handler that determines the cards to export
     * using the given function.
     * 
     * @param c supplier of the list of cards to export
     */
    public InventoryExportHandler(Supplier<? extends Collection<? extends Card>> c)
    {
        cards = c;
    }

    @Override
    public int getSourceActions(JComponent c)
    {
        return TransferHandler.COPY;
    }

    @Override
    public Transferable createTransferable(JComponent c)
    {
        return new CardTransferData(cards.get());
    }
}