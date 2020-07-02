package editor.gui.ccp;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import editor.collection.deck.CategorySpec;
import editor.gui.editor.EditorFrame;

public class CategoryTransferData implements Transferable
{
    protected final CategorySpec data;
    protected final EditorFrame source;

    public CategoryTransferData(EditorFrame f, CategorySpec d)
    {
        data = d;
        source = f;
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
        return data;
    }
}