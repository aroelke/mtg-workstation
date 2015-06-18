package gui.editor.action;

import java.util.List;

import database.Card;
import database.Deck;

public class AddCardsAction implements DeckAction
{
	private Deck deck;
	private List<Card> cardsAdded;
	private int count;
	
	public AddCardsAction(Deck d, List<Card> added, int c)
	{
		deck = d;
		cardsAdded = added;
		count = c;
	}
	
	@Override
	public void undo()
	{
		for (Card c: cardsAdded)
			if (deck.count(c) < count)
				throw new IllegalStateException("Not enough copies of " + c.name);
		for (Card c: cardsAdded)
			deck.remove(c, count);
	}

	@Override
	public void redo()
	{
		deck.addAll(cardsAdded, count);
	}
}
