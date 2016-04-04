package editor.filter.leaf.options.multi;

import java.util.Arrays;
import java.util.HashSet;

import editor.database.Card;
import editor.filter.Filter;
import editor.filter.FilterType;
import editor.util.Containment;

/**
 * This class represents a filter that groups cards by card type.
 *
 * @author Alec Roelke
 */
public class CardTypeFilter extends MultiOptionsFilter<String>
{
	
	/**
	 * Create a new CardTypeFilter.
	 */
	public CardTypeFilter()
	{
		super(FilterType.TYPE, Card::types);
	}
	
	/**
	 * Parse a String to determine this CardTypeFilter's containment
	 * and selected card types.
	 * 
	 * @param s String to parse
	 * @see editor.filter.Filter#parse(String)
	 */
	@Override
	public void parse(String s)
	{
		String content = checkContents(s, FilterType.TYPE);
		int delim = content.indexOf('{');
		contain = Containment.get(content.substring(0, delim));
		if (content.charAt(delim + 1) != '}')
			selected.addAll(Arrays.asList(content.substring(delim + 1, content.length() - 1).split(",")));
	}
	
	/**
	 * TODO: Comment this
	 */
	@Override
	public Filter copy()
	{
		CardTypeFilter filter = (CardTypeFilter)FilterType.TYPE.createFilter();
		filter.contain = contain;
		filter.selected = new HashSet<String>(selected);
		return filter;
	}
}
