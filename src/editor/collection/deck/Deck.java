package editor.collection.deck;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import editor.collection.CardList;
import editor.collection.category.CategoryListener;
import editor.collection.category.CategorySpec;
import editor.database.card.Card;
import editor.gui.MainFrame;

/**
 * This class represents a deck which can have cards added and removed (in quantity) and have several category
 * views (from which cards can also be added or removed).
 * 
 * @author Alec Roelke
 */
public class Deck implements CardList, Externalizable
{
	/**
	 * This class represents a category of a deck.  If a card is added or removed using the add and remove
	 * methods, the master list will be updated to reflect this only if the card passes through the Category's filter.
	 * 
	 * TODO: Try to make spec private, and use accessors for it so updates are automatic
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
		 * @param spec specifications for the new Category
		 */
		public Category(CategorySpec spec)
		{
			this.spec = spec;
			rank = categories.size();
			update();
		}
		
		/**
		 * {@inheritDoc}
		 * Only add the card if it passes through the specification's filter.  If it doesn't,
		 * return <code>false</code>.
		 */
		@Override
		public boolean add(Card card)
		{
			return add(card, 1);
		}
		
		/**
		 * {@inheritDoc}
		 * Only add the card if it passes through the specification's filter.  If it doesn't,
		 * return <code>false</code>.
		 */
		@Override
		public boolean add(Card card, int amount)
		{
			return spec.includes(card) && Deck.this.add(card, amount);
		}
		
		/**
		 * {@inheritDoc}
		 * Only add the cards that pass through the specification's filter to the deck.
		 */
		@Override
		public boolean addAll(CardList cards)
		{
			return Deck.this.addAll(cards.stream().filter(spec::includes).collect(Collectors.toMap(Function.identity(), (c) -> cards.getData(c).count())));
		}
		
		/**
		 * {@inheritDoc}
		 * Only add the cards that pass through the specification's filter to the deck.
		 */
		@Override
		public boolean addAll(Map<? extends Card, ? extends Integer> amounts)
		{
			return Deck.this.addAll(amounts.entrySet().stream().filter((e) -> spec.includes(e.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
		}
		
		/**
		 * {@inheritDoc}
		 * Only add the cards that pass through the specification's filter to the deck.
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
		
		@Override
		public boolean contains(Card card)
		{
			return filtrate.contains(card);
		}
		
		@Override
		public boolean containsAll(Collection<? extends Card> cards)
		{
			for (Card c: cards)
				if (!contains(c))
					return false;
			return true;
		}
		
		@Override
		public Card get(int index) throws IndexOutOfBoundsException
		{
			return filtrate.get(index);
		}
		
		@Override
		public Entry getData(Card card) throws IllegalArgumentException
		{
			return spec.includes(card) ? Deck.this.getData(card) : null;
		}
		
		@Override
		public Entry getData(int index) throws IndexOutOfBoundsException
		{
			return getData(get(index));
		}
		
		@Override
		public int indexOf(Card card) throws IllegalArgumentException
		{
			return spec.includes(card) ? filtrate.indexOf(card) : -1;
		}

		@Override
		public boolean isEmpty()
		{
			return size() == 0;
		}

		@Override
		public Iterator<Card> iterator()
		{
			return filtrate.iterator();
		}

		/**
		 * {@inheritDoc}
		 * Only remove the card if it is included in this Category's specification.
		 */
		@Override
		public boolean remove(Card card)
		{
			return remove(card, 1) > 0;
		}

		/**
		 * {@inheritDoc}
		 * Only remove the card if it is included in this Category's specification.
		 */
		@Override
		public int remove(Card card, int amount)
		{
			return contains(card) ? Deck.this.remove(card, amount) : 0;
		}
		
		/**
		 * {@inheritDoc}
		 * Only remove the cards that are included in this Category's specification.
		 */
		@Override
		public Map<Card, Integer> removeAll(CardList cards)
		{
			return Deck.this.removeAll(cards.stream().filter(spec::includes).collect(Collectors.toMap(Function.identity(), (c) -> cards.getData(c).count())));
		}
		
		/**
		 * {@inheritDoc}
		 * Only remove the cards that are included in this Category's specification.
		 */
		@Override
		public Map<Card, Integer> removeAll(Map<? extends Card, ? extends Integer> amounts)
		{
			return Deck.this.removeAll(amounts.entrySet().stream().filter((e) -> spec.includes(e.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
		}

		/**
		 * {@inheritDoc}
		 * Only remove the cards that are included in this Category's specification.
		 */
		@Override
		public Set<Card> removeAll(Set<? extends Card> cards)
		{
			return Deck.this.removeAll(cards.stream().filter(spec::includes).collect(Collectors.toSet()));
		}

		@Override
		public boolean set(Card card, int amount)
		{
			return spec.includes(card) && Deck.this.set(card, amount);
		}

		@Override
		public boolean set(int index, int amount) throws IllegalArgumentException
		{
			return set(get(index), amount);
		}

		@Override
		public int size()
		{
			return filtrate.size();
		}

		@Override
		public Card[] toArray()
		{
			return filtrate.toArray(new Card[filtrate.size()]);
		}

		/**
		 * {@inheritDoc}
		 * @see CategorySpec#toString()
		 */
		@Override
		public String toString()
		{
			return spec.toString();
		}
		
		@Override
		public int total()
		{
			return filtrate.stream().map(Deck.this::getEntry).mapToInt(DeckEntry::count).sum();
		}
		
		/**
		 * Update this category so its filtrate reflects the new filter, whitelist, and blacklist.
		 */
		public void update()
		{
			filtrate = masterList.stream().map((e) -> e.card).filter(spec::includes).collect(Collectors.toList());
			for (DeckEntry e: masterList)
			{
				if (spec.includes(e.card))
					e.categories.add(this);
				else
					e.categories.remove(this);
			}
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
		private final LocalDate date;
		/**
		 * Set of categories this DeckEntry's Card belongs to.  Implemented using a
		 * LinkedHashSet, so it will maintain the ordering that categories were added.
		 */
		private Set<Category> categories;
		
		/**
		 * Create a new DeckEntry.
		 * 
		 * @param card card for this DeckEntry
		 * @param amount number of initial copies in this Entry
		 * @param added date the Card was added
		 */
		public DeckEntry(Card card, int amount, LocalDate added)
		{
			this.card = card;
			count = amount;
			date = added;
			categories = new LinkedHashSet<Category>();
		}
		
		/**
		 * Add copies to this DeckEntry.
		 * 
		 * @param amount copies to add
		 * @return true if any copies were added, and false otherwise.
		 */
		private boolean add(int amount)
		{
			if (amount < 1)
				return false;
			count += amount;
			return true;
		}
		
		@Override
		public Card card()
		{
			return card;
		}
		
		@Override
		public Set<CategorySpec> categories()
		{
			return categories.stream().map((category) -> category.spec).collect(Collectors.toSet());
		}
		
		@Override
		public int count()
		{
			return count;
		}
		
		@Override
		public LocalDate dateAdded()
		{
			return date;
		}

		@Override
		public boolean equals(Object other)
		{
			if (other == null)
				return false;
			if (other == this)
				return true;
			if (!(other instanceof DeckEntry))
				return false;
			DeckEntry o = (DeckEntry)other;
			return card.equals(o.card) && o.count == count && o.date.equals(date);
		}
		
		@Override
		public int hashCode()
		{
			return Objects.hash(card, count, date);
		}
		
		/**
		 * Remove copies from this DeckEntry.  There can't be fewer than
		 * 0 copies.
		 * 
		 * @param amount Number of copies to remove.
		 * @return the number of copies that were actually removed (in case there
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
	 * This class represents an event during which a Deck may have changed. It can indicate how
	 * many copies of Cards may have been added to or removed from the Deck, how a category may
	 * have changed, or if any categories were removed.  If a parameter did not change and the
	 * contents of that parameter's change are requested, throw an {@link IllegalStateException}.
	 * 
	 * @author Alec Roelke
	 */
	@SuppressWarnings("serial")
	public class Event extends EventObject
	{
		/**
		 * If Cards were added to or removed from the Deck, this map contains which ones and how
		 * many copies.
		 */
		private Map<Card, Integer> cardsChanged;
		/**
		 * {@link editor.collection.category.CategorySpec.Event} representing the changes to the
		 * {@link CategorySpec} corresponding to the category that was changed, if any was changed.
		 */
		private CategorySpec.Event categoryChanges;
		/**
		 * If a category was added to the deck, its specification.
		 */
		private CategorySpec addedCategory;
		/**
		 * Set of names of categories that have been removed, if any.
		 */
		private CategorySpec removedCategory;
		/**
		 * Map of categories onto their old ranks, if they were changed.
		 */
		private Map<String, Integer> rankChanges;
		
		/**
		 * Create a new Event with no changes to the deck.
		 */
		public Event()
		{
			super(Deck.this);
			cardsChanged = new HashMap<Card, Integer>();
			categoryChanges = null;
			addedCategory = null;
			removedCategory = null;
			rankChanges = new HashMap<String, Integer>();
		}
		
		/**
		 * If a category was added, get its specification.
		 * 
		 * @return the specification of the category that was added, or null if there was
		 * none
		 */
		public CategorySpec addedCategory()
		{
			return addedCategory;
		}
		
		/**
		 * If cards were added, get the cards that were added and how many of each were added.
		 * 
		 * @return a map containing the Cards that were added and the number of copies that were
		 * added
		 */
		public Map<Card, Integer> cardsAdded()
		{
			return cardsChanged.entrySet().stream().filter((e) -> e.getValue() > 0).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		}
		
		/**
		 * Helper method for determining if cards were changed in the deck.  Equivalent
		 * to <code>!cardsAdded().isEmpty() || !cardsRemoved().isEmpty()</code>.
		 * 
		 * @return true if any cards were added or removed, and false otherwise
		 */
		public boolean cardsChanged()
		{
			return !cardsChanged.isEmpty();
		}
		
		/**
		 * Indicate that cards and/or counts of cards in the deck changed. A positive number
		 * means a card was added, and a negative one means it was removed.
		 * 
		 * @param change map of Cards onto their count changes
		 * @return the event representing the change
		 */
		private Event cardsChanged(Map<Card, Integer> change)
		{
			cardsChanged = change;
			return this;
		}
		
		/**
		 * If cards were removed, get the cards that were removed and how many of each were removed.
		 * 
		 * @return a map of cards that were removed and the number of copies that were removed.
		 * Positive numbers are used to indicate removed cards.
		 */
		public Map<Card, Integer> cardsRemoved()
		{
			return cardsChanged.entrySet().stream().filter((e) -> e.getValue() < 0).collect(Collectors.toMap(Map.Entry::getKey, (e) -> -e.getValue()));
		}
		
		/**
		 * Helper method for determining if a category was added to the deck.
		 * Equivalent of <code>addedCategory() != null</code>.
		 * 
		 * @return true if a category was added to the deck, and false otherwise.
		 */
		public boolean categoryAdded()
		{
			return addedCategory != null;
		}
		
		/**
		 * Indicate that a category was added to the deck.
		 * 
		 * @param added category that was added
		 * @return the event representing the change.
		 */
		private Event categoryAdded(Category added)
		{
			addedCategory = added.spec;
			return this;
		}
		
		/**
		 * Helper method for determining if a category was edited.  Equivalent to
		 * <code>categoryChanges() != null<code>.
		 * 
		 * @return true if a category was changed, and false otherwise
		 */
		public boolean categoryChanged()
		{
			return categoryChanges != null;
		}
		
		/**
		 * Indicate that a category was changed.
		 * 
		 * @param changes {@link editor.collection.category.CategorySpec.Event} indicating changes
		 * to the category
		 * @return the event representing the change.
		 */
		private Event categoryChanged(CategorySpec.Event changes)
		{
			categoryChanges = changes;
			return this;
		}
		
		/**
		 * Get the event that indicates a change to a category.
		 * 
		 * @return an event detailing the changes to the category, or none if no changes were made
		 */
		public CategorySpec.Event categoryChanges()
		{
			return categoryChanges;
		}
		
		/**
		 * Helper method for determining if a category was removed from the deck.
		 * Equivalent of <code>removedCategory() != null</code>.
		 * 
		 * @return true if a category was removed from the deck, and false otherwise
		 */
		public boolean categoryRemoved()
		{
			return removedCategory != null;
		}
		
		/**
		 * Indicate that a category was removed from the deck.
		 * 
		 * @param removed category that was removed
		 * @return the event representing the change.
		 */
		private Event categoryRemoved(Category removed)
		{
			removedCategory = removed.spec;
			return this;
		}
		
		/**
		 * Get the deck that was changed.
		 * 
		 * @return the deck that changed to create this DeckEvent.
		 */
		@Override
		public Deck getSource()
		{
			return Deck.this;
		}
		
		/**
		 * Get the old ranks of the categories whose ranks changed before they were changed.
		 * 
		 * @return a map of category names onto their old ranks before they were changed
		 */
		public Map<String, Integer> oldRanks()
		{
			return rankChanges;
		}
		
		/**
		 * Helper method for determining if any category's rank changed.  Equivalent to
		 * <code>!oldRanks().isEmpty()</code>.
		 * 
		 * @return true if any category's rank changed, and false otherwise
		 */
		public boolean ranksChanged()
		{
			return !rankChanges.isEmpty();
		}
		
		/**
		 * Indicate that the ranks of categories were changed.
		 * 
		 * @param changed map of category names onto their old ranks
		 * @return the event representing the change.
		 */
		private Event ranksChanged(Map<String, Integer> changed)
		{
			rankChanges = changed;
			return this;
		}
		
		/**
		 * Get the specification of the category that was removed, if any.
		 * 
		 * @return the specification of the category that was removed during the event,
		 * or null if there was none
		 */
		public CategorySpec removedCategory()
		{
			return removedCategory;
		}
	}

	/**
	 * This class represents data being transferred via drag and drop or cut/copy/paste
	 * between this Deck and another object.  The Deck only supports importing card or entry
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
		 * Create a new TransferData containing the given deck's entries for the given card.
		 * 
		 * @param d deck to get entries from
		 * @param cards cards to find entries for
		 */
		public TransferData(Deck d, Card... cards)
		{
			transferData = Arrays.stream(cards).collect(Collectors.toMap(Function.identity(), (c) -> d.getEntry(c).count()));
		}
		
		/**
		 * Create a new TransferData containing the given Deck's entries for the given card.
		 * 
		 * @param d deck to get entries from
		 * @param cards cards to find entries for
		 */
		public TransferData(Deck d, Collection<Card> cards)
		{
			this(d, cards.stream().toArray(Card[]::new));
		}

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

		@Override
		public DataFlavor[] getTransferDataFlavors()
		{
			return new DataFlavor[] {CardList.entryFlavor, Card.cardFlavor, DataFlavor.stringFlavor};
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor)
		{
			return Arrays.asList(getTransferDataFlavors()).contains(flavor);
		}
	}
	
	/**
	 * Formatter for dates, usually for formatting the add date of a card.
	 */
	public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy");
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
	
	@Override
	public boolean add(Card card)
	{
		return add(card, 1);
	}

	@Override
	public boolean add(Card card, int amount)
	{
		return add(card, amount, LocalDate.now());
	}
	
	/**
	 * Add some number of cards to this deck.  If the number is not positive, then no changes are
	 * made.  Normally this should only be used when loading a deck, and will not affects an
	 * existing card's add date.
	 * 
	 * @param card card to add
	 * @param amount number of copies to add
	 * @param date date the card was originally added
	 * @return true if the Deck changed as a result, and false otherwise, which is when the number
	 * to add is less than 1.
	 */
	public boolean add(Card card, int amount, LocalDate date)
	{
		if (do_add(card, amount, date))
		{
			Map<Card, Integer> added = new HashMap<Card, Integer>();
			added.put(card, amount);
			notifyListeners(new Event().cardsChanged(added));
			return true;
		}
		else
			return false;
	}

	@Override
	public boolean addAll(CardList d)
	{
		Map<Card, Integer> added = new HashMap<Card, Integer>();
		for (Card card: d)
			if (do_add(card, d.getData(card).count(), d.getData(card).dateAdded()))
				added.put(card, d.getData(card).count());
		if (!added.isEmpty())
			notifyListeners(new Event().cardsChanged(added));
		return !added.isEmpty();
	}

	@Override
	public boolean addAll(Map<? extends Card, ? extends Integer> amounts)
	{
		Map<Card, Integer> added = new HashMap<Card, Integer>();
		for (Card card: amounts.keySet())
			if (do_add(card, amounts.get(card), LocalDate.now()))
				added.put(card, amounts.get(card));
		if (!added.isEmpty())
			notifyListeners(new Event().cardsChanged(added));
		return !added.isEmpty();
	}

	@Override
	public boolean addAll(Set<? extends Card> cards)
	{
		return addAll(cards.stream().collect(Collectors.toMap(Function.identity(), (c) -> 1)));
	}
	
	/**
	 * Add a new Category.
	 * 
	 * @param spec specification for the new Category
	 * @return the new Category, or the old one if one with that name already existed.
	 */
	public CardList addCategory(CategorySpec spec)
	{
		Category c = do_addCategory(spec);
		if (c != null)
		{
			notifyListeners(new Event().categoryAdded(c));
			return c;
		}
		else
			return categories.get(spec.getName());
	}
	
	/**
	 * Add a new listener for listening to changes in the deck.
	 * 
	 * @param listener listener to add
	 */
	public void addDeckListener(DeckListener listener)
	{
		listeners.add(listener);
	}
	
	/**
	 * Get all the categories.
	 * 
	 * @return a collection of all of the specifications of the categories in the deck,
	 * in no particular order.
	 */
	public Collection<CategorySpec> categories()
	{
		return categories.values().stream().map((category) -> category.spec).collect(Collectors.toList());
	}
	
	/**
	 * {@inheritDoc}
	 * Also remove all categories.
	 * 
	 * TODO: Give this a special event
	 */
	@Override
	public void clear()
	{
		masterList.clear();
		categories.clear();
		total = 0;
		land = 0;
	}

	@Override
	public boolean contains(Card card)
	{
		return getEntry(card) != null;
	}

	@Override
	public boolean containsAll(Collection<? extends Card> cards)
	{
		for (Card c: cards)
			if (!contains(c))
				return false;
		return true;
	}
	
	/**
	 * Check if the deck contains a category with the given name.
	 * 
	 * @param name name of the category to look for
	 * @return true if this Deck has a Category with the given name, and false otherwise.
	 */
	public boolean containsCategory(String name)
	{
		return categories.containsKey(name);
	}
	
	/**
	 * Add the given amount of the given card to the deck.  Whichever method calls this
	 * is responsible for notifying anyone of the change (if there is one).  If the card
	 * is already present, ignore the date parameter.
	 * 
	 * @param card card to add copies of
	 * @param amount amount of copies to add
	 * @param date if the Card is not present in this Deck, the date it was added on 
	 * @return true if this Deck was changed as a result of this operation, and false
	 * otherwise.
	 */
	private boolean do_add(Card card, int amount, LocalDate date)
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
		if (card.isLand())
			land += amount;
		
		return true;
	}
	
	/**
	 * Add a new Category without causing updates to listeners.
	 * 
	 * @param spec specification for the new Category
	 * @return the new Category, or null if one with that name already existed.
	 */
	private Category do_addCategory(CategorySpec spec)
	{
		if (!categories.containsKey(spec.getName()))
		{
			Category c = new Category(spec);
			categories.put(spec.getName(), c);
			c.update();
			
			spec.addCategoryListener(c.listener = (e) -> {
				if (e.nameChanged())
				{
					categories.remove(e.oldSpec().getName());
					categories.put(e.newSpec().getName(), c);
				}
				if (e.filterChanged() || e.whitelistChanged() || e.blacklistChanged())
					c.update();
				
				Event event = new Event().categoryChanged(e);
				for (DeckListener listener: new HashSet<DeckListener>(listeners))
					listener.deckChanged(event);
			});
			return c;
		}
		else
			return null;
	}
	
	/**
	 * Remove the given amount of the given card from the deck.  Whichever method calls this
	 * is responsible for notifying anyone of the change (if there is one).  If the card
	 * is already present, ignore the date parameter.
	 * 
	 * @param card card to remove copies of
	 * @param amount amount of copies to remove 
	 * @return the number of copies of the given Card that were actually removed from the deck.
	 * Normally this is the given number unless that number is greater than the number that were
	 * originally present.
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
					if (category.spec.getWhitelist().contains(card))
						category.spec.exclude(card);
					if (category.spec.getBlacklist().contains(card))
						category.spec.include(card);
					category.filtrate.remove(card);
				}
				masterList.remove(entry);
			}
			total -= removed;
			if (card.isLand())
				land -= removed;
		}
		
		return removed;
	}
	
	@Override
	public boolean equals(Object other)
	{
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (!(other instanceof Deck))
			return false;
		Deck o = (Deck)other;
		return o.masterList.equals(masterList) && o.categories.equals(categories);
	}
	
	/**
	 * Exclude a card from a category, even if it passes through its filter.
	 * 
	 * @param name name of the category to exclude from
	 * @param card card to exclude
	 * @return true if the card was successfully excluded from the category, and false
	 * otherwise.
	 */
	public boolean exclude(String name, Card card)
	{
		return contains(card) && categories.get(name).spec.exclude(card);
	}
	
	@Override
	public Card get(int index) throws IndexOutOfBoundsException
	{
		return masterList.get(index).card;
	}
	
	/**
	 * Get the category with the given name.
	 * 
	 * @param name name of the category to get
	 * @return a {@link CardList} containing cards in the category with the given name.
	 */
	public CardList getCategoryList(String name)
	{
		return categories.get(name);
	}
	
	/**
	 * Get the rank of the category with the given name.
	 * 
	 * @param name name of the category to search for
	 * @return the rank of the given category, or -1 if no category with the given name
	 * exists.
	 */
	public int getCategoryRank(String name)
	{
		return containsCategory(name) ? categories.get(name).rank : -1;
	}
	
	/**
	 * Get the specification for the category with the given name.
	 * 
	 * @param name name of the category whose specification is desired
	 * @return the specification of the category with the given name.
	 * @throws IllegalArgumentException if no such category exists
	 */
	public CategorySpec getCategorySpec(String name) throws IllegalArgumentException
	{
		if (categories.containsKey(name))
			return categories.get(name).spec;
		else
			throw new IllegalArgumentException("No category named " + name + " found");
	}

	@Override
	public Entry getData(Card card)
	{
		return getEntry(card);
	}
	
	@Override
	public Entry getData(int index)
	{
		return masterList.get(index);
	}
	
	/**
	 * Get the {@link DeckEntry} for the given card.
	 * 
	 * @param card card to look up
	 * @return the entry corresponding to the card, or null if there is none.
	 */
	private DeckEntry getEntry(Card card)
	{
		for (DeckEntry e: masterList)
			if (e.card.equals(card))
				return e;
		return null;
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(masterList, categories);
	}
	
	@Override
	public int indexOf(Card card)
	{
		return masterList.indexOf(getEntry(card));
	}
	
	@Override
	public boolean isEmpty()
	{
		return size() == 0;
	}
	
	@Override
	public Iterator<Card> iterator()
	{
		return masterList.stream().map(DeckEntry::card).iterator();
	}
	
	/**
	 * Get the number of lands in the deck.
	 * 
	 * @return the number of land cards, including multiples.
	 */
	public int land()
	{
		return land;
	}
	
	/**
	 * Get the number of nonlands in the deck.
	 * 
	 * @return the number of nonland cards, including multiples.
	 */
	public int nonland()
	{
		return total - land;
	}

	/**
	 * Notify each listener of changes to this Deck.
	 * 
	 * @param event event containing information about the change.
	 */
	private void notifyListeners(Event event)
	{
		for (DeckListener listener: new HashSet<DeckListener>(listeners))
			listener.deckChanged(event);
	}

	/**
	 * Get the number of categories in the deck.
	 * 
	 * @return the number of categories.
	 */
	public int numCategories()
	{
		return categories.size();
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		clear();
		
		int n = in.readInt();
		for (int i = 0; i < n; i++)
		{
			Card card = MainFrame.inventory().get(in.readUTF());
			int count = in.readInt();
			LocalDate added = (LocalDate)in.readObject();
			do_add(card, count, added);
		}
		n = in.readInt();
		for (int i = 0; i < n; i++)
		{
			CategorySpec spec = new CategorySpec();
			spec.readExternal(in);
			do_addCategory(spec).rank = in.readInt();
		}
	}

	@Override
	public boolean remove(Card card)
	{
		return remove(card, Integer.MAX_VALUE) > 0;
	}

	@Override
	public int remove(Card card, int amount)
	{
		int removed = do_remove(card, amount);
		if (removed > 0)
			notifyListeners(new Event().cardsChanged(Collections.singletonMap(card, removed)));
		return removed;
	}

	/**
	 * Remove a category from the deck.
	 * 
	 * @param spec specification of the category to remove
	 * @return true if the deck changed as a result, and false otherwise.
	 */
	public boolean removeCategory(CategorySpec spec)
	{
		Category c = categories.get(spec.getName());
		if (c != null)
		{
			for (DeckEntry e: masterList)
				e.categories.remove(c);
			Map<String, Integer> oldRanks = new HashMap<String, Integer>();
			for (Category category: categories.values())
			{
				if (category.rank > c.rank)
				{
					oldRanks.put(category.spec.getName(), category.rank);
					category.rank--;
				}
			}
			categories.remove(spec.getName());
			c.spec.removeCategoryListener(c.listener);
			
			Event event = new Event().categoryRemoved(c);
			if (!oldRanks.isEmpty())
			{
				oldRanks.put(c.spec.getName(), c.rank);
				event = event.ranksChanged(oldRanks);
			}
			notifyListeners(event);
			return true;
			
		}
		else
			return false;
	}

	@Override
	public Map<Card, Integer> removeAll(CardList cards)
	{
		return removeAll(cards.stream().collect(Collectors.toMap(Function.identity(), (c) -> cards.getData(c).count())));
	}
	
	@Override
	public Map<Card, Integer> removeAll(Map<? extends Card, ? extends Integer> amounts)
	{
		Map<Card, Integer> removed = new HashMap<Card, Integer>();
		for (Card card: new HashSet<Card>(amounts.keySet()))
		{
			int r = do_remove(card, amounts.get(card));
			if (r > 0)
				removed.put(card, -r);
		}
		
		if (!removed.isEmpty())
			notifyListeners(new Event().cardsChanged(removed));
		
		return removed;
	}

	@Override
	public Set<Card> removeAll(Set<? extends Card> cards)
	{
		return removeAll(cards.stream().collect(Collectors.toMap(Function.identity(), (c) -> 1))).keySet();
	}

	/**
	 * Remove a listener so it no longer receives alerts to changes in the
	 * deck.
	 * 
	 * @param listener listener to remove
	 * @return true if the given listener was successfully removed, and false
	 * otherwise.
	 */
	public boolean removeDeckListener(DeckListener listener)
	{
		return listeners.remove(listener);
	}

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
			if (e.card.isLand())
				land += amount - e.count;
			
			Map<Card, Integer> change = new HashMap<Card, Integer>();
			change.put(card, amount - e.count);
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

	@Override
	public boolean set(int index, int amount)
	{
		return set(masterList.get(index).card, amount);
	}

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
	 * @param name name of the category whose rank should be changed
	 * @param target new rank for the category
	 * @return true if ranks were successfully changed, and false otherwise (such as if the
	 * named category doesn't exist, the target rank is too high, the target rank is negative,
	 * or the target rank is the named category's rank).
	 */
	public boolean swapCategoryRanks(String name, int target)
	{
		if (!categories.containsKey(name) || categories.get(name).rank == target
				|| target >= categories.size() || target < 0)
			return false;
		else
		{
			for (Category second: categories.values())
			{
				if (second.rank == target)
				{
					Map<String, Integer> oldRanks = new HashMap<String, Integer>();
					oldRanks.put(name, categories.get(name).rank);
					oldRanks.put(second.spec.getName(), second.rank);
					
					second.rank = categories.get(name).rank;
					categories.get(name).rank = target;
					
					notifyListeners(new Event().ranksChanged(oldRanks));
					return true;
				}
			}
			return false;
		}
	}

	@Override
	public Card[] toArray()
	{
		return stream().toArray(Card[]::new);
	}
	
	@Override
	public int total()
	{
		return total;
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(masterList.size());
		for (DeckEntry entry: masterList)
		{
			out.writeUTF(entry.card.id());
			out.writeInt(entry.count);
			out.writeObject(entry.date);
		}
		out.writeInt(categories.size());
		for (Category category: categories.values())
		{
			category.spec.writeExternal(out);
			out.writeInt(category.rank);
		}
	}
}
