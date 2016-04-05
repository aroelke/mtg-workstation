package editor.filter.leaf;

import java.util.Objects;

import editor.database.Card;
import editor.filter.Filter;
import editor.filter.FilterType;

/**
 * This class represents a filter with only two options: All
 * Cards or no Cards.
 * 
 * @author Alec Roelke
 */
public class BinaryFilter extends FilterLeaf<Void>
{
	/**
	 * Whether or not to let all Cards through the filter.
	 */
	private boolean all;
	
	/**
	 * Create a new BinaryFilter.
	 * 
	 * @param a Whether or not to let all Cards through the filter.
	 */
	public BinaryFilter(boolean a)
	{
		super(a ? FilterType.ALL : FilterType.NONE, null);
		all = a;
	}
	
	/**
	 * @param c Card to test
	 * @return <code>true</code> if this BinaryFilter lets Cards
	 * through, and <code>false</code> otherwise.
	 */
	@Override
	public boolean test(Card c)
	{
		return all;
	}

	/**
	 * @return The String representation of this Binary Filter's
	 * content, which is an empty String.
	 */
	@Override
	public String content()
	{
		return "";
	}

	/**
	 * BinaryFilters have no content, so there's nothing to parse.
	 */
	@Override
	public void parse(String s)
	{}
	
	/**
	 * @return A new Filter that is a copy of this BinaryFilter.
	 */
	@Override
	public Filter copy()
	{
		return type.createFilter();
	}
	
	/**
	 * @param other Object to compare with
	 * @return <code>true</code> if the other Object is a BinaryFilter and
	 * it filters the same Cards as this one.
	 */
	@Override
	public boolean equals(Object other)
	{
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (other.getClass() != BinaryFilter.class)
			return false;
		return ((BinaryFilter)other).all == all;
	}
	
	/**
	 * @return The hashCode of this BinaryFilter, which is composed from
	 * its filter direction.
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash(type, function, all);
	}
}
