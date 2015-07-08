package gui.editor;

import database.Card;

/**
 * This class represents an action to set the number of cards in a deck to a certain
 * value.
 * 
 * @author Alec Roelke
 */
public class SetCardCountAction implements DeckAction
{
	/**
	 * EditorFrame containing the deck being edited.
	 */
	private EditorFrame editor;
	/**
	 * Card whose count was modified.
	 */
	private Card card;
	/**
	 * Number of copies of the card before modification.
	 */
	private int before;
	/**
	 * Number of copies of the card after modification.
	 */
	private int after;
	
	/**
	 * Create a new SetCardCountAction.
	 * 
	 * @param e EditorFrame of the new action
	 * @param c Card for the new action
	 * @param b Previous count for the new action
	 * @param a New count for the new action
	 */
	public SetCardCountAction(EditorFrame e, Card c, int b, int a)
	{
		editor = e;
		card = c;
		before = b;
		after = a;
	}
	
	/**
	 * Undo the copy-count change, or set it to the old value.
	 */
	@Override
	public void undo()
	{
		editor.deck.setCount(card, before);
		editor.revalidate();
		editor.repaint();
	}

	/**
	 * Redo the copy-count change, or set it to the new value.
	 */
	@Override
	public void redo()
	{
		editor.deck.setCount(card, after);
		editor.revalidate();
		editor.repaint();
	}
}
