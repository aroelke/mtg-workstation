package editor.gui.ccp.handler;

import editor.gui.editor.EditorFrame;

/**
 * Transfer handler for importing cards into decks via the editor frame
 * (rather than a table in the frame).  Mainly used for convenience for
 * drag-and-drop.
 * 
 * @author Alec Roelke
 */
public class EditorFrameTransferHandler extends EditorTransferHandler
{
    /**
     * Create a new editor frame transfer handler.
     * 
     * @param e editor frame to handle transfers for
     * @param i ID of the list in the frame to handle transfers for
     */
    public EditorFrameTransferHandler(EditorFrame e, int i)
    {
        super(new EntryImportHandler(e, i), new CardImportHandler(e, i));
    }
}