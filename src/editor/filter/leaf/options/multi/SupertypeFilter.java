package editor.filter.leaf.options.multi;

import java.util.HashSet;

import editor.database.card.Card;
import editor.filter.Filter;
import editor.filter.FilterFactory;

/**
 * This class represents a filter that groups cards by supertype.
 * 
 * @author Alec Roelke
 */
public class SupertypeFilter extends MultiOptionsFilter<String>
{
	/**
	 * List of all supertypes that appear on cards.
	 */
	public static String[] supertypeList = {};

	/**
	 * Create a new SupertypeFilter.
	 */
	public SupertypeFilter()
	{
		super(FilterFactory.SUPERTYPE, Card::supertypes);
	}
	
	@Override
	public String convertFromString(String str)
	{
		return str;
	}

	@Override
	public Filter copy()
	{
		SupertypeFilter filter = (SupertypeFilter)FilterFactory.createFilter(FilterFactory.SUPERTYPE);
		filter.contain = contain;
		filter.selected = new HashSet<String>(selected);
		return filter;
	}
}
