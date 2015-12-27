package editor.filter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Pattern;

import editor.database.Card;

/**
 * TODO: Comment this class
 * @author Alec Roelke
 */
public class FilterGroup extends Filter implements Iterable<Filter>
{
	private static final Pattern GROUP_PATTERN = Pattern.compile("^\\s*" + Filter.BEGIN_GROUP + "\\s*(?:AND|OR)", Pattern.CASE_INSENSITIVE);
	
	private List<Filter> children;
	public Mode mode;
	
	public FilterGroup()
	{
		super();
		children = new ArrayList<Filter>();
		mode = Mode.AND;
	}
	
	public FilterGroup(String s)
	{
		this();
		parse(s);
	}
	
	public void addChild(Filter filter)
	{
		children.add(filter);
		if (filter.parent != null)
			filter.parent.children.remove(filter);
		filter.parent = this;
	}
	
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
	
	@Override
	public String representation()
	{
		StringJoiner join = new StringJoiner(" ");
		join.add(mode == Mode.AND ? "AND" : "OR");
		for (Filter filter: children)
			join.add(filter.toString());
		return join.toString();
	}

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
				filter = new FilterGroup(filterString);
			else
			{
				try
				{
					filter = FilterType.fromCode(filterString.substring(1, filterString.indexOf(':'))).createFilter(filterString);
				}
				catch (InstantiationException e)
				{
					// TODO Auto-generated catch block
					filter = null;
					e.printStackTrace();
				}
			}
			addChild(filter);
		}
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

	@Override
	public Iterator<Filter> iterator()
	{
		return children.iterator();
	}
}
