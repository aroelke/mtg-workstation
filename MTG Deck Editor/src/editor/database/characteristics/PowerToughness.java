package editor.database.characteristics;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

import editor.database.card.Card;

/**
 * This class represents a power value or a toughness value.
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
	public static class Tuple extends AbstractList<PowerToughness> implements Comparable<Tuple>
	{
		/**
		 * PowerToughnesses that are part of this Tuple.
		 */
		private final List<PowerToughness> values;
		
		/**
		 * Create a new, empty Tuple of PowerToughnesses.
		 */
		public Tuple()
		{
			this(Collections.emptyList());
		}
		
		/**
		 * Create a new Tuple out of the given collection of PowerToughnesses.
		 * 
		 * @param c Collection of PowerToughnesses to create the tuple out of
		 */
		public Tuple(List<? extends PowerToughness> c)
		{
			values = Collections.unmodifiableList(c);
		}
		
		/**
		 * Create a new Tuple out of the given PowerToughnesses.
		 * 
		 * @param c PowerToughnesses to create the tuple out of
		 */
		public Tuple(PowerToughness... c)
		{
			this(Arrays.asList(c));
		}
		
		/**
		 * {@inheritDoc}
		 * Only the first value of each Tuple is compared.
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
				PowerToughness first = stream().filter(PowerToughness::exists).findFirst().orElse(get(0));
				PowerToughness second = o.stream().filter(PowerToughness::exists).findFirst().orElse(o.get(0));
				return first.compareTo(second);
			}
		}
		
		@Override
		public PowerToughness get(int index)
		{
			return values.get(index);
		}

		@Override
		public int size()
		{
			return values.size();
		}

		/**
		 * {@inheritDoc}
		 * The String representation of this tuple is its existing values separated by
		 * {@link Card#FACE_SEPARATOR}.
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
	 * @param e expression for the new PowerToughness.
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
	
	@Override
	public int compareTo(PowerToughness o)
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
	 * @return true if this PowerToughness exists, and false otherwise.
	 */
	public boolean exists()
	{
		return !Double.isNaN(value);
	}
	
	/**
	 * {@inheritDoc}
	 * If this PowerToughness doesn't exist according to {@link #exists()}, this is
	 * an empty String.
	 */
	@Override
	public String toString()
	{
		return expression;
	}
	
	/**
	 * If this PowerToughness's expression contains a *, it is variable.  Otherwise,
	 * it is not.
	 * 
	 * @return true if this PowerToughness is variable, and false otherwise.
	 */
	public boolean variable()
	{
		return expression.contains("*");
	}
}
