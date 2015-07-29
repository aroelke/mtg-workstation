package database.characteristics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * This enum represents one of the five colors of Magic: The Gathering.
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
	 * functions.  If the list contains any duplicate colors, they will be removed.
	 * 
	 * @param colors List of MTGColors to sort.
	 */
	public static void sort(List<MTGColor> colors)
	{
		if (!colors.isEmpty())
		{
			Tuple t = new Tuple(colors);
			colors.clear();
			colors.addAll(t);
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
	 * @param other MTGColor to compare to
	 * @return The distance around the color pie from this color to the other color.
	 */
	public int distance(MTGColor other)
	{
		return (other.ordinal() - ordinal() + values().length)%values().length;
	}
	
	/**
	 * This class represents a sorted list of unique colors in the correct order around
	 * the color pie.
	 * 
	 * @author Alec
	 */
	public static class Tuple implements Comparable<Tuple>, Collection<MTGColor>
	{
		/**
		 * List of colors in the tuple.
		 */
		private List<MTGColor> colors;
		
		/**
		 * Create a new tuple out of the given list of colors.  Unique colors will be extracted
		 * and then sorted around the color pie.
		 * 
		 * @param cols Colors to make the tuple out of
		 */
		public Tuple(Collection<MTGColor> cols)
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
		
		/**
		 * Create a new tuple out of the given colors.
		 * 
		 * @param cols Colors to make the tuple out of
		 */
		public Tuple(MTGColor... cols)
		{
			this(Arrays.asList(cols));
		}
		
		/**
		 * @return The number of colors in this tuple.
		 */
		@Override
		public int size()
		{
			return colors.size();
		}
		
		/**
		 * @return <code>true</code> if there are no colors in this tuple, and
		 * <code>false</code> otherwise.
		 */
		@Override
		public boolean isEmpty()
		{
			return size() == 0;
		}
		
		/**
		 * @param index Index of the color to get
		 * @return The MTGColor at the given index.
		 */
		public MTGColor get(int index)
		{
			return colors.get(index);
		}
		
		/**
		 * @param o Object to look for
		 * @return <code>true</code> if this tuple contains the given object, and
		 * <code>false</code> otherwise.
		 */
		@Override
		public boolean contains(Object o)
		{
			return colors.contains(o);
		}
		
		/**
		 * @param c Collection of objects to look for
		 * @return <code>true</code> if this tuple contains all of the objects in the given
		 * collection, and <code>false</code> otherwise.
		 */
		@Override
		public boolean containsAll(Collection<?> c)
		{
			return colors.containsAll(c);
		}
		
		/**
		 * @return An iterator over the elements in this tuple.
		 */
		@Override
		public Iterator<MTGColor> iterator()
		{
			return colors.iterator();
		}
		
		/**
		 * @return An array containing the elements of this tuple.
		 */
		@Override
		public MTGColor[] toArray()
		{
			return colors.toArray(new MTGColor[colors.size()]);
		}
		
		/**
		 * @param a Array specifying the type of the array to return.
		 * @return An array containing the elements of this tuple.
		 */
		@Override
		public <T> T[] toArray(T[] a)
		{
			return colors.toArray(a);
		}
		
		/**
		 * @param other Tuple to compare with
		 * @return A negative number if this tuple comes before the other one, which happens
		 * if this one has fewer elements or if the colors come before the other one's colors,
		 * 0 if both tuples are the same, and a positive number otherwise.
		 */
		@Override
		public int compareTo(Tuple other)
		{
			int diff = size() - other.size();
			if (diff == 0)
				for (int i = 0; i < size(); i++)
					diff += get(i).compareTo(other.get(i))*Math.pow(10, size() - i);
			return diff;
		}
		
		/**
		 * @param other Object to compare with
		 * @return <code>true</code> if the other object is a Tuple with the same colors
		 * as this one, and <code>false</code> otherwise.
		 */
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
		
		/**
		 * @return An int representation of this Tuple.
		 */
		@Override
		public int hashCode()
		{
			int h = Integer.MAX_VALUE;
			for (MTGColor col: colors)
				h ^= col.hashCode();
			return h;
		}
		
		/**
		 * @return A String representation of this Tuple, which is the same as the
		 * underlying list's String representation.
		 */
		@Override
		public String toString()
		{
			return colors.toString();
		}

		@Override
		public boolean add(MTGColor c)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll(Collection<? extends MTGColor> c)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean remove(Object o)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll(Collection<?> c)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(Collection<?> c)
		{
			throw new UnsupportedOperationException();
		}
	}
}
