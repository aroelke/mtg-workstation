package editor.gui.ccp.handler;

import editor.gui.editor.EditorFrame;

@SuppressWarnings("serial")
public class EditorFrameTransferHandler extends EditorTransferHandler
{
    public EditorFrameTransferHandler(EditorFrame e, int i)
    {
        super(new EntryImportHandler(e, i), new CardImportHandler(e, i));
    }
}