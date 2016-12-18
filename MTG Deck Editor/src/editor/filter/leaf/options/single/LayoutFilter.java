package editor.filter.leaf.options.single;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

import editor.database.card.Card;
import editor.database.card.CardLayout;
import editor.filter.Filter;
import editor.filter.FilterFactory;
import editor.util.Containment;

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
		filter.selected = new HashSet<CardLayout>(selected);
		return filter;
	}

	@Override
	public void parse(String s)
	{
		String content = checkContents(s, FilterFactory.RARITY);
		int delim = content.indexOf('{');
		contain = Containment.parseContainment(content.substring(0, delim));
		if (content.charAt(delim + 1) != '}')
			selected = Arrays.stream(content.substring(delim + 1, content.length() - 1).split(",")).map((str) -> CardLayout.valueOf(str.toUpperCase().replaceAll("[^A-Z]", "_"))).collect(Collectors.toSet());
	}
}
