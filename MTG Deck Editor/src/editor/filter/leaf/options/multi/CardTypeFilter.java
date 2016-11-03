package editor.filter.leaf.options.multi;

import java.util.Arrays;
import java.util.HashSet;

import editor.database.card.Card;
import editor.filter.Filter;
import editor.filter.FilterFactory;
import editor.util.Containment;

/**
 * This class represents a filter that groups cards by card type.
 *
 * @author Alec Roelke
 */
public class CardTypeFilter extends MultiOptionsFilter<String>
{
	
	/**
	 * List of all types that appear on cards (including ones that appear on Unglued and Unhinged cards, whose
	 * type lines were not updated for the most modern templating).
	 */
	public static String[] typeList = {};

	/**
	 * Create a new CardTypeFilter.
	 */
	public CardTypeFilter()
	{
		super(FilterFactory.TYPE, Card::types);
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
		String content = checkContents(s, FilterFactory.TYPE);
		int delim = content.indexOf('{');
		contain = Containment.fromString(content.substring(0, delim));
		if (content.charAt(delim + 1) != '}')
			selected.addAll(Arrays.asList(content.substring(delim + 1, content.length() - 1).split(",")));
	}
	
	/**
	 * @return A new CardTypeFilter that is a copy of this CardTypeFilter.
	 */
	@Override
	public Filter copy()
	{
		CardTypeFilter filter = (CardTypeFilter)FilterFactory.createFilter(FilterFactory.TYPE);
		filter.contain = contain;
		filter.selected = new HashSet<String>(selected);
		return filter;
	}

	@Override
	public String convertFromString(String str)
	{
		return str;
	}
}
