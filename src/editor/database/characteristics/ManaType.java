package editor.database.characteristics;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
	 * @param color Character to get a color from
	 * @return ManaType that corresponds to the character
	 * @throws IllegalArgumentException if the character does not correspond to a mana type
	 */
	public static ManaType parseManaType(char color) throws IllegalArgumentException
	{
		ManaType type = tryParseManaType(color);
		if (type == null)
			throw new IllegalArgumentException("Illegal color shorthand '" + color + "'");
		return type;
	}
	
	/**
	 * Get a ManaType from a String.  Acceptable values are "white," "w," "blue,"
	 * "u," "black," "b," "red," "r," "green," "g," "colorless," or "c," case
	 * insensitive.
	 * 
	 * @param color string to create an ManaType from
	 * @return the ManaType that corresponds to the String
	 * @throws IllegalArgumentException if the String does not correspond to a mana type
	 */
	public static ManaType parseManaType(String color) throws IllegalArgumentException
	{
		ManaType type = tryParseManaType(color);
		if (type == null)
			throw new IllegalArgumentException("Illegal color string \"" + color + "\"");
		return type;
	}
	
	/**
	 * Sort a list of ManaTypes in color order.  It is recommended to use this rather than
	 * using Java's built-in sorting functions.
	 * 
	 * @param colors List of ManaTypes to sort
	 */
	public static void sort(List<ManaType> colors)
	{
		Map<ManaType, Integer> counts = new EnumMap<ManaType, Integer>(ManaType.class);
		for (ManaType type: colors)
			counts.compute(type, (k, v) -> v == null ? 1 : v + 1);
		
		List<ManaType> unique = Arrays.stream(colors()).filter(counts::containsKey).collect(Collectors.toList());
		switch (unique.size())
		{
		case 2:
			if (unique.get(0).colorOrder(unique.get(1)) > 0)
				Collections.reverse(unique);
			break;
		case 3:
			while (unique.get(0).distanceFrom(unique.get(1)) != unique.get(1).distanceFrom(unique.get(2)))
				Collections.rotate(unique, 1);
			break;
		case 4:
			ManaType missing = Arrays.stream(colors()).filter((m) -> !counts.containsKey(m)).collect(Collectors.toList()).get(0);
			while (missing.distanceFrom(unique.get(0)) != 1)
				Collections.rotate(unique, 1);
			break;
		default:
			// Don't have to do anything if there are 0, 1, or all 5 colors
			break;
		}
		
		colors.clear();
		if (counts.containsKey(COLORLESS))
			colors.addAll(Collections.nCopies(counts.get(COLORLESS), COLORLESS));
		for (ManaType type: unique)
			colors.addAll(Collections.nCopies(counts.get(type), type));
	}
	
	/**
	 * Get a ManaType from a character.  Acceptable characters are 'w,' 'u,' 'b,'
	 * 'r,' 'g,' or 'c,' case insensitive.
	 * 
	 * @param color Character to get a color from
	 * @return ManaType that corresponds to the character, or null if there is none
	 */
	public static ManaType tryParseManaType(char color)
	{
		for (ManaType c: ManaType.values())
			if (Character.toLowerCase(c.shorthand) == Character.toLowerCase(color))
				return c;
		return null;
	}
	
	/**
	 * Get a ManaType from a String.  Acceptable values are "white," "w," "blue,"
	 * "u," "black," "b," "red," "r," "green," "g," "colorless," or "c," case
	 * insensitive.
	 * 
	 * @param color string to create an ManaType from
	 * @return the ManaType that corresponds to the String, or null if there is none
	 */
	public static ManaType tryParseManaType(String color)
	{
		for (ManaType c: ManaType.values())
			if (c.name.equalsIgnoreCase(color) || color.equalsIgnoreCase(String.valueOf(c.shorthand)))
				return c;
		return null;
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
	ManaType(final String n, final char s, final Color c)
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
	public int distanceFrom(ManaType other)
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
