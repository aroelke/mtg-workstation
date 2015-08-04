package database;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * TODO: Comment this class
 * @author Alec
 *
 */
public interface CardCollection extends Set<Card>
{
	@Override
	public boolean add(Card c);

	@Override
	public boolean addAll(Collection<? extends Card> coll);

	@Override
	public void clear();

	@Override
	public boolean contains(Object o);

	@Override
	public boolean containsAll(Collection<?> coll);

	@Override
	public boolean isEmpty();

	@Override
	public Iterator<Card> iterator();

	@Override
	public boolean remove(Object o);

	@Override
	public boolean removeAll(Collection<?> coll);

	@Override
	public boolean retainAll(Collection<?> coll);

	@Override
	public int size();

	@Override
	public Object[] toArray();

	@Override
	public <T> T[] toArray(T[] a);
	
	@Override
	public Stream<Card> stream();
	
	public Card get(int index);
	
	public int indexOf(Object o);
	
	public boolean increase(Card c, int n);
	
	public boolean increase(Card c);
	
	public boolean increaseAll(Collection<? extends Card> coll, int n);
	
	public int decrease(Card c, int n);
	
	public int decrease(Card c);
	
	public boolean setCount(Card c, int n);
	
	public boolean setCount(int index, int n);
	
	public int count(Card c);
	
	public int count(int index);
	
	public int total();
	
	public List<Deck.Category> getCategories(Card c);
	
	public List<Deck.Category> getCategories(int index);
	
	public Date dateAdded(Card c);
	
	public Date dateAdded(int index);
}
