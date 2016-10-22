package editor.collection;

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

import editor.collection.category.CategorySpec;
import editor.database.card.Card;
import editor.filter.Filter;
import editor.filter.leaf.BinaryFilter;

/**
 * This class represents an inventory of cards that can be added to decks.
 * TODO: Correct comments
 * 
 * @author Alec Roelke
 * @see util.CategorizableList
 */
public class Inventory implements CardList
{
	/**
	 * TODO: Comment this class
	 * @author Alec
	 *
	 */
	private class InventoryEntry implements Entry
	{
		private final Card card;
		
		private InventoryEntry(Card card)
		{
			this.card = card;
		}
		
		@Override
		public Card card()
		{
			return card;
		}

		@Override
		public Set<CategorySpec> categories()
		{
			throw new UnsupportedOperationException("Inventory cannot have categories.");
		}

		@Override
		public int count()
		{
			throw new UnsupportedOperationException("Inventory does not count card copies.");
		}

		@Override
		public Date dateAdded()
		{
			return card.expansion().releaseDate;
		}
	}
	
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
		 * @param cards Cards to transfer
		 */
		public TransferData(Card... cards)
		{
			this.cards = cards;
		}
		
		/**
		 * Create a new TransferData from the given cards.
		 * 
		 * @param cards Cards to transfer
		 */
		public TransferData(Collection<Card> cards)
		{
			this(cards.stream().toArray(Card[]::new));
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
				return Arrays.stream(cards).map(Card::unifiedName).reduce("", (a, b) -> a + "\n" + b);
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
	};
	
	/**
	 * Master list of cards.
	 */
	private final List<Card> cards;
	/**
	 * Map of Card UIDs onto their Cards.
	 */
	private final Map<String, Card> IDs;
	/**
	 * Filter for Cards in the Inventory pane.
	 */
	private CategorySpec filter;
	/**
	 * Filtered view of the master list.
	 */
	private List<Card> filtrate;
	
	/**
	 * Create an empty inventory.
	 */
	public Inventory()
	{
		this(new ArrayList<Card>());
	}
	
	/**
	 * Create a new Inventory with the given list of Cards.
	 * 
	 * @param list List of Cards
	 */
	public Inventory(Collection<Card> list)
	{
		cards = new ArrayList<Card>(list);
		IDs = cards.stream().collect(Collectors.toMap(Card::id, Function.identity()));
		filter = new CategorySpec("Displayed Inventory", Color.BLACK, new BinaryFilter(true));
		filtrate = cards;
		
		filter.addCategoryListener((e) -> {
			if (e.filterChanged())
				filtrate = cards.stream().filter(filter::includes).collect(Collectors.toList());
		});
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
	public boolean addAll(Collection<? extends Card> cards)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean addAll(Collection<? extends Card> cards, Collection<? extends Integer> amounts)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void clear()
	{
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @param Card card to look for
	 * @return <code>true</code> if the inventory contains the given item, and
	 * <code>false</code> otherwise.
	 */
	@Override
	public boolean contains(Card card)
	{
		return IDs.values().contains(card);
	}
	
	/**
	 * @param cards Collection of objects to look for
	 * @return <code>true</code> if the inventory contains all of the given items,
	 * and <code>false</code> otherwise.
	 */
	@Override
	public boolean containsAll(Collection<? extends Card> cards)
	{
		return this.cards.containsAll(cards);
	}
	
	/**
	 * @param index Index of the Card to get
	 * @return The Card at the given index.
	 */
	@Override
	public Card get(int index)
	{
		return filtrate[index];
	}

	/**
	 * @param UID Unique identifier of the Card to look for.
	 * @return The Card with the given UID.
	 * @see editor.database.card.Card#ID
	 */
	public Card get(String UID)
	{
		return IDs[UID];
	}
	
	@Override
	public Entry getData(Card card)
	{
		return new InventoryEntry(card);
	}
	
	@Override
	public Entry getData(int index)
	{
		return new InventoryEntry(this[index]);
	}
	
	/**
	 * @return The current Filter for Cards in the inventory pane.
	 */
	public Filter getFilter()
	{
		return filter.getFilter();
	}

	/**
	 * @param o Object to look for
	 * @return The index of the object into the inventory, or -1 if it isn't
	 * in the inventory.
	 */
	@Override
	public int indexOf(Card card)
	{
		return filtrate.indexOf(card);
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
	 * @return An iterator over all the cards in the inventory.
	 */
	@Override
	public Iterator<Card> iterator()
	{
		return cards.iterator();
	}
	
	/**
	 * @return <code>true</code> if there are no cards in the inventory, and
	 * <code>false</code> otherwise.
	 */
	public boolean noCards()
	{
		return cards.isEmpty();
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
	public boolean removeAll(Collection<? extends Card> cards)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<? extends Card> cards, Collection<? extends Integer> amounts)
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
	 * @return The number of Cards in this Inventory.
	 */
	@Override
	public int size()
	{
		return filtrate.size();
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
	 * @return An array containing all the cards in the inventory.
	 */
	@Override
	public Card[] toArray()
	{
		return cards.toArray(new Card[size()]);
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

	/**
	 * Update the filtered view of this Inventory.
	 * 
	 * @param filter New filter
	 */
	public void updateFilter(Filter filter)
	{
		this.filter.setFilter(filter);
	}
}