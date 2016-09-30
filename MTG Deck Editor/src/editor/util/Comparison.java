package editor.util;

/**
 * This class represents a logical comparison between two values.
 * 
 * TODO: Change other enums in a similar way to this one.
 * 
 * @author Alec Roelke
 */
public interface Comparison
{
	/**
	 * List of comparison operations that can be performed.
	 */
	Character[] OPERATIONS = new Character[] {'=', '≠', '≥', '≤', '>', '<'};
	
	/**
	 * Test two values according to a type of comparison.
	 * 
	 * @param op Comparison to perform
	 * @param a First value to compare
	 * @param b Second value to compare
	 * @return <code>true</code> if the comparison tests true for the two values, and
	 * <code>false</code> otherwise.
	 */
	static <U extends Comparable<U>> boolean test(char op, U a, U b)
	{
		switch (op)
		{
		case '=':
			return a.equals(b);
		case '≠':
			return !a.equals(b);
		case '≥':
			return a >= b;
		case '≤':
			return a <= b;
		case '>':
			return a > b;
		case '<':
			return a < b;
		default:
			throw new IllegalArgumentException("Illegal comparison '" + op + "'");
		}
	}
}
