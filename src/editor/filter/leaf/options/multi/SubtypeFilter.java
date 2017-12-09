package editor.filter.leaf.options.multi;

import java.util.HashSet;

import editor.database.card.Card;
import editor.filter.Filter;
import editor.filter.FilterFactory;

/**
 * This class represents a filter that groups cards by subtype.
 * 
 * @author Alec Roelke
 */
public class SubtypeFilter extends MultiOptionsFilter<String>
{
	/**
	 * List of all subtypes that appear on cards.
	 */
	public static String[] subtypeList = {};

	/**
	 * Create a new SubtypeFilter.
	 */
	public SubtypeFilter()
	{
		super(FilterFactory.SUBTYPE, Card::subtypes);
	}
	
	@Override
	public String convertFromString(String str)
	{
		return str;
	}
	
	@Override
	public Filter copy()
	{
		SubtypeFilter filter = (SubtypeFilter)FilterFactory.createFilter(FilterFactory.SUBTYPE);
		filter.contain = contain;
		filter.selected = new HashSet<>(selected);
		return filter;
	}
}
