package editor.filter.leaf;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import editor.database.Card;
import editor.filter.Filter;
import editor.filter.FilterType;
import editor.util.Containment;

/**
 * This class represents a filter that filters a card by its entire
 * type line.
 * 
 * @author Alec Roelke
 */
public class TypeLineFilter extends FilterLeaf<List<List<String>>>
{
	/**
	 * Containment specification for the terms in the filter's
	 * text.
	 */
	public Containment contain;
	/**
	 * Text containing values to search for in a card's type line.
	 */
	public String line;
	
	/**
	 * Create a new TypeLineFilter.
	 */
	public TypeLineFilter()
	{
		super(FilterType.TYPE_LINE, Card::allTypes);
		contain = Containment.CONTAINS_ANY_OF;
		line = "";
	}

	/**
	 * @param c Card to test
	 * @return <code>true</code> if the given Card's type line matches
	 * the terms in this TypeLineFilter with the given containment, 
	 * and <code>false</code> otherwise.
	 */
	@Override
	public boolean test(Card c)
	{
		return !line.isEmpty()
				&& c.allTypes().stream().anyMatch((f) ->
				contain.test(f.stream().map(String::toLowerCase).collect(Collectors.toList()),
						Arrays.asList(line.toLowerCase().split("\\s"))));
	}

	/**
	 * @return The String representation of this TypeLineFilter's contents, which
	 * is its containment string followed by its text in quotes.
	 * @see FilterLeaf#content()
	 */
	@Override
	public String content()
	{
		return contain.toString() + "\"" + line + "\"";
	}
	
	/**
	 * Parse a String to determine the containment and text of
	 * this TypeLineFilter.
	 * 
	 * @param s String to parse
	 * @see editor.filter.Filter#parse(String)
	 */
	@Override
	public void parse(String s)
	{
		String content = checkContents(s, FilterType.TYPE_LINE);
		int delim = content.indexOf('"');
		contain = Containment.get(content.substring(0, delim));
		line = content.substring(delim + 1, content.lastIndexOf('"'));
	}
	
	/**
	 * TODO: Comment this
	 */
	@Override
	public Filter copy()
	{
		TypeLineFilter filter = (TypeLineFilter)FilterType.TYPE_LINE.createFilter();
		filter.contain = contain;
		filter.line = line;
		return filter;
	}
}
