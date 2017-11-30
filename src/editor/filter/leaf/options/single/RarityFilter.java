package editor.filter.leaf.options.single;

import java.util.HashSet;

import editor.database.card.Card;
import editor.database.characteristics.Rarity;
import editor.filter.Filter;
import editor.filter.FilterFactory;

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
		super(FilterFactory.RARITY, Card::rarity);
	}

	@Override
	public Filter copy()
	{
		RarityFilter filter = (RarityFilter)FilterFactory.createFilter(FilterFactory.RARITY);
		filter.contain = contain;
		filter.selected = new HashSet<Rarity>(selected);
		return filter;
	}

	@Override
	public Rarity convertFromString(String str)
	{
		return Rarity.parseRarity(str);
	}
}
