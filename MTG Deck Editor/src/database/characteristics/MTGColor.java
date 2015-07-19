package database.characteristics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * This enum represents one of the five colors of Magic: The Gathering.
 * 
 * TODO: Change the pair and triple classes into a single tuple class
 * TODO: Come up with a universal sorting algorithm for sorting any number of colors
 * 
 * @author Alec Roelke
 */
public enum MTGColor
{
	WHITE("White"),
	BLUE("Blue"),
	BLACK("Black"),
	RED("Red"),
	GREEN("Green");
	
	/**
	 * Get an MTGColor from a String.  Acceptable values are "white," "w," "blue,"
	 * "u," "black," "b," "red," "r," "green," or "g," case insensitive.
	 * 
	 * @param color String to create an MTGColor from.
	 * @return MTGColor that corresponds to the String.
	 */
	public static MTGColor get(String color)
	{
		for (MTGColor c: MTGColor.values())
			if (c.color.equalsIgnoreCase(color) || color.equalsIgnoreCase(String.valueOf(c.shorthand())))
				return c;
		throw new IllegalArgumentException("Illegal color string \"" + color + "\"");
	}
	
	/**
	 * Get an MTGColor from a character.  Acceptable characters are 'w,' 'u,' 'b,'
	 * 'r,' or 'g,' case insensitive.
	 * 
	 * @param color Character to get a color from.
	 * @return MTGColor that corresponds to the character.
	 */
	public static MTGColor get(char color)
	{
		switch (Character.toLowerCase(color))
		{
		case 'w':
			return WHITE;
		case 'u':
			return BLUE;
		case 'b':
			return BLACK;
		case 'r':
			return RED;
		case 'g':
			return GREEN;
		default:
			throw new IllegalArgumentException("Illegal color shorthand");
		}
	}
	
	/**
	 * Sort a list of MTGColors in color order.  If the list contains two colors, it will be
	 * sorted according to how they appear on a card.  Otherwise, it will be sorted according
	 * to WUBRG order.  It is recommended to use this rather than using Java's built-in sorting
	 * functions.  The list must not contain any duplicate colors.
	 * 
	 * @param colors List of MTGColors to sort.
	 */
	public static void sort(List<MTGColor> colors)
	{
		if (!colors.isEmpty())
		{
			Tuple t = new Tuple(colors);
			colors.clear();
			colors.addAll(Arrays.asList(t.toArray()));
		}
	}
	
	/**
	 * String representation of this MTGColor.
	 */
	private final String color;
	
	/**
	 * Create a new MTGColor.
	 * 
	 * @param color String representation of the new MTGColor
	 */
	private MTGColor(final String color)
	{
		this.color = color;
	}
	
	/**
	 * TODO: Comment this
	 * @return
	 */
	public MTGColor[] allies()
	{
		return new MTGColor[] {values()[ordinal() == 0 ? values().length - 1 : ordinal() - 1], values()[(ordinal() + 1)%values().length]};
	}
	
	/**
	 * TODO: Comment this
	 * @return
	 */
	public MTGColor[] enemies()
	{
		return new MTGColor[] {values()[(((ordinal() - 2)%values().length) + values().length)%values().length], values()[(ordinal() + 2)%values().length]};
	}
	
	/**
	 * @return A one-character shorthand for the name of this MTGColor.
	 */
	public char shorthand()
	{
		switch (this)
		{
		case WHITE:
			return 'W';
		case BLUE:
			return 'U';
		case BLACK:
			return 'B';
		case RED:
			return 'R';
		case GREEN:
			return 'G';
		default:
			return 'C';
		}
	}
	
	/**
	 * @return A String representation of this MTGColor (its name).
	 */
	@Override
	public String toString()
	{
		return color;
	}
	
	/**
	 * @param other MTGColor to compare to
	 * @return A negative number if this MTGColor should come before the other, 0 if they are the same,
	 * or a postive number if it should come after.  Typically this follows WUBRG order, but if the
	 * distance is too great (like white and green), then the order is reversed.
	 */
	public int colorOrder(MTGColor other)
	{
		int diff = compareTo(other);
		return Math.abs(diff) <= 2 ? diff : -diff;
	}
	
	/**
	 * TODO: Comment this
	 * TODO: Make this a Collection
	 * @param other
	 * @return
	 */
	public int distance(MTGColor other)
	{
		return (other.ordinal() - ordinal() + values().length)%values().length;
	}
	
	/**
	 * TODO: Comment this
	 * @author Alec
	 */
	public static class Tuple implements Comparable<Tuple>
	{
		private List<MTGColor> colors;
		
		public Tuple(List<MTGColor> cols)
		{
			colors = new ArrayList<MTGColor>(new HashSet<MTGColor>(cols));
			Collections.sort(colors);
			switch (colors.size())
			{
			case 2:
				if (colors.get(0).colorOrder(colors.get(1)) > 0)
					Collections.reverse(colors);
				break;
			case 3:
				while (colors.get(0).distance(colors.get(1)) != colors.get(1).distance(colors.get(2)))
					Collections.rotate(colors, 1);
				break;
			case 4:
				boolean equal;
				do
				{
					equal = true;
					for (int i = 0; i < 3; i++)
					{
						if (colors.get(i).distance(colors.get(i + 1)) != 1)
						{
							equal = false;
							Collections.rotate(colors, 1);
							break;
						}
					}
				} while (!equal);
				break;
			default:
				break;
			}
		}
		
		public Tuple(MTGColor... cols)
		{
			this(Arrays.asList(cols));
		}
		
		public int size()
		{
			return colors.size();
		}
		
		public MTGColor get(int index)
		{
			return colors.get(index);
		}
		
		public boolean contains(MTGColor color)
		{
			return colors.contains(color);
		}
		
		public boolean containsAll(Collection<MTGColor> cols)
		{
			return colors.containsAll(cols);
		}
		
		public boolean containsAll(Tuple cols)
		{
			return colors.containsAll(cols.colors);
		}
		
		public MTGColor[] toArray()
		{
			return colors.toArray(new MTGColor[colors.size()]);
		}
		
		@Override
		public int compareTo(Tuple other)
		{
			int diff = size() - other.size();
			if (diff == 0)
				for (int i = 0; i < size(); i++)
					diff += get(i).colorOrder(other.get(i))*Math.pow(10, size() - i);
			return diff;
		}
		
		@Override
		public boolean equals(Object other)
		{
			if (other == null)
				return false;
			if (other == this)
				return true;
			if (!(other instanceof Tuple))
				return false;
			Tuple t = (Tuple)other;
			return containsAll(t) && t.containsAll(this);
		}
		
		@Override
		public int hashCode()
		{
			int h = Integer.MAX_VALUE;
			for (MTGColor col: colors)
				h ^= col.hashCode();
			return h;
		}
		
		@Override
		public String toString()
		{
			return colors.toString();
		}
	}
}
