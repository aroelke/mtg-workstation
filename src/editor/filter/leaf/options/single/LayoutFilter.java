package editor.filter.leaf.options.single;

import java.util.HashSet;

import editor.database.card.Card;
import editor.database.card.CardLayout;
import editor.filter.Filter;
import editor.filter.FilterFactory;

/**
 * This class represents a filter that groups cards by layout.
 * 
 * @author Alec Roelke
 */
public class LayoutFilter extends SingletonOptionsFilter<CardLayout>
{
	/**
	 * Create a new LayoutFilter.
	 */
	public LayoutFilter()
	{
		super(FilterFactory.LAYOUT, Card::layout);
	}
	
	@Override
	public CardLayout convertFromString(String str)
	{
		return CardLayout.valueOf(str.replace(' ', '_').toUpperCase());
	}

	@Override
	public Filter copy()
	{
		LayoutFilter filter = (LayoutFilter)FilterFactory.createFilter(FilterFactory.LAYOUT);
		filter.contain = contain;
		filter.selected = new HashSet<>(selected);
		return filter;
	}
}
