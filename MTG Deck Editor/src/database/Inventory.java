package database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This class represents an inventory of cards that can be added to decks.
 * 
 * 
 * @author Alec Roelke
 * @see util.CategorizableList
 */
public class Inventory
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
		IDs = new HashMap<String, Card>();
		filtrate = cards;
		for (Card c: cards)
			IDs.put(c.ID, c);
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
	public int size()
	{
		return filtrate.size();
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
}