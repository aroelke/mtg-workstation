package editor.gui.ccp;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import editor.database.card.Card;
import editor.gui.editor.EditorFrame;

/**
 * This class represents data being transferred via drag and drop or cut/copy/paste
 * between a CardList and another object. CardLists only supports importing card or entry
 * data flavors, but can export Strings as well.
 *
 * @author Alec Roelke
 */
public class DeckTransferData implements Transferable
{
    /** Cards being transferred. */
    public final Map<Card, Integer> cards;
    /** Frame containing the deck being transferred from. */
    public final EditorFrame source;
    /** ID of the list in the deck being transferred from. */
    public final int id;

    public DeckTransferData(EditorFrame e, int id, Map<Card, Integer> cards)
    {
        this.cards = Collections.unmodifiableMap(cards);
        source = e;
        this.id = id;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
    {
        if (flavor.equals(DataFlavors.entryFlavor))
            return this;
        else if (flavor.equals(DataFlavors.cardFlavor))
            return cards.keySet().stream().sorted(Card::compareName).toArray(Card[]::new);
        else if (flavor.equals(DataFlavor.stringFlavor))
            return cards.entrySet().stream().map((e) -> e.getValue() + "x " + e.getKey().unifiedName()).collect(Collectors.joining("\n"));
        else
            throw new UnsupportedFlavorException(flavor);
    }

    @Override
    public DataFlavor[] getTransferDataFlavors()
    {
        return new DataFlavor[]{ DataFlavors.entryFlavor, DataFlavors.cardFlavor, DataFlavor.stringFlavor };
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor)
    {
        return Arrays.asList(getTransferDataFlavors()).contains(flavor);
    }
}