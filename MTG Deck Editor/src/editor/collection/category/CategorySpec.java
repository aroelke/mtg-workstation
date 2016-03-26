package editor.collection.category;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import editor.collection.Inventory;
import editor.database.Card;
import editor.filter.Filter;
import editor.filter.FilterGroup;
import editor.gui.SettingsDialog;

/**
 * This class represents a set of specifications for a category.  Those specifications are its name,
 * the lists of Cards to include or exclude regardless of filter, its color, its filter, and its String
 * representation.
 * 
 * @author Alec Roelke
 */
public class CategorySpec
{
	/**
	 * List separator for UIDs of cards in the String representation of a whitelist or a blacklist.
	 */
	private static final String EXCEPTION_SEPARATOR = ":";
	/**
	 * Regex pattern for matching category strings and extracting their contents.  The first group
	 * will be the category's name, the second group will be the UIDs of the cards in its whitelist,
	 * the third group will the UIDs of the cards in its blacklist, the fourth group will be its color,
	 * and the fifth group will be its filter's String representation.  The first four groups will
	 * not include the group enclosing characters, but the fifth will.  The first through third groups
	 * will be empty strings if they are empty, but the fourth will be null.  The first and fifth groups
	 * should never be empty.
	 * @see editor.gui.filter.original.FilterGroupPanel#setContents(String)
	 */
	private static final Pattern CATEGORY_PATTERN = Pattern.compile(
			"^" + Filter.BEGIN_GROUP + "([^" + Filter.END_GROUP + "]+)" + Filter.END_GROUP		// Name
			+ "\\s*" + Filter.BEGIN_GROUP + "([^" + Filter.END_GROUP + "]*)" + Filter.END_GROUP 	// Whitelist
			+ "\\s*" + Filter.BEGIN_GROUP + "([^" + Filter.END_GROUP + "]*)" + Filter.END_GROUP	// Blacklist
			+ "\\s*" + Filter.BEGIN_GROUP + "(#[0-9A-F-a-f]{6})?" + Filter.END_GROUP						// Color
			+ "\\s*(.*)$");	
	
	/**
	 * Name of the category.
	 */
	private String name;
	/**
	 * List of cards to include in the category regardless of filter.
	 */
	private Set<Card> whitelist;
	/**
	 * List of cards to exclude from the category regardless of filter.
	 */
	private Set<Card> blacklist;
	/**
	 * Color of the category.
	 */
	private Color color;
	/**
	 * Filter of the category.
	 */
	private Filter filter;
	/**
	 * TODO: Comment this
	 */
	private Collection<CategoryListener> listeners;
	
	/**
	 * Create a new CategorySpec with the given specifications.
	 * 
	 * @param name Name of the new spec
	 * @param whitelist Whitelist of the new spec
	 * @param blacklist Blacklist of the new spec
	 * @param color Color of the new spec
	 * @param filter Filter of the new spec
	 */
	public CategorySpec(String name, Collection<Card> whitelist, Collection<Card> blacklist, Color color, Filter filter)
	{
		this.name = name;
		this.whitelist = new HashSet<Card>(whitelist);
		this.blacklist = new HashSet<Card>(blacklist);
		this.color = color;
		this.filter = filter;
		listeners = new HashSet<CategoryListener>();
	}
	
	/**
	 * Create a new CategorySpec with the given specifications and an empty white- and
	 * blacklist.
	 * 
	 * @param name Name of the new spec
	 * @param color Color of the new spec
	 * @param filter Filter of the new spec
	 */
	public CategorySpec(String name, Color color, Filter filter)
	{
		this(name, new HashSet<Card>(), new HashSet<Card>(), color, filter);
	}
	
	/**
	 * Create a new CategorySpec from the given category String representation.
	 * 
	 * @param pattern String to parse
	 * @param inventory Inventory containing cards to convert from their IDs.
	 * 
	 * TODO: Remove the inventory argument by making the map from ID onto Card be a global variable (or not)
	 */
	public CategorySpec(String pattern, Inventory inventory)
	{
		Matcher m = CATEGORY_PATTERN.matcher(pattern);
		if (m.matches())
		{
			name = m.group(1);
			if (!m.group(2).isEmpty())
				whitelist = Arrays.stream(m.group(2).split(EXCEPTION_SEPARATOR)).map(inventory::get).collect(Collectors.toSet());
			else
				whitelist = new HashSet<Card>();
			if (!m.group(3).isEmpty())
				blacklist = Arrays.stream(m.group(3).split(EXCEPTION_SEPARATOR)).map(inventory::get).collect(Collectors.toSet());
			else
				blacklist = new HashSet<Card>();
			if (m.group(4) != null)
				color = SettingsDialog.stringToColor(m.group(4));
			else
			{
				Random rand = new Random();
				color = Color.getHSBColor(rand.nextFloat(), rand.nextFloat(), (float)Math.sqrt(rand.nextFloat()));
			}
			filter = new FilterGroup();
			filter.parse(m.group(5));
			listeners = new HashSet<CategoryListener>();
		}
		else
			throw new IllegalArgumentException("Illegal category string " + pattern);
	}
	
	/**
	 * Create a new CategorySpec from the given category String with
	 * nothing in its white- or blacklist.
	 * 
	 * @param pattern String to parse
	 */
	public CategorySpec(String pattern)
	{
		Matcher m = CATEGORY_PATTERN.matcher(pattern);
		if (m.matches())
		{
			name = m.group(1);
			whitelist = new HashSet<Card>();
			blacklist = new HashSet<Card>();
			if (m.group(4) != null)
				color = SettingsDialog.stringToColor(m.group(4));
			else
			{
				Random rand = new Random();
				color = Color.getHSBColor(rand.nextFloat(), rand.nextFloat(), (float)Math.sqrt(rand.nextFloat()));
			}
			filter = new FilterGroup();
			filter.parse(m.group(5));
			listeners = new HashSet<CategoryListener>();
		}
		else
			throw new IllegalArgumentException("Illegal category string " + pattern);
	}
	
	/**
	 * Copy constructor for CategorySpec, except the copy has no listeners.
	 * 
	 * TODO: Make this unnecessary.
	 * 
	 * @param original Original CategorySpec to copy
	 */
	public CategorySpec(CategorySpec original)
	{
		name = original.name;
		whitelist = new HashSet<Card>(original.whitelist);
		blacklist = new HashSet<Card>(original.blacklist);
		color = original.color;
		filter = original.filter; // TODO: Make this copy
		listeners = new HashSet<CategoryListener>();
	}
	
	/**
	 * @param c Card to test for inclusion
	 * @return <code>true</code> if this CategorySpec includes the given Card, and
	 * <code>false</code> otherwise.
	 */
	public boolean includes(Card c)
	{
		return (filter.test(c) || whitelist.contains(c)) && !blacklist.contains(c);
	}
	
	/**
	 * TODO: Comment this
	 * @return
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * TODO: Comment this
	 * @param n
	 */
	public void setName(String n)
	{
		if (!name.equals(n))
		{
			CategoryEvent e = new CategoryEvent(this, name, null, null, null, null);
			
			name = n;
			
			for (CategoryListener listener: listeners)
				listener.categoryChanged(e);
		}
	}
	
	/**
	 * TODO: Comment this
	 * @return
	 */
	public Set<Card> getWhitelist()
	{
		return new HashSet<Card>(whitelist);
	}
	
	/**
	 * TODO: Comment this
	 * @param c
	 */
	public boolean include(Card c)
	{
		Set<Card> oldWhitelist = new HashSet<Card>(whitelist);
		Set<Card> oldBlacklist = new HashSet<Card>(blacklist);
		
		if (!filter.test(c) && !whitelist.add(c))
				oldWhitelist = null;
		if (!blacklist.remove(c))
			oldBlacklist = null;
		
		if (oldWhitelist != null || oldBlacklist != null)
		{
			CategoryEvent e = new CategoryEvent(this, null, oldWhitelist, oldBlacklist, null, null);
			for (CategoryListener listener: listeners)
				listener.categoryChanged(e);
		}
		
		return oldWhitelist != null || oldBlacklist != null;
	}
	
	/**
	 * TODO: Comment this
	 * @return
	 */
	public Set<Card> getBlacklist()
	{
		return new HashSet<Card>(blacklist);
	}
	
	/**
	 * TODO: Comment this
	 * @param c
	 */
	public boolean exclude(Card c)
	{
		Set<Card> oldWhitelist = new HashSet<Card>(whitelist);
		Set<Card> oldBlacklist = new HashSet<Card>(blacklist);
		
		if (!(filter.test(c) && blacklist.add(c)))
			oldBlacklist = null;
		if (!whitelist.remove(c))
			oldWhitelist = null;
		
		if (oldWhitelist != null || oldBlacklist != null)
		{
			CategoryEvent e = new CategoryEvent(this, null, oldWhitelist, oldBlacklist, null, null);
			for (CategoryListener listener: listeners)
				listener.categoryChanged(e);
		}
		
		return oldWhitelist != null || oldBlacklist != null;
	}
	
	/**
	 * TODO: Comment this
	 * @return
	 */
	public Color getColor()
	{
		return color;
	}
	
	/**
	 * TODO: Comment this
	 * @param c
	 */
	public void setColor(Color c)
	{
		if (!color.equals(c))
		{
			CategoryEvent e = new CategoryEvent(this, null, null, null, color, null);
			
			color = c;
			
			for (CategoryListener listener: listeners)
				listener.categoryChanged(e);
		}
	}
	
	/**
	 * TODO: Comment this
	 * @return
	 */
	public Filter getFilter()
	{
		return filter;
	}
	
	/**
	 * TODO: Comment this
	 * @param f
	 */
	public void setFilter(Filter f)
	{
		if (!filter.equals(f))
		{
			CategoryEvent e = new CategoryEvent(this, null, null, null, null, filter);
			
			filter = f;
			
			for (CategoryListener listener: listeners)
				listener.categoryChanged(e);
		}
	}
	
	/**
	 * TODO: Comment this
	 * @param other
	 */
	public boolean copy(CategorySpec other)
	{
		CategoryEvent e = new CategoryEvent(this,
				name.equals(other.name) ? null : name,
				whitelist.equals(other.whitelist) ? null : new HashSet<Card>(whitelist),
				blacklist.equals(other.blacklist) ? null : new HashSet<Card>(blacklist),
				color.equals(other.color) ? null : color,
				filter.equals(other.filter) ? null : filter);
		
		name = other.name;
		whitelist.clear();
		whitelist.addAll(other.whitelist);
		blacklist.clear();
		blacklist.addAll(other.blacklist);
		color = other.color;
		filter = new FilterGroup();
		filter.parse(other.filter.toString());
		
		if (e.nameChanged() || e.whitelistChanged() || e.blacklistChanged() || e.colorChanged() || e.filterChanged())
		for (CategoryListener listener: listeners)
			listener.categoryChanged(e);
		
		return e.nameChanged() || e.whitelistChanged() || e.blacklistChanged() || e.colorChanged() || e.filterChanged();
	}
	
	/**
	 * TODO: Comment this
	 * @param listener
	 */
	public void addCategoryListener(CategoryListener listener)
	{
		listeners.add(listener);
	}
	
	/**
	 * TODO: Comment this
	 * @param listener
	 */
	public void removeCategoryListener(CategoryListener listener)
	{
		listeners.remove(listener);
	}
	
	/**
	 * @return This CategorySpec's String representation.
	 * @see editor.gui.filter.original.editor.FilterEditorPanel#setContents(String)
	 * @see gui.editor.CategoryDialog#setContents(String)
	 */
	@Override
	public String toString()
	{
		StringJoiner white = new StringJoiner(EXCEPTION_SEPARATOR, String.valueOf(Filter.BEGIN_GROUP), String.valueOf(Filter.END_GROUP));
		for (Card c: whitelist)
			white.add(c.id());
		StringJoiner black = new StringJoiner(EXCEPTION_SEPARATOR, String.valueOf(Filter.BEGIN_GROUP), String.valueOf(Filter.END_GROUP));
		for (Card c: blacklist)
			black.add(c.id());
		return Filter.BEGIN_GROUP + name + Filter.END_GROUP
				+ " " + white.toString()
				+ " " + black.toString()
				+ " " + Filter.BEGIN_GROUP + SettingsDialog.colorToString(color, 3) + Filter.END_GROUP
				+ " " + filter.toString();
	}
	
	/**
	 * TODO: Comment this
	 * @return
	 */
	public String toListlessString()
	{
		return Filter.BEGIN_GROUP + name + Filter.END_GROUP
				+ " " + Filter.BEGIN_GROUP + Filter.END_GROUP
				+ " " + Filter.BEGIN_GROUP + Filter.END_GROUP
				+ " " + Filter.BEGIN_GROUP + SettingsDialog.colorToString(color, 3) + Filter.END_GROUP
				+ " " + filter.toString();
	}
}
