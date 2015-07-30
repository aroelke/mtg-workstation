package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * TODO: Comment this
 * @author Alec
 *
 * @param <E>
 */
public class Tuple<E> implements List<E>
{
	private List<E> items;
	
	public Tuple(Collection<E> c)
	{
		items = new ArrayList<E>(c);
	}
	
	@SafeVarargs
	public Tuple(E... c)
	{
		this(Arrays.asList(c));
	}

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
	
	@Override
	public E get(int index)
	{
		return items.get(index);
	}
	
	@Override
	public Iterator<E> iterator()
	{
		return items.iterator();
	}

	@Override
	public boolean contains(Object o)
	{
		return items.contains(o);
	}

	@Override
	public Object[] toArray()
	{
		return items.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		return items.toArray(a);
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return items.containsAll(c);
	}

	@Override
	public int indexOf(Object o)
	{
		return items.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o)
	{
		return items.lastIndexOf(o);
	}

	@Override
	public ListIterator<E> listIterator()
	{
		return items.listIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index)
	{
		return items.listIterator(index);
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex)
	{
		return items.subList(fromIndex, toIndex);
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
