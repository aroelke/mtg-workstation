package editor.gui.ccp;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import editor.collection.deck.CategorySpec;

@SuppressWarnings("serial")
public class CategoryTransferHandler extends TransferHandler
{
    final private Supplier<CategorySpec> supplier;
    final private Predicate<CategorySpec> contains;
    final private Predicate<CategorySpec> add;
    final private Consumer<CategorySpec> remove;

    public CategoryTransferHandler(Supplier<CategorySpec> s, Predicate<CategorySpec> c, Predicate<CategorySpec> a, Consumer<CategorySpec> r)
    {
        supplier = s;
        contains = c;
        add = a;
        remove = r;
    }

    @Override
    public int getSourceActions(JComponent c)
    {
        return TransferHandler.MOVE | TransferHandler.COPY;
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
                return !contains.test(data.data);
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
    public Transferable createTransferable(JComponent c)
    {
        return new CategoryTransferData(supplier.get());
    }

    @Override
    public boolean importData(TransferSupport supp)
    {
        try
        {
            if (!canImport(supp))
                return false;
            else
                return add.test(((CategoryTransferData)supp.getTransferable().getTransferData(DataFlavors.categoryFlavor)).data);
        }
        catch (UnsupportedFlavorException | IOException e)
        {
            return false;
        }
    }

    @Override
    public void exportDone(JComponent source, Transferable data, int action)
    {
        if (action == TransferHandler.MOVE)
            remove.accept(((CategoryTransferData)data).data);
    }
}