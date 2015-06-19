package gui.editor;

import java.util.List;

import database.Card;

/**
 * This class represents an action to add cards to a deck.
 * 
 * @author Alec Roelke
 */
public class AddCardsAction implements DeckAction
{
	/**
	 * EditorFrame that contains the deck.
	 */
	private EditorFrame editor;
	/**
	 * List of cards that was added to the deck.
	 */
	private List<Card> cardsAdded;
	/**
	 * Amount of each card that was added to the deck.
	 */
	private int count;
	
	/**
	 * Create a new AddCardsAction.
	 * 
	 * @param e Editor this action was performed on
	 * @param added Cards that were added to the deck
	 * @param c Number of each card that was added to the deck
	 */
	public AddCardsAction(EditorFrame e, List<Card> added, int c)
	{
		editor = e;
		cardsAdded = added;
		count = c;
	}
	
	/**
	 * Undo the addition of the cards to the deck, or remove them from
	 * the deck.
	 */
	@Override
	public void undo()
	{
		editor.removeCardsUnbuffered(cardsAdded, count);
	}

	/**
	 * Redo the addition of the cards to the deck, or add them to the deck.
	 */
	@Override
	public void redo()
	{
		editor.addCardsUnbuffered(cardsAdded, count);
	}
}
