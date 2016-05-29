package editor.collection.deck;

import java.util.EventListener;

/**
 * This is an interface for  listener that performs an action when a
 * Deck is changed.
 * 
 * @author Alec Roelke
 */
@FunctionalInterface
public interface DeckListener extends EventListener
{
	/**
	 * Based on the changes specified by the given DeckEvent,
	 * perform an action.
	 * 
	 * @param e DeckEvent specifying the change in the Deck
	 */
	public void deckChanged(Deck.Event e);
}
