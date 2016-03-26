package editor.collection.deck;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import editor.collection.category.CategoryEvent;
import editor.database.Card;

/**
 * TODO: Comment this class
 * 
 * @author Alec Roelke
 */
public class DeckEvent
{
	private Deck source;
	private Map<Card, Integer> cardsChanged;
	private String changedName;
	private CategoryEvent categoryChanges;
	private Set<String> removedCategories;
	
	public DeckEvent(Deck s,
			Map<Card, Integer> cards,
			String changeName, CategoryEvent catChange, Set<String> catRem)
	{
		source = s;
		
		cardsChanged = cards;
		changedName = changeName;
		categoryChanges = catChange;
		removedCategories = catRem;
		
		if ((changedName == null) != (categoryChanges == null))
			throw new IllegalStateException("Reporting changes to category without name");
	}
	
	public Deck getSource()
	{
		return source;
	}
	
	public boolean cardsChanged()
	{
		return cardsChanged != null;
	}
	
	public Map<Card, Integer> cardsAdded()
	{
		if (cardsChanged())
		{
			Map<Card, Integer> cards = new HashMap<Card, Integer>(cardsChanged);
			for (Card c: cardsChanged.keySet())
				if (cardsChanged.get(c) < 1)
					cards.remove(c);
			return cards;
		}
		else
			throw new IllegalStateException("Deck cards were not changed");
	}
	
	public Map<Card, Integer> cardsRemoved()
	{
		if (cardsChanged())
		{
			Map<Card, Integer> cards = new HashMap<Card, Integer>(cardsChanged);
			for (Card c: cardsChanged.keySet())
				if (cardsChanged.get(c) > -1)
					cards.remove(c);
				else
					cardsChanged.compute(c, (k, v) -> -v);
			return cards;
		}
		else
			throw new IllegalStateException("Deck cards were not changed");
	}
	
	public boolean categoryChanged()
	{
		return categoryChanges != null;
	}
	
	public String categoryName()
	{
		if (categoryChanged())
			return changedName;
		else
			throw new IllegalStateException("Category was not changed");
	}
	
	public CategoryEvent categoryChanges()
	{
		if (categoryChanged())
			return categoryChanges;
		else
			throw new IllegalStateException("Category was not changed");
	}
	
	public boolean categoryRemoved()
	{
		return removedCategories != null;
	}
	
	public Set<String> removedName()
	{
		if (categoryRemoved())
			return removedCategories;
		else
			throw new IllegalStateException("No category has been removed from the deck");
	}
}
