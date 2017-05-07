package editor.database.characteristics;

import java.util.Objects;

public class Loyalty implements Comparable<Loyalty>
{
	public static final Loyalty NO_LOYALTY = new Loyalty(0);
	
	public final int value;
	
	public Loyalty(int v)
	{
		value = Math.max(-1, v);
	}
	
	public Loyalty(String s)
	{
		int v;
		try
		{
			v = Integer.valueOf(s);
		}
		catch (NumberFormatException x)
		{
			v = s.compareToIgnoreCase("X") == 0 ? -1 : 0;
		}
		value = v;
	}

	@Override
	public int compareTo(Loyalty other)
	{
		return Integer.compare(value, other.value);
	}
	
	@Override
	public boolean equals(Object other)
	{
		if (other == this)
			return true;
		if (other == null)
			return false;
		if (!(other instanceof Loyalty))
			return false;
		return value == ((Loyalty)other).value;
	}
	
	public boolean exists()
	{
		return value > 0;
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(value);
	}
	
	@Override
	public String toString()
	{
		if (value < 0)
			return "X";
		else if (value == 0)
			return "";
		else return Integer.toString(value);
	}
	
	public boolean variable()
	{
		return value < 0;
	}
}
