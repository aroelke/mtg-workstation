package editor.filter.leaf.options.single;

import editor.database.Card;
import editor.database.characteristics.Expansion;
import editor.filter.FilterType;
import editor.util.Containment;

public class ExpansionFilter extends SingletonOptionsFilter<Expansion>
{
	public ExpansionFilter()
	{
		super(FilterType.EXPANSION, Card::expansion);
	}

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
}