package editor.filter.leaf;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

import editor.database.card.Card;
import editor.filter.Filter;
import editor.filter.FilterFactory;
import editor.util.Comparison;

/**
 * This class represents a filter for a card characteristic that is a number.
 * 
 * TODO: Change this to ComparableFilter and make it generic.
 * 
 * @author Alec Roelke
 */
public class NumberFilter extends FilterLeaf<Collection<Double>>
{
	/**
	 * Operation to compare the characteristic with this NumberFilter's
	 * operand.
	 */
	public Comparison operation;
	/**
	 * Operand to perform the operation on.
	 */
	public double operand;
	
	/**
	 * Create a new NumberFilter.
	 * 
	 * @param t type of the new NumberFilter
	 * @param f function for the new NumberFilter
	 */
	public NumberFilter(String t, Function<Card, Collection<Double>> f)
	{
		super(t, f);
		operation = Comparison.valueOf('=');
		operand = 0.0;
	}
	
	/**
	 * Create a new NumberFilter without a type or function.  Should only be used for
	 * deserialization.
	 */
	public NumberFilter()
	{
		this("", null);
	}

	/**
	 * {@inheritDoc}
	 * Filter cards by a numerical value according to this NumberFilter's operation and operand.
	 */
	@Override
	public boolean test(Card c)
	{
		return function().apply(c).stream().anyMatch((v) -> !v.isNaN() && operation.test(v, operand));
	}
	
	/**
	 * @return A new NumberFilter that is a copy of this one.
	 */
	@Override
	public Filter copy()
	{
		NumberFilter filter = (NumberFilter)FilterFactory.createFilter(type());
		filter.operation = operation;
		filter.operand = operand;
		return filter;
	}
	
	/**
	 * @param other Object to compare with
	 * @return <code>true</code> if the other Object is a NumberFilter and its type,
	 * comparison, and operand is the same.
	 */
	@Override
	public boolean equals(Object other)
	{
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (other.getClass() != getClass())
			return false;
		NumberFilter o = (NumberFilter)other;
		return o.type().equals(type()) && o.operation.equals(operation) && o.operand == operand;
	}
	
	/**
	 * @return The hash code of this NumberFilter, which is composed of its comparison and
	 * operand.
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash(type(), function(), operation, operand);
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		operand = in.readDouble();
		operation = (Comparison)in.readObject();
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeDouble(operand);
		out.writeObject(operation);
	}
}