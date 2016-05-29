package editor.collection.deck;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import editor.collection.category.CategoryEvent;
import editor.database.Card;

/**
 * This class represents an event during which a Deck may have changed.
 * It can indicate how many copies of Cards may have been added to or
 * removed from the Deck, how a category may have changed, or if any
 * categories were removed.  If a parameter did not change and the contents
 * of that parameter's change are requested, throw an IllegalStateException.
 * 
 * @author Alec Roelke
 */
public class DeckEvent
{
	/**
	 * Deck that experienced the change that generated this DeckEvent.
	 */
	private Deck source;
	/**
	 * If Cards were added to or removed from the Deck, this map
	 * contains which ones and how many copies.
	 */
	private Map<Card, Integer> cardsChanged;
	/**
	 * If a category's name was changed, its old name.
	 */
	private String changedName;
	/**
	 * CategoryEvent representing the changes to the CategorySpec corresponding
	 * to the category that was changed, if any was changed.
	 */
	private CategoryEvent categoryChanges;
	/**
	 * If a category was added to the deck, its name.
	 */
	private String addedCategory;
	/**
	 * Set of names of categories that have been removed, if any.
	 */
	private Set<String> removedCategories;
	
	/**
	 * Create a new DeckEvent.  Use <code>null</code> for any parameter that did
	 * not change as a result of the event.
	 * 
	 * @param s Deck that changed to generate the new DeckEvent
	 * @param cards Cards that may have been added or removed.  Removed cards
	 * are indicated using negative numbers
	 * @param changeName Name of the category that may have changed
	 * @param catChange CategoryEvent specifying changes to the category if any
	 * were made
	 * @param catAdd set of category names that were added to the Deck, if any were
	 * @param catRem Set of category names that were removed from the Deck, if
	 * any were
	 */
	public DeckEvent(Deck s,
			Map<Card, Integer> cards,
			String changeName, CategoryEvent catChange,
			String catAdd, Set<String> catRem)
	{
		source = s;
		
		cardsChanged = cards;
		changedName = changeName;
		categoryChanges = catChange;
		addedCategory = catAdd;
		removedCategories = catRem;
		
		if ((changedName == null) != (categoryChanges == null))
			throw new IllegalStateException("Reporting changes to category without name");
	}
	
	/**
	 * Create a new DeckEvent that shows no changes to categories.
	 * 
	 * @param s Deck that was changed
	 * @param cards Map of cards onto counts that were changed
	 */
	public DeckEvent(Deck s, Map<Card, Integer> cards)
	{
		this(s, cards, null, null, null, null);
	}
	
	/**
	 * TODO: Comment this
	 * @param s
	 * @param changeName
	 * @param catChange
	 */
	public DeckEvent(Deck s, String changeName, CategoryEvent catChange)
	{
		this(s, null, changeName, catChange, null, null);
	}
	
	/**
	 * TODO: Comment this
	 * @param s
	 * @param catAdd
	 */
	public DeckEvent(Deck s, String catAdd)
	{
		this(s, null, null, null, catAdd, null);
	}
	
	/**
	 * TODO: Comment this
	 * @param s
	 * @param catRem
	 */
	public DeckEvent(Deck s, Set<String> catRem)
	{
		this(s, null, null, null, null, catRem);
	}
	
	/**
	 * @return The Deck that changed to create this DeckEvent.
	 */
	public Deck getSource()
	{
		return source;
	}
	
	/**
	 * @return <code>true</code> if Cards were added to or removed from the Deck
	 * during the event.
	 */
	public boolean cardsChanged()
	{
		return cardsChanged != null;
	}
	
	/**
	 * @return A map containing the Cards that were added and the number of copies
	 * that were added.
	 * @throws IllegalStateException If no cards were added or removed during the
	 * event.
	 */
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
	
	/**
	 * @return A map of cards that were removed and the number of copies that
	 * were removed.  Positive numbers are used to indicate removed cards.
	 * @throws IllegalStateException If no cards were added or removed during
	 * the event.
	 */
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
	
	/**
	 * @return <code>true</code> if a category in the Deck was changed, and
	 * <code>false</code> otherwise.
	 */
	public boolean categoryChanged()
	{
		return categoryChanges != null;
	}
	
	/**
	 * @return The name of the category that was changed before the event.
	 * Use this rather than the CategoryEvent returned by {@link categoryChanges()}
	 * to identify which category was changed if its name was not changed.
	 * @throws IllegalStateException If no category was changed during the
	 * event.
	 */
	public String categoryName()
	{
		if (categoryChanged())
			return changedName;
		else
			throw new IllegalStateException("Category was not changed");
	}
	
	/**
	 * @return A CategoryEvent detailing the changes to the category.
	 * @throws IllegalStateException if no category was changed during
	 * the event.
	 */
	public CategoryEvent categoryChanges()
	{
		if (categoryChanged())
			return categoryChanges;
		else
			throw new IllegalStateException("Category was not changed");
	}
	
	/**
	 * @return <code>true</code> if a category was added to the deck, and
	 * <code>false</code>.
	 */
	public boolean categoryAdded()
	{
		return addedCategory != null;
	}
	
	/**
	 * @return The name of the category that was added.
	 * @throws IllegalStateException If no category was added.
	 */
	public String addedName()
	{
		if (categoryAdded())
			return addedCategory;
		else
			throw new IllegalStateException("No category has been added to the deck");
	}
	
	/**
	 * @return <code>true</code> if any categories were removed during the
	 * event, and <code>false</code> otherwise.
	 */
	public boolean categoriesRemoved()
	{
		return removedCategories != null;
	}
	
	/**
	 * @return The set of names of the categories that were removed during the
	 * event.
	 * @throws IllegalStateException If no categories were removed during the
	 * event.
	 */
	public Set<String> removedNames()
	{
		if (categoriesRemoved())
			return removedCategories;
		else
			throw new IllegalStateException("No category has been removed from the deck");
	}
}
