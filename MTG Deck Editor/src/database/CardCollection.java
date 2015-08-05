package database;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * This class represents a collection of Cards.  The collection can choose whether or
 * not each card can be represented by multiple entries (add/remove) or through some
 * other means (increase/decrease).
 * 
 * @author Alec Roelke
 */
public interface CardCollection extends Collection<Card>
{
	
	/**
	 * Add a new Card to this CardCollection (optional operation).  This should
	 * return <code>true</code> if a new entry in the list is created, and
	 * <code>false</code> otherwise.  If a Card can only have one entry, but multiple
	 * copies are allowed, <code>false</code> should still be returned on an attempt
	 * to add another copy of a Card, and its count should not increase.
	 * 
	 * @param c Card to add
	 * @return <code>true</code> if the Card was added, and <code>false</code>
	 * otherwise.
	 * @throws UnsupportedOperationException if this operation is not supported
	 */
	@Override
	public boolean add(Card c);

	/**
	 * Add several new Cards to this CardCollection (optional operation).
	 * 
	 * @param coll Cards to add
	 * @return <code>true</code> if any of the Cards were successfully added to this
	 * CardCollection, and <code>false</code> otherwise.
	 * @throws UnsupportedOperationException if this operation is not supported
	 * @see CardCollection#add(Card)
	 */
	@Override
	public boolean addAll(Collection<? extends Card> coll);

	/**
	 * Remove all entries from this CardCollection (optional operation).
	 * @throws UnsupportedOperationException if this operation is not supported
	 */
	@Override
	public void clear();

	/**
	 * @param o Object to look for
	 * @return <code>true</code> if this CardCollection contains the specified object,
	 * and <code>false</code> otherwise.
	 */
	@Override
	public boolean contains(Object o);

	/**
	 * @param coll Collection of objects to look for
	 * @return <code>true</code> if this CardCollection contains all of the specified
	 * objects, and <code>false</code> otherwise.
	 */
	@Override
	public boolean containsAll(Collection<?> coll);

	/**
	 * @return <code>true</code> if this CardCollection contains no Cards, and
	 * <code>false</code> otherwise.
	 */
	@Override
	public boolean isEmpty();

	/**
	 * @return An iterator over the Cards in this CardCollection.
	 */
	@Override
	public Iterator<Card> iterator();

	/**
	 * Remove an object from this CardCollection (optional operation).  If multiple
	 * copies of a Card are represented by a single entry, then an implementation of
	 * this should remove the entire entry.
	 * 
	 * @param o Object to remove
	 * @return <code>true</code> if the object was remove, and <code>false</code>
	 * otherwise.
	 * @throws UnsuportedOperationException if this operation is not supported
	 */
	@Override
	public boolean remove(Object o);

	/**
	 * Remove all of the given objects from this CardCollection (optional
	 * operation).
	 * 
	 * @param coll Collection containing objects to remove
	 * @return <code>true</code> if any of the objects were removed, and
	 * <code>false</code> otherwise
	 * @throws UnsupportedOperationException if this operation is not supported
	 * @see CardCollection#remove(Object)
	 */
	@Override
	public boolean removeAll(Collection<?> coll);

	/**
	 * Retain only the objects in the specified collection (optaional operation).
	 * 
	 * @param coll Collection of objects to retain
	 * @return <code>true</code> if this CardCollection changed as a result, and
	 * <code>false</code> otherwise.
	 * @throws UnsupportedOperationException if this operation is not supported
	 * @see CardCollection#remove(Object)
	 */
	@Override
	public boolean retainAll(Collection<?> coll);

	/**
	 * @return The number of entries in this CardCollection.  If multiple copies
	 * of a Card are to be represented by a single entry, then all copies of that
	 * Card count only once for this method.
	 * @see CardCollection#total()
	 */
	@Override
	public int size();

	/**
	 * @return An array containing all of the Cards in this CardCollection.  If
	 * multiple copies of a Card are represented by a single entry, then each entry
	 * should only appear once.
	 */
	@Override
	public Object[] toArray();

	/**
	 * @param a Array specifying the runtime type of the data to return
	 * @return An array containing all of the Cards in this CardCollection whose type
	 * is specified by the given array.  If that array is large enough to fit all of the
	 * cards, then it will be filled with them.  Otherwise, a new array will be allocated.
	 * @see CardCollection#toArray()
	 */
	@Override
	public <T> T[] toArray(T[] a);
	
	/**
	 * @return A sequential Stream over all the Cards in this CardCollection.  If
	 * multiple copies of a Card are to be represented using only one entry, then
	 * that entry should only be included in the Stream once.
	 */
	@Override
	public Stream<Card> stream();
	
	/**
	 * @param index Index of the Card to look for
	 * @return The Card at the given index
	 * @throws IndexOutOfBoundsException if the index is less than 0 or is too big
	 */
	public Card get(int index);
	
	/**
	 * @param o Object to look for
	 * @return The index of the first ocurrence of the given object in this
	 * CardCollection, or -1 if there is none of them.
	 */
	public int indexOf(Object o);
	
	/**
	 * Increase the number of copies of the given Card by the given amount.  If
	 * the Card is not in this CardCollection, then it should be added (optional
	 * operation).
	 * 
	 * @param c Card to add
	 * @param n Number of copies to add
	 * @return <code>true</code> if the Card was successfully added, and
	 * <code>false</code> otherwise.
	 * @throws UnsupportedOperationException if this operation is not supported
	 */
	public boolean increase(Card c, int n);
	
	/**
	 * Increase the number of copies of the given Card by one (optional operation).
	 * 
	 * @param c Card to add
	 * @return <code>true</code> if the Card was sucessfully added, and
	 * <code>false</code> otherwise.
	 * @throws UnsupportedOperationException if this operation is not supported
	 * @see CardCollection#increase(Card, int)
	 */
	public boolean increase(Card c);
	
	/**
	 * Increase the number of copies of each of the given Cards by the given amount
	 * (optional operation).
	 * 
	 * @param coll Collection of Cards to increase
	 * @param n Number of copies of each card to add
	 * @return <code>true</code> if any of the given Cards were successfully added,
	 * and <code>false</code> otherwise.
	 * @throws UnsupportedOperationException if this operation is not supported
	 * @see CardCollection#increase(Card, int)
	 */
	public boolean increaseAll(Collection<? extends Card> coll, int n);
	
	/**
	 * Decrease the number of copies of the given Card by the given amount
	 * (optional operation).  If the number of copies of the Card is reduced
	 * to 0 this way, there should be no entries left of it.
	 * 
	 * @param c Card to remove
	 * @param n Number of copies of the Card to remove
	 * @return The number of copies of the Card that were removed.
	 * @throws UnsupportedOperationException if this operation is not supported
	 */
	public int decrease(Card c, int n);
	
	/**
	 * Decrease the number of copies of the given Card by one (optional
	 * operation).
	 * 
	 * @param c Card to remove
	 * @return 1 if a copy of the Card was removed, and 0 otherwise.
	 * @see CardCollection#decrease(Card, int)
	 * @throws UnsupportedOperationException if this operation is not supported
	 */
	public int decrease(Card c);
	
	/**
	 * Set the number of copies of a Card to the specified number (optional
	 * operation).  If the number is 0, then there should be no entries representing
	 * the Card left.  If there are no entries representing the Card and the number
	 * is greater than 0, then entries should be created.
	 * 
	 * @param c Card to set the count of
	 * @param n Number of copies to set
	 * @return <code>true</code> if this CardCollection changed as a result, and
	 * <code>false</code> otherwise.
	 * @throws UnsupportedOperationException if this operation is not supported
	 */
	public boolean setCount(Card c, int n);
	
	/**
	 * Set the number of copies of the Card at the specified index (optional
	 * operation).
	 * 
	 * @param index Index of the Card to set the number of
	 * @param n Number of copies of the Card to set
	 * @return <code>true</code> if this CardCollection changed as a result, and
	 * <code>false</code> otherwise.
	 * @throws UnsupportedOperationException if this operation is not supported
	 * @throws IndexOutOfBoundsException if the index is less than 0 or is too big
	 * @see CardCollection#setCount(Card, int)
	 */
	public boolean setCount(int index, int n);
	
	/**
	 * @param c Card to look for
	 * @return The number of copies of the given Card in this CardCollection.
	 * @throws UnsupportedOperationException if this operation is not supported
	 */
	public int count(Card c);
	
	/**
	 * @param index Index of the Card to look for
	 * @return The number of copies of the Card at the given index in this CardCollection.
	 * @throws UnsupportedOperationException if this Operation is not supported
	 * @throws IndexOutOfBoundsException if the index is less than 0 or is too big
	 */
	public int count(int index);
	
	/**
	 * @return The total number of Cards in this CardCollection.  If multiple copies of a
	 * Card are represented as a single entry, this counts each copy separately.  If they
	 * are not, this should return the same value as {@link CardCollection#size()}.
	 * @see CardCollection#size()
	 */
	public int total();
	
	/**
	 * @param c Card to look for
	 * @return A list of categories that the Card belongs to.
	 * @throws UnsupportedOperationException if this operation is not supported
	 * @see Deck.Category
	 */
	public List<Deck.Category> getCategories(Card c);
	
	/**
	 * @param index Index of the Card to look for
	 * @return A list of categories that the Card at the given index belongs to.
	 * @throws UnsupportedOperationException if this operation is not supported
	 * @throws IndexOutOfBoundsException if the index is less than 0 or if it is too big
	 * @see Deck.Category
	 */
	public List<Deck.Category> getCategories(int index);
	
	/**
	 * @param c Card to look for
	 * @return The Date the Card was added to this CardCollection.
	 * @throws UnsupportedOperationException if this operation is not supported
	 */
	public Date dateAdded(Card c);
	
	/**
	 * @param index Index of the Card to look for
	 * @return The Date the Card at the given index was added to this CardCollection
	 * @throws UnsupportedOperationException if this operation is not supported
	 * @throws IndexOutOfBoundsException if the index is less than 0 or if it is too big
	 */
	public Date dateAdded(int index);
}
