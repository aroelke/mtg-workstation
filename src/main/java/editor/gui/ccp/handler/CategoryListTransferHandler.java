package editor.gui.ccp.handler;

import java.awt.datatransfer.Transferable;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import editor.collection.deck.CategorySpec;
import editor.gui.ccp.data.CategoryTransferData;

@SuppressWarnings("serial")
public class CategoryListTransferHandler extends TransferHandler
{
    private final ImportHandler importer;
    private final Supplier<CategorySpec> supplier;
    private final Consumer<CategorySpec> remove;

    public CategoryListTransferHandler(Supplier<CategorySpec> s, Predicate<CategorySpec> c, Predicate<CategorySpec> a, Consumer<CategorySpec> r)
    {
        importer = new CategoryImportHandler(c, a);
        supplier = s;
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
        return importer.canImport(supp);
    }

    @Override
    public Transferable createTransferable(JComponent c)
    {
        return new CategoryTransferData(supplier.get());
    }

    @Override
    public boolean importData(TransferSupport supp)
    {
        return importer.importData(supp);
    }

    @Override
    public void exportDone(JComponent source, Transferable data, int action)
    {
        if (action == TransferHandler.MOVE)
            remove.accept(((CategoryTransferData)data).data);
    }
}