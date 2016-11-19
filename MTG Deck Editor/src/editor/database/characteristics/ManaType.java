package editor.database.characteristics;

import java.awt.Color;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * This enum represents one of the five colors of Magic: The Gathering.
 * 
 * @author Alec Roelke
 */
public enum ManaType
{
	/**
	 * White mana.
	 */
	WHITE("White", 'W', Color.YELLOW.darker()),
	/**
	 * Blue mana.
	 */
	BLUE("Blue", 'U', Color.BLUE),
	/**
	 * Black mana.
	 */
	BLACK("Black", 'B', Color.BLACK),
	/**
	 * Red mana.
	 */
	RED("Red", 'R', Color.RED),
	/**
	 * Green mana.
	 */
	GREEN("Green", 'G', Color.GREEN.darker()),
	/**
	 * Colorless mana.  While this is not a color, it is a type.
	 */
	COLORLESS("Colorless", 'C', null);
	
	/**
	 * This class represents a sorted list of unique colors in the correct order around
	 * the color pie.
	 * 
	 * @author Alec Roelke
	 */
	public static class Tuple extends AbstractList<ManaType> implements Comparable<Tuple>
	{
		/**
		 * Helper method for cleaning and sorting a collection of colors before calling the
		 * super constructor on it.
		 * 
		 * @param cols collection of colors to sort
		 * @return a cleaned and sorted copy of the given collection of colors.  Each color
		 * will only appear once.
		 */
		private static List<ManaType> sorted(Collection<ManaType> cols)
		{
			List<ManaType> colors = new ArrayList<ManaType>(new HashSet<ManaType>(cols));
			boolean colorless = colors.remove(COLORLESS);
			Collections.sort(colors);
			switch (colors.size())
			{
			case 2:
				if (colors[0].colorOrder(colors[1]) > 0)
					Collections.reverse(colors);
				break;
			case 3:
				while (colors[0].distance(colors[1]) != colors[1].distance(colors[2]))
					Collections.rotate(colors, 1);
				break;
			case 4:
				boolean equal;
				do
				{
					equal = true;
					for (int i = 0; i < 3; i++)
					{
						if (colors[i].distance(colors[i + 1]) != 1)
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
			if (colorless)
				colors.add(0, COLORLESS);
			return colors;
		}
		
		/**
		 * ManaTypes in this Tuple.
		 */
		private final List<ManaType> types;
		
		/**
		 * Create a new, empty Tuple of ManaTypes.
		 */
		public Tuple()
		{
			this(Collections.emptyList());
		}
		
		/**
		 * Create a new Tuple out of the given list of colors.  Unique colors will be extracted
		 * and then sorted around the color pie.
		 * 
		 * @param cols colors to make the tuple out of
		 */
		public Tuple(Collection<? extends ManaType> cols)
		{
			types = Collections.unmodifiableList(sorted(new ArrayList<ManaType>(cols)));
		}
		
		/**
		 * Create a new Tuple out of the given colors.
		 * 
		 * @param cols Colors to make the tuple out of
		 */
		public Tuple(ManaType... cols)
		{
			this(Arrays.asList(cols));
		}
		
		@Override
		public int compareTo(Tuple other)
		{
			int diff = size() - other.size();
			if (diff == 0)
				for (int i = 0; i < size(); i++)
					diff += this[i].compareTo(other[i])*Math.pow(10, size() - i);
			return diff;
		}

		@Override
		public ManaType get(int index)
		{
			return types[index];
		}

		@Override
		public int size()
		{
			return types.size();
		}
	}
	
	/**
	 * Get the ManaTypes that represent colors, which is all of them except
	 * {@link #COLORLESS}.
	 * 
	 * @return the colors of Magic, which is the list of ManaTypes
	 * minus the ManaTypes that do not represent colors.
	 */
	public static ManaType[] colors()
	{
		return new ManaType[] {WHITE, BLUE, BLACK, RED, GREEN};
	}
	
	/**
	 * Get a ManaType from a character.  Acceptable characters are 'w,' 'u,' 'b,'
	 * 'r,' 'g,' or 'c,' case insensitive.
	 * 
	 * @param color Character to get a color from.
	 * @return ManaType that corresponds to the character.
	 */
	public static ManaType get(char color)
	{
		for (ManaType c: ManaType.values())
			if (Character.toLowerCase(c.shorthand) == Character.toLowerCase(color))
				return c;
		throw new IllegalArgumentException("Illegal color shorthand '" + color + "'");
	}
	
	/**
	 * Get a ManaType from a String.  Acceptable values are "white," "w," "blue,"
	 * "u," "black," "b," "red," "r," "green," "g," "colorless," or "c," case
	 * insensitive.
	 * 
	 * @param color string to create an ManaType from.
	 * @return the ManaType that corresponds to the String.
	 */
	public static ManaType get(String color)
	{
		for (ManaType c: ManaType.values())
			if (c.name.equalsIgnoreCase(color) || color.equalsIgnoreCase(String.valueOf(c.shorthand)))
				return c;
		throw new IllegalArgumentException("Illegal color string \"" + color + "\"");
	}
	
	/**
	 * Sort a list of ManaTypes in color order.  If the list contains two colors, it will be
	 * sorted according to how they appear on a card.  Otherwise, it will be sorted according
	 * to CWUBRG order.  It is recommended to use this rather than using Java's built-in sorting
	 * functions.  If the list contains any duplicate colors, they will be removed.
	 * 
	 * TODO: Make this not remove duplicate colors
	 * 
	 * @param colors List of ManaTypes to sort.
	 */
	public static void sort(List<ManaType> colors)
	{
		if (!colors.isEmpty())
		{
			Tuple t = new Tuple(colors);
			colors.clear();
			colors.addAll(t);
		}
	}
	
	/**
	 * Color corresponding to this ManaType (should be null for colorless).
	 */
	public final Color color;
	/**
	 * String representation of this ManaType.
	 */
	private final String name;
	/**
	 * Single-character shorthand for this ManaType.
	 */
	private final char shorthand;
	
	/**
	 * Create a new ManaType.
	 * 
	 * @param n String representation of the new ManaType
	 * @param s single-character shorthand representation of the new ManaType
	 * @param c color corresponding to this ManaType
	 */
	private ManaType(final String n, final char s, final Color c)
	{
		name = n;
		shorthand = s;
		color = c;
	}
	
	/**
	 * Compare this ManaType to another ManaType according to the order they would appear in
	 * a mana cost.  The ordering is determined according to the direction around the color pie
	 * in which the distance from this ManaType to the other is shortest.
	 * 
	 * @param other ManaType to compare to
	 * @return a negative number if this ManaType should come first, 0 if they are the same,
	 * or a positive number if it should come after.
	 * @see #compareTo(ManaType)
	 */
	public int colorOrder(ManaType other)
	{
		if (this == COLORLESS && other == COLORLESS)
			return 0;
		else if (this == COLORLESS)
			return -1;
		else if (other == COLORLESS)
			return 1;
		else
		{
			int diff = compareTo(other);
			return Math.abs(diff) <= 2 ? diff : -diff;
		}
	}
	
	/**
	 * Get the shortest distance around the color pie from this ManaType to the other.
	 * 
	 * @param other ManaType to compare to
	 * @return The distance around the color pie from this color to the other ManaType.
	 */
	public int distance(ManaType other)
	{
		if (this == COLORLESS || other == COLORLESS)
			throw new IllegalArgumentException("Colorless is not a color");
		return (other.ordinal() - ordinal() + colors().length)%colors().length;
	}
	
	/**
	 * Get the one-character shorthand for this ManaType.
	 * 
	 * @return a one-character shorthand for the name of this ManaType.
	 */
	public char shorthand()
	{
		return shorthand;
	}
	
	@Override
	public String toString()
	{
		return name;
	}
}
