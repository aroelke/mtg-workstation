package editor.filter.leaf;

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
	 * TODO: Comment this
	 */
	@Override
	public Filter copy()
	{
		return type.createFilter();
	}
	
	/**
	 * TODO: Comment this
	 */
	@Override
	public boolean equals(Object other)
	{
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (!(other instanceof BinaryFilter))
			return false;
		return ((BinaryFilter)other).all == all;
	}
	
	/**
	 * TODO: Comment this
	 */
	@Override
	public int hashCode()
	{
		return Boolean.hashCode(all);
	}
}
