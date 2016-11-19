package editor.collection.deck;

import java.util.EventListener;

/**
 * This is an interface for  listener that performs an action when a
 * deck is changed.
 * 
 * @author Alec Roelke
 */
@FunctionalInterface
public interface DeckListener extends EventListener
{
	/**
	 * Based on the changes specified by the given event, perform an action.
	 * 
	 * @param e event specifying the change in the Deck
	 */
	public void deckChanged(Deck.Event e);
}
