package editor.database.characteristics;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

import editor.database.card.Card;
import editor.util.UnmodifiableList;

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
	 * This class represents a tuple of powers and/or toughnesses.  It is useful for displaying
	 * and sorting these values for cards that may have multiple faces.
	 * 
	 * @author Alec Roelke
	 */
	@SuppressWarnings("serial")
	public static class Tuple extends UnmodifiableList<PowerToughness> implements Comparable<Tuple>
	{
		/**
		 * Create a new tuple out of the given collection of PowerToughnesses.
		 * 
		 * @param c Collection of PowerToughnesses to create the tuple out of
		 */
		public Tuple(List<? extends PowerToughness> c)
		{
			super(c);
		}
		
		/**
		 * Create a new tuple out of the given PowerToughnesses.
		 * 
		 * @param c PowerToughnesses to create the tuple out of
		 */
		public Tuple(PowerToughness... c)
		{
			super(Arrays.asList(c));
		}
		
		/**
		 * @param o Tuple to compare to (must be a PowerToughness tuple)
		 * @return A negative number if this tuple has a value and the other is empty or if
		 * the first element of this one is less than the first element of the other one, a
		 * positive number if the opposite is true, or 0 if both tuples are empty or if their
		 * first elements are the same.
		 */
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
				PowerToughness first = stream().filter(PowerToughness::exists).findFirst().orElse(this[0]);
				PowerToughness second = stream().filter(PowerToughness::exists).findFirst().orElse(o[0]);
				return first.compareTo(second);
			}
		}
		
		/**
		 * @return A String representation of this tuple, which is its existing values
		 * separated by the String used to separate elements of multi-faced cards.
		 */
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
	 * TODO: Comment this
	 * @return
	 */
	public boolean exists()
	{
		return !Double.isNaN(value);
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
	 * TODO: Comment this
	 * @param other
	 * @return
	 */
	public int compareTo(double other)
	{
		if (Double.isNaN(value) && Double.isNaN(other))
			return 0;
		else if (Double.isNaN(value))
			return 1;
		else if (Double.isNaN(other))
			return -1;
		else
			return (int)(2.0*value - 2.0*other);
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
