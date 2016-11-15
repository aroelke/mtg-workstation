package editor.filter.leaf;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

import editor.database.card.Card;
import editor.database.characteristics.ManaCost;
import editor.filter.Filter;
import editor.filter.FilterFactory;
import editor.util.Containment;

/**
 * This class represents a filter to group cards by mana costs.
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
		super(FilterFactory.MANA_COST, null);
		contain = Containment.CONTAINS_ANY_OF;
		cost = new ManaCost("");
	}
	
	/**
	 * {@inheritDoc}
	 * This ManaCostFilter's content String is its containment followed by its cost's
	 * content String in quotes.
	 */
	@Override
	public String content()
	{
		return contain.toString() + "\"" + cost.toString() + "\"";
	}

	@Override
	public Filter copy()
	{
		ManaCostFilter filter = (ManaCostFilter)FilterFactory.createFilter(FilterFactory.MANA_COST);
		filter.contain = contain;
		filter.cost = cost;
		return filter;
	}

	@Override
	public boolean equals(Object other)
	{
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (other.getClass() != getClass())
			return false;
		ManaCostFilter o = (ManaCostFilter)other;
		return o.contain == contain && o.cost.equals(cost);
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(type(), contain, cost);
	}
	
	@Override
	public void parse(String s)
	{
		String content = checkContents(s, FilterFactory.MANA_COST);
		int delim = content.indexOf('"');
		contain = Containment.fromString(content.substring(0, delim));
		cost = ManaCost.valueOf(content.substring(delim + 1, content.lastIndexOf('"')));
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		super.readExternal(in);
		contain = (Containment)in.readObject();
		cost = ManaCost.valueOf(in.readUTF());
	}
	
	/**
	 * {@inheritDoc}
	 * Filter cards by their mana costs.
	 */
	@Override
	public boolean test(Card c)
	{
		switch (contain)
		{
		case CONTAINS_ANY_OF:
			return c.manaCost().stream().anyMatch((m) -> Containment.CONTAINS_ANY_OF.test(m, cost));
		case CONTAINS_NONE_OF:
			return c.manaCost().stream().anyMatch((m) -> Containment.CONTAINS_NONE_OF.test(m, cost));
		case CONTAINS_ALL_OF:
			return c.manaCost().stream().anyMatch((m) -> m.isSuperset(cost));
		case CONTAINS_NOT_ALL_OF:
			return c.manaCost().stream().anyMatch((m) -> !m.isSuperset(cost));
		case CONTAINS_EXACTLY:
			return c.manaCost().stream().anyMatch((m) -> m.equals(cost));
		case CONTAINS_NOT_EXACTLY:
			return c.manaCost().stream().anyMatch((m) -> !m.equals(cost));
		default:
			return false;
		}
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		super.writeExternal(out);
		out.writeObject(contain);
		out.writeUTF(cost.toString());
	}
}
