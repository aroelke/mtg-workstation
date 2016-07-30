package editor.util;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This class represents a list of similarly-typed items that can't be
 * changed.
 * 
 * @author Alec Roelke
 *
 * @param <E> Type of the elements of this Tuple
 */
@SuppressWarnings("serial")
public class UnmodifiableList<E> extends ArrayList<E>
{
	/**
	 * Create an empty UnmodifiableList.
	 */
	public UnmodifiableList()
	{
		super();
	}
	
	/**
	 * Create a new UnmodifiableList out of the given collection of items.
	 * 
	 * @param c List to create the Tuple out of
	 */
	public UnmodifiableList(Collection<? extends E> c)
	{
		super(c);
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
