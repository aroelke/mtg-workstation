package editor.util;

import java.util.function.BiPredicate;

/**
 * This class represents a logical comparison between two values.
 * 
 * TODO: Change other enums in a similar way to this one.
 * 
 * @author Alec Roelke
 */
@FunctionalInterface
public interface Comparison<T extends Comparable<T>> extends BiPredicate<T, T>
{
	/**
	 * List of comparison operations that can be performed.
	 */
	static Character[] OPERATIONS = new Character[] {'=', '≠', '≥', '≤', '>', '<'};
	
	/**
	 * Convert an operation character into a comparison between two comparable values.
	 * 
	 * @param op Operation to perform
	 * @return A Comparison representing the comparison to perform.
	 */
	static <U extends Comparable<U>> Comparison<U> valueOf(char op)
	{
		switch (op)
		{
		case '=':
			return U::equals;
		case '≠':
			return (a, b) -> !a.equals(b);
		case '≥':
			return (a, b) -> a >= b;
		case '≤':
			return (a, b) -> a <= b;
		case '>':
			return (a, b) -> a > b;
		case '<':
			return (a, b) -> a < b;
		default:
			throw new IllegalArgumentException("Illegal comparison '" + op + "'");
		}
	}
	
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
		return Comparison.<U>valueOf(op).test(a, b);
	}
}
