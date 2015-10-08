package gui.editor;

/**
 * TODO: Use this interface for undo/redo instead of the more clunky DeckAction
 * - Create an anonymous class with the appropriate edits to the deck implemented
 * in undo() and redo()
 * TODO: Comment this class
 * @author Alec
 *
 */
public interface UndoableAction
{
	public boolean undo();
	
	public boolean redo();
}
