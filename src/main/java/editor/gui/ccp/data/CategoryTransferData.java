package editor.gui.ccp.data;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import editor.collection.deck.CategorySpec;
import editor.database.card.Card;
import editor.gui.editor.EditorFrame;

public class CategoryTransferData extends EntryTransferData
{
    public final CategorySpec data;

    public CategoryTransferData(EditorFrame e, CategorySpec d, Map<Card, Integer> cards)
    {
        super(e, 0, cards); // Only main deck can have categories
        data = d;
    }

    public CategoryTransferData(CategorySpec d)
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