package editor.collection;

import java.awt.datatransfer.DataFlavor;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import editor.collection.category.CategorySpec;
import editor.database.card.Card;
import editor.database.characteristics.CardData;

/**
 * This class represents a collection of Cards.  Each Card should be represented by a single
 * Entry that keeps track of information such as how many copies are in the CardList.
 * 
 * This does not implement Collection<Card>, List<Card>, or Set<Card> because its behavior for
 * add and remove are slightly different than any of them.  For example, if a card is added twice,
 * it should only take up one place in the list (like Set), but when if it is removed once, it should
 * still be part of the CardList (like List).  It doesn't implement Collection<Card> because Collection
 * is bound by legacy definitions like remove taking an Object and not being generic.
 * 
 * @author Alec Roelke
 */
public interface CardList extends Iterable<Card>
{
	/**
	 * This class represents an entry in a CardList whose actual implementation is dependend on the parent
	 * list.  It contains metadata about a Card such as count and date added.
	 * 
	 * @author Alec Roelke
	 */
	public interface Entry
	{
		/**
		 * @return The Card this Entry has data for.
		 */
		public Card card();
		
		/**
		 * @return The CategorySpecs in the parent CardList this Entry's Card matches (optional
		 * operation).
		 */
		public Set<CategorySpec> categories();
		
		/**
		 * @return The number of copies in the parent CardList of this Entry's Card (optional operation).
		 */
		public int count();
		
		/**
		 * @return The date this Entry's Card was added to the parent CardList (optional operation).
		 */
		public Date dateAdded();
		
		/**
		 * Get some information about this Entry's Card.
		 * 
		 * @param data Type of information to get
		 * @return The value of the given information about this Entry's Card.
		 */
		public default Object get(CardData data)
		{
			switch (data)
			{
			case NAME:
				return card().unifiedName();
			case LAYOUT:
				return card().layout();
			case MANA_COST:
				return card().manaCost();
			case CMC:
				return card().cmc();
			case COLORS:
				return card().colors();
			case COLOR_IDENTITY:
				return card().colorIdentity();
			case TYPE_LINE:
				return card().unifiedTypeLine();
			case EXPANSION_NAME:
				return card().expansion().toString();
			case RARITY:
				return card().rarity();
			case POWER:
				return card().power();
			case TOUGHNESS:
				return card().toughness();
			case LOYALTY:
				return card().loyalty();
			case ARTIST:
				return card().artist()[0];
			case LEGAL_IN:
				return card().legalIn();
			case COUNT:
				return count();
			case CATEGORIES:
				return categories();
			case DATE_ADDED:
				return dateAdded();
			default:
				throw new IllegalArgumentException("Unknown data type " + data);
			}
		}
	}

	/**
	 * Data flavor representing entries in a deck.  Transfer data will appear as a
	 * map of cards onto an integer representing the number of copies to transfer.
	 */
	DataFlavor entryFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + Map.class.getName() + "\"", "Deck Entries");
	
	/**
	 * Add one copy of a Card to this CardList (optional operation).
	 * 
	 * @param card Card to add
	 * @return <code>true</code> if a copy of the Card was added, and <code>false</code> otherwise.
	 * @throws UnsupportedOperationException if this operation is not supported
	 */
	public boolean add(Card card);
	
	/**
	 * Add some number of copies of a Card to this CardList (optional operation).
	 * 
	 * @param card Card to add copies of
	 * @param amount Number of copies to add
	 * @return <code>true</code> if any copies were added, and <code>false</code> otherwise.
	 * @throws UnsupportedOperationException if this operation is not supported
	 */
	public boolean add(Card card, int amount);

	/**
	 * Add all Cards in the given CardList to this CardList.
	 * 
	 * @param cards CardList of Cards to add
	 * @return <code>true</code> if any of the Cards were added, and <code>false</code> otherwise.
	 * @throws UnsupportedOperationException if this operation is not supported
	 */
	public boolean addAll(CardList cards);
	
	/**
	 * Add some numbers of copies of each of the given Cards to this CardList (optional operation).
	 * 
	 * @param amounts Map of Cards onto amounts of them to add to this CardList
	 * @return <code>true</code> if any Cards were added, and <code>false</code> otherwise.
	 * @throws UnsupportedOperationException if this operation is not supported
	 */
	public boolean addAll(Map<? extends Card, ? extends Integer> amounts);

	/**
	 * Add one copy of each of the given set of Cards to this CardList (optional operation).
	 * 
	 * @param cards Set of Cards to add
	 * @return <code>true</code> if any of the Cards were added, and <code>false</code> otherwise.
	 * @throws UnsupportedOperationException if this operation is not supported
	 */
	public boolean addAll(Set<? extends Card> cards);

	/**
	 * Remove all Cards from this CardList (optional operation).
	 * @throws UnsupportedOperationException if this operation is not supported
	 */
	public void clear();

	/**
	 * @param card Card to look for
	 * @return <code>true</code> if this CardList contains the specified Card,
	 * and <code>false</code> otherwise.
	 */
	public boolean contains(Card card);

	/**
	 * @param cards Collection of Cards to look for
	 * @return <code>true</code> if this CardCollection contains all of the specified
	 * Cards, and <code>false</code> otherwise.
	 */
	public boolean containsAll(Collection<? extends Card> cards);

	/**
	 * @param index Index of the Card to look for
	 * @return The Card at the given index
	 * @throws IndexOutOfBoundsException if the index is less than 0 or is too big
	 */
	public Card get(int index);

	/**
	 * Get the metadata of the given Card
	 * 
	 * @param card Card to look up
	 * @return The Entry corresponding to the given Card, or null if no such Card exists in
	 * this CardList.
	 */
	public Entry getData(Card card);
	
	/**
	 * Get the metadata of the Card at a specific position in this CardList
	 * 
	 * @param index Index to look up
	 * @return The Entry corresponding to the Card at the given index
	 * @throws IndexOutOfBoundsException if the index is less than 0 or is too big
	 */
	public Entry getData(int index);

	/**
	 * @param card Card to look for
	 * @return The index of the given Card in this CardList, or -1 if there is none of them.
	 */
	public int indexOf(Card card);
	
	/**
	 * @return <code>true</code> if this CardList contains no Cards, and
	 * <code>false</code> otherwise.
	 */
	public boolean isEmpty();

	/**
	 * @return An iterator over the Cards in this CardList.
	 */
	@Override
	public Iterator<Card> iterator();

	/**
	 * @return A parallel Stream of the Cards in this CardList.
	 */
	public default Stream<Card> parallelStream()
	{
		return StreamSupport.stream(spliterator(), true);
	}

	/**
	 * Remove a copy of a Card from this CardList (optional operation).
	 * 
	 * @param card Card to remove
	 * @return <code>true</code> if the Card was removed, and <code>false</code>
	 * otherwise.
	 * @throws UnsuportedOperationException if this operation is not supported
	 */
	public boolean remove(Card card);

	/**
	 * Remove some number of copies of a Card from this CardList (optional operation).
	 * 
	 * @param card Card to remove
	 * @param amount Number of copies to remove
	 * @return The actual number of copies that were removed.
	 */
	public int remove(Card card, int amount);
	
	/**
	 * For each Card in the given CardList, remove the number of copies of that Card in
	 * that CardList from this CardList (optional operation).
	 * 
	 * @param cards CardList of Cards to remove
	 * @return A Map<Card, Integer> containing the Cards that had copies removed and
	 * the number of copies of each one that were removed.
	 */
	public Map<Card, Integer> removeAll(CardList cards);
	
	/**
	 * Remove some numbers of copies of the given Cards from this CardList.
	 * 
	 * @param amounts Cards to remove and the number of copies of each one to remove
	 * @return A Map<Card, Integer> containing the Cards that had copies removed and
	 * the number of copies of each one that were removed.
	 */
	public Map<Card, Integer> removeAll(Map<? extends Card, ? extends Integer> cards);
	
	/**
	 * Remove one copy of each of the given Cards from this CardList.
	 * 
	 * @param cards Cards to remove
	 * @return The set of Cards that had a copy removed.
	 */
	public Set<Card> removeAll(Set<? extends Card> cards);
	
	/**
	 * Set the number of copies of a Card to the specified number (optional
	 * operation).  If the number is 0, then the Card is removed entirely.
	 * 
	 * @param card Card to set the count of
	 * @param amount Number of copies to set
	 * @return <code>true</code> if this CardList changed as a result, and
	 * <code>false</code> otherwise.
	 * @throws UnsupportedOperationException if this operation is not supported
	 */
	public boolean set(Card card, int amount);
	
	/**
	 * Set the number of copies of the Card at the specified index (optional
	 * operation).
	 * 
	 * @param index Index of the Card to set the number of
	 * @param amount Number of copies of the Card to set
	 * @return <code>true</code> if this CardList changed as a result, and
	 * <code>false</code> otherwise.
	 * @throws UnsupportedOperationException if this operation is not supported
	 * @throws IndexOutOfBoundsException if the index is less than 0 or is too big
	 * @see CardList#set(Card, int)
	 */
	public boolean set(int index, int amount);
	
	/**
	 * @return The number of unique Cards in this CardList.
	 * @see CardList#total()
	 */
	public int size();
	
	/**
	 * @return A Stream of the Cards in this CardList that is not necessarily parallel.
	 */
	public default Stream<Card> stream()
	{
		return StreamSupport.stream(spliterator(), false);
	}
	
	/**
	 * @return An array containing all of the Cards in this CardList.
	 */
	public Card[] toArray();
	
	/**
	 * @return The total number of Cards in this CardList, accounting for multiple
	 * copies of Cards.
	 * @see CardList#size()
	 */
	public int total();
}
