package database;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import gui.SettingsDialog;
import gui.filter.FilterGroupPanel;

/**
 * TODO: Comment this class
 * @author Alec
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
	 * @see gui.filter.FilterGroupPanel#setContents(String)
	 */
	private static final Pattern CATEGORY_PATTERN = Pattern.compile(
			"^" + FilterGroupPanel.BEGIN_GROUP + "([^" + FilterGroupPanel.END_GROUP + "]+)" + FilterGroupPanel.END_GROUP		// Name
			+ "\\s*" + FilterGroupPanel.BEGIN_GROUP + "([^" + FilterGroupPanel.END_GROUP + "]*)" + FilterGroupPanel.END_GROUP 	// Whitelist
			+ "\\s*" + FilterGroupPanel.BEGIN_GROUP + "([^" + FilterGroupPanel.END_GROUP + "]*)" + FilterGroupPanel.END_GROUP	// Blacklist
			+ "\\s*" + FilterGroupPanel.BEGIN_GROUP + "(#[0-9A-F-a-f]{6})?" + FilterGroupPanel.END_GROUP						// Color
			+ "\\s*(.*)$");	
	
	public String name;
	public Set<Card> whitelist;
	public Set<Card> blacklist;
	public Color color;
	public Predicate<Card> filter;
	public String filterString;
	
	public CategorySpec(String name, Collection<Card> whitelist, Collection<Card> blacklist, Color color, Predicate<Card> filter, String filterString)
	{
		this.name = name;
		this.whitelist = new HashSet<Card>(whitelist);
		this.blacklist = new HashSet<Card>(blacklist);
		this.color = color;
		this.filter = filter;
		this.filterString = filterString;
	}
	
	public CategorySpec(String name, Color color, Predicate<Card> filter, String filterString)
	{
		this(name, new HashSet<Card>(), new HashSet<Card>(), color, filter, filterString);
	}
	
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
			filterString = m.group(5);
			FilterGroupPanel panel = new FilterGroupPanel();
			panel.setContents(filterString);
			filter = panel.filter();
		}
		else
			throw new IllegalArgumentException("Illegal category string " + pattern);
	}
	
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
			filterString = m.group(5);
			FilterGroupPanel panel = new FilterGroupPanel();
			panel.setContents(filterString);
			filter = panel.filter();
		}
		else
			throw new IllegalArgumentException("Illegal category string " + pattern);
	}
	
	public CategorySpec(CategorySpec original)
	{
		name = original.name;
		whitelist = new HashSet<Card>(original.whitelist);
		blacklist = new HashSet<Card>(original.blacklist);
		color = original.color;
		filterString = original.filterString;
	}
	
	/**
	 * @return This Category's String representation.
	 * @see gui.filter.editor.FilterEditorPanel#setContents(String)
	 * @see gui.editor.CategoryDialog#setContents(String)
	 */
	@Override
	public String toString()
	{
		StringJoiner white = new StringJoiner(EXCEPTION_SEPARATOR, String.valueOf(FilterGroupPanel.BEGIN_GROUP), String.valueOf(FilterGroupPanel.END_GROUP));
		for (Card c: whitelist)
			white.add(c.id());
		StringJoiner black = new StringJoiner(EXCEPTION_SEPARATOR, String.valueOf(FilterGroupPanel.BEGIN_GROUP), String.valueOf(FilterGroupPanel.END_GROUP));
		for (Card c: blacklist)
			black.add(c.id());
		return FilterGroupPanel.BEGIN_GROUP + name + FilterGroupPanel.END_GROUP
				+ " " + white.toString()
				+ " " + black.toString()
				+ " " + FilterGroupPanel.BEGIN_GROUP + SettingsDialog.colorToString(color, 3) + FilterGroupPanel.END_GROUP
				+ " " + filterString;
	}
}
