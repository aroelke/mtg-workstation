package editor.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
		return mode.test(children, c);
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
		join.add(mode.name());
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
	 * @return A new FilterGroup that is a copy of this one, with copies of all of
	 * its children.
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
	 * @param other Object to compare with
	 * @return <code>true</code> if the other Object is a FilterGroup with exactly the
	 * same children, who are also all equal.
	 */
	@Override
	public boolean equals(Object other)
	{
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (other.getClass() != FilterGroup.class)
			return false;
		FilterGroup o = (FilterGroup)other;
		if (o.mode != mode)
			return false;
		if (children.size() != o.children.size())
			return false;
		List<Filter> otherChildren = new ArrayList<Filter>(o.children);
		for (Filter child: children)
			otherChildren.remove(child);
		if (!otherChildren.isEmpty())
			return false;
		return true;
	}
	
	/**
	 * @return The hash code of this FilterGroup, which is composed of the hash codes
	 * of its children and mode.
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash(children, mode);
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
	 * This class represents a method of combining Filters to test a Card
	 * with all of them collectively.
	 * 
	 * @author Alec Roelke
	 */
	public enum Mode implements BiPredicate<Collection<Filter>, Card>
	{
		AND("all of", Stream<Filter>::allMatch),
		OR("any of", Stream<Filter>::anyMatch),
		NOR("none of", Stream<Filter>::noneMatch);
		
		/**
		 * String representation of this Mode.
		 */
		private final String mode;
		/**
		 * Function representing the mode to test a Card with a Collection of Filters.
		 */
		private final BiPredicate<Stream<Filter>, Predicate<? super Filter>> function;
		
		/**
		 * Create a new Mode.
		 * 
		 * @param m String representation of the new Mode.
		 */
		private Mode(String m, BiPredicate<Stream<Filter>, Predicate<? super Filter>> f)
		{
			mode = m;
			function = f;
		}
		
		/**
		 * @param filters Collection of filters to test
		 * @param c Card to test the filters on
		 * @return <code>true</code> if the card passes through the collection of Filters
		 * with the correct mode, and <code>false</code> otherwise.
		 */
		public boolean test(Collection<Filter> filters, Card c)
		{
			return function.test(filters.stream(), (f) -> f.test(c));
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
