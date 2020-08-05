package editor.gui.ccp.data;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import editor.collection.deck.CategorySpec;

public class CategoryTransferData implements Transferable
{
    public final CategorySpec data;

    public CategoryTransferData(CategorySpec d)
    {
        data = d;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors()
    {
        return new DataFlavor[]{ DataFlavors.categoryFlavor };
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor)
    {
        return flavor == DataFlavors.categoryFlavor;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
    {
        if (!isDataFlavorSupported(flavor))
            throw new UnsupportedFlavorException(flavor);
        return this;
    }
}