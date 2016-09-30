package editor.util;

import java.util.function.BiPredicate;

/**
 * This class represents a logical comparison between two values.
 * 
 * @author Alec Roelke
 */
@FunctionalInterface
public interface Comparison<T extends Comparable<? super T>> extends BiPredicate<T, T>
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
	static <U extends Comparable<? super U>> boolean test(char op, U a, U b)
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
