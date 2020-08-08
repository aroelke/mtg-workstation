package editor.gui.ccp.handler;

import java.awt.datatransfer.Transferable;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import editor.collection.CardList;
import editor.gui.ccp.data.EntryTransferData;
import editor.gui.editor.EditorFrame;

/**
 * This class represents a transfer handler for moving data to and from a table
 * in the editor. It can import or export data of the card or entry flavors.
 *
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class EditorTableTransferHandler extends EditorFrameTransferHandler
{
    /** Editor containing the deck to import cards into. */
    private final EditorFrame editor;
    /** ID of the list in the deck to import into. */
    private final int id;

    /**
     * Create a new EditorTableTransferHandler that handles transfers to or from
     * the main deck or extra lists.
     *
     * @param n ID of the list to make changes to
     */
    public EditorTableTransferHandler(EditorFrame e, int i)
    {
        super(e, i);
        editor = e;
        id = i;
    }

    @Override
    public Transferable createTransferable(JComponent c)
    {
        CardList source = editor.getList(id);
        var data = editor.getSelectedCards().stream().collect(Collectors.toMap(Function.identity(), (card) -> source.getEntry(card).count()));
        return new EntryTransferData(editor, id, data);
    }

    @Override
    public int getSourceActions(JComponent c)
    {
        return TransferHandler.MOVE | TransferHandler.COPY;
    }

    @Override
    public void exportDone(JComponent source, Transferable data, int action)
    {
        if (data instanceof EntryTransferData)
        {
            EntryTransferData d = (EntryTransferData)data;
            switch (action)
            {
            case TransferHandler.MOVE:
                if (d.source == d.target)
                {
                    d.source.moveCards(d.from, d.to, d.entries);
                    break;
                }
                else
                    d.source.modifyCards(d.from, d.entries.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (e) -> -e.getValue())));
            case TransferHandler.COPY:
                if (d.target != null)
                    d.target.modifyCards(d.to, d.entries);
                break;
            default:
                break;
            }
        }
    }
}