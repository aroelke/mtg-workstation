package editor.gui.ccp.handler

import editor.gui.editor.EditorFrame

/**
 * Transfer handler for importing cards into decks via the [[EditorFrame]] itself, rather than the tables it contains. Mainly a convenience
 * for drag-and-drop.
 * 
 * @constructor create a new editor frame transfer handler for a particular list in a particular [[EditorFrame]]
 * @param editor frame to which the handler belongs
 * @param id ID of the list
 * 
 * @author Alec Roelke
 */
class EditorFrameTransferHandler(editor: EditorFrame, id: Int) extends EditorTransferHandler(Seq(EntryImportHandler(editor, id), CardImportHandler(editor, id)))