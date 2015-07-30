package database.characteristics;

import java.util.Collection;
import java.util.StringJoiner;

import database.Card;

/**
 * TODO: Comment this
 * @author Alec
 *
 */
public class Loyalty implements Comparable<Loyalty>
{
	public static class Tuple extends util.Tuple<Loyalty> implements Comparable<Tuple>
	{
		public Tuple(Collection<? extends Loyalty> c)
		{
			super(c);
		}
		
		public Tuple(Loyalty... c)
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
				Loyalty first = stream().filter((l) -> l.value > 0).findFirst().orElse(get(0));
				Loyalty second = stream().filter((l) -> l.value > 0).findFirst().orElse(o.get(0));
				return first.compareTo(second);
			}
		}
		
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
	
	public final int value;
	
	public Loyalty(int l)
	{
		value = l;
	}
	
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
		if (value < 1 && other.value < 1)
			return 0;
		else if (value < 1)
			return 1;
		else if (other.value < 1)
			return -1;
		else
			return value - other.value;
	}
	
	@Override
	public String toString()
	{
		return value > 0 ? String.valueOf(value) : "";
	}
}
