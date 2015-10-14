package database;

import gui.SettingsDialog;
import gui.filter.FilterGroupPanel;

import java.awt.Color;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
	
	public final String name;
	public final Set<String> whitelist;
	public final Set<String> blacklist;
	public final Color color;
	public final String filter;
	
	public CategorySpec(String name, Collection<String> whitelist, Collection<String> blacklist, Color color, String filter)
	{
		this.name = name;
		this.whitelist = whitelist.stream().collect(Collectors.toSet());
		this.blacklist = blacklist.stream().collect(Collectors.toSet());
		this.color = color;
		this.filter = filter;
	}
	
	public CategorySpec(String name, Color color, String filter)
	{
		this(name, new HashSet<String>(), new HashSet<String>(), color, filter);
	}
	
	public CategorySpec(String pattern)
	{
		Matcher m = CATEGORY_PATTERN.matcher(pattern);
		if (m.matches())
		{
			name = m.group(1);
			whitelist = new HashSet<String>();
			if (!m.group(2).isEmpty())
				for (String id: m.group(2).split(EXCEPTION_SEPARATOR))
					whitelist.add(id);
			blacklist = new HashSet<String>();
			if (!m.group(3).isEmpty())
				for (String id: m.group(3).split(EXCEPTION_SEPARATOR))
					blacklist.add(id);
			if (m.group(4) != null)
				color = SettingsDialog.stringToColor(m.group(4));
			else
			{
				Random rand = new Random();
				color = Color.getHSBColor(rand.nextFloat(), rand.nextFloat(), (float)Math.sqrt(rand.nextFloat()));
			}
			filter = m.group(5);
		}
		else
			throw new IllegalArgumentException("Illegal category string " + pattern);
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
		for (String c: whitelist)
			white.add(c);
		StringJoiner black = new StringJoiner(EXCEPTION_SEPARATOR, String.valueOf(FilterGroupPanel.BEGIN_GROUP), String.valueOf(FilterGroupPanel.END_GROUP));
		for (String c: blacklist)
			black.add(c);
		return FilterGroupPanel.BEGIN_GROUP + name + FilterGroupPanel.END_GROUP
				+ " " + white.toString()
				+ " " + black.toString()
				+ " " + FilterGroupPanel.BEGIN_GROUP + SettingsDialog.colorToString(color, 3) + FilterGroupPanel.END_GROUP
				+ " " + filter;
	}
}
