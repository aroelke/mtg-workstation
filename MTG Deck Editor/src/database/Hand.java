package database;

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

import database.Deck.Category;

/**
 * TODO: Comment this
 * 
 * @author Alec Roelke
 */
public class Hand implements CardCollection
{
	private List<Card> hand;
	private Set<Card> exclusion;
	private int inHand;
	private Deck deck;
	
	public Hand(Deck d, Collection<Card> e)
	{
		super();
		hand = new ArrayList<Card>();
		exclusion = new LinkedHashSet<Card>(e);
		inHand = 0;
		deck = d;
		refresh();
	}
	
	public Hand(Deck d)
	{
		this(d, new HashSet<Card>());
	}
	
	public void refresh()
	{
		clear();
		for (Card c: deck)
			if (!exclusion.contains(c))
				for (int i = 0; i < deck.count(c); i++)
					hand.add(c);
	}
	
	public void newHand(int n)
	{
		refresh();
		Collections.shuffle(hand);
		inHand = n;
	}
	
	public void mulligan()
	{
		if (inHand > 0)
		{
			refresh();
			Collections.shuffle(hand);
			inHand--;
		}
	}
	
	public void draw()
	{
		inHand++;
	}
	
	public List<Card> getHand()
	{
		return hand.subList(0, size());
	}
	
	public void clearExclusion()
	{
		exclusion.clear();
	}
	
	public boolean exclude(Card c)
	{
		return exclusion.add(c);
	}
	
	public boolean excludeAll(Collection<? extends Card> coll)
	{
		return exclusion.addAll(coll);
	}
	
	public List<Card> excluded()
	{
		return new ArrayList<Card>(exclusion);
	}
	
	@Override
	public int size()
	{
		return Math.min(inHand, hand.size());
	}
	
	@Override
	public Stream<Card> stream()
	{
		return hand.stream();
	}

	@Override
	public int count(Card c)
	{
		return deck.count(c);
	}

	@Override
	public int count(int index)
	{
		return deck.count(get(index));
	}

	@Override
	public int total()
	{
		return size();
	}

	@Override
	public List<Category> getCategories(Card c)
	{
		return deck.getCategories(c);
	}

	@Override
	public List<Category> getCategories(int index)
	{
		return deck.getCategories(get(index));
	}

	@Override
	public Date dateAdded(Card c)
	{
		return deck.dateAdded(c);
	}

	@Override
	public Date dateAdded(int index)
	{
		return deck.dateAdded(get(index));
	}

	@Override
	public boolean add(Card c)
	{
		return hand.add(c);
	}

	@Override
	public boolean addAll(Collection<? extends Card> coll)
	{
		return hand.addAll(coll);
	}

	@Override
	public void clear()
	{
		hand.clear();
		inHand = 0;
	}

	@Override
	public boolean contains(Object o)
	{
		return hand.subList(0, inHand).contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> coll)
	{
		return hand.subList(0, inHand).containsAll(coll);
	}

	@Override
	public boolean isEmpty()
	{
		return hand.isEmpty() || inHand == 0;
	}

	@Override
	public Iterator<Card> iterator()
	{
		return hand.subList(0, inHand).iterator();
	}

	@Override
	public boolean remove(Object o)
	{
		return hand.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> coll)
	{
		return hand.removeAll(coll);
	}

	@Override
	public boolean retainAll(Collection<?> coll)
	{
		return hand.retainAll(coll);
	}

	@Override
	public Object[] toArray()
	{
		return hand.subList(0, inHand).toArray();
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		return hand.subList(0, inHand).toArray(a);
	}

	@Override
	public Card get(int index)
	{
		return hand.get(index);
	}

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
