package editor.filter.leaf.options.single;

import java.util.HashSet;

import editor.database.Card;
import editor.database.characteristics.Expansion;
import editor.filter.Filter;
import editor.filter.FilterType;
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
		super(FilterType.EXPANSION, Card::expansion);
	}

	/**
	 * Parse a String to determine this ExpansionFilter's containment
	 * and expansions.
	 * 
	 * @param s String to parse
	 * @see editor.filter.Filter#parse(String)
	 */
	@Override
	public void parse(String s)
	{
		String content = checkContents(s, FilterType.EXPANSION);
		int delim = content.indexOf('{');
		contain = Containment.get(content.substring(0, delim));
		if (content.charAt(delim + 1) != '}')
			for (String o: content.substring(delim + 1, content.length() - 1).split(","))
				for (Expansion expansion: Expansion.expansions)
					if (o.equals(expansion.name))
						selected.add(expansion);
	}
	
	/**
	 * @return A new ExpansionFilter that is a copy of this ExpansionFilter.
	 */
	@Override
	public Filter copy()
	{
		ExpansionFilter filter = (ExpansionFilter)FilterType.EXPANSION.createFilter();
		filter.contain = contain;
		filter.selected = new HashSet<Expansion>(selected);
		return filter;
	}
}