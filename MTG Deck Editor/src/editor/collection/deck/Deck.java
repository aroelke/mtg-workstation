package editor.collection.deck;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.Externalizable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
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
 * TODO: Decide if the sideboard should be part of the Deck or a separate class
 * 
 * @author Alec Roelke
 */
public class Deck implements CardList, Externalizable
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
			return spec.includes(card) ? Deck.this.add(card, amount) : false;
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
		public boolean equals(Object other)
		{
			if (other == null)
				return false;
			if (other == this)
				return true;
			if (!(other instanceof Category))
				return false;
			return spec.equals(((Category)other).spec);
		}
		
		@Override
		public Card get(int index) throws IndexOutOfBoundsException
		{
			return filtrate[index];
		}
		
		@Override
		public Entry getData(Card card) throws IllegalArgumentException
		{
			return spec.includes(card) ? Deck.this.getData(card) : null;
		}
		
		@Override
		public Entry getData(int index) throws IndexOutOfBoundsException
		{
			return getData(this[index]);
		}
		
		@Override
		public int hashCode()
		{
			return Objects.hash(spec);
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
			return set(this[index], amount);
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
		 * @param card card for this DeckEntry
		 * @param amount number of initial copies in this Entry
		 * @param added date the Card was added
		 */
		public DeckEntry(Card card, int amount, Date added)
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
		public Date dateAdded()
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
		 * If a category's name was changed, its old name.
		 */
		private String changedName;
		/**
		 * CategoryEvent representing the changes to the CategorySpec corresponding to the
		 * category that was changed, if any was changed.
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
		
		/**
		 * Create a new Event with no changes to the deck.
		 */
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
		 * If a category was added, get its name.
		 * 
		 * @return the name of the category that was added.
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
		 * If cards were added, get the cards that were added and how many of each were added.
		 * 
		 * @return a map containing the Cards that were added and the number of copies that were
		 * added.
		 * @throws IllegalStateException If no cards were added or removed during the event.
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
		 * Check if the cards in the deck were changed.
		 * 
		 * @return true if cards were added to or removed from the deck during the event, and
		 * false otherwise.
		 */
		public boolean cardsChanged()
		{
			return cardsChanged != null;
		}
		
		/**
		 * Indicate that cards and/or counts of cards in the deck changed. A positive number
		 * means a card was added, and a negative one means it was removed.
		 * 
		 * @param change map of Cards onto their count changes
		 * @return the event representing the change.
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
		 * @throws IllegalStateException if no cards were added or removed during the event.
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
		 * Check if any categories were removed.
		 * 
		 * @return true if any categories were removed during the event, and false otherwise.
		 */
		public boolean categoriesRemoved()
		{
			return removedCategories != null;
		}
		
		/**
		 * Indicate that categories were removed from the deck.
		 * 
		 * @param removed collection of categories that were removed
		 * @return the event representing the change.
		 */
		private Event categoriesRemoved(Collection<Category> removed)
		{
			removedCategories = new HashSet<String>(removed.stream().map((c) -> c.spec.getName()).collect(Collectors.toSet()));
			return this;
		}
		
		/**
		 * Check if a category was added to the deck.
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
			addedCategory = added.spec.getName();
			return this;
		}
		
		/**
		 * Check if a category was changed.
		 * 
		 * @return true if a category in the Deck was changed, and false otherwise.
		 */
		public boolean categoryChanged()
		{
			return categoryChanges != null;
		}
		
		/**
		 * Indicate that a category was changed.
		 * 
		 * @param changeName name of the category that was changed
		 * @param changes {@link editor.collection.category.CategorySpec.Event} indicating changes
		 * to the category
		 * @return the event representing the change.
		 */
		private Event categoryChanged(String changeName, CategorySpec.Event changes)
		{
			changedName = changeName;
			categoryChanges = changes;
			return this;
		}
		
		/**
		 * Get the event that indicates a change to a category.
		 * 
		 * @return an event detailing the changes to the category.
		 * @throws IllegalStateException if no category was changed during the event.
		 */
		public CategorySpec.Event categoryChanges()
		{
			if (categoryChanged())
				return categoryChanges;
			else
				throw new IllegalStateException("Category was not changed");
		}
		
		/**
		 * Get the name of the category that was changed as it was before the event.
		 * Use this rather than the CategoryEvent returned by
		 * {@link #categoryChanged(String, editor.collection.category.CategorySpec.Event)}.
		 * to identify which category was changed if its name was not changed.
		 * 
		 * @return the name of the category that was changed before the event.
		 * @throws IllegalStateException if no category was changed during the
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
		 * @param removed category that was removed
		 * @return the event representing the change.
		 */
		private Event categoryRemoved(Category removed)
		{
			removedCategories = new HashSet<String>(Arrays.asList(removed.spec.getName()));
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
		 * @return a map of category names onto their old ranks before they were changed.
		 * @throws IllegalStateException if no category ranks were changed.
		 */
		public Map<String, Integer> oldRanks()
		{
			if (ranksChanged())
				return rankChanges;
			else
				throw new IllegalStateException("No category's rank changed");
		}
		
		/**
		 * Check if any category ranks changed.
		 * 
		 * @return true if the ranks of any categories were changed, and false otherwise.
		 */
		public boolean ranksChanged()
		{
			return rankChanges != null;
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
		 * Get the names of categories that were removed, if any.
		 * 
		 * @return the set of names of the categories that were removed during the event.
		 * @throws IllegalStateException if no categories were removed during the event.
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
	
	@Override
	public boolean add(Card card)
	{
		return add(card, 1);
	}

	@Override
	public boolean add(Card card, int amount)
	{
		return add(card, amount, new Date());
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

	@Override
	public boolean addAll(CardList d)
	{
		return addAll(d.stream().collect(Collectors.toMap(Function.identity(), (c) -> d.getData(c).count())));
	}

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

		for (DeckEntry e: masterList)
		{
			if (!e.equals(entry) && e.card.equals(entry.card))
			{
				System.out.println("e:     " + e.card.unifiedName() + ',' + e.count + ',' + e.date);
				System.out.println("entry: " + entry.card.unifiedName() + ',' + entry.count + ',' + entry.date);
				System.out.println("Card: " + e.card.equals(entry.card));
				System.out.println("Count: " + (e.count == entry.count));
				System.out.println("Date: " + e.date.equals(entry.date));
				System.out.println("Equal: " + e.equals(entry));
			}
		}
		
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
		return contains(card) && categories[name].spec.exclude(card);
	}
	
	@Override
	public Card get(int index) throws IndexOutOfBoundsException
	{
		return masterList[index].card;
	}
	
	/**
	 * Get the category with the given name.
	 * 
	 * @param name name of the category to get
	 * @return a {@link CardList} containing cards in the category with the given name.
	 */
	public CardList getCategoryList(String name)
	{
		return categories[name];
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
		return containsCategory(name) ? categories[name].rank : -1;
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
		if (categories[name] != null)
			return categories[name].spec;
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
		return masterList[index];
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
			Card card = MainFrame.inventory()[in.readUTF()];
			int count = in.readInt();
			Date added = (Date)in.readObject();
			do_add(card, count, added);
		}
		n = in.readInt();
		for (int i = 0; i < n; i++)
		{
			Category category = new Category((CategorySpec)in.readObject());
			categories[category.spec.getName()] = category;
			category.rank = in.readInt();
			category.update();
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
	 * @param name name of the category to remove.
	 * @return true if the deck changed as a result, and false otherwise.
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
			int r = do_remove(card, amounts[card]);
			if (r > 0)
				removed[card] = -r;
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
	 * @param file file to save to
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

	@Override
	public boolean set(int index, int amount)
	{
		return set(masterList[index].card, amount);
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
			out.writeObject(category.spec);
			out.writeInt(category.rank);
		}
	}
}
