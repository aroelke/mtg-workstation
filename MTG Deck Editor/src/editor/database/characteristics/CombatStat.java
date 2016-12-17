package editor.database.characteristics;

import editor.util.UnicodeSymbols;

/**
 * This class represents a power value or a toughness value.
 * 
 * @author Alec Roelke
 */
public class CombatStat implements Comparable<CombatStat>
{
	/**
	 * Representation for a combat stat that doesn't exist.
	 */
	public static final CombatStat NO_COMBAT = new CombatStat(Double.NaN);
	
	/**
	 * String expression showing the power or toughness value (may contain *).
	 */
	public final String expression;
	/**
	 * Numeric value of the power or toughness for sorting.
	 */
	public final double value;
	
	/**
	 * Create a new PowerToughness from a number.
	 * 
	 * @param v Numeric value of the CombatStat
	 */
	public CombatStat(double v)
	{
		value = v;
		expression = Double.isNaN(v) ? "" : String.valueOf(v);
	}
	
	/**
	 * Create a new PowerToughness from an expression.
	 * 
	 * @param e expression for the new CombatStat
	 */
	public CombatStat(String e)
	{
		if (e == null || e.isEmpty())
		{
			value = Double.NaN;
			expression = "";
		}
		else
		{
			expression = e.replaceAll("\\s+", "");
			e = e.replaceAll("[*" + UnicodeSymbols.SUPERSCRIPT_TWO + "]+","").replaceAll("[+-]$", "");
			value = e.isEmpty() ? 0.0 : Double.valueOf(e);
		}
	}
	
	@Override
	public int compareTo(CombatStat o)
	{
		if (!exists() && !o.exists())
			return 0;
		else if (!exists())
			return 1;
		else if (!o.exists())
			return -1;
		else
			return (int)(2.0*value - 2.0*o.value);
	}
	
	/**
	 * Not all cards have power or toughness.  For those cards, a value of
	 * {@link Double#NaN} is used.
	 * 
	 * @return true if this CombatStat exists, and false otherwise.
	 */
	public boolean exists()
	{
		return !Double.isNaN(value);
	}
	
	/**
	 * {@inheritDoc}
	 * If this CombatStat doesn't exist according to {@link #exists()}, this is
	 * an empty String.
	 */
	@Override
	public String toString()
	{
		return expression;
	}
	
	/**
	 * If this CombatStat's expression contains a *, it is variable.  Otherwise,
	 * it is not.
	 * 
	 * @return true if this CombatStat is variable, and false otherwise
	 */
	public boolean variable()
	{
		return expression.contains("*");
	}
}
