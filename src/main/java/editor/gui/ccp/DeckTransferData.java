package editor.gui.ccp;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.Arrays;
import java.util.Map;

import editor.collection.CardList;
import editor.database.card.Card;

/**
 * This class represents data being transferred via drag and drop or cut/copy/paste
 * between a CardList and another object. CardLists only supports importing card or entry
 * data flavors, but can export Strings as well.
 *
 * @author Alec Roelke
 */
public class DeckTransferData implements Transferable
{
    /**
     * Entries being exported.
     */
    private Map<Card, Integer> transferData;

    public DeckTransferData(CardList d, Map<Card, Integer> cards)
    {
        transferData = cards;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
    {
        if (flavor.equals(CardList.entryFlavor))
            return transferData;
        else if (flavor.equals(Card.cardFlavor))
            return transferData.keySet().stream().sorted(Card::compareName).toArray(Card[]::new);
        else if (flavor.equals(DataFlavor.stringFlavor))
            return transferData.entrySet().stream().map((e) -> e.getValue() + "x " + e.getKey().unifiedName()).reduce("", (a, b) -> a + "\n" + b);
        else
            throw new UnsupportedFlavorException(flavor);
    }

    @Override
    public DataFlavor[] getTransferDataFlavors()
    {
        return new DataFlavor[]{ CardList.entryFlavor, Card.cardFlavor, DataFlavor.stringFlavor };
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor)
    {
        return Arrays.asList(getTransferDataFlavors()).contains(flavor);
    }
}