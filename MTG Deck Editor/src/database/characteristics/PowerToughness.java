package database.characteristics;

import java.util.Collection;
import java.util.StringJoiner;

import database.Card;

/**
 * This class represents a power value or a toughness value.  If the card is missing
 * power or toughness, the String expression will be blank and the numeric value will
 * be "not-a-number."
 * 
 * @author Alec Roelke
 */
public class PowerToughness implements Comparable<PowerToughness>
{
	/**
	 * TODO: Comment this
	 * @author Alec
	 *
	 */
	public static class Tuple extends util.Tuple<PowerToughness> implements Comparable<Tuple>
	{
		public Tuple(Collection<? extends PowerToughness> c)
		{
			super(c);
		}
		
		public Tuple(PowerToughness... c)
		{
			super(c);
		}
		
		@Override
		public int compareTo(Tuple o)
		{
			if (isEmpty() && o.isEmpty())
				return 0;
			else if (isEmpty())
				return -1;
			else if (o.isEmpty())
				return 1;
			else
			{
				PowerToughness first = stream().filter((pt) -> !Double.isNaN(pt.value)).findFirst().orElse(get(0));
				PowerToughness second = stream().filter((pt) -> !Double.isNaN(pt.value)).findFirst().orElse(o.get(0));
				return first.compareTo(second);
			}
		}
		
		@Override
		public String toString()
		{
			StringJoiner str = new StringJoiner(" " + Card.FACE_SEPARATOR + " ");
			for (PowerToughness pt: this)
				if (!Double.isNaN(pt.value))
					str.add(pt.toString());
			return str.toString();
		}
	}
	
	/**
	 * Numeric value of the power or toughness for sorting.
	 */
	public final double value;
	/**
	 * String expression showing the power or toughness value (may contain *).
	 */
	public final String expression;
	
	/**
	 * Create a new PowerToughness from a number.
	 * 
	 * @param v Numeric value of the PowerToughness
	 */
	public PowerToughness(double v)
	{
		value = v;
		expression = String.valueOf(v);
	}
	
	/**
	 * Create a new PowerToughness from an expression.
	 * 
	 * @param e Expression for the new PowerToughness.
	 */
	public PowerToughness(String e)
	{
		if (e == null || e.isEmpty())
		{
			value = Double.NaN;
			expression = "";
		}
		else
		{
			expression = e.replaceAll("\\s+", "");
			e = e.replaceAll("[^0-9+-.]+","").replaceAll("[+-]$", "");
			value = e.isEmpty() ? 0.0 : Double.valueOf(e);
		}
	}
	
	/**
	 * @return <code>true</code> if this PowerToughness's expression contains *,
	 * and <code>false</code> otherwise.
	 */
	public boolean variable()
	{
		return expression.contains("*");
	}
	
	/**
	 * @return A negative number if this PowerToughness's value is less than the other
	 * one's or if this one is empty and the other isn't, a positive value if the reverse
	 * is true, or 0 if they are the same.
	 */
	@Override
	public int compareTo(PowerToughness o)
	{
		if (Double.isNaN(value) && Double.isNaN(o.value))
			return 0;
		else if (Double.isNaN(value))
			return 1;
		else if (Double.isNaN(o.value))
			return -1;
		else
			return (int)(2.0*value - 2.0*o.value);
	}
	
	/**
	 * @return A String representation of this PowerToughness, which is its
	 * expression.
	 */
	@Override
	public String toString()
	{
		return expression;
	}
}
