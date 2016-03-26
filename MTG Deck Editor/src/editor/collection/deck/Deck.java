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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import editor.collection.CardCollection;
import editor.collection.category.CategoryListener;
import editor.collection.category.CategorySpec;
import editor.database.Card;

/**
 * This class represents a deck which can have cards added and removed (in quantity) and
 * have several category views (from which cards can also be added or removed).
 * 
 * @author Alec Roelke
 */
public class Deck implements CardCollection
{
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
			transferData = Arrays.stream(cards).collect(Collectors.toMap((c) -> c, (c) -> d.count(c)));
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
		 * @return An Array containing the data flavors a Deck can support.  A Deck can
		 * only support Entry, Card, and String flavors.
		 */
		@Override
		public DataFlavor[] getTransferDataFlavors()
		{
			return new DataFlavor[] {entryFlavor, Card.cardFlavor, DataFlavor.stringFlavor};
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

		/**
		 * @param flavor Flavor of data to retrieve
		 * @return Data of the given flavor corresponding to the Entries retrieved by this
		 * TransferData.
		 * @throws UnsupportedFlavorException If the given flavor is unsupported
		 */
		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
		{
			if (flavor.equals(entryFlavor))
				return transferData;
			else if (flavor.equals(Card.cardFlavor))
				return transferData.keySet().stream().sorted(Card::compareName).toArray(Card[]::new);
			else if (flavor.equals(DataFlavor.stringFlavor))
				return transferData.entrySet().stream().map((e) -> e.getValue() + "x " + e.getKey().name()).reduce("", (a, b) -> a + "\n" + b);
			else
				throw new UnsupportedFlavorException(flavor);
		}
	}
	
	/**
	 * Data flavor representing entries in a deck.  Transfer data will appear as a
	 * map of cards onto an integer representing the number of copies to transfer.
	 */
	public static final DataFlavor entryFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + Entry[].class.getName() + "\"", "Deck Entries");
	
	/**
	 * Formatter for dates, usually for formatting the add date of a card.
	 */
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMMM d, yyyy");
	
	/**
	 * This class represents an entry into a deck.  It has a card and a
	 * number of copies.
	 * 
	 * @author Alec Roelke
	 */
	private static class Entry
	{
		/**
		 * Card in this Entry.  It can't be changed.
		 */
		private final Card card;
		/**
		 * Number of copies of the Card.
		 */
		private int count;
		/**
		 * Date this Entry was created (the Card was originally added).
		 */
		private final Date date;
		/**
		 * Set of categories this Entry's Card belongs to.  Implemented using a
		 * LinkedHashSet, so it will maintain the ordering that categories were
		 * added.
		 */
		private Set<Category> categories;
		
		/**
		 * Create a new Entry.
		 * 
		 * @param c Card for this Entry
		 * @param n Number of initial copies in this Entry
		 * @param d Date the Card was added
		 */
		private Entry(Card c, int n, Date d)
		{
			card = c;
			count = n;
			date = d;
			categories = new LinkedHashSet<Category>();
		}
		
		/**
		 * Copy constructor for Entry.
		 * 
		 * @param e Original Entry to copy
		 */
		private Entry(Entry e)
		{
			card = e.card;
			count = e.count;
			date = e.date;
			categories = new LinkedHashSet<Category>(e.categories);
		}
		
		/**
		 * Add copies to this Entry.
		 * 
		 * @param n Copies to add
		 * @return The new number of copies in this Entry.
		 */
		private int increase(int n)
		{
			return count += n;
		}
		
		/**
		 * Remove copies from this Entry.  There can't be fewer than
		 * 0 copies.
		 * 
		 * @param n Number of copies to remove.
		 * @return The new number of copies in this Entry.
		 */
		private int decrease(int n)
		{
			if (n > count)
				return count = 0;
			else
				return count -= n;
		}
	}
	
	/**
	 * List of cards in this Deck.
	 */
	private List<Entry> masterList;
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
	 * TODO: Comment this
	 */
	private Collection<DeckListener> listeners;
	
	/**
	 * Create a new, empty Deck with no categories.
	 */
	public Deck()
	{
		masterList = new ArrayList<Entry>();
		categories = new LinkedHashMap<String, Category>();
		total = 0;
		land = 0;
		listeners = new HashSet<DeckListener>();
	}
	
	/**
	 * Create a new Deck with the given cards. There will be no
	 * categories.
	 * 
	 * @param cards Cards to add to the new Deck.
	 */
	public Deck(Collection<Card> cards)
	{
		this();
		for (Card c: cards)
			increase(c, 1);
	}
	
	/**
	 * @param c Card to search for an Entry.
	 * @return The Entry corresponding to the Card, or <code>null</code>
	 * if there is none.
	 */
	private Entry getEntry(Card c)
	{
		for (Entry e: masterList)
			if (e.card.equals(c))
				return e;
		return null;
	}
	
	/**
	 * Add some number of Cards to this Deck.  If the number is not positive,
	 * then no changes are made.
	 * 
	 * @param c Card to add
	 * @param n Number of copies to add
	 * @param d Date the card was originally added
	 * @return <code>true</code> if the Deck changed as a result, and
	 * <code>false</code> otherwise, which is when the number to add
	 * is less than 1.
	 */
	public boolean increase(Card c, int n, Date d)
	{
		if (n < 1)
			return false;
		else
		{
			Entry e = getEntry(c);
			if (e == null)
			{
				masterList.add(e = new Entry(c, n, d));
				for (Category category: categories.values())
				{
					if (category.includes(c))
					{
						category.filtrate.add(c);
						e.categories.add(category);
					}
				}
			}
			else
				e.increase(n);
			total += n;
			if (c.typeContains("land"))
				land += n;
			
			Map<Card, Integer> added = new HashMap<Card, Integer>();
			added.put(c, n);
			DeckEvent event = new DeckEvent(this, added, null, null, null);
			for (DeckListener listener: listeners)
				listener.DeckChanged(event);
			
			return true;
		}
	}
	
	/**
	 * Add some number of Cards to this Deck.  If the number is not positive,
	 * then no changes are made.
	 * 
	 * @param c Card to add
	 * @param n Number of copies to add
	 * @return <code>true</code> if the Deck changed as a result, and
	 * <code>false</code> otherwise, which is when the number to add
	 * is less than 1.
	 */
	@Override
	public boolean increase(Card c, int n)
	{
		return increase(c, n, new Date());
	}
	
	/**
	 * Add some number of copies of a collection of Cards to this Deck.  If
	 * the number is not positive, then no changes are made.
	 * 
	 * @param coll Collection of Cards to add
	 * @param n Number of copies of each card to add
	 * @return <code>true</code> if the Deck was changed as a result, and
	 * <code>false</code> otherwise.
	 */
	@Override
	public boolean increaseAll(Collection<? extends Card> coll, int n)
	{
		boolean changed = false;
		for (Card c: coll)
			changed |= increase(c, n);
		return changed;
	}
	
	/**
	 * Add a single copy of a Card to this Deck.
	 * 
	 * @param c Card to add
	 * @return <code>true</code>, since the Deck will always change as
	 * a result.
	 */
	@Override
	public boolean increase(Card c)
	{
		return increase(c, 1);
	}
	
	/**
	 * Remove some number of copies of the given Card from this Deck.  If that
	 * number is less than one, no changes are made.
	 * 
	 * @param c Card to remove
	 * @param n Number of copies to remove
	 * @return The number of copies of the Card that were actually removed.
	 */
	@Override
	public int decrease(Card c, int n)
	{
		if (n < 1)
			return 0;
		else
		{
			Entry e = getEntry(c);
			if (e == null)
				return 0;
			else
			{
				if (n > e.count)
					n = e.count;
				e.decrease(n);
				if (e.count == 0)
				{
					masterList.remove(e);
					for (Category category: categories.values())
					{
						category.filtrate.remove(c);
						category.spec.getWhitelist().remove(c);
						category.spec.getBlacklist().remove(c);
					}
				}
				total -= n;
				if (c.typeContains("land"))
					land -= n;
				
				Map<Card, Integer> removed = new HashMap<Card, Integer>();
				removed.put(c, -n);
				DeckEvent event = new DeckEvent(this, removed, null, null, null);
				for (DeckListener listener: listeners)
					listener.DeckChanged(event);
				
				return n;
			}
		}
	}
	
	/**
	 * Remove one copy of the given Card from this Deck.
	 * 
	 * @param c Card to remove
	 * @return 0 if no copies were removed, and 1 if a copy was removed.
	 */
	@Override
	public int decrease(Card c)
	{
		return decrease(c, 1);
	}
	
	/**
	 * TODO: Comment this
	 * @param name
	 * @param c
	 * @return
	 */
	public boolean include(String name, Card c)
	{
		if (!contains(c))
			return false;
		else
			return categories.get(name).spec.include(c);
	}
	
	/**
	 * TODO: Comment this
	 * @param name
	 * @param c
	 * @return
	 */
	public boolean exclude(String name, Card c)
	{
		if (!contains(c))
			return false;
		else
			return categories.get(name).spec.exclude(c);
	}
	
	/**
	 * @param index Index to look at in the list
	 * @return The Card at the given index.
	 */
	@Override
	public Card get(int index)
	{
		return masterList.get(index).card;
	}
	
	/**
	 * TODO: Comment this
	 * @param name
	 * @param index
	 * @return
	 */
	public Card get(String name, int index)
	{
		return categories.get(name).get(index);
	}
	
	/**
	 * Set the number of copies of the Card at the given index to be the given value.
	 * 
	 * @param index Index to find the Card at
	 * @param n Number of copies to change to
	 * @return <code>true</code> if the Card is in the Deck and if the number of copies
	 * was changed, and <code>false</code> otherwise.
	 */
	@Override
	public boolean setCount(int index, int n)
	{
		return setCount(masterList.get(index).card, n);
	}
	
	/**
	 * Set the number of copies of the given Card to be the given value.  If the card
	 * isn't in the deck, it will be added.
	 * 
	 * @param c Card to change
	 * @param n Number of copies to change to
	 * @return <code>true</code> if the number of copies was changed or if the card was
	 * added, and <code>false</code> otherwise.
	 */
	@Override
	public boolean setCount(Card c, int n)
	{
		if (n < 0)
			n = 0;
		Entry e = getEntry(c);
		if (e == null)
			return increase(c, n);
		else if (e.count == n)
			return false;
		else
		{
			total += n - e.count;
			if (e.card.typeContains("land"))
				land += n - e.count;
			
			Map<Card, Integer> change = new HashMap<Card, Integer>();
			change.put(c, n - e.count);
			DeckEvent event = new DeckEvent(this, change, null, null, null);
			
			e.count = n;
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
			
			for (DeckListener listener: listeners)
				listener.DeckChanged(event);
			
			return true;
		}
	}
	
	/**
	 * @param o Object to look for
	 * @return Index of that Object in the master list.
	 */
	@Override
	public int indexOf(Object o)
	{
		if (!(o instanceof Card))
			return -1;
		else
			return masterList.indexOf(getEntry((Card)o));
	}
	
	/**
	 * @param c Card to look at
	 * @return The number of copies of the given Card in this Deck.
	 */
	@Override
	public int count(Card c)
	{
		Entry e = getEntry(c);
		if (e == null)
			return 0;
		else
			return e.count;
	}
	
	/**
	 * @param index Index into the Deck list of the Card to look at
	 * @return The number of copies of the Card at the given index.
	 */
	@Override
	public int count(int index)
	{
		return masterList.get(index).count;
	}
	
	/**
	 * @param c Card to look for
	 * @return The Date the Card was originally added to the Deck.
	 */
	@Override
	public Date dateAdded(Card c)
	{
		Entry e = getEntry(c);
		if (e == null)
			return null;
		else
			return e.date;
	}
	
	/**
	 * @param index Index of the Card to look for
	 * @return The Date the Card was originally added to the deck.
	 */
	@Override
	public Date dateAdded(int index)
	{
		return masterList.get(index).date;
	}
	
	/**
	 * @param o Object to look for
	 * @return <code>true</code> if this Deck contains one or more copies
	 * of the given Object, and <code>false</code> otherwise.
	 */
	@Override
	public boolean contains(Object o)
	{
		return o instanceof Card && getEntry((Card)o) != null;
	}
	
	/**
	 * TODO: Comment this
	 * @param name
	 * @param o
	 * @return
	 */
	public boolean contains(String name, Object o)
	{
		return o instanceof Card && categories.get(name).contains(o);
	}
	
	/**
	 * Add a new Category.  If there is already a Category with the same name,
	 * instead do nothing.
	 * 
	 * @param spec Specifications for the new Category
	 * if there already was one with that name.
	 */
	public CardCollection addCategory(CategorySpec spec)
	{
		if (!categories.containsKey(spec.getName()))
		{
			Category c = new Category(spec);
			categories.put(spec.getName(), c);
			c.update();
			
			spec.addCategoryListener(c.listener = (e) -> {
				if (e.nameChanged())
				{
					categories.remove(e.oldName());
					categories.put(e.newName(), c);
				}
				if (e.filterChanged() || e.whitelistChanged() || e.blacklistChanged())
					c.update();
				
				DeckEvent event = new DeckEvent(this, null, e.nameChanged() ? e.oldName() : e.getSource().getName(), e, null);
				for (DeckListener listener: listeners)
					listener.DeckChanged(event);
			});
			
			return c;
		}
		else
			return categories.get(spec.getName());
	}
	
	/**
	 * Remove a Category from this Deck.
	 * 
	 * @param name Name of the Category to remove.
	 * @return <code>true</code> if the deck changed as a result, and
	 * <code>false</code> otherwise.
	 */
	public boolean removeCategory(String name)
	{
		Category c = categories.get(name);
		if (c != null)
		{
			for (Entry e: masterList)
				e.categories.remove(c);
			categories.remove(name);
			c.spec.removeCategoryListener(c.listener);
			
			DeckEvent event = new DeckEvent(this, null, null, null,
					new HashSet<String>(Arrays.asList(c.spec.getName())));
			for (DeckListener listener: listeners)
				listener.DeckChanged(event);
			
			return true;
		}
		else
			return false;
	}
	
	/**
	 * TODO: Comment this
	 * @param name
	 * @return
	 */
	public CategorySpec getCategorySpec(String name)
	{
		if (categories.get(name) != null)
			return categories.get(name).spec;
		else
			throw new IllegalArgumentException("No category named " + name + " found");
	}
	
	/**
	 * TODO: Comment this
	 * @param name
	 * @return
	 */
	public CardCollection getCategoryCards(String name)
	{
		return categories.get(name);
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
	 * @param c Card to look for
	 * @return The set of Categories the Card belongs to.
	 */
	@Override
	public Set<CategorySpec> getCategories(Card c)
	{
		return getEntry(c).categories.stream().map(Category::spec).collect(Collectors.toSet());
	}
	
	/**
	 * @param index The index of the Card to look for
	 * @return The set of Categories the Card belongs to.
	 */
	@Override
	public Set<CategorySpec> getCategories(int index)
	{
		return masterList.get(index).categories.stream().map(Category::spec).collect(Collectors.toSet());
	}
	
	/**
	 * Reset this Deck to being empty and having no categories.
	 */
	@Override
	public void clear()
	{
		Map<Card, Integer> removed = masterList.stream().collect(Collectors.toMap((c) -> c.card, (c) -> -c.count));
		Set<String> categoriesRemoved = categories.keySet();
		
		masterList.clear();
		categories.clear();
		total = 0;
		land = 0;
		
		DeckEvent e = new DeckEvent(this, removed, null, null, categoriesRemoved);
		for (DeckListener listener: listeners)
			listener.DeckChanged(e);
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
	 * TODO: Comment this
	 * @param name
	 * @return
	 */
	public int size(String name)
	{
		return categories.get(name).size();
	}
	
	/**
	 * @return The number of Cards in this Deck.
	 */
	@Override
	public int total()
	{
		return total;
	}
	
	/**
	 * TODO: Comment this
	 * @param name
	 * @return
	 */
	public int total(String name)
	{
		if (containsCategory(name))
			return categories.get(name).total();
		else
			return 0;
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
	 * @return <code>true</code> if there are no cards in this Deck, and
	 * <code>false</code> otherwise.
	 */
	@Override
	public boolean isEmpty()
	{
		return size() == 0;
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
			for (Entry e: masterList)
				wr.println(e.card.id() + "\t" + e.count + "\t" + DATE_FORMAT.format(e.date));
			wr.println(String.valueOf(categories.size()));
			for (Category c: categories.values())
				wr.println(c.toString());
		} 
	}
	
	/**
	 * @return An Iterator over the list of Cards in this Deck.
	 */
	@Override
	public Iterator<Card> iterator()
	{
		return stream().iterator();
	}
	
	/**
	 * @return A sequential Stream whose source is this Deck.
	 */
	@Override
	public Stream<Card> stream()
	{
		return masterList.stream().map((e) -> e.card);
	}
	
	/**
	 * If the given Card isn't in this Deck, add a copy of it to the Deck.
	 * 
	 * @param c Card to add
	 * @return <code>true</code> if the card wasn't in the deck and was successfully added,
	 * and <code>false</code> otherwise.
	 */
	@Override
	public boolean add(Card c)
	{
		if (contains(c))
			return false;
		else
			return increase(c);
	}

	/**
	 * Add each of the given collection of Cards that aren't already in the Deck to the Deck.
	 * 
	 * @param coll Collection of Cards to add.
	 * @return <code>true</code> if any of the Cards were successfully added, and
	 * <code>false</code> otherwise.
	 */
	@Override
	public boolean addAll(Collection<? extends Card> coll)
	{
		boolean changed = false;
		for (Card c: coll)
			changed |= add(c);
		return changed;
	}
	
	/**
	 * Add all of the cards in the given Deck to this Deck.
	 * 
	 * @param d Deck to copy
	 * @return <code>true</code> if cards were successfully added, and <code>false</code>
	 * otherwise.
	 */
	public boolean addAll(Deck d)
	{
		boolean changed = false;
		for (Entry e: d.masterList)
			changed |= increase(e.card, e.count);
		return changed;
	}

	/**
	 * @param coll Collection of objects to look for
	 * @return <code>true</code> if all of the objects in the given collection are present
	 * in this Deck, and <code>false</code> otherwise.
	 */
	@Override
	public boolean containsAll(Collection<?> coll)
	{
		for (Object o: coll)
			if (!contains(o))
				return false;
		return true;
	}

	/**
	 * @param o Object to remove
	 * @return <code>true</code> if the object is a Card and if one or more copies were
	 * removed, and <code>false</code> otherwise.
	 */
	@Override
	public boolean remove(Object o)
	{
		if (!(o instanceof Card))
			return false;
		else
			return decrease((Card)o, Integer.MAX_VALUE) > 0;
	}

	/**
	 * Remove as many of the objects in the given list from this Deck as possible.
	 * 
	 * @param coll Collection of objects to remove
	 * @return <code>true</code> if any of the given objects were remove, and
	 * <code>false</code> otherwise.
	 */
	@Override
	public boolean removeAll(Collection<?> coll)
	{
		boolean changed = false;
		for (Object o: coll)
			changed |= remove(o);
		return changed;
	}

	/**
	 * Retain only the elements in the given collection that are in this Deck.
	 * 
	 * @param coll Collection of elemnts to retain
	 * @return <code>true</code> if this Deck was changed as a result, and
	 * <code>false</code> otherwise.
	 */
	@Override
	public boolean retainAll(Collection<?> coll)
	{
		boolean changed = false;
		for (Card c: new ArrayList<Card>(this))
			if (!coll.contains(c))
				changed |= remove(c);
		return changed;
	}

	/**
	 * @return An array containin all of the Cards in this Deck.
	 */
	@Override
	public Object[] toArray()
	{
		return stream().toArray();
	}

	/**
	 * @param a Array indicating runtime type of the data to return
	 * @return An array containing all of the Cards in this Deck.  If the provided
	 * array is large enough to fit all of the Cards, it will be filled with them.
	 * Otherwise, a new array will be allocated.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a)
	{
		if (a.length >= size())
		{
			for (int i = 0; i < size(); i++)
				a[i] = (T)get(i);
			return a;
		}
		else
		{
			return (T[])toArray();
		}
	}
	
	/**
	 * @return A Collection containing all of the specifications of the categories
	 * in this Deck, in no particular order.
	 */
	public Collection<CategorySpec> categories()
	{
		return categories.values().stream().map(Category::spec).collect(Collectors.toList());
	}
	
	/**
	 * TODO: Comment this
	 * @param listener
	 */
	public void addDeckListener(DeckListener listener)
	{
		listeners.add(listener);
	}
	
	/**
	 * TODO: Comment this
	 * @param listener
	 */
	public void removeDeckListener(DeckListener listener)
	{
		listeners.remove(listener);
	}
	
	/**
	 * This class represents a category of a deck.  It looks like a deck since it
	 * contains a list of cards and can report how many copies of them there are, 
	 * so it extends Deck.  If a card is added or removed using the add and remove
	 * methods, the master list will be updated to reflect this only if the card
	 * passes through the Category's filter.
	 * 
	 * @author Alec Roelke
	 */
	private class Category implements CardCollection
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
		 * TODO: Comment this
		 */
		public CategoryListener listener;
		
		/**
		 * Create a new Category.
		 * 
		 * @param s Specifications for the new Category
		 */
		private Category(CategorySpec s)
		{
			spec = s;
			update();
		}
		
		/**
		 * TODO: Comment this
		 */
		public void update()
		{
			filtrate = masterList.stream().map((e) -> e.card).filter(spec::includes).collect(Collectors.toList());
			for (Entry e: masterList)
				if (spec.includes(e.card))
					e.categories.add(this);
				else
					e.categories.remove(this);
		}
		
		/**
		 * @return This Category's specifications.
		 */
		public CategorySpec spec()
		{
			return spec;
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
		 * Add some number of copies of a Card to this Category if it passes
		 * through this Category's filter.
		 * 
		 * @param c Card to add
		 * @param n Number of copies to add
		 * @return <code>true</code> if the Deck was changed as a result, and
		 * <code>false</code> otherwise.
		 */
		@Override
		public boolean increase(Card c, int n)
		{
			if (includes(c))
				return Deck.this.increase(c, n);
			else
				return false;
		}
		
		/**
		 * Add one copy of a Card to this Category if it passes through this
		 * Category's filter.
		 * 
		 * @param c Card to add
		 * @return <code>true</code> if the Deck was changed as a result, and
		 * <code>false</code> otherwise.
		 */
		@Override
		public boolean increase(Card c)
		{
			return increase(c, 1);
		}
		
		/**
		 * Add some number of copies of a collection of Cards to this Deck.  If
		 * the number is not positive, then no changes are made.  Only Cards that
		 * pass through the filter will be added
		 * 
		 * @param coll Collection of Cards to add
		 * @param n Number of copies of each card to add
		 * @return <code>true</code> if the Deck was changed as a result, and
		 * <code>false</code> otherwise.
		 */
		@Override
		public boolean increaseAll(Collection<? extends Card> coll, int n)
		{
			boolean changed = false;
			for (Card c: coll)
				changed |= increase(c, n);
			return changed;
		}
		
		/**
		 * Remove some number of copies of a Card from this Category if it passes
		 * through this Category's filter.
		 * 
		 * @param c Card to add
		 * @param n Number of copies to remove
		 * @return The numbe of copies of the Card that were actually removed.
		 */
		@Override
		public int decrease(Card c, int n)
		{
			if (includes(c))
				return Deck.this.decrease(c, n);
			else
				return 0;
		}
		
		/**
		 * Remove one copy of a Card from this Category if it passes through
		 * this Category's filter.
		 * 
		 * @param c Card to add
		 * @return 0 if the Card was removed, and 1 otherwise.
		 */
		@Override
		public int decrease(Card c)
		{
			return decrease(c, 1);
		}
		
		/**
		 * Set the number of copies of the Card at the given index to be the given value.
		 * 
		 * @param index Index to find the Card at
		 * @param n Number of copies to change to
		 * @return <code>true</code> if the Card is in the Category and if the number of copies
		 * was changed, and <code>false</code> otherwise.
		 */
		@Override
		public boolean setCount(int index, int n)
		{
			Card c = get(index);
			return c != null && setCount(c, n);
		}
		
		/**
		 * Set the number of copies of the given Card to be the given value.  If the card
		 * isn't in the deck, it will be added.  If it isn't included in the category,
		 * then nothing will happen.
		 * 
		 * @param c Card to change
		 * @param n Number of copies to change to
		 * @return <code>true</code> if the number of copies was changed or if the card was
		 * added, and <code>false</code> otherwise.
		 */
		@Override
		public boolean setCount(Card c, int n)
		{
			return includes(c) & Deck.this.setCount(c, n);
		}
		
		/**
		 * @param c Card to test
		 * @return <code>true</code> if the given Card can belong to this Category and
		 * <code>false</code> otherwise.
		 */
		public boolean includes(Card c)
		{
			return spec.includes(c);
		}
		
		/**
		 * @param index Index into this Category's view of the master list to
		 * look at
		 * @return The Card at the given index.
		 */
		@Override
		public Card get(int index)
		{
			return filtrate.get(index);
		}
		
		/**
		 * @param o Object to look for
		 * @return The index of that Object in this Category's view of the master
		 * list.
		 */
		@Override
		public int indexOf(Object o)
		{
			return filtrate.indexOf(o);
		}
		
		/**
		 * @param c Card to look at
		 * @return The number of copies of the given Card in this Category.  If
		 * the Card is in the deck but does not pass through this Category's filter,
		 * it is treated as though it isn't in the deck (and 0 is returned).
		 */
		@Override
		public int count(Card c)
		{
			if (includes(c))
				return Deck.this.count(c);
			else
				return 0;
		}
		
		/**
		 * @param index Index of the Card to look at
		 * @return The number of copies of the Card at the given index in this Category.
		 * If the Card is in the deck but does not pass through this Category's filter,
		 * it is treated as though it isn't in the deck (and 0 is returned).
		 */
		@Override
		public int count(int index)
		{
			return Deck.this.count(get(index));
		}
		
		/**
		 * @param c Card to look for
		 * @return The Date the card was originally added to the deck, if it is in the category,
		 * and null otherwise.
		 */
		@Override
		public Date dateAdded(Card c)
		{
			if (includes(c))
				return Deck.this.dateAdded(c);
			else
				return null;
		}
		
		/**
		 * @param index Index into the filtered list of the Card to look for
		 * @return The Date the card was originally added to the deck.
		 */
		@Override
		public Date dateAdded(int index)
		{
			return Deck.this.dateAdded(get(index));
		}
		
		/**
		 * @param o Object to look for
		 * @return <code>true</code> if the given Object is in this Category, and
		 * <code>false</code> otherwise.
		 */
		@Override
		public boolean contains(Object o)
		{
			return filtrate.contains(o);
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
		 * @return the total number of Cards in this Category.
		 */
		@Override
		public int total()
		{
			return filtrate.stream().map(Deck.this::getEntry).mapToInt((e) -> e.count).sum();
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
		 * @return A sequential Stream whose source is this Category.
		 */
		@Override
		public Stream<Card> stream()
		{
			return filtrate.stream();
		}
		
		/**
		 * @param c Card to look for
		 * @return The list of Categories the given Card belongs to, if it
		 * belongs to this Category.
		 */
		@Override
		public Set<CategorySpec> getCategories(Card c)
		{
			Entry e = getEntry(c);
			if (e != null && includes(c))
				return Deck.this.getCategories(c);
			else
				return null;
		}
		
		/**
		 * @param index Index of the Card to look for
		 * @return The list of Categories the Card at the given index belongs
		 * to.
		 */
		@Override
		public Set<CategorySpec> getCategories(int index)
		{
			return getCategories(filtrate.get(index));
		}

		/**
		 * If the given Card belongs to this Category and isn't in the Deck, add it to the
		 * Deck.
		 * 
		 * @param c Card to add
		 * @return <code>true</code> if the Card was added and <code>false</code> otherwise.
		 */
		@Override
		public boolean add(Card c)
		{
			if (includes(c))
				return Deck.this.add(c);
			else
				return false;
		}

		/**
		 * Add all of the given Cards that can belong to this Category and aren't already
		 * in the Deck to the Deck.
		 * 
		 * @param coll Collection of Cards to add
		 * @return <code>true</code> if the Deck changed as a result, and <code>false</code>
		 * otherwise.
		 */
		@Override
		public boolean addAll(Collection<? extends Card> coll)
		{
			return Deck.this.addAll(coll.stream().filter(this::includes).collect(Collectors.toList()));
		}

		/**
		 * @param coll Collection of objects to look for
		 * @return <code>true</code> if this Category contains all of the given objects,
		 * and <code>false</code> otherwise.
		 */
		@Override
		public boolean containsAll(Collection<?> coll)
		{
			for (Object o: coll)
				if (!contains(o))
					return false;
			return true;
		}

		/**
		 * Remove the given object from the Deck if it is in this Category.
		 * 
		 * @param o Object to remove
		 * @return <code>true</code> if the given object was successfully remove, and
		 * <code>false</code> otherwise.
		 */
		@Override
		public boolean remove(Object o)
		{
			if (!contains(o))
				return false;
			else
				return Deck.this.remove(o);
		}

		/**
		 * Remove all objects from the given collection that are in this Category
		 * from the Deck.
		 * 
		 * @param coll Collection of objects to remove
		 * @return <code>true</code> if any of the objects were successfully removed
		 * from the Deck, and <code>false</code> otherwise.
		 */
		@Override
		public boolean removeAll(Collection<?> coll)
		{
			boolean changed = false;
			for (Object o: coll)
				changed |= remove(o);
			return changed;
		}

		/**
		 * Retain only the Cards in this Category that are also in the given collection.
		 * 
		 * @param Collection of objects to retain
		 * @return <code>true</code> if the Deck was changed as a result, and
		 * <code>false</code> otherwise.
		 */
		@Override
		public boolean retainAll(Collection<?> coll)
		{
			boolean changed = false;
			for (Card c: new ArrayList<Card>(this))
				if (!coll.contains(c))
					changed |= remove(c);
			return changed;
		}

		/**
		 * @return An array containing all of the Cards in this Category.
		 */
		@Override
		public Object[] toArray()
		{
			return filtrate.toArray();
		}

		/**
		 * @param a Array specifying the runtime type of the data to return
		 * @return An array containing all of the Cards in this Category.  If
		 * the provided array can fit those Cards, it is populated with them.
		 * Otherwise, a new array is allocated.
		 */
		@Override
		public <T> T[] toArray(T[] a)
		{
			return filtrate.toArray(a);
		}
		
		@Override
		public void clear()
		{
			throw new UnsupportedOperationException();
		}
	}
}
