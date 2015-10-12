package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.StringJoiner;
import java.util.stream.Stream;

/**
 * This class represents a tuple of similarly-typed items.  It's basically
 * a list, but it can't be changed.
 * 
 * @author Alec Roelke
 *
 * @param <E> Type of the elements of this Tuple
 */
public class Tuple<E> implements List<E>
{
	/**
	 * List backing this Tuple.
	 */
	private List<E> items;
	
	/**
	 * Create a new Tuple out of the given collection of items.
	 * 
	 * @param c List to create the Tuple out of
	 */
	public Tuple(List<? extends E> c)
	{
		items = new ArrayList<E>(c);
	}
	
	/**
	 * Create a new Tuple out of the given items.
	 * 
	 * @param c Items to create the tuple out of
	 */
	@SafeVarargs
	public Tuple(E... c)
	{
		this(Arrays.asList(c));
	}

	/**
	 * @return The number of items in this Tuple.
	 */
	@Override
	public int size()
	{
		return items.size();
	}
	
	@Override
	public boolean isEmpty()
	{
		return items.isEmpty();
	}
	
	/**
	 * @param index Index of the item to get
	 * @return The item at the given index.
	 */
	@Override
	public E get(int index)
	{
		return items.get(index);
	}
	
	/**
	 * @return An Iterator over the elements of this Tuple.
	 */
	@Override
	public Iterator<E> iterator()
	{
		return items.iterator();
	}

	/**
	 * @return <code>true</code> if this Tuple contains the given object, and
	 * <code>false</code> otherwise.
	 */
	@Override
	public boolean contains(Object o)
	{
		return items.contains(o);
	}

	/**
	 * @return An array containing all the elements of this Tuple.
	 */
	@Override
	public Object[] toArray()
	{
		return items.toArray();
	}

	/**
	 * @param a Array specifying the runtime type of the array to return
	 * @return An array containing all the elements of this Tuple.  If the
	 * specified array is not big enough to fit all the elements, a new one
	 * will be allocated.
	 */
	@Override
	public <T> T[] toArray(T[] a)
	{
		return items.toArray(a);
	}

	/**
	 * @param c Collection to look for
	 * @return <code>true</code> if this Tuple contains all the items in the given
	 * collection, and <code>false</code> otherwise.
	 */
	@Override
	public boolean containsAll(Collection<?> c)
	{
		return items.containsAll(c);
	}

	/**
	 * @param o Object to look for
	 * @return The index into this Tuple of the given object, or -1 if it isn't in
	 * this Tuple.
	 */
	@Override
	public int indexOf(Object o)
	{
		return items.indexOf(o);
	}

	/**
	 * @param o Object to look for
	 * @return The index into this Tuple of the last occurrence of the given object,
	 * or -1 if it isn't in this Tuple.
	 */
	@Override
	public int lastIndexOf(Object o)
	{
		return items.lastIndexOf(o);
	}

	/**
	 * @return A ListIterator over the elements of this Tuple.
	 */
	@Override
	public ListIterator<E> listIterator()
	{
		return items.listIterator();
	}

	/**
	 * @param index Index to start at
	 * @return A ListIterator over the elements of this Tuple starting at
	 * the given index.
	 */
	@Override
	public ListIterator<E> listIterator(int index)
	{
		return items.listIterator(index);
	}

	/**
	 * @param fromIndex Index to start the sublist at, inclusive
	 * @param toIndex Index to end the sublist at, non-inclusive
	 * @return A sublist of this Tuple starting at the given index
	 * and ending just before the other given index.
	 */
	@Override
	public List<E> subList(int fromIndex, int toIndex)
	{
		return items.subList(fromIndex, toIndex);
	}
	
	/**
	 * @return A sequential Stream of this Tuple's elements.
	 */
	@Override
	public Stream<E> stream()
	{
		return items.stream();
	}
	
	/**
	 * @return A String representation of this Tuple.
	 */
	@Override
	public String toString()
	{
		StringJoiner join = new StringJoiner(",", "[", "]");
		for (E e: this)
			join.add(String.valueOf(e));
		return join.toString();
	}
	
	@Override
	public boolean add(E e)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean addAll(Collection<? extends E> c)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public E set(int index, E element)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(int index, E element)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public E remove(int index)
	{
		throw new UnsupportedOperationException();
	}
}
