package editor.filter.leaf.options.single;

import java.util.HashSet;

import editor.database.card.Card;
import editor.database.characteristics.Expansion;
import editor.filter.Filter;
import editor.filter.FilterFactory;
import editor.util.Containment;

/**
 * This class represents a filter that groups cards by expansion.
 * 
 * @author Alec Roelke
 */
public class ExpansionFilter extends SingletonOptionsFilter<Expansion>
{
	/**
	 * Create a new ExpansionFilter.
	 */
	public ExpansionFilter()
	{
		super(FilterFactory.EXPANSION, Card::expansion);
	}

	@Override
	public Expansion convertFromString(String str)
	{
		for (Expansion expansion: Expansion.expansions)
			if (str.equalsIgnoreCase(expansion.name))
				return expansion;
		throw new IllegalArgumentException("Unknown expansion name \"" + str + "\"");
	}
	
	@Override
	public Filter copy()
	{
		ExpansionFilter filter = (ExpansionFilter)FilterFactory.createFilter(FilterFactory.EXPANSION);
		filter.contain = contain;
		filter.selected = new HashSet<Expansion>(selected);
		return filter;
	}

	@Override
	public void parse(String s)
	{
		String content = checkContents(s, FilterFactory.EXPANSION);
		int delim = content.indexOf('{');
		contain = Containment.parseContainment(content.substring(0, delim));
		if (content.charAt(delim + 1) != '}')
			for (String o: content.substring(delim + 1, content.length() - 1).split(","))
				selected.add(convertFromString(o));
	}
}