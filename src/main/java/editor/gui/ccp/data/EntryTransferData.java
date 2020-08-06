package editor.gui.ccp.data;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import editor.database.card.Card;
import editor.gui.editor.EditorFrame;

/**
 * This class represents data being transferred via drag and drop or cut/copy/paste
 * between a CardList and another object. CardLists only supports importing card or entry
 * data flavors, but can export Strings as well.
 *
 * @author Alec Roelke
 */
public class EntryTransferData extends CardTransferData
{
    /** Cards being transferred. */
    public final Map<Card, Integer> entries;
    /** Frame containing the deck being transferred from. */
    public final EditorFrame source;
    /** Frame containing the deck being transferred to. */
    public EditorFrame target;
    /** ID of the list in the deck being transferred from. */
    public final int from;
    /** ID of the list int he deck being transferred to. */
    public int to;

    public EntryTransferData(EditorFrame e, int id, Map<Card, Integer> cards)
    {
        super(cards.keySet().stream().sorted(Card::compareName).toArray(Card[]::new));
        this.entries = Collections.unmodifiableMap(cards);
        source = e;
        target = null;
        from = id;
        to = -1;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
    {
        if (flavor.equals(DataFlavors.entryFlavor))
            return this;
        else
            return super.getTransferData(flavor);
    }

    @Override
    public DataFlavor[] getTransferDataFlavors()
    {
        DataFlavor[] superFlavors = super.getTransferDataFlavors();
        DataFlavor[] flavors = new DataFlavor[superFlavors.length + 1];
        flavors[0] = DataFlavors.entryFlavor;
        for (int i = 0; i < superFlavors.length; i++)
            flavors[i + 1] = superFlavors[i];
        return flavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor)
    {
        return Arrays.asList(getTransferDataFlavors()).contains(flavor);
    }
}