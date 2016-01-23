package editor.collection.deck;

import java.util.EventListener;

/**
 * TODO: Comment this class
 * @author Alec Roelke
 */
@FunctionalInterface
public interface DeckListener extends EventListener
{
	public void DeckChanged(DeckEvent e);
}
