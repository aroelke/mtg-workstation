package editor.filter.leaf;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Objects;

import editor.database.card.Card;
import editor.filter.Filter;
import editor.filter.FilterFactory;
import editor.util.Comparison;
import editor.util.SerializableFunction;

/**
 * This class represents a filter for a card characteristic that is a
 * number.
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
	 * @param t Type of the new NumberFilter
	 * @param f Function for the new NumberFilter
	 */
	public NumberFilter(String t, SerializableFunction<Card, Collection<Double>> f)
	{
		super(t, f);
		operation = '=';
		operand = 0.0;
	}
	
	public NumberFilter()
	{
		this("", null);
	}

	/**
	 * @param c Card to test
	 * @return <code>true</code> if the numeric characteristic of the given Card
	 * compares correctly with this NumberFilter's operand.
	 */
	@Override
	public boolean test(Card c)
	{
		return function().apply(c).stream().anyMatch((v) -> !v.isNaN() && operation.test(v, operand));
	}

	/**
	 * @return The String representation of this NumberFilter's contents,
	 * which is its comparison's String representation followed by its operand.
	 * @see FilterLeaf#content()
	 */
	@Override
	public String content()
	{
		return "" + operation + operand;
	}
	
	/**
	 * Parse a String to determine this NumberFilter's operation and operand.
	 * 
	 * @param s String to parse
	 * @see editor.filter.Filter#parse(String)
	 */
	@Override
	public void parse(String s)
	{
		String content = checkContents(s, type());
		operation = content.charAt(0);
		operand = Double.valueOf(content.substring(1));
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
		super.readExternal(in);
		operand = in.readDouble();
		operation = (Comparison)in.readObject();
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		super.writeExternal(out);
		out.writeDouble(operand);
		out.writeObject(operation);
	}
}