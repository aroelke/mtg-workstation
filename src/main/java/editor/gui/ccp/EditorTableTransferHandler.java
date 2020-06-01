package editor.gui.ccp;

import java.awt.datatransfer.Transferable;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import editor.collection.CardList;
import editor.gui.editor.EditorFrame;

/**
 * This class represents a transfer handler for moving data to and from a table
 * in the editor. It can import or export data of the card or entry flavors.
 *
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class EditorTableTransferHandler extends EditorImportHandler
{
    /**
     * Create a new EditorTableTransferHandler that handles transfers to or from
     * the main deck or extra lists.
     *
     * @param n ID of the list to make changes to
     */
    public EditorTableTransferHandler(int id, EditorFrame e)
    {
        super(id, e);
    }

    @Override
    public Transferable createTransferable(JComponent c)
    {
        CardList source = editor.getList(id);
        var data = editor.getSelectedCards().stream().collect(Collectors.toMap(Function.identity(), (card) -> source.getEntry(card).count()));
        return new DeckTransferData(editor, id, data);
    }

    @Override
    public int getSourceActions(JComponent c)
    {
        return TransferHandler.MOVE | TransferHandler.COPY;
    }
}