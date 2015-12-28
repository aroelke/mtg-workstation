package editor.filter.leaf;

import java.util.Collection;
import java.util.function.Function;

import editor.database.Card;
import editor.filter.FilterType;
import editor.util.Comparison;

/**
 * This class represents a filter for a card characteristic that is a
 * number.
 * 
 * @author Alec Roelke
 */
public class NumberFilter extends FilterLeaf<Collection<Double>>
{
	/**
	 * Operation to compare the characteristic with this NumberFilter's
	 * operand.
	 */
	public Comparison compare;
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
	public NumberFilter(FilterType t, Function<Card, Collection<Double>> f)
	{
		super(t, f);
		compare = Comparison.EQ;
		operand = 0.0;
	}

	/**
	 * @param c Card to test
	 * @return <code>true</code> if the numeric characteristic of the given Card
	 * compares correctly with this NumberFilter's operand.
	 */
	@Override
	public boolean test(Card c)
	{
		Collection<Double> values = function.apply(c);
		return !values.stream().allMatch((v) -> v.isNaN()) && values.stream().anyMatch((v) -> compare.test(v, operand));
	}

	/**
	 * @return The String representation of this NumberFilter's contents,
	 * which is its comparison's String representation followed by its operand.
	 * @see FilterLeaf#content()
	 */
	@Override
	public String content()
	{
		return compare.toString() + operand;
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
		String content = checkContents(s, type);
		compare = Comparison.get(content.charAt(0));
		operand = Double.valueOf(content.substring(1));
	}
}