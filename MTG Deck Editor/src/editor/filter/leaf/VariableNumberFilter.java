package editor.filter.leaf;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import editor.database.Card;
import editor.filter.Filter;
import editor.filter.FilterType;
import editor.util.Comparison;

/**
 * This class represents a filter for a numeric card characteristic that can
 * be variable.
 * 
 * @author Alec Roelke
 */
public class VariableNumberFilter extends NumberFilter
{
	/**
	 * Whether or not the filter should search for varying values of its characteristic.
	 */
	public boolean varies;
	/**
	 * The function that determines if the filter's characteristic is variable.
	 */
	private Predicate<Card> variable;
	
	/**
	 * Create a new VariableNumberFilter.
	 * 
	 * @param t FilterType of the new VariableNumberFilter
	 * @param f Function representing the card characteristic
	 * @param v Function checking if the card characteristic is variable
	 */
	public VariableNumberFilter(FilterType t, Function<Card, Collection<Double>> f, Predicate<Card> v)
	{
		super(t, f);
		varies = false;
		variable = v;
	}
	
	/**
	 * @param c Card to test
	 * @return <code>true</code> if a variable value is desired and the given Card's
	 * value for the characteristic is variable, or if it isn't variable and matches
	 * the equation/inequality given, and <code>false</code> otherwise.
	 */
	@Override
	public boolean test(Card c)
	{
		return varies ? variable.test(c) : super.test(c);
	}

	/**
	 * @return A String representing the content of this VariableNumberFilter,
	 * which is either a * if it should vary or the inequality if it shouldn't.
	 * @see FilterLeaf#content()
	 * @see NumberFilter#content()
	 */
	@Override
	public String content()
	{
		return varies ? "*" : super.content();
	}

	/**
	 * Parse a String to determine the values of this FilterLeaf's fields.
	 * The String should contain the code for the filter, a :, and then either
	 * a * or the right-hand side of an equation/inequality specifying the
	 * number to be filtered.
	 * @param s String to parse
	 * @see NumberFilter#parse(String)
	 */
	@Override
	public void parse(String s)
	{
		String content = checkContents(s, type);
		if (content.equals("*"))
			varies = true;
		else
		{
			varies = false;
			compare = Comparison.get(content.charAt(0));
			operand = Double.valueOf(content.substring(1));
		}
	}
	
	/**
	 * @return A new VariableNumberFilter that is a copy of this one.
	 */
	@Override
	public Filter copy()
	{
		VariableNumberFilter filter = (VariableNumberFilter)type.createFilter();
		filter.varies = varies;
		filter.variable = variable;
		filter.compare = compare;
		filter.operand = operand;
		return filter;
	}
	
	/**
	 * @param other Object to compare with
	 * @return <code>true</code> if the other Object is a VariableNumberFilter,
	 * its type is the same as this one's type, its "varies" flag is the same as this
	 * one's, its variable function is the same as this one's, its comparison is the
	 * same, and its operand is the same.
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
		VariableNumberFilter o = (VariableNumberFilter)other;
		return o.type == type && o.varies == varies && o.variable.equals(variable)
				&& o.compare == compare & o.operand == operand;
	}
	
	/**
	 * @return The hash code of this VariableNumberFilter, which is composed of
	 * the hash codes of its "varies" flag, variable function, comparison, and
	 * operand.
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash(type, function, varies, variable, compare, operand);
	}
}
