package editor.gui.ccp.handler;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.function.Predicate;

import javax.swing.TransferHandler;

import editor.collection.deck.CategorySpec;
import editor.gui.ccp.data.CategoryTransferData;
import editor.gui.ccp.data.DataFlavors;

/**
 * Importer for category specifications.
 * 
 * @author Alec Roelke
 */
public class CategoryImportHandler extends TransferHandler implements ImportHandler
{
    /** Function specifying how to tell if the deck contains the category already. */
    private final Predicate<CategorySpec> contains;
    /** Function specifying how to add the category. */
    private final Predicate<CategorySpec> add;

    /**
     * Create a new category import handler.
     * 
     * @param c function specifying whether or not the source component contains the
     * category, if applicable (should just return false if it's not applicable)
     * @param a function specifying how to add the category to the component
     */
    public CategoryImportHandler(Predicate<CategorySpec> c, Predicate<CategorySpec> a)
    {
        contains = c;
        add = a;
    }

    @Override
    public DataFlavor supportedFlavor()
    {
        return DataFlavors.categoryFlavor;
    }

    @Override
    public boolean canImport(TransferSupport supp)
    {
        if (supp.isDrop())
            return false;
        else if (supp.isDataFlavorSupported(DataFlavors.categoryFlavor))
        {
            try
            {
                CategoryTransferData data = (CategoryTransferData)supp.getTransferable().getTransferData(DataFlavors.categoryFlavor);
                return !contains.test(data.spec);
            }
            catch (UnsupportedFlavorException | IOException e)
            {
                return false;
            }
        }
        else
            return false;
    }

    @Override
    public boolean importData(TransferSupport supp)
    {
        try
        {
            if (!canImport(supp))
                return false;
            else
                return add.test(((CategoryTransferData)supp.getTransferable().getTransferData(DataFlavors.categoryFlavor)).spec);
        }
        catch (UnsupportedFlavorException | IOException e)
        {
            return false;
        }
    }
}