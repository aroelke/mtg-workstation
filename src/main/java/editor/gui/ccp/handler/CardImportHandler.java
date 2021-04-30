package editor.gui.ccp.handler;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.swing.TransferHandler;

import editor.database.card.Card;
import editor.gui.ccp.data.DataFlavors;
import editor.gui.editor.EditorFrame;

/**
 * Import handler for importing a list of single copies of cards.
 * 
 * @author Alec Roelke
 */
public class CardImportHandler extends TransferHandler implements ImportHandler
{
    /** Editor frame that is importing the cards. */
    private final EditorFrame editor;
    /** ID of the list to import cards to. */
    private final int id;

    /**
     * Create a new card import handler.
     * 
     * @param e editor frame containing the deck to import cards into
     * @param i ID of the list in the deck to import into
     */
    public CardImportHandler(EditorFrame e, int i)
    {
        editor = e;
        id = i;
    }

    @Override
    public DataFlavor supportedFlavor()
    {
        return DataFlavors.cardFlavor;
    }

    @Override
    public boolean canImport(TransferSupport supp)
    {
        return supp.isDataFlavorSupported(supportedFlavor());
    }

    @Override
    public boolean importData(TransferSupport supp)
    {
        if (!canImport(supp))
            return false;
        else
        {
            try
            {
                var data = Arrays.stream((Card[])supp.getTransferable().getTransferData(supportedFlavor())).collect(Collectors.toSet());
                return editor.addCards(id, data, 1);
            }
            catch (UnsupportedFlavorException | IOException e)
            {
                return false;
            }
        }
    }
}