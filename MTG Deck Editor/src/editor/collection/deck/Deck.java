package editor.collection.deck;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import editor.collection.CardList;
import editor.collection.category.CategoryListener;
import editor.collection.category.CategorySpec;
import editor.database.card.Card;

/**
 * This class represents a deck which can have cards added and removed (in quantity) and
 * have several category views (from which cards can also be added or removed).
 * TODO: Decide if the sideboard should be part of the Deck or a separate class
 * 
 * @author Alec Roelke
 */
public class Deck implements CardList
{
	/**
	 * This class represents a category of a deck.  If a card is added or removed using the add and remove
	 * methods, the master list will be updated to reflect this only if the card passes through the Category's filter.
	 * 
	 * @author Alec Roelke
	 */
	private class Category implements CardList
	{
		/**
		 * Specification for the cards contained in this Category.
		 */
		private CategorySpec spec;
		/**
		 * List representing the filtered view of the master list.
		 */
		private List<Card> filtrate;
		/**
		 * Listener for changes in this category's CategorySpec.
		 */
		private CategoryListener listener;
		/**
		 * Rank of this Category.
		 */
		private int rank;
		
		/**
		 * Create a new Category.
		 * 
		 * @param spec Specifications for the new Category
		 */
		private Category(CategorySpec spec)
		{
			this.spec = spec;
			rank = categories.size();
			update();
		}
		
		/**
		 * If the given Card belongs to this Category and isn't in the Deck, add one copy of it
		 * to the deck.
		 * 
		 * @param card Card to add
		 * @return <code>true</code> if the Card was added and <code>false</code> otherwise.
		 */
		@Override
		public boolean add(Card card)
		{
			return add(card, 1);
		}
		
		/**
		 * Add some number of copies of a Card to the deck if it passes
		 * through this Category's filter.
		 * 
		 * @param card Card to add
		 * @param amount Number of copies to add
		 * @return <code>true</code> if the Deck was changed as a result, and
		 * <code>false</code> otherwise.
		 */
		@Override
		public boolean add(Card card, int amount)
		{
			return spec.includes(card) ? Deck.this.add(card, amount) : false;
		}
		
		/**
		 * Add all of the copies of the Cards in the given CardList that pass through this
		 * Category's filter to the deck.
		 * 
		 * @param cards CardList of Cards to add
		 * @return <code>true</code> if any cards were added, and <code>false</code>
		 * otherwise.
		 */
		@Override
		public boolean addAll(CardList cards)
		{
			return Deck.this.addAll(cards.stream().filter(spec::includes).collect(Collectors.toMap(Function.identity(), (c) -> cards.getData(c).count())));
		}
		
		/**
		 * Add some amounts of the given Cards that pass through this Category's
		 * filter to the deck.
		 * 
		 * @param amounts Map containing Cards to add and amount of each one to add
		 * @return <code>true</code> if any of the given Cards were added, and <code>false</code>
		 * otherwise.
		 */
		@Override
		public boolean addAll(Map<? extends Card, ? extends Integer> amounts)
		{
			return Deck.this.addAll(amounts.entrySet().stream().filter((e) -> spec.includes(e.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
		}
		
		/**
		 * Add a single copy of each of the given Cards that pass through this Category's
		 * filter to the deck.
		 * 
		 * @param cards Collection of Cards to add
		 * @return <code>true</code> if the Deck changed as a result, and <code>false</code>
		 * otherwise.
		 */
		@Override
		public boolean addAll(Set<? extends Card> cards)
		{
			return Deck.this.addAll(cards.stream().filter(spec::includes).collect(Collectors.toSet()));
		}
		
		/**
		 * Remove all copies of Cards that belong to this Category from the deck.
		 */
		@Override
		public void clear()
		{
			Deck.this.removeAll(this);
		}
		
		/**
		 * @param card Card to look for
		 * @return <code>true</code> if the given Card is in this Category, and
		 * <code>false</code> otherwise.
		 */
		@Override
		public boolean contains(Card card)
		{
			return filtrate.contains(card);
		}
		
		/**
		 * @param cards Collection of Cards to look for
		 * @return <code>true</code> if this Category contains all of the given cards,
		 * and <code>false</code> otherwise.
		 */
		@Override
		public boolean containsAll(Collection<? extends Card> cards)
		{
			for (Card c: cards)
				if (!contains(c))
					return false;
			return true;
		}
		
		/**
		 * @param index Index into this Category's view of the master list to
		 * look at
		 * @return The Card at the given index.
		 */
		@Override
		public Card get(int index)
		{
			return filtrate[index];
		}
		
		/**
		 * @param card Card to look up
		 * @return The deck's data for the Card.
		 */
		@Override
		public Entry getData(Card card)
		{
			if (spec.includes(card))
				return Deck.this.getData(card);
			else
				throw new IllegalArgumentException("Category " + spec.getName() + " does not contain card " + card);
		}
		
		/**
		 * @param index Index into this Category's view of the deck to look up
		 * @return The data of the Card at the given index.
		 */
		@Override
		public Entry getData(int index)
		{
			return getData(this[index]);
		}
		
		/**
		 * @param card Card to look for
		 * @return The index of that Card in this Category's view of the master
		 * list.
		 */
		@Override
		public int indexOf(Card card)
		{
			return filtrate.indexOf(card);
		}
		
		/**
		 * @return <code>true</code> if there are no cards in this Category, and
		 * <code>false</code> otherwise.
		 */
		@Override
		public boolean isEmpty()
		{
			return size() == 0;
		}
		
		/**
		 * @return An iterator over this Category's Cards.
		 */
		@Override
		public Iterator<Card> iterator()
		{
			return filtrate.iterator();
		}

		/**
		 * Remove the given Card from the Deck if it is in this Category.
		 * 
		 * @param card Card to remove
		 * @return <code>true</code> if the given object was successfully remove, and
		 * <code>false</code> otherwise.
		 */
		@Override
		public boolean remove(Card card)
		{
			return remove(card, 1) > 0;
		}

		/**
		 * Remove some number of copies of a Card from this Category if it passes
		 * through this Category's filter.
		 * 
		 * @param card Card to add
		 * @param amount Number of copies to remove
		 * @return The number of copies of the Card that were actually removed.
		 */
		@Override
		public int remove(Card card, int amount)
		{
			return contains(card) ? Deck.this.remove(card, amount) : 0;
		}

		/**
		 * Remove all copies of Cards in the given CardList from this Category that
		 * pass through its filter.
		 * 
		 * @param cards Cards to remove
		 * @return A Map<Card, Integer> containing the Cards that were removed and how many
		 * were removed.
		 */
		@Override
		public Map<Card, Integer> removeAll(CardList cards)
		{
			return Deck.this.removeAll(cards.stream().filter(spec::includes).collect(Collectors.toMap(Function.identity(), (c) -> cards.getData(c).count())));
		}

		/**
		 * Remove some numbers of copies of each of the given Cards from the deck that
		 * pass through this Category's filter.
		 * 
		 * @param amounts Cards to remove and number of each one to remove
		 * @return A Map<Card, Integer> containing the Cards that were removed and how many
		 * were removed.
		 */
		@Override
		public Map<Card, Integer> removeAll(Map<? extends Card, ? extends Integer> amounts)
		{
			return Deck.this.removeAll(amounts.entrySet().stream().filter((e) -> spec.includes(e.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
		}
		
		/**
		 * Remove one copy of each of the given Cards from the deck that pass through
		 * this Category's filter.
		 * 
		 * @param cards Cards to remove
		 * @return A Set<Card> containing which Cards were removed.
		 */
		@Override
		public Set<Card> removeAll(Set<? extends Card> cards)
		{
			return Deck.this.removeAll(cards.stream().filter(spec::includes).collect(Collectors.toSet()));
		}
		
		/**
		 * Set the number of copies of the given Card to be the given value.  If the card
		 * isn't in the deck, it will be added.  If it isn't included in the category,
		 * an error is thrown.
		 * 
		 * @param card Card to change
		 * @param amount Number of copies to change to
		 * @return <code>true</code> if the number of copies was changed or if the card was
		 * added, and <code>false</code> otherwise.
		 * @throws IllegalArgumentException If the Card does not pass through this Category's filter.
		 */
		@Override
		public boolean set(Card card, int amount)
		{
			if (spec.includes(card))
				throw new IllegalArgumentException("Category " + spec.getName() + " does not include " + card);
			else
				return Deck.this.set(card, amount);
		}

		/**
		 * Set the number of copies of the Card at the given index to be the given value.
		 * 
		 * @param index Index to find the Card at
		 * @param amount Number of copies to change to
		 * @return <code>true</code> if the Card is in the Category and if the number of copies
		 * was changed, and <code>false</code> otherwise.
		 */
		@Override
		public boolean set(int index, int amount)
		{
			return set(this[index], amount);
		}

		/**
		 * @return The number of unique Cards in this Category.
		 */
		@Override
		public int size()
		{
			return filtrate.size();
		}

		/**
		 * @return An array containing all of the Cards in this Category.
		 */
		@Override
		public Card[] toArray()
		{
			return filtrate.toArray(new Card[filtrate.size()]);
		}

		/**
		 * @return This Category's String representation.
		 * @see editor.gui.filter.original.editor.FilterEditorPanel#setContents(String)
		 * @see gui.editor.CategoryDialog#setContents(String)
		 */
		@Override
		public String toString()
		{
			return spec.toString();
		}

		/**
		 * @return the total number of Cards in this Category.
		 */
		@Override
		public int total()
		{
			return filtrate.stream().map(Deck.this::getEntry).mapToInt(DeckEntry::count).sum();
		}

		/**
		 * Update this category so its filtrate reflects the new filter,
		 * whitelist, and blacklist.
		 */
		public void update()
		{
			filtrate = masterList.stream().map((e) -> e.card).filter(spec::includes).collect(Collectors.toList());
			for (DeckEntry e: masterList)
				if (spec.includes(e.card))
					e.categories.add(this);
				else
					e.categories.remove(this);
		}
	}
	
	/**
	 * This class represents an entry into a deck.  It has a card, a number of copies of that card,
	 * a set of categories the card belongs to, and the date the card was added.
	 * 
	 * @author Alec Roelke
	 */
	private class DeckEntry implements Entry
	{
		/**
		 * Card in this DeckEntry.
		 */
		private final Card card;
		/**
		 * Number of copies of the Card.
		 */
		private int count;
		/**
		 * Date this DeckEntry was created (the Card was originally added).
		 */
		private final Date date;
		/**
		 * Set of categories this DeckEntry's Card belongs to.  Implemented using a
		 * LinkedHashSet, so it will maintain the ordering that categories were added.
		 */
		private Set<Category> categories;
		
		/**
		 * Create a new DeckEntry.
		 * 
		 * @param card Card for this DeckEntry
		 * @param amount Number of initial copies in this Entry
		 * @param added Date the Card was added
		 */
		private DeckEntry(Card card, int amount, Date added)
		{
			this.card = card;
			count = amount;
			date = added;
			categories = new LinkedHashSet<Category>();
		}
		
		/**
		 * Copy constructor for DeckEntry.
		 * 
		 * @param original Original DeckEntry to copy
		 */
		private DeckEntry(DeckEntry original)
		{
			card = original.card;
			count = original.count;
			date = original.date;
			categories = new LinkedHashSet<Category>(original.categories);
		}
		
		/**
		 * Add copies to this DeckEntry.
		 * 
		 * @param amount Copies to add
		 * @return <code>true</code> if any copies were added, and <code>false</code>
		 * otherwise 
		 */
		private boolean add(int amount)
		{
			if (amount < 1)
				return false;
			count += amount;
			return true;
		}
		
		/**
		 * @return This DeckEntry's card.
		 */
		@Override
		public Card card()
		{
			return card;
		}
		
		/**
		 * @return The categories this DeckEntry's card belongs to.
		 */
		@Override
		public Set<CategorySpec> categories()
		{
			return categories.stream().map((category) -> category.spec).collect(Collectors.toSet());
		}
		
		/**
		 * @return The number of copies of this DeckEntry's card in the deck.
		 */
		@Override
		public int count()
		{
			return count;
		}
		
		/**
		 * @return The date the card was added to the deck.
		 */
		@Override
		public Date dateAdded()
		{
			return date;
		}

		/**
		 * Remove copies from this DeckEntry.  There can't be fewer than
		 * 0 copies.
		 * 
		 * @param amount Number of copies to remove.
		 * @return The number of copies that were actually removed (in case there
		 * are now 0).
		 */
		private int remove(int amount)
		{
			if (amount < 1)
				return 0;
			
			int old = count;
			count -= Math.min(count, amount);
			return old - count;
		}
	}
	
	/**
	 * This class represents an event during which a Deck may have changed.
	 * It can indicate how many copies of Cards may have been added to or
	 * removed from the Deck, how a category may have changed, or if any
	 * categories were removed.  If a parameter did not change and the contents
	 * of that parameter's change are requested, throw an IllegalStateException.
	 * 
	 * @author Alec Roelke
	 */
	@SuppressWarnings("serial")
	public class Event extends EventObject
	{
		/**
		 * If Cards were added to or removed from the Deck, this map
		 * contains which ones and how many copies.
		 */
		private Map<Card, Integer> cardsChanged;
		/**
		 * If a category's name was changed, its old name.
		 */
		private String changedName;
		/**
		 * CategoryEvent representing the changes to the CategorySpec corresponding
		 * to the category that was changed, if any was changed.
		 */
		private CategorySpec.Event categoryChanges;
		/**
		 * If a category was added to the deck, its name.
		 */
		private String addedCategory;
		/**
		 * Set of names of categories that have been removed, if any.
		 */
		private Set<String> removedCategories;
		/**
		 * Map of categories onto their old ranks, if they were changed.
		 */
		private Map<String, Integer> rankChanges;
		
		public Event()
		{
			super(Deck.this);
			cardsChanged = null;
			changedName = null;
			categoryChanges = null;
			addedCategory = null;
			removedCategories = null;
			rankChanges = null;
		}
		
		/**
		 * @return The name of the category that was added.
		 * @throws IllegalStateException If no category was added.
		 */
		public String addedName()
		{
			if (categoryAdded())
				return addedCategory;
			else
				throw new IllegalStateException("No category has been added to the deck");
		}
		
		/**
		 * @return A map containing the Cards that were added and the number of copies
		 * that were added.
		 * @throws IllegalStateException If no cards were added or removed during the
		 * event.
		 */
		public Map<Card, Integer> cardsAdded()
		{
			if (cardsChanged())
			{
				Map<Card, Integer> cards = new HashMap<Card, Integer>(cardsChanged);
				for (Card c: cardsChanged.keySet())
					if (cardsChanged[c].intValue() < 1)
						cards.remove(c);
				return cards;
			}
			else
				throw new IllegalStateException("Deck cards were not changed");
		}
		
		/**
		 * @return <code>true</code> if Cards were added to or removed from the Deck
		 * during the event.
		 */
		public boolean cardsChanged()
		{
			return cardsChanged != null;
		}
		
		/**
		 * Indicate that cards and/or counts of cards in the deck changed.
		 * A positive number means a card was added, and a negative one means it
		 * was removed.
		 * 
		 * @param change Map of Cards onto their count changes
		 * @return The Event representing the change.
		 */
		private Event cardsChanged(Map<Card, Integer> change)
		{
			cardsChanged = change;
			return this;
		}
		
		/**
		 * @return A map of cards that were removed and the number of copies that
		 * were removed.  Positive numbers are used to indicate removed cards.
		 * @throws IllegalStateException If no cards were added or removed during
		 * the event.
		 */
		public Map<Card, Integer> cardsRemoved()
		{
			if (cardsChanged())
			{
				Map<Card, Integer> cards = new HashMap<Card, Integer>(cardsChanged);
				for (Card c: cardsChanged.keySet())
					if (cardsChanged[c].intValue() > -1)
						cards.remove(c);
					else
						cardsChanged.compute(c, (k, v) -> -v);
				return cards;
			}
			else
				throw new IllegalStateException("Deck cards were not changed");
		}
		
		/**
		 * @return <code>true</code> if any categories were removed during the
		 * event, and <code>false</code> otherwise.
		 */
		public boolean categoriesRemoved()
		{
			return removedCategories != null;
		}
		
		/**
		 * Indicate that categories were removed from the deck.
		 * 
		 * @param removed Collection of categories that were removed
		 * @return The Event representing the change.
		 */
		private Event categoriesRemoved(Collection<Category> removed)
		{
			removedCategories = new HashSet<String>(removed.stream().map((c) -> c.spec.getName()).collect(Collectors.toSet()));
			return this;
		}
		
		/**
		 * @return <code>true</code> if a category was added to the deck, and
		 * <code>false</code>.
		 */
		public boolean categoryAdded()
		{
			return addedCategory != null;
		}
		
		/**
		 * Indicate that a category was added to the deck.
		 * 
		 * @param added Category that was added
		 * @return The Event representing the change.
		 */
		private Event categoryAdded(Category added)
		{
			addedCategory = added.spec.getName();
			return this;
		}
		
		/**
		 * @return <code>true</code> if a category in the Deck was changed, and
		 * <code>false</code> otherwise.
		 */
		public boolean categoryChanged()
		{
			return categoryChanges != null;
		}
		
		/**
		 * Indicate that a category was changed.
		 * 
		 * @param changeName Name of the category that was changed
		 * @param changes CategorySpec.Event indicating changes to the category
		 * @return The Event representing the change.
		 */
		private Event categoryChanged(String changeName, CategorySpec.Event changes)
		{
			changedName = changeName;
			categoryChanges = changes;
			return this;
		}
		
		/**
		 * @return A CategoryEvent detailing the changes to the category.
		 * @throws IllegalStateException if no category was changed during
		 * the event.
		 */
		public CategorySpec.Event categoryChanges()
		{
			if (categoryChanged())
				return categoryChanges;
			else
				throw new IllegalStateException("Category was not changed");
		}
		
		/**
		 * @return The name of the category that was changed before the event.
		 * Use this rather than the CategoryEvent returned by {@link categoryChanges()}
		 * to identify which category was changed if its name was not changed.
		 * @throws IllegalStateException If no category was changed during the
		 * event.
		 */
		public String categoryName()
		{
			if (categoryChanged())
				return changedName;
			else
				throw new IllegalStateException("Category was not changed");
		}
		
		/**
		 * Indicate that a category was removed from the deck.
		 * 
		 * @param removed Category that was removed
		 * @return The Event representing the change.
		 */
		private Event categoryRemoved(Category removed)
		{
			removedCategories = new HashSet<String>(Arrays.asList(removed.spec.getName()));
			return this;
		}
		
		/**
		 * @return The Deck that changed to create this DeckEvent.
		 */
		@Override
		public Deck getSource()
		{
			return Deck.this;
		}
		
		/**
		 * @return A map of category names onto their old ranks before they were
		 * changed.
		 * @throws IllegalStateException If no category ranks were changed.
		 */
		public Map<String, Integer> oldRanks()
		{
			if (ranksChanged())
				return rankChanges;
			else
				throw new IllegalStateException("No category's rank changed");
		}
		
		/**
		 * @return <code>true</code> if the ranks of any categories were changed, and
		 * <code>false</code> otherwise.
		 */
		public boolean ranksChanged()
		{
			return rankChanges != null;
		}
		
		/**
		 * Indicate that the ranks of categories were changed.
		 * 
		 * @param changed Map of category names onto their old ranks
		 * @return The Event representing the change.
		 */
		private Event ranksChanged(Map<String, Integer> changed)
		{
			rankChanges = changed;
			return this;
		}
		
		/**
		 * @return The set of names of the categories that were removed during the
		 * event.
		 * @throws IllegalStateException If no categories were removed during the
		 * event.
		 */
		public Set<String> removedNames()
		{
			if (categoriesRemoved())
				return removedCategories;
			else
				throw new IllegalStateException("No category has been removed from the deck");
		}
	}
	
	/**
	 * This class represents data being transferred via drag and drop or cut/copy/paste
	 * between this Deck and another object.  The Deck only supports importing Card or Entry
	 * data flavors, but can export Strings as well.
	 * 
	 * @author Alec Roelke
	 */
	public static class TransferData implements Transferable
	{
		/**
		 * Entries being exported.
		 */
		private Map<Card, Integer> transferData;
		
		/**
		 * Create a new TransferData containing the given Deck's entries for the given
		 * Card.
		 * 
		 * @param d Deck to get entries from
		 * @param cards Cards to find entries for
		 */
		public TransferData(Deck d, Card... cards)
		{
			transferData = Arrays.stream(cards).collect(Collectors.toMap(Function.identity(), (c) -> d.getEntry(c).count()));
		}
		
		/**
		 * Create a new TransferData containing the given Deck's entries for the given
		 * Card.
		 * 
		 * @param d Deck to get entries from
		 * @param cards Cards to find entries for
		 */
		public TransferData(Deck d, Collection<Card> cards)
		{
			this(d, cards.stream().toArray(Card[]::new));
		}
		
		/**
		 * @param flavor Flavor of data to retrieve
		 * @return Data of the given flavor corresponding to the Entries retrieved by this
		 * TransferData.
		 * @throws UnsupportedFlavorException If the given flavor is unsupported
		 */
		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
		{
			if (flavor.equals(CardList.entryFlavor))
				return transferData;
			else if (flavor.equals(Card.cardFlavor))
				return transferData.keySet().stream().sorted(Card::compareName).toArray(Card[]::new);
			else if (flavor.equals(DataFlavor.stringFlavor))
				return transferData.entrySet().stream().map((e) -> e.getValue() + "x " + e.getKey().unifiedName()).reduce("", (a, b) -> a + "\n" + b);
			else
				throw new UnsupportedFlavorException(flavor);
		}

		/**
		 * @return An Array containing the data flavors a Deck can support.  A Deck can
		 * only support Entry, Card, and String flavors.
		 */
		@Override
		public DataFlavor[] getTransferDataFlavors()
		{
			return new DataFlavor[] {CardList.entryFlavor, Card.cardFlavor, DataFlavor.stringFlavor};
		}

		/**
		 * @param flavor DataFlavor to check
		 * @return <code>true</code> if a Deck can support the given data flavor, and
		 * <code>false</code> otherwise.
		 */
		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor)
		{
			return Arrays.asList(getTransferDataFlavors()).contains(flavor);
		}
	}
	
	/**
	 * Formatter for dates, usually for formatting the add date of a card.
	 */
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMMM d, yyyy");
	/**
	 * List of cards in this Deck.
	 */
	private List<DeckEntry> masterList;
	/**
	 * Categories in this Deck.
	 */
	private Map<String, Category> categories;
	/**
	 * Total number of cards in this Deck, accounting for multiples.
	 */
	private int total;
	/**
	 * Number of land cards in this Deck, accounting for multiples.
	 */
	private int land;
	/**
	 * List of listeners for changes in this Deck.
	 */
	private Collection<DeckListener> listeners;
	
	/**
	 * Create a new, empty Deck with no categories.
	 */
	public Deck()
	{
		masterList = new ArrayList<DeckEntry>();
		categories = new LinkedHashMap<String, Category>();
		total = 0;
		land = 0;
		listeners = new HashSet<DeckListener>();
	}
	
	/**
	 * Add a copy of the given Card to the Deck.
	 * 
	 * @param card Card to add
	 * @return <code>true</code> if the card was successfully added,
	 * and <code>false</code> otherwise.
	 */
	@Override
	public boolean add(Card card)
	{
		return add(card, 1);
	}
	
	/**
	 * Add some number of Cards to this Deck.  If the number is not positive,
	 * then no changes are made.
	 * 
	 * @param card Card to add
	 * @param amount Number of copies to add
	 * @return <code>true</code> if the Deck changed as a result, and
	 * <code>false</code> otherwise, which is when the number to add
	 * is less than 1.
	 */
	@Override
	public boolean add(Card card, int amount)
	{
		return add(card, amount, new Date());
	}
	
	/**
	 * Add some number of Cards to this Deck.  If the number is not positive,
	 * then no changes are made.  Normally this should only be used when loading
	 * a Deck, and will not affects an existing Card's add date.
	 * 
	 * @param card Card to add
	 * @param amount Number of copies to add
	 * @param date Date the card was originally added
	 * @return <code>true</code> if the Deck changed as a result, and
	 * <code>false</code> otherwise, which is when the number to add
	 * is less than 1.
	 */
	public boolean add(Card card, int amount, Date date)
	{
		if (do_add(card, amount, date))
		{
			Map<Card, Integer> added = new HashMap<Card, Integer>();
			added[card] = amount;
			notifyListeners(new Event().cardsChanged(added));
			return true;
		}
		else
			return false;
	}
	
	/**
	 * Add all of the cards in the given CardList to this Deck.
	 * 
	 * @param d Deck to copy
	 * @return <code>true</code> if cards were successfully added, and <code>false</code>
	 * otherwise.
	 */
	@Override
	public boolean addAll(CardList d)
	{
		return addAll(d.stream().collect(Collectors.toMap(Function.identity(), (c) -> d.getData(c).count())));
	}
	
	/**
	 * Add some amounts of each of the given Cards to this Deck.
	 * 
	 * @param amounts Cards to add and amounts of them to add
	 * @return <code>true</code> if any of the given Cards were added, and
	 * <code>false</code> otherwise.
	 */
	@Override
	public boolean addAll(Map<? extends Card, ? extends Integer> amounts)
	{
		Map<Card, Integer> added = new HashMap<Card, Integer>();
		
		for (Card card: amounts.keySet())
			if (do_add(card, amounts[card], new Date()))
				added[card] = amounts[card];
		if (!added.isEmpty())
			notifyListeners(new Event().cardsChanged(added));
		return !added.isEmpty();
	}
	
	/**
	 * Add one copy of each of the given Cards to this Deck.
	 * 
	 * @param cards Collection of Cards to add.
	 * @return <code>true</code> if any of the Cards were successfully added, and
	 * <code>false</code> otherwise.
	 */
	@Override
	public boolean addAll(Set<? extends Card> cards)
	{
		return addAll(cards.stream().collect(Collectors.toMap(Function.identity(), (c) -> 1)));
	}
	
	/**
	 * Add a new Category.  If there is already a Category with the same name,
	 * instead do nothing.
	 * 
	 * @param spec Specifications for the new Category
	 * if there already was one with that name.
	 */
	public CardList addCategory(CategorySpec spec)
	{
		if (!categories.containsKey(spec.getName()))
		{
			Category c = new Category(spec);
			categories[spec.getName()] = c;
			c.update();
			
			spec.addCategoryListener(c.listener = (e) -> {
				if (e.nameChanged())
				{
					categories.remove(e.oldName());
					categories[e.newName()] = c;
				}
				if (e.filterChanged() || e.whitelistChanged() || e.blacklistChanged())
					c.update();
				
				Event event = new Event().categoryChanged(e.nameChanged() ? e.oldName() : e.getSource().getName(), e);
				for (DeckListener listener: new HashSet<DeckListener>(listeners))
					listener.deckChanged(event);
			});

			notifyListeners(new Event().categoryAdded(c));
			return c;
		}
		else
			return categories[spec.getName()];
	}
	
	/**
	 * Add a new listener for listening to changes in this Deck.
	 * 
	 * @param listener Listener to add.
	 */
	public void addDeckListener(DeckListener listener)
	{
		listeners.add(listener);
	}
	
	/**
	 * @return A Collection containing all of the specifications of the categories
	 * in this Deck, in no particular order.
	 */
	public Collection<CategorySpec> categories()
	{
		return categories.values().stream().map((category) -> category.spec).collect(Collectors.toList());
	}
	
	/**
	 * Reset this Deck to being empty and having no categories.
	 */
	@Override
	public void clear()
	{
		Map<Card, Integer> removed = masterList.stream().collect(Collectors.toMap((c) -> c.card, (c) -> -c.count));
		Collection<Category> categoriesRemoved = categories.values();
		
		masterList.clear();
		categories.clear();
		total = 0;
		land = 0;
		
		notifyListeners(new Event().cardsChanged(removed).categoriesRemoved(categoriesRemoved));
	}
	
	/**
	 * @param card Card to look for
	 * @return <code>true</code> if this Deck contains one or more copies
	 * of the given Card, and <code>false</code> otherwise.
	 */
	@Override
	public boolean contains(Card card)
	{
		return getEntry(card) != null;
	}
	
	/**
	 * @param cards Collection of Cards to look for
	 * @return <code>true</code> if all of the Cards in the given collection are present
	 * in this Deck, and <code>false</code> otherwise.
	 */
	@Override
	public boolean containsAll(Collection<? extends Card> cards)
	{
		for (Card c: cards)
			if (!contains(c))
				return false;
		return true;
	}
	
	/**
	 * @param name Name of the Category to look for
	 * @return <code>true</code> if this Deck has a Category with the given
	 * name, and <code>false</code> otherwise.
	 */
	public boolean containsCategory(String name)
	{
		return categories.containsKey(name);
	}
	
	/**
	 * Add the given amount of the given Card to this Deck.  Whichever method calls this
	 * is responsible for notifying anyone of the change (if there is one).  If the Card
	 * is already present in this Deck, ignore the date parameter.
	 * 
	 * @param card Card to add copies of
	 * @param amount Amount of copies to add
	 * @param date If the Card is not present in this Deck, the date it was added on 
	 * @return <code>true</code> if this Deck was changed as a result of this operation,
	 * and <code>false</code> otherwise.
	 */
	private boolean do_add(Card card, int amount, Date date)
	{
		if (amount < 1)
			return false;
		
		DeckEntry entry = getEntry(card);
		if (entry == null)
		{
			masterList.add(entry = new DeckEntry(card, 0, date));
			for (Category category: categories.values())
			{
				if (category.spec.includes(card))
				{
					category.filtrate.add(card);
					entry.categories.add(category);
				}
			}
		}
		entry.add(amount);
		total += amount;
		if (card.typeContains("land"))
			land += amount;
		
		return true;
	}
	
	/**
	 * Remove the given amount of the given Card from this Deck.  Whichever method calls this
	 * is responsible for notifying anyone of the change (if there is one).  If the Card
	 * is already present in this Deck, ignore the date parameter.
	 * 
	 * @param card Card to remove copies of
	 * @param amount Amount of copies to remove 
	 * @return The number of copies of the given Card that were actually removed from
	 * this Deck.  Normally this is the given number unless that number is greater than
	 * the number that were originally present.
	 */
	public int do_remove(Card card, int amount)
	{
		if (amount < 1)
			return 0;
		
		DeckEntry entry = getEntry(card);
		if (entry == null)
			return 0;
		
		int removed = entry.remove(amount);
		if (removed > 0)
		{
			if (entry.count == 0)
			{
				for (Category category: categories.values())
				{
					category.filtrate.remove(card);
					if (category.spec.getWhitelist().contains(card))
						category.spec.exclude(card);
					if (category.spec.getBlacklist().contains(card))
						category.spec.include(card);
				}
				masterList.remove(entry);
			}
			total -= removed;
			if (card.typeContains("land"))
				land -= removed;
		}
		
		return removed;
	}
	
	/**
	 * Exclude a Card from a category, even if it passes through its
	 * filter.
	 * 
	 * @param name Name of the category to exclude from
	 * @param card Card to exclude
	 * @return <code>true</code> if the Card was successfully excluded
	 * from the category, and <code>false</code> otherwise.
	 */
	public boolean exclude(String name, Card card)
	{
		return contains(card) && categories[name].spec.exclude(card);
	}
	
	/**
	 * @param index Index to look at in the list
	 * @return The Card at the given index.
	 */
	@Override
	public Card get(int index)
	{
		return masterList[index].card;
	}
	
	/**
	 * @param name Name of the category to get Cards from
	 * @return The CardCollection containing Cards in the category with the
	 * given name.
	 */
	public CardList getCategoryList(String name)
	{
		return categories[name];
	}
	
	/**
	 * @param name Name of the category to search for
	 * @return The user-defined rank of the given category, or -1 if no
	 * category with the given name exists.
	 */
	public int getCategoryRank(String name)
	{
		return containsCategory(name) ? categories[name].rank : -1;
	}
	
	/**
	 * @param name Name of the category whose CategorySpec is desired
	 * @return The CategorySpec of the category with the given name.
	 */
	public CategorySpec getCategorySpec(String name)
	{
		if (categories[name] != null)
			return categories[name].spec;
		else
			throw new IllegalArgumentException("No category named " + name + " found");
	}
	
	/**
	 * @param Card card to look up
	 * @return The metadata of the given Card.
	 */
	@Override
	public Entry getData(Card card)
	{
		return getEntry(card);
	}
	
	/**
	 * @param index Index of the Card to look up
	 * @return The metadata of the Card at the given index.
	 */
	@Override
	public Entry getData(int index)
	{
		return masterList[index];
	}
	
	/**
	 * @param card Card to search for a DeckEntry.
	 * @return The Entry corresponding to the Card, or <code>null</code>
	 * if there is none.
	 */
	private DeckEntry getEntry(Card card)
	{
		for (DeckEntry e: masterList)
			if (e.card.equals(card))
				return e;
		return null;
	}
	
	/**
	 * @param card Card to look for
	 * @return Index of that Card in the master list.
	 */
	@Override
	public int indexOf(Card card)
	{
		return masterList.indexOf(getEntry(card));
	}
	
	/**
	 * @return <code>true</code> if there are no cards in this Deck, and
	 * <code>false</code> otherwise.
	 */
	@Override
	public boolean isEmpty()
	{
		return size() == 0;
	}
	
	/**
	 * @return An Iterator over the list of Cards in this Deck.
	 */
	@Override
	public Iterator<Card> iterator()
	{
		return masterList.stream().map(DeckEntry::card).iterator();
	}
	
	/**
	 * @return The number of land cards in this Deck.
	 */
	public int land()
	{
		return land;
	}
	
	/**
	 * @return The number of nonland cards in this Deck.
	 */
	public int nonland()
	{
		return total - land;
	}
	
	/**
	 * Notify each listener of changes to this Deck.
	 * 
	 * @param event Event containing information about the change.
	 */
	private void notifyListeners(Event event)
	{
		for (DeckListener listener: new HashSet<DeckListener>(listeners))
			listener.deckChanged(event);
	}
	
	/**
	 * @return The number of categories in this Deck
	 */
	public int numCategories()
	{
		return categories.size();
	}

	/**
	 * Remove a single copy of the given Card from this Deck
	 * 
	 * @param card Card to remove
	 * @return <code>true</code> if a copy was removed and <code>false</code>
	 * otherwise, which is if there wasn't one to begin with.
	 */
	@Override
	public boolean remove(Card card)
	{
		return remove(card, Integer.MAX_VALUE) > 0;
	}
	
	/**
	 * Remove some number of copies of the given Card from this Deck.  If that
	 * number is less than one, no changes are made.
	 * 
	 * @param card Card to remove
	 * @param amount Number of copies to remove
	 * @return The number of copies of the Card that were actually removed.
	 */
	@Override
	public int remove(Card card, int amount)
	{
		int removed = do_remove(card, amount);
		if (removed > 0)
			notifyListeners(new Event().cardsChanged(Collections.singletonMap(card, removed)));
		return removed;
	}
	
	/**
	 * Remove a Category from this Deck.
	 * 
	 * @param name Name of the Category to remove.
	 * @return <code>true</code> if the deck changed as a result, and
	 * <code>false</code> otherwise.
	 */
	public boolean remove(String name)
	{
		Category c = categories[name];
		if (c != null)
		{
			for (DeckEntry e: masterList)
				e.categories.remove(c);
			Map<String, Integer> oldRanks = new HashMap<String, Integer>();
			for (Category category: categories.values())
			{
				if (category.rank > c.rank)
				{
					oldRanks[category.spec.getName()] = category.rank;
					category.rank--;
				}
			}
			categories.remove(name);
			c.spec.removeCategoryListener(c.listener);
			
			notifyListeners(new Event().categoryRemoved(c).ranksChanged(oldRanks));
			return true;
		}
		else
			return false;
	}

	/**
	 * Remove all Cards in the given CardList from this Deck.
	 * 
	 * @param cards Cards to remove
	 * @return A map containing the Cards that were removed and the number of each one that was removed.
	 */
	@Override
	public Map<Card, Integer> removeAll(CardList cards)
	{
		return removeAll(cards.stream().collect(Collectors.toMap(Function.identity(), (c) -> cards.getData(c).count())));
	}

	/**
	 * Remove some number of copies of the given Cards from this Deck.
	 * 
	 * @param amounts Cards to remove and amounts of each one to remove
	 * @return A map containing the Cards that were removed and the number of each one that was removed.
	 */
	@Override
	public Map<Card, Integer> removeAll(Map<? extends Card, ? extends Integer> amounts)
	{
		Map<Card, Integer> removed = new HashMap<Card, Integer>();
		for (Card card: new HashSet<Card>(amounts.keySet()))
		{
			int r = do_remove(card, amounts[card]);
			if (r > 0)
				removed[card] = -r;
		}
		
		if (!removed.isEmpty())
			notifyListeners(new Event().cardsChanged(removed));
		
		return removed;
	}

	/**
	 * Remove one copy of each of the given Cards from this Deck.
	 * 
	 * @param cards Cards to remove
	 * @return All of the Cards that were removed.
	 */
	@Override
	public Set<Card> removeAll(Set<? extends Card> cards)
	{
		return removeAll(cards.stream().collect(Collectors.toMap(Function.identity(), (c) -> 1))).keySet();
	}

	/**
	 * Remove a listener so it no longer receives alerts to changes in this
	 * Deck.
	 * 
	 * @param listener Listener to remove
	 * @return <code>true</code> if the given listener was successfully
	 * removed, and <code>false</code> otherwise.
	 */
	public boolean removeDeckListener(DeckListener listener)
	{
		return listeners.remove(listener);
	}
	
	/**
	 * Write this Deck to a file.  The format will appear like this:
	 * [Number of unique cards]
	 * [Card 1 UID]\t[count]
	 * [Card 2 UID]\t[count]
	 * 		.			.
	 * 		.			.
	 * 		.			.
	 * [Number of categories]
	 * [Category 1 String representation]
	 * [Category 2 String representation]
	 * 				  .
	 * 				  .
	 * 				  .
	 * 
	 * @param file File to save to
	 * @throws IOException
	 */
	public void save(File file) throws IOException
	{
		try (PrintWriter wr = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8")))
		{
			wr.println(String.valueOf(size()));
			for (DeckEntry e: masterList)
				wr.println(e.card.id() + "\t" + e.count + "\t" + DATE_FORMAT.format(e.date));
			wr.println(String.valueOf(categories.size()));
			for (Category c: categories.values())
				wr.println(c.toString());
		} 
	}
	
	/**
	 * Set the number of copies of the given Card to be the given value.  If the card
	 * isn't in the deck, it will be added.
	 * 
	 * @param card Card to change
	 * @param amount Number of copies to change to
	 * @return <code>true</code> if the number of copies was changed or if the card was
	 * added, and <code>false</code> otherwise.
	 */
	@Override
	public boolean set(Card card, int amount)
	{
		if (amount < 0)
			amount = 0;
		DeckEntry e = getEntry(card);
		if (e == null)
			return add(card, amount);
		else if (e.count == amount)
			return false;
		else
		{
			total += amount - e.count;
			if (e.card.typeContains("land"))
				land += amount - e.count;
			
			Map<Card, Integer> change = new HashMap<Card, Integer>();
			change[card] = amount - e.count;
			Event event = new Event().cardsChanged(change);
			
			e.count = amount;
			if (e.count == 0)
			{
				masterList.remove(e);
				for (Category category: categories.values())
				{
					category.filtrate.remove(e.card);
					category.spec.getWhitelist().remove(e.card);
					category.spec.getBlacklist().remove(e.card);
				}
			}
			
			notifyListeners(event);
			return true;
		}
	}
	
	/**
	 * Set the number of copies of the Card at the given index to be the given value.
	 * 
	 * @param index Index to find the Card at
	 * @param amount Number of copies to change to
	 * @return <code>true</code> if the Card is in the Deck and if the number of copies
	 * was changed, and <code>false</code> otherwise.
	 */
	@Override
	public boolean set(int index, int amount)
	{
		return set(masterList[index].card, amount);
	}

	/**
	 * @return The number of unique Cards in this Deck.
	 */
	@Override
	public int size()
	{
		return masterList.size();
	}

	/**
	 * Change the rank of the category with the given name to the target value.  The
	 * category that has that value will have its rank changed to that of the one with
	 * the given name.
	 * 
	 * @param name Name of the category whose rank should be changed
	 * @param target New rank for the category
	 * @return <code>true</code> if ranks were successfully changed, and <code>false</code>
	 * otherwise (such as if the named category doesn't exist, the target rank is too
	 * high, the target rank is negative, or the target rank is the named category's rank).
	 */
	public boolean swapCategoryRanks(String name, int target)
	{
		if (!categories.containsKey(name) || categories[name].rank == target
				|| target >= categories.size() || target < 0)
			return false;
		else
		{
			for (Category second: categories.values())
			{
				if (second.rank == target)
				{
					Map<String, Integer> oldRanks = new HashMap<String, Integer>();
					oldRanks[name] = categories[name].rank;
					oldRanks[second.spec.getName()] = second.rank;
					
					second.rank = categories[name].rank;
					categories[name].rank = target;
					
					notifyListeners(new Event().ranksChanged(oldRanks));
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * @return An array containing all of the Cards in this Deck.
	 */
	@Override
	public Card[] toArray()
	{
		return stream().toArray(Card[]::new);
	}

	/**
	 * @return The number of Cards in this Deck.
	 */
	@Override
	public int total()
	{
		return total;
	}
}
