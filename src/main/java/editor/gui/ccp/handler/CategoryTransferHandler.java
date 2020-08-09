package editor.gui.ccp.handler;

import java.awt.datatransfer.Transferable;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import editor.collection.deck.CategorySpec;
import editor.gui.ccp.data.CategoryTransferData;

/**
 * Handler for transferring category specifications between components in the handler.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class CategoryTransferHandler extends EditorTransferHandler
{
    /** Function supplying the category specification to transfer. */
    private final Supplier<CategorySpec> supplier;
    /** Function specifying how to remove cut data. */
    private final Consumer<CategorySpec> remove;

    /**
     * Create a new category transfer handler.
     * 
     * @param s function supplying the category specification to transfer
     * @param c function determining if the source component contains the category,
     * if applicable (should simply return false if not applicable)
     * @param a function indicating how to add the category to the source component
     * @param r function indicating how to remove the category if it is cut from the
     * source component
     */
    public CategoryTransferHandler(Supplier<CategorySpec> s, Predicate<CategorySpec> c, Predicate<CategorySpec> a, Consumer<CategorySpec> r)
    {
        super(new CategoryImportHandler(c, a));
        supplier = s;
        remove = r;
    }

    @Override
    public int getSourceActions(JComponent c)
    {
        return TransferHandler.MOVE | TransferHandler.COPY;
    }

    @Override
    public Transferable createTransferable(JComponent c)
    {
        return new CategoryTransferData(supplier.get());
    }

    @Override
    public void exportDone(JComponent source, Transferable data, int action)
    {
        if (action == TransferHandler.MOVE)
            remove.accept(((CategoryTransferData)data).spec);
    }
}