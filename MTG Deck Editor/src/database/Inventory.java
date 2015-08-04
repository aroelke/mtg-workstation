package database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import database.Deck.Category;

/**
 * This class represents an inventory of cards that can be added to decks.
 * 
 * 
 * @author Alec Roelke
 * @see util.CategorizableList
 */
public class Inventory implements CardCollection
{
	/**
	 * Master list of cards.
	 */
	private final List<Card> cards;
	/**
	 * Map of Card UIDs onto their Cards.
	 */
	private final Map<String, Card> IDs;
	/**
	 * Filtered view of the master list.
	 */
	private List<Card> filtrate;
	
	/**
	 * Create a new Inventory with the given list of Cards.
	 * 
	 * @param list List of Cards
	 */
	public Inventory(Collection<Card> list)
	{
		cards = new ArrayList<Card>(list);
		IDs = cards.stream().collect(Collectors.toMap((c) -> c.id(), Function.identity()));
		filtrate = cards;
	}
	
	/**
	 * Create an empty inventory.
	 */
	public Inventory()
	{
		this(new ArrayList<Card>());
	}
	
	/**
	 * @param index Index of the Card to get
	 * @return The Card at the given index.
	 */
	public Card get(int index)
	{
		return filtrate.get(index);
	}
	
	/**
	 * @param UID Unique identifier of the Card to look for.
	 * @return The Card with the given UID.
	 * @see database.Card#ID
	 */
	public Card get(String UID)
	{
		return IDs.get(UID);
	}
	
	/**
	 * Update the filtered view of this Inventory.
	 * 
	 * @param p New filter
	 */
	public void updateFilter(Predicate<Card> p)
	{
		filtrate = cards.stream().filter(p).collect(Collectors.toList());
	}
	
	/**
	 * @return The number of Cards in this Inventory.
	 */
	@Override
	public int size()
	{
		return filtrate.size();
	}
	
	/**
	 * @return <code>true</code> if there are no cards in the inventory, or if they're
	 * all filtered out, and <code>false</code> otherwise.
	 */
	@Override
	public boolean isEmpty()
	{
		return filtrate.isEmpty();
	}
	
	/**
	 * @return <code>true</code> if there are no cards in the inventory, and
	 * <code>false</code> otherwise.
	 */
	public boolean noCards()
	{
		return cards.isEmpty();
	}
	
	/**
	 * Sort the list using the specified Comparator.
	 * 
	 * @param comp Comparator to use for sorting
	 */
	public void sort(Comparator<Card> comp)
	{
		cards.sort(comp);
	}

	/**
	 * @param Object to look for
	 * @return <code>true</code> if the inventory contains the given item, and
	 * <code>false</code> otherwise.
	 */
	@Override
	public boolean contains(Object o)
	{
		return cards.contains(o);
	}
	
	/**
	 * @param coll Collection of objects to look for
	 * @return <code>true</code> if the inventory contains all of the given items,
	 * and <code>false</code> otherwise.
	 */
	@Override
	public boolean containsAll(Collection<?> coll)
	{
		return cards.containsAll(coll);
	}
	
	/**
	 * @return An iterator over all the cards in the inventory.
	 */
	@Override
	public Iterator<Card> iterator()
	{
		return cards.iterator();
	}
	
	/**
	 * @return An array containing all the cards in the inventory.
	 */
	@Override
	public Object[] toArray()
	{
		return cards.toArray();
	}

	/**
	 * @param a Array specifying the runtime type of the returned array
	 * @return An array containing all the cards in the inventory.  If the
	 * given array is big enough to fit them all, they are placed in it.  Otherwise,
	 * a new array is allocated.
	 */
	@Override
	public <T> T[] toArray(T[] a)
	{
		return cards.toArray(a);
	}
	
	/**
	 * @param c Card to look for
	 * @return 1 if the card is in the inventory and 0 otherwise.
	 */
	@Override
	public int count(Card c)
	{
		return contains(c) ? 1 : 0;
	}

	/**
	 * @param index Index of the card to look for
	 * @return 1, since there is only one copy of a card in the inventory
	 * @throws IndexOutOfBoundsException if the index is out of range
	 */
	@Override
	public int count(int index)
	{
		if (index >= size() || index < 0)
			throw new IndexOutOfBoundsException(String.valueOf(index));
		return 1;
	}
	
	@Override
	public boolean add(Card c)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends Card> coll)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> coll)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> coll)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Category> getCategories(Card c)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Category> getCategories(int index)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Date dateAdded(Card c)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Date dateAdded(int index)
	{
		throw new UnsupportedOperationException();
	}
}