package editor.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import editor.database.Deck.Category;

/**
 * This class represents a hand of Cards.  It is a subset of a Deck
 * that is randomized and with multiple copies represented by separate
 * entries. 
 * 
 * @author Alec Roelke
 */
public class Hand implements CardCollection
{
	/**
	 * Cards in the Deck, in a random order and with multiple copies
	 * represented by multiple entries.
	 */
	private List<Card> hand;
	/**
	 * Cards to not include in a drawn hand (for example, sideboards or
	 * Commanders).
	 */
	private Set<Card> exclusion;
	/**
	 * Number of cards in the drawn hand.
	 */
	private int inHand;
	/**
	 * Deck containing the cards in the hand.
	 */
	private Deck deck;
	
	/**
	 * Create a new Hand from the specified Deck, excluding the specified
	 * Cards.
	 * 
	 * @param d Deck to draw Cards from
	 * @param e Cards to never include in the sample hand
	 */
	public Hand(Deck d, Collection<Card> e)
	{
		super();
		hand = new ArrayList<Card>();
		exclusion = new LinkedHashSet<Card>(e);
		inHand = 0;
		deck = d;
		refresh();
	}
	
	/**
	 * Create a new Hand from the specified Deck.
	 * 
	 * @param d Deck to draw Cards from
	 */
	public Hand(Deck d)
	{
		this(d, new HashSet<Card>());
	}
	
	/**
	 * Update the state of this Hand to exclude Cards in the exclusion
	 * list.
	 */
	public void refresh()
	{
		clear();
		for (Card c: deck)
			if (!exclusion.contains(c))
				for (int i = 0; i < deck.count(c); i++)
					hand.add(c);
	}
	
	/**
	 * Shuffle the deck and draw a new starting hand.
	 * 
	 * @param n Size of the new hand
	 */
	public void newHand(int n)
	{
		refresh();
		Collections.shuffle(hand);
		inHand = Math.min(n, hand.size());
	}
	
	/**
	 * Take a mulligan, or shuffle the deck and draw a new hand, but
	 * with one fewer card in it.
	 */
	public void mulligan()
	{
		if (inHand > 0)
		{
			Collections.shuffle(hand);
			inHand--;
		}
	}
	
	/**
	 * Draw a card.
	 */
	public void draw()
	{
		inHand++;
	}
	
	/**
	 * @return A list of Cards representing those in the sample hand.
	 */
	public List<Card> getHand()
	{
		return hand.subList(0, size());
	}
	
	/**
	 * Remove all Cards from the exclusion list.
	 */
	public void clearExclusion()
	{
		exclusion.clear();
	}
	
	/**
	 * Exclude a Card from being drawn in the sample hand.
	 * 
	 * @param c Card to exclude
	 * @return <code>true</code> if the Card was added (which only happens
	 * if it wasn't in the exclusion list already), and <code>false</code>
	 * otherwise.
	 */
	public boolean exclude(Card c)
	{
		return exclusion.add(c);
	}
	
	/**
	 * @return The list of Cards to never draw in a hand.
	 */
	public List<Card> excluded()
	{
		return new ArrayList<Card>(exclusion);
	}
	
	/**
	 * @return The number of Cards in this Hand.
	 */
	@Override
	public int size()
	{
		return Math.min(inHand, hand.size());
	}
	
	/**
	 * @return A sequential Stream over all the Cards in this Hand.
	 */
	@Override
	public Stream<Card> stream()
	{
		return hand.stream();
	}

	/**
	 * @param c Card to look for
	 * @return The number of copies of the given Card in the deck.
	 */
	@Override
	public int count(Card c)
	{
		return deck.count(c);
	}

	/**
	 * @param index Index of the Card to look for
	 * @return The number of copies in the deck of the Card at the
	 * given index
	 */
	@Override
	public int count(int index)
	{
		return deck.count(get(index));
	}

	/**
	 * @return The number of Cards in this Hand.
	 * @see Hand#size()
	 */
	@Override
	public int total()
	{
		return size();
	}

	/**
	 * @param c Card to look for
	 * @return The Categories the given Card belongs to
	 */
	@Override
	public Set<Category> getCategories(Card c)
	{
		return deck.getCategories(c);
	}

	/**
	 * @param index Index of the Card to look for
	 * @return The Categories the Card at the given index belongs to
	 */
	@Override
	public Set<Category> getCategories(int index)
	{
		return deck.getCategories(get(index));
	}

	/**
	 * @param c Card to look for
	 * @return The Date the given Card was added to the deck
	 */
	@Override
	public Date dateAdded(Card c)
	{
		return deck.dateAdded(c);
	}

	/**
	 * @param index Index of the Card to look for
	 * @return The Date the Card at the given index was added to the deck
	 */
	@Override
	public Date dateAdded(int index)
	{
		return deck.dateAdded(get(index));
	}

	/**
	 * Add a card to the hand (but not to the deck).
	 * 
	 * @param c Card to add
	 * @return <code>true</code> if the Card was successfully added, and
	 * <code>false</code> otherwise.
	 */
	@Override
	public boolean add(Card c)
	{
		return hand.add(c);
	}

	/**
	 * Add all of the given Cards to the hand, but not the deck.
	 * 
	 * @param coll Collection of Cards to add
	 * @return <code>true</code> if any of the Cards were added,
	 * and <code>false</code> otherwise.
	 */
	@Override
	public boolean addAll(Collection<? extends Card> coll)
	{
		return hand.addAll(coll);
	}

	/**
	 * Remove all Cards from this hand.  New ones cannot be drawn
	 * until a refresh is performed.
	 * @see Hand#refresh()
	 */
	@Override
	public void clear()
	{
		hand.clear();
		inHand = 0;
	}

	/**
	 * @param o Object to look for
	 * @return <code>true</code> if the given Object is in the drawn cards
	 * of this Hand, and <code>false</code> otherwise.
	 */
	@Override
	public boolean contains(Object o)
	{
		return hand.subList(0, inHand).contains(o);
	}

	/**
	 * @param coll Collection of Objects to look for
	 * @return <code>true</code> if the given Objects are all in the drawn
	 * cards of this Hand, and <code>false</code> otherwise.
	 */
	@Override
	public boolean containsAll(Collection<?> coll)
	{
		return hand.subList(0, inHand).containsAll(coll);
	}

	/**
	 * @return <code>true</code> if there are no cards drawn in this Hand,
	 * and <code>false</code> otherwise.
	 */
	@Override
	public boolean isEmpty()
	{
		return hand.isEmpty() || inHand == 0;
	}

	/**
	 * @return An iterator over the drawn cards in this Hand.
	 */
	@Override
	public Iterator<Card> iterator()
	{
		return hand.subList(0, inHand).iterator();
	}

	/**
	 * Remove the given Object from this Hand (but not the Deck).
	 * 
	 * @param o Object to remove
	 * @return <code>true</code> if the Object was removed, and
	 * <code>false</code> otherwise.
	 */
	@Override
	public boolean remove(Object o)
	{
		return hand.remove(o);
	}

	/**
	 * Remove all of the given Objects from this Hand (but not the Deck).
	 * 
	 * @param coll Collection of Objects to remove
	 * @return <code>true</code> if any of the given Objects were removed
	 * from this Hand, and <code>false</code> otherwise.
	 */
	@Override
	public boolean removeAll(Collection<?> coll)
	{
		return hand.removeAll(coll);
	}

	/**
	 * Retain only the Objects in the given collection in this Hand.
	 * 
	 * @param coll Collection of Objects to retain
	 * @return <code>true</code> if this Hand changed as a result, and
	 * <code>false</code> otherwise.
	 */
	@Override
	public boolean retainAll(Collection<?> coll)
	{
		return hand.retainAll(coll);
	}

	/**
	 * @return An Array containing the Cards drawn in this Hand.
	 */
	@Override
	public Object[] toArray()
	{
		return hand.subList(0, inHand).toArray();
	}

	/**
	 * @param a Array specifying the runtime type of the array to return
	 * @return An Array containing the Cards drawn in this Hand.  If the given
	 * Array is big enough to fit them, they will placed into it.  Otherwise,
	 * a new Array will be allocated.
	 */
	@Override
	public <T> T[] toArray(T[] a)
	{
		return hand.subList(0, inHand).toArray(a);
	}

	/**
	 * @param index Index of the Card to look for
	 * @return The Card at the given index.
	 */
	@Override
	public Card get(int index)
	{
		return hand.get(index);
	}

	/**
	 * @param o Object to look for
	 * @return The index of the first occurrence of the given Object in this
	 * Hand, or -1 if it isn't there.
	 */
	@Override
	public int indexOf(Object o)
	{
		return hand.indexOf(o);
	}
	
	@Override
	public boolean increase(Card c, int n)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean increase(Card c)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean increaseAll(Collection<? extends Card> coll, int n)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int decrease(Card c, int n)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int decrease(Card c)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean setCount(Card c, int n)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean setCount(int index, int n)
	{
		throw new UnsupportedOperationException();
	}
}
