package editor.gui.editor;

/**
 * This interface represents an action that can be undone (and subsequently redone).
 * 
 * @author Alec
 */
public interface UndoableAction
{
	/**
	 * Instructions to redo what was undone, or to just do the action.
	 * 
	 * @return <code>true</code> if the action was successfully redone
	 * or done.
	 */
	boolean redo();
	
	/**
	 * Instructions to undo what was done.
	 * 
	 * @return <code>true</code> if the action was successfully undone.
	 */
	boolean undo();
}
