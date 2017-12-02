package editor.util;

import java.util.Comparator;
import java.util.function.Predicate;

/**
 * This enum represents a logical comparison between two values.
 * 
 * @author Alec Roelke
 */
public enum Comparison
{
	/**
	 * The two values are equal.  Opposite of {@link #NE}.
	 */
	EQ('=', (x) -> x == 0),
	/**
	 * The first value is greater than or equal to the second.  Opposite of {@link #LT}.
	 */
	GE(UnicodeSymbols.GREATER_OR_EQUAL, (x) -> x >= 0),
	/**
	 * The first value is strictly greater than the second.  Opposite of {@link #LE}.
	 */
	GT('>', (x) -> x > 0),
	/**
	 * The first value is less than or equal to the second.  Opposite of {@link #GT}.
	 */
	LE(UnicodeSymbols.LESS_OR_EQUAL, (x) -> x <= 0),
	/**
	 * The first value is strictly less than the second.  Opposite of {@link #GE}.
	 */
	LT('<', (x) -> x < 0),
	/**
	 * The two values are equal.  Opposite of {@link #EQ}.
	 */
	NE(UnicodeSymbols.NOT_EQUAL, (x) -> x != 0);
	
	/**
	 * Get the Comparison corresponding to the given operator.
	 * 
	 * @param op Operator of the comparison to get
	 * @return The corresponding Comparison.
	 */
	public static Comparison valueOf(char op)
	{
		switch (op)
		{
		case '=':
			return EQ;
		case UnicodeSymbols.NOT_EQUAL:
			return NE;
		case UnicodeSymbols.GREATER_OR_EQUAL:
			return GE;
		case UnicodeSymbols.LESS_OR_EQUAL:
			return LE;
		case '>':
			return GT;
		case '<':
			return LT;
		default:
			throw new IllegalArgumentException("Illegal comparison " + op);
		}
	}
	
	/**
	 * Operator of the comparison this Comparison performs.
	 */
	private final char operator;
	/**
	 * Operation to perform when comparing two items.
	 */
	private final Predicate<Integer> comparison;
	
	/**
	 * Create a new Comparison.
	 * 
	 * @param op Operator of the new Comparison
	 * @param comp Operation on the result of {@link Comparable#compareTo(Object)}
	 */
	Comparison(final char op, final Predicate<Integer> comp)
	{
		operator = op;
		comparison = comp;
	}
	
	/**
	 * Test two values according to this Comparison's operation.
	 * 
	 * @param a First value to test
	 * @param b Second value to test
	 * @return true if the two arguments satisfy the comparison, and false otherwise
	 */
	public <T extends Comparable<? super T>> boolean test(T a, T b)
	{
		return test(a, b, T::compareTo);
	}
	
	/**
	 * Test two values according to this Comparison's operation and the given comparator.
	 * 
	 * @param a first value to test
	 * @param b second value to test
	 * @param comparator comparator to use for comparison
	 * @return true if the two arguments satisfy the comparison, and false otherwise
	 */
	public <T> boolean test(T a, T b, Comparator<? super T> comparator)
	{
		return comparison.test(comparator.compare(a, b));
	}
	
	/**
	 * {@inheritDoc}
	 * The String representation of a Comparison is its mathematical symbol.
	 */
	@Override
	public String toString()
	{
		return String.valueOf(operator);
	}
}