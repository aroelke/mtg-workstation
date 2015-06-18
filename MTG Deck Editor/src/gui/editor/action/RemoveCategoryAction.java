package gui.editor.action;

import java.util.function.Predicate;

import database.Card;
import database.Deck;

public class RemoveCategoryAction implements DeckAction
{
	private Deck deck;
	private String nameRemoved;
	private String reprRemoved;
	private Predicate<Card> filterRemoved;
	
	public RemoveCategoryAction(Deck d, String n, String r, Predicate<Card> f)
	{
		deck = d;
		nameRemoved = n;
		reprRemoved = r;
		filterRemoved = f;
	}
	
	@Override
	public void undo()
	{
		if (deck.containsCategory(nameRemoved))
			throw new IllegalStateException("Deck already contains category " + nameRemoved);
		else
			deck.addCategory(nameRemoved, reprRemoved, filterRemoved);
	}

	@Override
	public void redo()
	{
		if (!deck.containsCategory(nameRemoved))
			throw new IllegalStateException("Deck does not contain category " + nameRemoved);
		else
			deck.removeCategory(nameRemoved);
	}

}
