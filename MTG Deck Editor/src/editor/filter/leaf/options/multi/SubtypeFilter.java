package editor.filter.leaf.options.multi;

import java.util.Arrays;
import java.util.HashSet;

import editor.database.card.Card;
import editor.filter.Filter;
import editor.filter.FilterType;
import editor.util.Containment;

/**
 * This class represents a filter that groups cards by subtype.
 * 
 * @author Alec Roelke
 */
public class SubtypeFilter extends MultiOptionsFilter<String>
{
	/**
	 * Create a new SubtypeFilter.
	 */
	public SubtypeFilter()
	{
		super(FilterType.SUBTYPE, Card::subtypes);
	}
	
	/**
	 * Parse a String to determine this SubtypeFilter's containment
	 * and selected subtypes.
	 * 
	 * @param s String to parse
	 * @see editor.filter.Filter#parse(String)
	 */
	@Override
	public void parse(String s)
	{
		String content = checkContents(s, FilterType.SUBTYPE);
		int delim = content.indexOf('{');
		contain = Containment.get(content.substring(0, delim));
		if (content.charAt(delim + 1) != '}')
			selected.addAll(Arrays.asList(content.substring(delim + 1, content.length() - 1).split(",")));
	}
	
	/**
	 * @return A new SubtypeFilter that is a copy of this one.
	 */
	@Override
	public Filter copy()
	{
		SubtypeFilter filter = (SubtypeFilter)FilterType.SUBTYPE.createFilter();
		filter.contain = contain;
		filter.selected = new HashSet<String>(selected);
		return filter;
	}
}
