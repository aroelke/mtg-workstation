package editor.filter.leaf.options.single;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

import editor.database.Card;
import editor.database.characteristics.Rarity;
import editor.filter.Filter;
import editor.filter.FilterType;
import editor.util.Containment;

/**
 * This class represents a filter that groups cards by rarity.
 * 
 * @author Alec Roelke
 */
public class RarityFilter extends SingletonOptionsFilter<Rarity>
{
	/**
	 * Create a new RarityFilter.
	 */
	public RarityFilter()
	{
		super(FilterType.RARITY, Card::rarity);
	}
	
	
	/**
	 * Parse a String to determine the containment and rarities of this
	 * RarityFilter.
	 * 
	 * @param s String to parse
	 * @see editor.filter.Filter#parse(String)
	 */
	@Override
	public void parse(String s)
	{
		String content = checkContents(s, FilterType.RARITY);
		int delim = content.indexOf('{');
		contain = Containment.get(content.substring(0, delim));
		if (content.charAt(delim + 1) != '}')
			selected = Arrays.stream(content.substring(delim + 1, content.length() - 1).split(",")).map(Rarity::get).collect(Collectors.toSet());
	}
	
	/**
	 * @return A new RarityFilter that is a copy of this one.
	 */
	@Override
	public Filter copy()
	{
		RarityFilter filter = (RarityFilter)FilterType.RARITY.createFilter();
		filter.contain = contain;
		filter.selected = new HashSet<Rarity>(selected);
		return filter;
	}
}
