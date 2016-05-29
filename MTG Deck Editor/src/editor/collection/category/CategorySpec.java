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
	 * Collection of listeners listening for changes in this
	 * CategorySpec.
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
	 * @param original Original CategorySpec to copy
	 */
	public CategorySpec(CategorySpec original)
	{
		name = original.name;
		whitelist = new HashSet<Card>(original.whitelist);
		blacklist = new HashSet<Card>(original.blacklist);
		color = original.color;
		filter = original.filter.copy();
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
	 * @return The name of the category this CategorySpec represents.
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Change the name of the category and update any listeners of this event.
	 * 
	 * @param n New name for the category
	 */
	public void setName(String n)
	{
		if (!name.equals(n))
		{
			Event e = new Event().changeName(name);
			
			name = n;
			
			for (CategoryListener listener: listeners)
				listener.categoryChanged(e);
		}
	}
	
	/**
	 * @return The set of Cards that should be included in the category
	 * even if they don't pass through the filter.
	 */
	public Set<Card> getWhitelist()
	{
		return new HashSet<Card>(whitelist);
	}
	
	/**
	 * Include a Card in the category, even if it doesn't pass through
	 * the filter.
	 * 
	 * @param c Card to include
	 * @return <code>true</code> if the Card was successfully included (either it
	 * was added to the whitelist or removed from the blacklist), and
	 * <code>false</code> otherwise. 
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
			Event e = new Event().changeWhitelist(oldWhitelist).changeBlacklist(oldBlacklist);
			for (CategoryListener listener: listeners)
				listener.categoryChanged(e);
		}
		
		return oldWhitelist != null || oldBlacklist != null;
	}
	
	/**
	 * @return The set of Cards that should not be included in the category,
	 * even if they pass through the filter.
	 */
	public Set<Card> getBlacklist()
	{
		return new HashSet<Card>(blacklist);
	}
	
	/**
	 * Exclude a Card from the category, even if it passes through
	 * the filter.
	 * 
	 * @param c Card to exclude
	 * @return <code>true</code> if the card was successfully excluded (it
	 * was added to the blacklist or removed from the whitelist), and
	 * <code>false</code> otherwise.
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
			Event e = new Event().changeWhitelist(oldWhitelist).changeBlacklist(oldBlacklist);
			for (CategoryListener listener: listeners)
				listener.categoryChanged(e);
		}
		
		return oldWhitelist != null || oldBlacklist != null;
	}
	
	/**
	 * @return The Color of the category.
	 */
	public Color getColor()
	{
		return color;
	}
	
	/**
	 * Set the Color of the category, and alert any listeners of this event.
	 * 
	 * @param c New Color for the category
	 */
	public void setColor(Color c)
	{
		if (!color.equals(c))
		{
			Event e = new Event().changeColor(color);
			
			color = c;
			
			for (CategoryListener listener: listeners)
				listener.categoryChanged(e);
		}
	}
	
	/**
	 * @return The Filter of the category for automatically including Cards.
	 */
	public Filter getFilter()
	{
		return filter;
	}
	
	/**
	 * Change the Filter of the category so a new set of Cards is
	 * automatically included, and alert any listeners of this event.
	 * 
	 * @param f New Filter for the category
	 */
	public void setFilter(Filter f)
	{
		if (!filter.equals(f))
		{
			Event e = new Event().changeFilter(filter);
			
			filter = f;
			
			for (CategoryListener listener: listeners)
				listener.categoryChanged(e);
		}
	}
	
	/**
	 * Copy the name, whitelist, blacklist, Color, and Filter from the given
	 * CategorySpec, discarding those values from this one, alerting any
	 * listeners of this event.
	 * 
	 * @param other CategorySpec to copy
	 * @return <code>true</code> if any changes were made to this CategorySpec,
	 * and <code>false</code> otherwise.
	 */
	public boolean copy(CategorySpec other)
	{
		Event e = new Event()
				.changeName(name.equals(other.name) ? null : name)
				.changeWhitelist(whitelist.equals(other.whitelist) ? null : new HashSet<Card>(whitelist))
				.changeBlacklist(blacklist.equals(other.blacklist) ? null : new HashSet<Card>(blacklist))
				.changeColor(color.equals(other.color) ? null : color)
				.changeFilter(filter.equals(other.filter) ? null : filter);
		
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
	 * Add a new listener for changes in this CategorySpec.
	 * 
	 * @param listener New listener to add
	 */
	public void addCategoryListener(CategoryListener listener)
	{
		listeners.add(listener);
	}
	
	/**
	 * Remove a CategoryListener from this CategorySpec's list of
	 * listeners.
	 * 
	 * @param listener Listener to remove
	 * @return <code>true</code> if the listener was successfully
	 * removed from the list, and <code>false</code> otherwise.
	 */
	public boolean removeCategoryListener(CategoryListener listener)
	{
		return listeners.remove(listener);
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
	 * @return A String representation of this CategorySpec, with the whitelist
	 * and blacklist empty.  Used for creating preset categories, as they should
	 * not have exclusions or inclusions.
	 */
	public String toListlessString()
	{
		return Filter.BEGIN_GROUP + name + Filter.END_GROUP
				+ " " + Filter.BEGIN_GROUP + Filter.END_GROUP
				+ " " + Filter.BEGIN_GROUP + Filter.END_GROUP
				+ " " + Filter.BEGIN_GROUP + SettingsDialog.colorToString(color, 3) + Filter.END_GROUP
				+ " " + filter.toString();
	}
	
	/**
	 * This class represents an event that changes a CategorySpec.  It can
	 * tell which CategorySpec changed, and which of its parameters changed
	 * as a result of the event.  Use the *changed methods to tell if a
	 * parameter changed.  If a parameter did not change and its old or
	 * new value is requested from this CategoryEvent, an IllegalStateException
	 * will be thrown.
	 * 
	 * @author Alec Roelke
	 */
	public class Event
	{
		/**
		 * The name of the CategorySpec before it was changed.
		 */
		private String oldName;
		/**
		 * The whitelist of the CategorySpec before it was changed.
		 */
		private Set<Card> oldWhitelist;
		/**
		 * The blacklist of the CategorySpec before it was changed.
		 */
		private Set<Card> oldBlacklist;
		/**
		 * The Color of the CategorySpec before it was changed.
		 */
		private Color oldColor;
		/**
		 * The Filter of the CategorySpec before it was changed.
		 */
		private Filter oldFilter;
		
		/**
		 * Create a new Event showing no changes.
		 */
		public Event()
		{
			oldName = null;
			oldWhitelist = null;
			oldBlacklist = null;
			oldColor = null;
			oldFilter = null;
		}
		
		/**
		 * Indicate that the name of the category changed.
		 * 
		 * @param old Old name of the category
		 * @return Event representing the change.
		 */
		private Event changeName(String old)
		{
			oldName = old;
			return this;
		}
		
		/**
		 * Indicate that the whitelist of the category changed.
		 * 
		 * @param old Old whitelist of the category
		 * @return Event representing the change.
		 */
		private Event changeWhitelist(Set<Card> old)
		{
			oldWhitelist = old;
			return this;
		}
		
		/**
		 * Indicate that the blacklist of the category changed.
		 * 
		 * @param old Old blacklist of the category
		 * @return Event representing the change.
		 */
		private Event changeBlacklist(Set<Card> old)
		{
			oldBlacklist = old;
			return this;
		}
		
		/**
		 * Indicate that the color of the category changed.
		 * 
		 * @param old Old color of the category
		 * @return Event representing the change.
		 */
		private Event changeColor(Color old)
		{
			oldColor = old;
			return this;
		}
		
		/**
		 * Indicate that the filter of the category changed.
		 * 
		 * @param old Old filter of the category
		 * @return Event representing the change.
		 */
		private Event changeFilter(Filter old)
		{
			oldFilter = old;
			return this;
		}
		
		/**
		 * @return The CategorySpec that generated this CategoryEvent.
		 */
		public CategorySpec getSource()
		{
			return CategorySpec.this;
		}
		
		/**
		 * @return <code>true</code> if the CategorySpec's name changed,
		 * and <code>false</code> otherwise.
		 */
		public boolean nameChanged()
		{
			return oldName != null;
		}
		
		/**
		 * @return The old name of the CategorySpec before this CategoryEvent
		 * was generated.
		 * @throws IllegalStateException If the CategorySpec's name has not changed.
		 */
		public String oldName()
		{
			if (nameChanged())
				return oldName;
			else
				throw new IllegalStateException("Name of the category has not changed.");
		}
		
		/**
		 * @return The new name of the CategorySpec as a result of the event.
		 * @throws IllegalStateException If the CategorySpec's name has not changed.
		 */
		public String newName()
		{
			if (nameChanged())
				return getSource().getName();
			else
				throw new IllegalStateException("Name of the category has not changed.");
		}
		
		/**
		 * @return <code>true</code> if the CategorySpec's whitelist changed as a
		 * result of the event that generated this CategoryEvent.
		 */
		public boolean whitelistChanged()
		{
			return oldWhitelist != null;
		}
		
		/**
		 * @return The CategorySpec's whitelist before the event.
		 * @throws IllegalStateException if the CategorySpec's whitelist did not
		 * change.
		 */
		public Set<Card> oldWhitelist()
		{
			if (whitelistChanged())
				return oldWhitelist;
			else
				throw new IllegalStateException("Whitelist of the category has not changed.");
		}
		
		/**
		 * @return <code>true</code> if the event that generated this CategoryEvent
		 * changed the CategorySpec's blacklist.
		 */
		public boolean blacklistChanged()
		{
			return oldBlacklist != null;
		}
		
		/**
		 * @return The CategorySpec's blacklist as it was before the event.
		 * @throws IllegalStateException If the CategorySpec's blacklist did not
		 * change.
		 */
		public Set<Card> oldBlacklist()
		{
			if (blacklistChanged())
				return oldBlacklist;
			else
				throw new IllegalStateException("Blacklist of the category has not changed.");
		}
		
		/**
		 * @return <code>true</code> if the event that generated this CategoryEvent
		 * changed the CategorySpec's Color, and <code>false</code> otherwise.
		 */
		public boolean colorChanged()
		{
			return oldColor != null;
		}
		
		/**
		 * @return The CategorySpec's Color as it was before the event.
		 * @throws IllegalStateException if the CategorySpec's color did not
		 * change.
		 */
		public Color oldColor()
		{
			if (colorChanged())
				return oldColor;
			else
				throw new IllegalStateException("Color of the category has not changed.");
		}
		
		/**
		 * @return <code>true</code> if the CategorySpec's Filter changed as a result
		 * of the event that generated this CategoryEvent.
		 */
		public boolean filterChanged()
		{
			return oldFilter != null;
		}
		
		/**
		 * @return The CategorySpec's Filter as it was before the event.
		 * @throws IllegalStateException If the CategorySpec's Filter did not
		 * change.
		 */
		public Filter oldFilter()
		{
			if (filterChanged())
				return oldFilter;
			else
				throw new IllegalStateException("Filter of the category has not changed.");
		}
	}
}
