package editor.util;

import java.util.Comparator;

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
	EQ('='),
	/**
	 * The first value is greater than or equal to the second.  Opposite of {@link #LT}.
	 */
	GE(UnicodeSymbols.GREATER_OR_EQUAL),
	/**
	 * The first value is strictly greater than the second.  Opposite of {@link #LE}.
	 */
	GT('>'),
	/**
	 * The first value is less than or equal to the second.  Opposite of {@link #GT}.
	 */
	LE(UnicodeSymbols.LESS_OR_EQUAL),
	/**
	 * The first value is strictly less than the second.  Opposite of {@link #GE}.
	 */
	LT('<'),
	/**
	 * The two values are equal.  Opposite of {@link #EQ}.
	 */
	NE(UnicodeSymbols.NOT_EQUAL);
	
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
	 * Create a new Comparison.
	 * 
	 * @param op Operator of the new Comparison
	 */
	private Comparison(final char op)
	{
		operator = op;
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
		switch (this)
		{
		case EQ:
			return comparator.compare(a, b) == 0;
		case NE:
			return comparator.compare(a, b) != 0;
		case GE:
			return comparator.compare(a, b) >= 0;
		case LE:
			return comparator.compare(a, b) <= 0;
		case GT:
			return comparator.compare(a, b) > 0;
		case LT:
			return comparator.compare(a, b) < 0;
		default:
			throw new IllegalArgumentException("Illegal comparison " + this);
		}
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