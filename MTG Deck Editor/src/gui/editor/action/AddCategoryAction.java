package gui.editor.action;

import java.util.function.Predicate;

import database.Card;
import database.Deck;

public class AddCategoryAction implements DeckAction
{
	private Deck deck;
	private String nameAdded;
	private String reprAdded;
	private Predicate<Card> filterAdded;
	
	public AddCategoryAction(Deck d, String n, String r, Predicate<Card> f)
	{
		deck = d;
		nameAdded = n;
		reprAdded = r;
		filterAdded = f;
	}
	
	@Override
	public void undo()
	{
		if (!deck.containsCategory(nameAdded))
			throw new IllegalStateException("Deck does not contain category " + nameAdded);
		else
			deck.removeCategory(nameAdded);
	}

	@Override
	public void redo()
	{
		if (deck.containsCategory(nameAdded))
			throw new IllegalStateException("Deck already contains category " + nameAdded);
		else
			deck.addCategory(nameAdded, reprAdded, filterAdded);
	}
}
