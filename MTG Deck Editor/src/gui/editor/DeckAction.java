package gui.editor;

/**
 * This class represents an action that can be performed on a Deck.
 * 
 * TODO: Add actions for including and excluding cards from categories
 * 
 * @author Alec Roelke
 */
public interface DeckAction
{
	/**
	 * Undo an action that was performed on a Deck.
	 */
	public void undo();
	
	/**
	 * Redo an action that was undone on a Deck.
	 */
	public void redo();
}
