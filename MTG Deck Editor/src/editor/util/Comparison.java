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
	NE(UnicodeTable.NOT_EQUAL),
	GE(UnicodeTable.GREATER_OR_EQUAL),
	LE(UnicodeTable.LESS_OR_EQUAL),
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
		case UnicodeTable.NOT_EQUAL:
			return NE;
		case UnicodeTable.GREATER_OR_EQUAL:
			return GE;
		case UnicodeTable.LESS_OR_EQUAL:
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
		case UnicodeTable.NOT_EQUAL:
			return !a.equals(b);
		case UnicodeTable.GREATER_OR_EQUAL:
			return a >= b;
		case UnicodeTable.LESS_OR_EQUAL:
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