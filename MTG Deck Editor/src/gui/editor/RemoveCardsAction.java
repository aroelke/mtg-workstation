package gui.editor;

import java.util.List;

import database.Card;

/**
 * This class represents an action to remove cards from a deck.
 * 
 * @author Alec Roelke
 */
public class RemoveCardsAction implements DeckAction
{
	/**
	 * Editor containing the deck that had cards removed from it.
	 */
	private EditorFrame editor;
	/**
	 * Cards that were removed from the deck.
	 */
	private List<Card> cardsRemoved;
	/**
	 * Number of each card that was removed.
	 */
	private int count;
	
	/**
	 * Create a new RemoveCardsAction.
	 * 
	 * @param e Editor the action was performed on
	 * @param removed Cards that were removed
	 * @param c Number of each card that was removed
	 */
	public RemoveCardsAction(EditorFrame e, List<Card> removed, int c)
	{
		editor = e;
		cardsRemoved = removed;
		count = c;
	}
	
	/**
	 * Undo the removal of cards from the deck, or add them back in.
	 */
	@Override
	public void undo()
	{
		editor.addCardsUnbuffered(cardsRemoved, count);
	}

	/**
	 * Redo the removal of cards from the deck, or remove them again.
	 */
	@Override
	public void redo()
	{
		editor.removeCardsUnbuffered(cardsRemoved, count);
	}
}
