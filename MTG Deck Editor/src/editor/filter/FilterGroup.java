package editor.filter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Pattern;

import editor.database.Card;

/**
 * This class represents a group of Filters that are ANDed or ORed
 * together.
 * 
 * @author Alec Roelke
 */
public class FilterGroup extends Filter implements Iterable<Filter>
{
	/**
	 * Pattern to match for parsing a String to determine a FilterGroup's
	 * properties.
	 */
	private static final Pattern GROUP_PATTERN = Pattern.compile("^\\s*" + Filter.BEGIN_GROUP + "\\s*(?:AND|OR)", Pattern.CASE_INSENSITIVE);
	
	/**
	 * Children of this FilterGroup.
	 */
	private List<Filter> children;
	/**
	 * Combination mode of this FilterGroup.
	 */
	public Mode mode;
	
	/**
	 * Create a new FilterGroup with no children and in AND mode.
	 */
	public FilterGroup()
	{
		super();
		children = new ArrayList<Filter>();
		mode = Mode.AND;
	}
	
	/**
	 * Create a new FilterGroup using the given list of Filters
	 * as its children.
	 * 
	 * @param c Filters that will be the new FilterGroup's children
	 */
	public FilterGroup(Filter... c)
	{
		this();
		for (Filter f: c)
			addChild(f);
	}
	
	/**
	 * Add a new child to this FilterGroup.
	 * 
	 * @param filter Filter to add
	 */
	public void addChild(Filter filter)
	{
		children.add(filter);
		if (filter.parent != null)
			filter.parent.children.remove(filter);
		filter.parent = this;
	}
	
	/**
	 * @param c Card to test
	 * @return <code>true</code> if this FilterGroup's children match
	 * the given Card with the correct mode, and <code>false</code>
	 * otherwise.
	 */
	@Override
	public boolean test(Card c)
	{
		if (children.isEmpty())
			return false;
		else
		{
			switch (mode)
			{
			case AND:
				return children.stream().allMatch((f) -> f.test(c));
			case OR:
				return children.stream().anyMatch((f) -> f.test(c));
			default:
				return false;
			}
		}
	}
	
	/**
	 * @return The String representation of this FilterGroup, which is
	 * the mode's representation followed by each child's entire
	 * representation including beginning and ending markers.  The
	 * outermost markers are omitted.
	 * @see Filter#representation()
	 */
	@Override
	public String representation()
	{
		StringJoiner join = new StringJoiner(" ");
		join.add(mode == Mode.AND ? "AND" : "OR");
		for (Filter filter: children)
			join.add(filter.toString());
		return join.toString();
	}

	/**
	 * Parse a String for a FilterGroup.  The String should consist of 
	 * beginning and ending markers followed by the mode of the group,
	 * followed by any number of Filters (that can also be groups) that
	 * are each surrounded by beginning and ending markers.
	 */
	@Override
	public void parse(String s)
	{
		children.clear();
		String[] contents = s.substring(1, s.length() - 1).split("\\s+", 2);
		mode = Mode.valueOf(contents[0]);
		List<String> filterStrings = new ArrayList<String>();
		int depth = 0;
		StringBuilder str = new StringBuilder();
		for (char c: contents[1].toCharArray())
		{
			switch (c)
			{
			case Filter.BEGIN_GROUP:
				depth++;
				if (depth == 1)
					str = new StringBuilder();
				break;
			case Filter.END_GROUP:
				if (depth == 1)
				{
					str.append(Filter.END_GROUP);
					filterStrings.add(str.toString());
				}
				depth--;
				break;
			default:
				break;
			}
			if (depth > 0)
				str.append(c);
		}
		if (depth != 0)
			throw new IllegalArgumentException("Unclosed " + String.valueOf(Filter.BEGIN_GROUP) + String.valueOf(Filter.END_GROUP) + " detected in string \"" + contents + "\"");
		for (String filterString: filterStrings)
		{
			Filter filter;
			if (GROUP_PATTERN.matcher(filterString).find())
				filter = new FilterGroup();
			else
				filter = FilterType.fromCode(filterString.substring(1, filterString.indexOf(':'))).createFilter();
			filter.parse(filterString);
			addChild(filter);
		}
	}
	
	/**
	 * TODO: Comment this
	 * @return
	 */
	@Override
	public Filter copy()
	{
		FilterGroup filter = new FilterGroup();
		for (Filter child: children)
			filter.addChild(child.copy());
		return filter;
	}
	
	/**
	 * @return An Iterator over this FilterGroup's children.
	 */
	@Override
	public Iterator<Filter> iterator()
	{
		return children.iterator();
	}
	
	/**
	 * This class represents a method of combining Filters:  Either ANDing them
	 * together or ORing them.
	 * 
	 * @author Alec Roelke
	 */
	public enum Mode
	{
		AND("all of"),
		OR("any of");
		
		/**
		 * String representation of this Mode.
		 */
		private final String mode;
		
		/**
		 * Create a new Mode.
		 * 
		 * @param m String representation of the new Mode.
		 */
		private Mode(String m)
		{
			mode = m;
		}
		
		/**
		 * @return The String representation of this Mode.
		 */
		@Override
		public String toString()
		{
			return mode;
		}
	}
}
