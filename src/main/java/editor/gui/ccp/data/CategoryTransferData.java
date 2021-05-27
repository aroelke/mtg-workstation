package editor.gui.ccp.data;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import editor.collection.deck.Category;
import editor.database.card.Card;
import editor.gui.editor.EditorFrame;

/**
 * This class contains the specification for a category to transfer using cut,
 * copy, and paste.
 * 
 * @author Alec Roelke
 */
public class CategoryTransferData extends EntryTransferData
{
    /** Category specification to transfer. */
    public final Category spec;

    /**
     * Create a new category transfer data for data from a deck. This constructor
     * is only a placeholder until how to handle the cards that may be in the
     * categories is determined.
     * 
     * @param e reserved for future use
     * @param d specification to transfer
     * @param cards reserved for future use
     */
    public CategoryTransferData(EditorFrame e, Category d, Map<Card, Integer> cards)
    {
        super(e, 0, cards); // Only main deck can have categories
        spec = d;
    }

    /**
     * Create a new category transfer data.
     * 
     * @param d specification to transfer
     */
    public CategoryTransferData(Category d)
    {
        this(null, d, Collections.emptyMap());
    }

    @Override
    public DataFlavor[] getTransferDataFlavors()
    {
        DataFlavor[] superFlavors = super.getTransferDataFlavors();
        DataFlavor[] flavors = new DataFlavor[superFlavors.length + 1];
        flavors[0] = DataFlavors.categoryFlavor;
        for (int i = 0; i < superFlavors.length; i++)
            flavors[i + 1] = superFlavors[i];
        return flavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor)
    {
        return Arrays.asList(getTransferDataFlavors()).contains(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
    {
        if (flavor.equals(DataFlavors.categoryFlavor))
            return this;
        else
            return super.getTransferData(flavor);
    }
}