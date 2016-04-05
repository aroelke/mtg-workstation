package editor.filter.leaf;

import java.util.Objects;

import editor.database.Card;
import editor.database.characteristics.ManaCost;
import editor.filter.Filter;
import editor.filter.FilterType;
import editor.util.Containment;

/**
 * This class represents a filter to group Cards by mana costs.
 * 
 * @author Alec Roelke
 */
public class ManaCostFilter extends FilterLeaf<ManaCost>
{
	/**
	 * Containment for this ManaCostFilter.
	 */
	public Containment contain;
	/**
	 * Mana cost to filter by.
	 */
	public ManaCost cost;
	
	/**
	 * Create a new ManaCostFilter.
	 */
	public ManaCostFilter()
	{
		super(FilterType.MANA_COST, null);
		contain = Containment.CONTAINS_ANY_OF;
		cost = new ManaCost("");
	}
	
	/**
	 * @param c Card to test
	 * @return <code>true</code> if the given Card's mana cost matches this
	 * ManaCostFilter's cost with the correct containment type, and
	 * <code>false</code> otherwise.
	 */
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

	/**
	 * @return The String representation of this ManaCostFilter's content,
	 * which is its containment's String representation followed by its
	 * cost's String representation in quotes.
	 * @see FilterLeaf#content()
	 */
	@Override
	public String content()
	{
		return contain.toString() + "\"" + cost.toString() + "\"";
	}

	/**
	 * Parse a String to determine this ManaCostFilter's containment
	 * and mana cost.
	 * 
	 * @param s String to parse
	 * @see editor.filter.Filter#parse(String)
	 */
	@Override
	public void parse(String s)
	{
		String content = checkContents(s, FilterType.MANA_COST);
		int delim = content.indexOf('"');
		contain = Containment.get(content.substring(0, delim));
		cost = ManaCost.valueOf(content.substring(delim + 1, content.lastIndexOf('"')));
	}
	
	/**
	 * @return A new ManaCostFilter that is a copy of this one.
	 */
	@Override
	public Filter copy()
	{
		ManaCostFilter filter = (ManaCostFilter)FilterType.MANA_COST.createFilter();
		filter.contain = contain;
		filter.cost = cost;
		return filter;
	}
	
	/**
	 * @param other Object to compare with
	 * @return <code>true</code> if the other Object is a ManaCostFilter with
	 * the same containment and symbol list.
	 */
	@Override
	public boolean equals(Object other)
	{
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (other.getClass() != ManaCostFilter.class)
			return false;
		ManaCostFilter o = (ManaCostFilter)other;
		return o.contain == contain && o.cost.equals(cost);
	}
	
	/**
	 * @return The hash code of this ManaCostFilter, which is composed of the hash
	 * codes of its containment and its symbol list.
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash(type, function, contain, cost);
	}
}
