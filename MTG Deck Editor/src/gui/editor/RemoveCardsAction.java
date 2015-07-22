package gui.editor;

import java.util.Collections;
import java.util.Map;

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
	 * Map of Cards onto integers indicating which cards and how many were removed.
	 */
	private Map<Card, Integer> removed;
	
	/**
	 * Create a new RemoveCardsAction.
	 * 
	 * @param e Editor the action was performed on
	 * @param r Map indicating cards and counts removed.
	 */
	public RemoveCardsAction(EditorFrame e, Map<Card, Integer> r)
	{
		editor = e;
		removed = r;
	}
	
	/**
	 * Undo the removal of cards from the deck, or add them back in.
	 */
	@Override
	public void undo()
	{
		for (Map.Entry<Card, Integer> e: removed.entrySet())
			editor.addCardUnbuffered(e.getKey(), e.getValue());
	}

	/**
	 * Redo the removal of cards from the deck, or remove them again.
	 */
	@Override
	public void redo()
	{
		editor.removeCardsUnbuffered(removed.keySet(), Collections.max(removed.values()));
	}
}
