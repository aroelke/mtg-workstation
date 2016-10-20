package editor.util;

/**
 * This enum represents a logical comparison between two values.
 * 
 * TODO: Change other enums to be like this one if appropriate
 * 
 * @author Alec Roelke
 */
public enum Comparison
{
	EQ('='),
	NE('\u2260'),
	GE('\u2265'),
	LE('\u2264'),
	GT('>'),
	LT('<');
	
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
		case '\u2260':
			return NE;
		case '\u2265':
			return GE;
		case '\u2264':
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
	 * Test two values of the same type according to this Comparison's operation.
	 * 
	 * @param a First value to test
	 * @param b Second value to test
	 * @return <code>true</code> if the two values pass the comparison, and
	 * <code>false</code> otherwise.
	 */
	public <T extends Comparable<? super T>> boolean test(T a, T b)
	{
		switch (operator)
		{
		case '=':
			return a.equals(b);
		case '\u2260':
			return !a.equals(b);
		case '\u2265':
			return a >= b;
		case '\u2264':
			return a <= b;
		case '>':
			return a > b;
		case '<':
			return a < b;
		default:
			throw new IllegalArgumentException("Illegal comparison " + operator);
		}
	}
	
	/**
	 * @return A String representation of this Comparison, which is its operator.
	 */
	@Override
	public String toString()
	{
		return String.valueOf(operator);
	}
}