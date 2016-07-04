package editor.database.characteristics;

import java.util.List;
import java.util.StringJoiner;

import editor.database.card.CardInterface;

/**
 * This class represents a loyalty value for a card.  If the card has no loyalty,
 * then the value will be 0.
 * 
 * @author Alec Roelke
 */
public class Loyalty implements Comparable<Loyalty>
{
	/**
	 * This class represents a tuple of loyalties, which is useful for displaying and
	 * sorting cards by loyalty in a table when they are multi-faced cards and may have
	 * multiple possibilities.
	 * 
	 * @author Alec Roelke
	 */
	public static class Tuple extends editor.util.Tuple<Loyalty> implements Comparable<Tuple>
	{
		/**
		 * Create a new tuple out of the given collection of loyalties.
		 * 
		 * @param c Collection of loyalties to create the tuple out of
		 */
		public Tuple(List<? extends Loyalty> c)
		{
			super(c);
		}
		
		/**
		 * Create a new tuple out of the given loyalties.
		 * 
		 * @param c Loyalties to create the new tuple out of
		 */
		public Tuple(Loyalty... c)
		{
			super(c);
		}
		
		/**
		 * @param o Loyalty tuple to compare with (this only works with Loyalty tuples)
		 * @return A negative number if this tuple has elements and the other is empty or
		 * if the first element of this tuple is less than the first element of the other,
		 * a positive number if the opposite is true, or 0 if both are empty or the first
		 * element of both are the same.
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
				Loyalty first = stream().filter((l) -> l.value > 0).findFirst().orElse(get(0));
				Loyalty second = stream().filter((l) -> l.value > 0).findFirst().orElse(o.get(0));
				return first.compareTo(second);
			}
		}
		
		/**
		 * @return A String representation of this tuple, which is its positive loyalty values separated by
		 * the String that separates card faces.
		 */
		@Override
		public String toString()
		{
			StringJoiner str = new StringJoiner(" " + CardInterface.FACE_SEPARATOR + " ");
			for (Loyalty l: this)
				if (l.value > 0)
					str.add(l.toString());
			return str.toString();
		}
	}
	
	/**
	 * Value of this Loyalty.
	 */
	public final int value;
	
	/**
	 * Create a new Loyalty out of the given integer.
	 * 
	 * @param l Integer value of the new Loyalty
	 */
	public Loyalty(int l)
	{
		value = l;
	}
	
	/**
	 * Create a new Loyalty out of the given String.
	 * 
	 * @param l String to parse to get the value of the new Loyalty
	 */
	public Loyalty(String l)
	{
		int loyal;
		try
		{
			loyal = Integer.valueOf(l);
		}
		catch (NumberFormatException e)
		{
			loyal = 0;
		}
		value = loyal;
	}
	
	/**
	 * @param other Loyalty to compare with
	 * @return A negative number if this loyalty is less than the other, a positive
	 * number if it is greater, or 0 if both are the same.  Any value less than 1 is
	 * treated the same.
	 */
	@Override
	public int compareTo(Loyalty other)
	{
		if (value < 1 && other.value < 1)
			return 0;
		else if (value < 1)
			return 1;
		else if (other.value < 1)
			return -1;
		else
			return value - other.value;
	}
	
	/**
	 * @return A String representation of this Loyalty, which is its value, unless its value
	 * is less than 1, in which case it is the empty String.
	 */
	@Override
	public String toString()
	{
		return value > 0 ? String.valueOf(value) : "";
	}
}
