package editor.collection.deck;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import editor.collection.CardList;
import editor.database.card.Card;

/**
 * This class represents a hand of Cards.  It is a subset of a Deck
 * that is randomized and with multiple copies represented by separate
 * entries. 
 * TODO: Correct comments
 * @author Alec Roelke
 */
public class Hand implements CardList
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
	 * Create a new Hand from the specified Deck.
	 * 
	 * @param deck Deck to draw Cards from
	 */
	public Hand(Deck deck)
	{
		this(deck, new HashSet<Card>());
	}
	
	/**
	 * Create a new Hand from the specified Deck, excluding the specified
	 * Cards.
	 * 
	 * @param deck Deck to draw Cards from
	 * @param cards Cards to never include in the sample hand
	 */
	public Hand(Deck deck, Collection<Card> cards)
	{
		super();
		hand = new ArrayList<Card>();
		exclusion = new LinkedHashSet<Card>(cards);
		inHand = 0;
		this.deck = deck;
		refresh();
	}
	
	@Override
	public boolean add(Card card)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean add(Card card, int amount)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean addAll(CardList cards)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean addAll(Map<? extends Card, ? extends Integer> amounts)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean addAll(Set<? extends Card> cards)
	{
		throw new UnsupportedOperationException();
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
	 * Remove all Cards from the exclusion list.
	 */
	public void clearExclusion()
	{
		exclusion.clear();
	}
	
	/**
	 * @param o Object to look for
	 * @return <code>true</code> if the given Object is in the drawn cards
	 * of this Hand, and <code>false</code> otherwise.
	 */
	@Override
	public boolean contains(Card card)
	{
		return hand.subList(0, inHand).contains(card);
	}
	
	/**
	 * @param cards Collection of Objects to look for
	 * @return <code>true</code> if the given Objects are all in the drawn
	 * cards of this Hand, and <code>false</code> otherwise.
	 */
	@Override
	public boolean containsAll(Collection<? extends Card> cards)
	{
		return hand.subList(0, inHand).containsAll(cards);
	}
	
	/**
	 * Draw a card.
	 */
	public void draw()
	{
		inHand++;
	}

	/**
	 * Exclude a Card from being drawn in the sample hand.
	 * 
	 * @param card Card to exclude
	 * @return <code>true</code> if the Card was added (which only happens
	 * if it wasn't in the exclusion list already), and <code>false</code>
	 * otherwise.
	 */
	public boolean exclude(Card card)
	{
		return exclusion.add(card);
	}
	
	/**
	 * @return The list of Cards to never draw in a hand.
	 */
	public List<Card> excluded()
	{
		return new ArrayList<Card>(exclusion);
	}
	
	/**
	 * @param index Index of the Card to look for
	 * @return The Card at the given index.
	 */
	@Override
	public Card get(int index)
	{
		return hand[index];
	}

	@Override
	public Entry getData(Card card)
	{
		return deck.getData(card);
	}

	@Override
	public Entry getData(int index)
	{
		return deck.getData(this[index]);
	}

	/**
	 * @return A list of Cards representing those in the sample hand.
	 */
	public List<Card> getHand()
	{
		return hand.subList(0, size());
	}

	/**
	 * @param o Object to look for
	 * @return The index of the first occurrence of the given Object in this
	 * Hand, or -1 if it isn't there.
	 */
	@Override
	public int indexOf(Card card)
	{
		return hand.indexOf(card);
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
	 * Update the state of this Hand to exclude Cards in the exclusion
	 * list.
	 */
	public void refresh()
	{
		clear();
		for (Card c: deck)
			if (!exclusion.contains(c))
				for (int i = 0; i < deck.getData(c).count(); i++)
					hand.add(c);
	}

	@Override
	public boolean remove(Card card)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int remove(Card card, int amount)
	{
		throw new UnsupportedOperationException();
	}
	
	
	@Override
	public Map<Card, Integer> removeAll(CardList cards)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<Card, Integer> removeAll(Map<? extends Card, ? extends Integer> amounts)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Card> removeAll(Set<? extends Card> coll)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean set(Card card, int amount)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean set(int index, int amount)
	{
		throw new UnsupportedOperationException();
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
	 * @return An Array containing the Cards drawn in this Hand.
	 */
	@Override
	public Card[] toArray()
	{
		return hand.subList(0, inHand).toArray(new Card[inHand]);
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
}
