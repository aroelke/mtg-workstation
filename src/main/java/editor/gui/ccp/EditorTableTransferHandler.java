package editor.gui.ccp;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import editor.collection.CardList;
import editor.database.card.Card;
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
     * @param n name of the list to make changes to
     */
    public EditorTableTransferHandler(String n, EditorFrame e)
    {
        super(n, e);
    }

    @Override
    public Transferable createTransferable(JComponent c)
    {
        CardList source = editor.getList(name);
        var data = editor.getSelectedCards().stream().collect(Collectors.toMap(Function.identity(), (card) -> source.getEntry(card).count()));
        return new DeckTransferData(source, data);
    }

    @Override
    public void exportDone(JComponent c, Transferable t, int action)
    {
        if (t instanceof DeckTransferData)
        {
            try
            {
                @SuppressWarnings("unchecked")
                var data = (Map<Card, Integer>)((DeckTransferData)t).getTransferData(CardList.entryFlavor);
                if (action == TransferHandler.MOVE)
                    editor.modifyCards(name, data.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (e) -> -e.getValue())));
            }
            catch (UnsupportedFlavorException e)
            {}
        }
        else
            throw new UnsupportedOperationException("Can't export data of type " + t.getClass());
    }

    /**
     * {@inheritDoc}
     * Only copying is supported.
     */
    @Override
    public int getSourceActions(JComponent c)
    {
        return TransferHandler.MOVE | TransferHandler.COPY;
    }
}