package editor.filter;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
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

import editor.database.card.Card;

/**
 * This class represents a group of filters that are ANDed or ORed together.
 * 
 * @author Alec Roelke
 */
public class FilterGroup extends Filter implements Iterable<Filter>
{
	/**
	 * This class represents a method of combining filters to test a card with all of
	 * them collectively.
	 * 
	 * @author Alec Roelke
	 */
	public enum Mode implements BiPredicate<Collection<Filter>, Card>
	{
		/**
		 * All of the filters must pass a card.
		 */
		AND("all of", Stream<Filter>::allMatch),
		/**
		 * None of the filters can pass a card.
		 */
		NOR("none of", Stream<Filter>::noneMatch),
		/**
		 * Any of the filters must pass a card.
		 */
		OR("any of", Stream<Filter>::anyMatch);
		
		/**
		 * Function representing the mode to test a card with a collection of filters.
		 */
		private final BiPredicate<Stream<Filter>, Predicate<? super Filter>> function;
		/**
		 * String representation of this Mode.
		 */
		private final String mode;
		
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
		
		@Override
		public boolean test(Collection<Filter> filters, Card c)
		{
			return function.test(filters.stream(), (f) -> f.test(c));
		}
		
		@Override
		public String toString()
		{
			return mode;
		}
	}
	
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
	 * Create a new FilterGroup using the given list of filters
	 * as its children.
	 * 
	 * @param c filters that will be the new FilterGroup's children
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
	 * @param filter filter to add
	 */
	public void addChild(Filter filter)
	{
		children.add(filter);
		if (filter.parent != null)
			filter.parent.children.remove(filter);
		filter.parent = this;
	}
	
	/**
	 * {@inheritDoc}
	 * The copy will be a deep copy in that its new children will also be copies of
	 * the original's children.
	 */
	@Override
	public Filter copy()
	{
		FilterGroup filter = new FilterGroup();
		for (Filter child: children)
			filter.addChild(child.copy());
		filter.mode = mode;
		return filter;
	}

	@Override
	public boolean equals(Object other)
	{
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (other.getClass() != getClass())
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
	
	@Override
	public int hashCode()
	{
		return Objects.hash(children, mode);
	}

	@Override
	public Iterator<Filter> iterator()
	{
		return children.iterator();
	}
	
	/**
	 * {@inheritDoc}
	 * The String should consist of  beginning and ending markers followed by the mode
	 * of the group, followed by any number of Filters (that can also be groups) that
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
				filter = FilterFactory.createFilter(filterString.substring(1, filterString.indexOf(':')));
			filter.parse(filterString);
			addChild(filter);
		}
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		mode = (Mode)in.readObject();
		int n = in.readInt();
		for (int i = 0; i < n; i++)
			children.add((Filter)in.readObject());
	}
	
	/**
	 * {@inheritDoc}
	 * The String representation of this FilterGroup is the mode's representation
	 * followed by each child's entire representation including beginning and ending
	 * markers.  The outermost markers are omitted.
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

	@Override
	public boolean test(Card c)
	{
		return mode.test(children, c);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeObject(mode);
		out.writeInt(children.size());
		for (Filter child: children)
			out.writeObject(child);
	}
}
