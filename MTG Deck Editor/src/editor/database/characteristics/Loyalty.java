package editor.database.characteristics;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

import editor.database.card.Card;

/**
 * This class represents a loyalty value for a card.
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
	public static class Tuple extends AbstractList<Loyalty> implements Comparable<Tuple>
	{
		/**
		 * Loyalties that are part of this Tuple.
		 */
		private final List<Loyalty> values;
		
		/**
		 * Create an empty Tuple of Loyalties.
		 */
		public Tuple()
		{
			this(Collections.emptyList());
		}
		
		/**
		 * Create a new Tuple out of the given collection of loyalties.
		 * 
		 * @param c collection of loyalties to create the tuple out of
		 */
		public Tuple(List<? extends Loyalty> c)
		{
			values = Collections.unmodifiableList(c);
		}
		
		/**
		 * Create a new Tuple out of the given loyalties.
		 * 
		 * @param c Loyalties to create the new tuple out of
		 */
		public Tuple(Loyalty... c)
		{
			this(Arrays.asList(c));
		}
		
		/**
		 * {@inheritDoc}
		 * Only the first Loyalty from each Tuple is compared.
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
				Loyalty first = stream().filter(Loyalty::exists).findFirst().orElse(get(0));
				Loyalty second = stream().filter(Loyalty::exists).findFirst().orElse(o.get(0));
				return first.compareTo(second);
			}
		}
		
		@Override
		public Loyalty get(int index)
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
		 * The String representation of each Loyalty is separated by {@link Card#FACE_SEPARATOR}.
		 */
		@Override
		public String toString()
		{
			StringJoiner str = new StringJoiner(" " + Card.FACE_SEPARATOR + " ");
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
	 * Create a new Loyalty out of the given integer.  A value of 0 indicates
	 * that there is no loyalty on a card.
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
	
	@Override
	public int compareTo(Loyalty other)
	{
		if (!exists() && !other.exists())
			return 0;
		else if (!exists())
			return 1;
		else if (!other.exists())
			return -1;
		else
			return value - other.value;
	}

	/**
	 * Get if this Loyalty exists, which is if it is nonzero.
	 * 
	 * @return true if this Loyalty exists, and false otherwise.
	 */
	public boolean exists()
	{
		return value >= 1;
	}
	
	/**
	 * {@inheritDoc}
	 * If this Loyalty does not exist, this is the empty String rather than 0.
	 */
	@Override
	public String toString()
	{
		return value > 0 ? String.valueOf(value) : "";
	}
}
