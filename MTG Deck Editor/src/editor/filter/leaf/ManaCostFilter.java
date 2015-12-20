package editor.filter.leaf;

import editor.database.Card;
import editor.database.characteristics.ManaCost;
import editor.filter.FilterType;
import editor.util.Containment;

public class ManaCostFilter extends FilterLeaf<ManaCost>
{
	public Containment contain;
	public ManaCost cost;
	
	public ManaCostFilter()
	{
		super(FilterType.MANA_COST, null);
	}
	
	@Override
	public boolean test(Card c)
	{
		switch (contain)
		{
		case CONTAINS_ANY_OF:
			return c.mana().stream().anyMatch((m) -> Containment.CONTAINS_ANY_OF.test(m.symbols(), cost.symbols()));
		case CONTAINS_NONE_OF:
			return c.mana().stream().anyMatch((m) -> Containment.CONTAINS_NONE_OF.test(m.symbols(), cost.symbols()));
		case CONTAINS_ALL_OF:
			return c.mana().stream().anyMatch((m) -> m.isSuperset(cost));
		case CONTAINS_NOT_ALL_OF:
			return c.mana().stream().anyMatch((m) -> !m.isSuperset(cost));
		case CONTAINS_EXACTLY:
			return c.mana().stream().anyMatch((m) -> m.equals(cost));
		case CONTAINS_NOT_EXACTLY:
			return c.mana().stream().anyMatch((m) -> !m.equals(cost));
		default:
			return false;
		}
	}

	@Override
	public String content()
	{
		return contain.toString() + "\"" + cost.toString() + "\"";
	}

	@Override
	public void parse(String s)
	{
		String content = checkContents(s, FilterType.MANA_COST);
		int delim = content.indexOf('"');
		contain = Containment.get(content.substring(0, delim));
		cost = ManaCost.valueOf(content.substring(delim + 1, content.lastIndexOf('"')));
	}
}
