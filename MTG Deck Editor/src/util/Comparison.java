package util;

import java.util.function.BiPredicate;

/**
 * This class represents a logical comparison.  If either operand is null, then
 * a NullPointerException will be thrown except if using equals or not-equals.
 * 
 * @author Alec Roelke
 */
public enum Comparison implements BiPredicate<Double, Double>
{
	EQ('=', (a, b) -> (a == null && b == null) || (a != null && a.equals(b))),
	NE('≠', (a, b) -> (a == null && b != null) || (a != null && !a.equals(b))),
	GE('≥', (a, b) -> (a == null && b == null) || (a.compareTo(b) >= 0 || a.equals(b))),
	LE('≤', (a, b) -> (a == null && b == null) || (a.compareTo(b) <= 0 || a.equals(b))),
	GT('>', (a, b) -> a.compareTo(b) > 0 && !a.equals(b)),
	LT('<', (a, b) -> a.compareTo(b) < 0 && !a.equals(b));
	
	/**
	 * Get a Comparison from the specified character.
	 * 
	 * @param comp Character to parse
	 * @return A Comparison represented by the specified character.
	 */
	public static Comparison get(char comp)
	{
		for (Comparison c: Comparison.values())
			if (c.comparison == comp)
				return c;
		throw new IllegalArgumentException("Illegal comparison character '" + comp + "'");
	}
	
	/**
	 * Mathematical symbol for this type of comparison.
	 */
	private final char comparison;
	/**
	 * Function to perform when making this type of comparison.
	 */
	private final BiPredicate<Double, Double> func;
	
	/**
	 * Create a new Comparison.
	 * 
	 * @param comp Symbol for the new type of comparison
	 * @param f Function to perform for the new type of comparison
	 */
	private Comparison(char comp, BiPredicate<Double, Double> f)
	{
		comparison = comp;
		func = f;
	}
	
	/**
	 * @return A String representation of this Comparison, which is its
	 * mathematical symbol.
	 */
	@Override
	public String toString()
	{
		return String.valueOf(comparison);
	}

	/**
	 * Perform this Comparison on the specified operands.
	 * 
	 * @return <code>true</code> if this Comparison is true for the operands, and
	 * <code>false</code> otherwise. 
	 */
	@Override
	public boolean test(Double a, Double b)
	{
		return func.test(a, b);
	}
}
