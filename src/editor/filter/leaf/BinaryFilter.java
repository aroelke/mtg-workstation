package editor.filter.leaf;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

import editor.database.card.Card;
import editor.filter.Filter;
import editor.filter.FilterFactory;

/**
 * This class represents a filter with only two options: All cards or no cards.
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
	 * Create a new BinaryFilter that lets all cards through.  Should only be used
	 * for deserialization.
	 */
	public BinaryFilter()
	{
		this(true);
	}
	
	/**
	 * Create a new BinaryFilter.
	 * 
	 * @param a whether or not to let all Cards through the filter.
	 */
	public BinaryFilter(boolean a)
	{
		super(a ? FilterFactory.ALL : FilterFactory.NONE, null);
		all = a;
	}

	@Override
	public Filter copy()
	{
		return FilterFactory.createFilter(type());
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
		return ((BinaryFilter)other).all == all;
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(type(), function(), all);
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		all = in.readBoolean();
	}
	
	/**
	 * {@inheritDoc}
	 * Either let all cards through or none of them.
	 */
	@Override
	public boolean test(Card c)
	{
		return all;
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeBoolean(all);
	}
}
