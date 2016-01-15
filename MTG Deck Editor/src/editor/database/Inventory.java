package editor.database;

import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import editor.collection.CategorySpec;
import editor.database.Deck.Category;
import editor.filter.Filter;
import editor.filter.leaf.FilterLeaf;

/**
 * This class represents an inventory of cards that can be added to decks.
 * 
 * 
 * @author Alec Roelke
 * @see util.CategorizableList
 */
public class Inventory implements CardCollection
{
	/**
	 * This class represents the data that can be transferred from an inventory via
	 * drag and drop or cut/copy/paste.  It supports card and String flavors.
	 * 
	 * @author Alec Roelke
	 */
	public static class TransferData implements Transferable
	{
		/**
		 * Cards to be transferred.
		 */
		private Card[] cards;
		
		/**
		 * Create a new TransferData from the given cards.
		 * 
		 * @param c Cards to transfer
		 */
		public TransferData(Card... c)
		{
			cards = c;
		}
		
		/**
		 * Create a new TransferData from the given cards.
		 * 
		 * @param c Cards to transfer
		 */
		public TransferData(Collection<Card> c)
		{
			this(c.stream().toArray(Card[]::new));
		}
		
		/**
		 * @param flavor Flavor of data to retrieve
		 * @return The list of cards in the given flavor
		 * @throws UnsupportedFlavorException If the given flavor is not supported
		 */
		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
		{
			if (flavor.equals(Card.cardFlavor))
				return cards;
			else if (flavor.equals(DataFlavor.stringFlavor))
				return Arrays.stream(cards).map(Card::name).reduce("", (a, b) -> a + "\n" + b);
			else
				throw new UnsupportedFlavorException(flavor);
		}

		/**
		 * @return An Array containing the data flavors supported by the inventory, which
		 * is only card and String flavors.
		 */
		@Override
		public DataFlavor[] getTransferDataFlavors()
		{
			return new DataFlavor[] {Card.cardFlavor, DataFlavor.stringFlavor};
		}

		/**
		 * @param flavor DataFlavor to check
		 * @return <code>true</code> if the given DataFlavor is supported, and
		 * <code>false</code> otherwise.
		 */
		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor)
		{
			return Arrays.asList(getTransferDataFlavors()).contains(flavor);
		}
	}
	
	/**
	 * Master list of cards.
	 */
	private final List<Card> cards;
	/**
	 * Map of Card UIDs onto their Cards.
	 */
	private final Map<String, Card> IDs;
	/**
	 * TODO: Comment this;
	 */
	private CategorySpec filter;
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
		IDs = cards.stream().collect(Collectors.toMap((c) -> c.id(), Function.identity()));
		filter = new CategorySpec("Displayed Inventory", Color.BLACK, FilterLeaf.ALL_CARDS);
		filtrate = cards;
		
		filter.addCategoryListener((e) -> {
			if (e.filterChanged())
				filtrate = cards.stream().filter(filter::includes).collect(Collectors.toList());
		});
	}
	
	/**
	 * Create an empty inventory.
	 */
	public Inventory()
	{
		this(new ArrayList<Card>());
	}
	
	/**
	 * @param index Index of the Card to get
	 * @return The Card at the given index.
	 */
	@Override
	public Card get(int index)
	{
		return filtrate.get(index);
	}
	
	/**
	 * @param UID Unique identifier of the Card to look for.
	 * @return The Card with the given UID.
	 * @see editor.database.Card#ID
	 */
	public Card get(String UID)
	{
		return IDs.get(UID);
	}
	
	/**
	 * Update the filtered view of this Inventory.
	 * 
	 * @param f New filter
	 */
	public void updateFilter(Filter f)
	{
		filter.setFilter(f);
	}
	
	/**
	 * TODO: Comment this
	 * @return
	 */
	public Filter getFilter()
	{
		return filter.getFilter();
	}
	
	/**
	 * @return The number of Cards in this Inventory.
	 */
	@Override
	public int size()
	{
		return filtrate.size();
	}
	
	/**
	 * @return <code>true</code> if there are no cards in the inventory, or if they're
	 * all filtered out, and <code>false</code> otherwise.
	 */
	@Override
	public boolean isEmpty()
	{
		return filtrate.isEmpty();
	}
	
	/**
	 * @return <code>true</code> if there are no cards in the inventory, and
	 * <code>false</code> otherwise.
	 */
	public boolean noCards()
	{
		return cards.isEmpty();
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

	/**
	 * @param Object to look for
	 * @return <code>true</code> if the inventory contains the given item, and
	 * <code>false</code> otherwise.
	 */
	@Override
	public boolean contains(Object o)
	{
		return cards.contains(o);
	}
	
	/**
	 * @param coll Collection of objects to look for
	 * @return <code>true</code> if the inventory contains all of the given items,
	 * and <code>false</code> otherwise.
	 */
	@Override
	public boolean containsAll(Collection<?> coll)
	{
		return cards.containsAll(coll);
	}
	
	/**
	 * @return An iterator over all the cards in the inventory.
	 */
	@Override
	public Iterator<Card> iterator()
	{
		return cards.iterator();
	}
	
	/**
	 * @return An array containing all the cards in the inventory.
	 */
	@Override
	public Object[] toArray()
	{
		return cards.toArray();
	}

	/**
	 * @param a Array specifying the runtime type of the returned array
	 * @return An array containing all the cards in the inventory.  If the
	 * given array is big enough to fit them all, they are placed in it.  Otherwise,
	 * a new array is allocated.
	 */
	@Override
	public <T> T[] toArray(T[] a)
	{
		return cards.toArray(a);
	}
	
	/**
	 * @return A sequential stream over the filtered card list.
	 */
	@Override
	public Stream<Card> stream()
	{
		return filtrate.stream();
	}
	
	/**
	 * @param c Card to look for
	 * @return 1 if the card is in the inventory and 0 otherwise.
	 */
	@Override
	public int count(Card c)
	{
		return contains(c) ? 1 : 0;
	}

	/**
	 * @param index Index of the card to look for
	 * @return 1, since there is only one copy of a card in the inventory
	 * @throws IndexOutOfBoundsException if the index is out of range
	 */
	@Override
	public int count(int index)
	{
		if (index >= size() || index < 0)
			throw new IndexOutOfBoundsException(String.valueOf(index));
		return 1;
	}
	
	/**
	 * @param o Object to look for
	 * @return The index of the object into the inventory, or -1 if it isn't
	 * in the inventory.
	 */
	@Override
	public int indexOf(Object o)
	{
		return filtrate.indexOf(o);
	}
	
	/**
	 * @return The total number of cards in the inventory, even ones that are filtered
	 * out.
	 */
	@Override
	public int total()
	{
		return cards.size();
	}
	
	@Override
	public boolean add(Card c)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends Card> coll)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> coll)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> coll)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Category> getCategories(Card c)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Category> getCategories(int index)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Date dateAdded(Card c)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Date dateAdded(int index)
	{
		throw new UnsupportedOperationException();
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