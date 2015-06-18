package gui.editor.action;

import java.util.List;

import database.Card;
import database.Deck;

public class RemoveCardsAction implements DeckAction
{
	private Deck deck;
	private List<Card> cardsRemoved;
	private int count;
	
	public RemoveCardsAction(Deck d, List<Card> removed, int c)
	{
		deck = d;
		cardsRemoved = removed;
		count = c;
	}
	
	@Override
	public void undo()
	{
		deck.addAll(cardsRemoved, count);
	}

	@Override
	public void redo()
	{
		for (Card c: cardsRemoved)
			if (deck.count(c) < count)
				throw new IllegalStateException("Not enough copies of " + c.name);
		for (Card c: cardsRemoved)
			deck.remove(c, count);
	}
}
