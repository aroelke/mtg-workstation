package database;

import gui.filter.FilterGroupPanel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class represents a deck which can have cards added and removed (in quantity) and
 * have several category views (from which cards can also be added or removed).
 * 
 * @author Alec Roelke
 */
public class Deck implements Iterable<Card>
{
	/**
	 * Regex pattern for matching category strings and extracting their contents.  The first group
	 * will be the category's name, the second group will be the UIDs of the cards in its whitelist,
	 * the third group will the UIDs of the cards in its blacklist, and the fourth group will be
	 * its filter's String representation.  The second and third groups will not include the group
	 * enclosing characters, but the fourth will.  The first group needs to be trimmed, or the
	 * trailing white space will be included.
	 * @see gui.filter.FilterGroupPanel#setContents(String)
	 */
	public static Pattern CATEGORY_PATTERN = Pattern.compile("^([^" + FilterGroupPanel.BEGIN_GROUP + "]+)"
			+ FilterGroupPanel.BEGIN_GROUP + "([^" + FilterGroupPanel.END_GROUP + "]*)" + FilterGroupPanel.END_GROUP
			+ "\\s*" + FilterGroupPanel.BEGIN_GROUP + "([^" + FilterGroupPanel.END_GROUP + "]*)" + FilterGroupPanel.END_GROUP
			+ "\\s*(" + FilterGroupPanel.BEGIN_GROUP + ".*$)");
	/**
	 * List separator for UIDs of cards in the String representation of a whitelist or a blacklist.
	 */
	public static String EXCEPTION_SEPARATOR = ":";
	
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
		 * Create a new Entry.
		 * 
		 * @param c Card for this Entry
		 * @param n Number of initial copies in this Entry
		 */
		public Entry(Card c, int n)
		{
			card = c;
			count = n;
		}
		
		/**
		 * Add copies to this Entry.
		 * 
		 * @param n Copies to add
		 * @return The new number of copies in this Entry.
		 */
		public int add(int n)
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
		public int remove(int n)
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
	 * Create a new, empty Deck with no categories.
	 */
	public Deck()
	{
		masterList = new ArrayList<Entry>();
		categories = new HashMap<String, Category>();
		total = 0;
		land = 0;
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
			add(c, 1);
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
	 * @return <code>true</code> if the Deck changed as a result, and
	 * <code>false</code> otherwise, which is when the number to add
	 * is less than 1.
	 */
	public boolean add(Card c, int n)
	{
		if (n < 1)
			return false;
		else
		{
			Entry e = getEntry(c);
			if (e == null)
				masterList.add(new Entry(c, n));
			else
				e.add(n);
			for (Category category: categories.values())
				if (category.includes(c) && !category.contains(c))
					category.filtrate.add(c);
			total += n;
			if (c.typeContains("land"))
				land += n;
			return true;
		}
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
	public boolean addAll(Collection<? extends Card> coll, int n)
	{
		boolean changed = false;
		for (Card c: coll)
			changed |= add(c, n);
		return changed;
	}
	
	/**
	 * Add a single copy of a Card to this Deck.
	 * 
	 * @param c Card to add
	 * @return <code>true</code>, since the Deck will always change as
	 * a result.
	 */
	public boolean add(Card c)
	{
		return add(c, 1);
	}
	
	/**
	 * Remove some number of copies of the given Card from this Deck.  If that
	 * number is less than one, no changes are made.
	 * 
	 * @param c Card to remove
	 * @param n Number of copies to remove
	 * @return <code>true</code> if this Deck changed as a result, and
	 * <code>false</code> otherwise.
	 */
	public boolean remove(Card c, int n)
	{
		if (n < 1)
			return false;
		else
		{
			Entry e = getEntry(c);
			if (e == null)
				return false;
			else
			{
				total -= e.count;
				if (c.typeContains("land"))
					land -= e.count;
				e.remove(n);
				if (e.count == 0)
				{
					masterList.remove(e);
					for (Category category: categories.values())
					{
						category.filtrate.remove(c);
						category.whitelist.remove(c);
						category.blacklist.remove(c);
					}
				}
				total += e.count;
				if (c.typeContains("land"))
					land += e.count;
				return true;
			}
		}
	}
	
	/**
	 * Remove one copy of the given Card from this Deck.
	 * 
	 * @param c Card to remove
	 * @return <code>true</code> if this Deck changed as a result, and
	 * <code>false</code> otherwise.
	 */
	public boolean remove(Card c)
	{
		return remove(c, 1);
	}
	
	/**
	 * @param index Index to look at in the list
	 * @return The Card at the given index.
	 */
	public Card get(int index)
	{
		return masterList.get(index).card;
	}
	
	/**
	 * Set the number of copies of the Card at the given index to be the given value.
	 * 
	 * @param index Index to find the Card at
	 * @param n Number of copies to change to
	 * @return <code>true</code> if the Card is in the Deck and if the number of copies
	 * was changed, and <code>false</code> otherwise.
	 */
	public boolean setCount(int index, int n)
	{
		Entry e = masterList.get(index);
		if (e.count == n)
			return false;
		else
		{
			total += n - e.count;
			if (e.card.typeContains("land"))
				land += n - e.count;
			e.count = n;
			if (e.count == 0)
				remove(e.card, Integer.MAX_VALUE);
			return true;
		}
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
	public boolean setCount(Card c, int n)
	{
		Entry e = getEntry(c);
		if (e == null)
			return add(c, n);
		else if (e.count == n)
			return false;
		else
		{
			total += n - e.count;
			if (e.card.typeContains("land"))
				land += n - e.count;
			e.count = n;
			if (e.count == 0)
				remove(c, Integer.MAX_VALUE);
			return true;
		}
	}
	
	/**
	 * @param o Object to look for
	 * @return Index of that Object in the master list.
	 */
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
	public int count(int index)
	{
		return masterList.get(index).count;
	}
	
	/**
	 * @param o Object to look for
	 * @return <code>true</code> if this Deck contains one or more copies
	 * of the given Object, and <code>false</code> otherwise.
	 */
	public boolean contains(Object o)
	{
		return o instanceof Card && getEntry((Card)o) != null;
	}
	
	/**
	 * @param name Name of the Category to look for
	 * @return The Category with the given name, or <code>null</code> if no
	 * such category exists.
	 */
	public Category getCategory(String name)
	{
		return categories.get(name);
	}
	
	/**
	 * Add a new Category.  If there is already a Category with the same name,
	 * instead do nothing.
	 * 
	 * @param name Name of the new Category
	 * @param repr String representation of the new Category
	 * @param filter Filter for the Category's view of the Deck list
	 * @return The new Category that was created, or the existing Category
	 * if there already was one with that name.
	 */
	public Category addCategory(String name, String repr, Predicate<Card> filter)
	{
		if (!categories.containsKey(name))
		{
			Category c = new Category(name, repr, filter);
			categories.put(name, c);
			return c;
		}
		else
			return categories.get(name);
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
		if (categories.containsKey(name))
			return categories.remove(name) != null;
		else
			return false;
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
	 * Reset this Deck to being empty and having no categories.
	 */
	public void clear()
	{
		masterList.clear();
		categories.clear();
		total = 0;
	}
	
	/**
	 * @return The number of unique Cards in this Deck.
	 */
	public int size()
	{
		return masterList.size();
	}
	
	/**
	 * @return The number of Cards in this Deck.
	 */
	public int total()
	{
		return total;
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
				wr.println(e.card.ID + "\t" + e.count);
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
		return masterList.stream().map((e) -> e.card).iterator();
	}
	
	/**
	 * This class represents a category of a deck.  It looks like a deck since it
	 * contains a list of cards and can report how many copies of them there are, 
	 * so it extends Deck.  If a card is added or removed using the add and remove
	 * methods, the master list will be updated to reflect this only if the card
	 * passes through the Category's filter.
	 * 
	 * manually
	 * 
	 * @author Alec Roelke
	 */
	public class Category extends Deck
	{
		/**
		 * Name of this Category.
		 */
		private String name;
		/**
		 * String representation of this Category.
		 * @see gui.filter.editor.FilterEditorPanel#setContents(String)
		 */
		private String repr;
		/**
		 * Filter of this Category.
		 */
		private Predicate<Card> filter;
		/**
		 * List representing the filtered view of the master list.
		 */
		private List<Card> filtrate;
		/**
		 * Blacklist of cards that should not be included even if they
		 * pass through the filter.
		 */
		private Set<Card> blacklist;
		/**
		 * Whitelist of cards that should be included even if they do not
		 * pass through the filter.
		 */
		private Set<Card> whitelist;
		
		/**
		 * Create a new Category.
		 * 
		 * @param s Name of the new Category
		 * @param f Filter of the new Category
		 */
		private Category(String s, String r, Predicate<Card> f)
		{
			name = s;
			repr = r;
			filter = f;
			filtrate = masterList.stream().map((e) -> e.card).filter(filter).collect(Collectors.toList());
			blacklist = new HashSet<Card>();
			whitelist = new HashSet<Card>();
		}
		
		/**
		 * @return This Category's name.
		 */
		public String name()
		{
			return name;
		}
		
		/**
		 * @return This Category's whitelist.
		 */
		public Set<Card> whitelist()
		{
			return whitelist;
		}
		
		/**
		 * @return This Category's blacklist
		 */
		public Set<Card> blacklist()
		{
			return blacklist;
		}
		
		/**
		 * @return This Category's String representation.
		 * @see gui.filter.editor.FilterEditorPanel#setContents(String)
		 * @see gui.editor.CategoryDialog#setContents(String)
		 */
		@Override
		public String toString()
		{
			StringJoiner white = new StringJoiner(":", String.valueOf(FilterGroupPanel.BEGIN_GROUP), String.valueOf(FilterGroupPanel.END_GROUP));
			for (Card c: whitelist)
				white.add(c.ID);
			StringJoiner black = new StringJoiner(":", String.valueOf(FilterGroupPanel.BEGIN_GROUP), String.valueOf(FilterGroupPanel.END_GROUP));
			for (Card c: blacklist)
				black.add(c.ID);
			return name + " " + white.toString() + " " + black.toString() + " " + repr;
		}
		
		/**
		 * @return This Category's filter.
		 */
		public Predicate<Card> filter()
		{
			return filter;
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
		public boolean add(Card c, int n)
		{
			if (includes(c))
				return Deck.this.add(c, n);
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
		public boolean add(Card c)
		{
			return add(c, 1);
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
		public boolean addAll(Collection<? extends Card> coll, int n)
		{
			boolean changed = false;
			for (Card c: coll)
				changed |= add(c, n);
			return changed;
		}
		
		/**
		 * Remove some number of copies of a Card from this Category if it passes
		 * through this Category's filter.
		 * 
		 * @param c Card to add
		 * @param n Number of copies to remove
		 * @return <code>true</code> if the Deck was changed as a result, and
		 * <code>false</code> otherwise.
		 */
		@Override
		public boolean remove(Card c, int n)
		{
			if (includes(c))
				return Deck.this.remove(c, n);
			else
				return false;
		}
		
		/**
		 * Remove one copy of a Card from this Category if it passes through
		 * this Category's filter.
		 * 
		 * @param c Card to add
		 * @return <code>true</code> if the Deck was changed as a result, and
		 * <code>false</code> otherwise.
		 */
		@Override
		public boolean remove(Card c)
		{
			return remove(c, 1);
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
		 * Include the given Card in this Category.  This will remove it from
		 * the blacklist if it is in the blacklist.  No copies of the Card will
		 * be added to the deck.
		 * 
		 * @param c Card to include in this Category
		 * @return <code>true</code> if this Category changed as a result of the
		 * inclusion, and <code>false</code> otherwise.
		 */
		public boolean include(Card c)
		{
			boolean changed = blacklist.remove(c);
			if (!filter.test(c))
				changed |= whitelist.add(c);
			if (!contains(c))
				changed |= filtrate.add(c);
			return changed;
		}
		
		/**
		 * Exclude the given Card from this Category.  This will remove it from
		 * the whitelist if it is in the whitelist.  No copies of the Card will be
		 * removed from the deck.
		 * 
		 * @param c Card to exclude from this Category
		 * @return <code>true</code> if this Category was changed as a result of the
		 * exclusion, and <code>false</code> otherwise.
		 */
		public boolean exclude(Card c)
		{
			boolean changed = whitelist.remove(c);
			if (filter.test(c))
				changed |= blacklist.add(c);
			if (contains(c))
				changed |= filtrate.remove(c);
			return changed;
		}
		
		/**
		 * @param c Card to test
		 * @return <code>true</code> if the given Card can belong to this Category and
		 * <code>false</code> otherwise.
		 */
		public boolean includes(Card c)
		{
			return !blacklist.contains(c) && (filter.test(c) || whitelist.contains(c));
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
			return count(get(index));
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
			return total = filtrate.stream().map(Deck.this::getEntry).mapToInt((e) -> e.count).sum();
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
		 * Change the properties of this Category.
		 * 
		 * @param n New name for this Category (names of categories should be unique!)
		 * @param r New String representation of this Category
		 * @param f New filter for this Category
		 * @return <code>true</code> if the category was successfully changed, which
		 * happens if its new name isn't the name of another category or if the name
		 * isn't changed, and <code>false</code> otherwise.
		 */
		public boolean edit(String n, String r, Predicate<Card> f)
		{
			if (n.equals(name) || !categories.containsKey(n))
			{
				if (!n.equals(name))
				{
					categories.remove(name);
					name = n;
					categories.put(name, this);
				}
				repr = r;
				filter = f;
				filtrate = masterList.stream().map((e) -> e.card).filter(this::includes).collect(Collectors.toList());
				return true;
			}
			else
				return false;
		}
		
		@Override
		public Category getCategory(String n)
		{
			throw new UnsupportedOperationException("Categories can't have categories");
		}
		
		@Override
		public Category addCategory(String n, String r, Predicate<Card> f)
		{
			throw new UnsupportedOperationException("Categories can't have categories");
		}
		
		@Override
		public boolean removeCategory(String n)
		{
			throw new UnsupportedOperationException("Categories can't have categories");
		}
		
		@Override
		public boolean containsCategory(String n)
		{
			throw new UnsupportedOperationException("Categories can't have categories");
		}
	}
}
