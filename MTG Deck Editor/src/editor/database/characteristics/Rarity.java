package editor.database.characteristics;

/**
 * This enum represents a rarity a Magic: The Gathering card can have.
 * 
 * @author Alec Roelke
 */
public enum Rarity implements CharSequence
{
	BASIC_LAND("Basic Land"),
	COMMON("Common"),
	UNCOMMON("Uncommon"),
	RARE("Rare"),
	MYTHIC_RARE("Mythic Rare"),
	SPECIAL("Special");
	
	/**
	 * Create a Rarity from the specified String.
	 * 
	 * @param rarity String to create a Rarity from.
	 * @return A Rarity representing the specified String.
	 * @throws IllegalArgumentException If a Rarity cannot be created from the String.
	 */
	public static Rarity get(String rarity)
	{
		for (Rarity r: Rarity.values())
			if (rarity.equalsIgnoreCase(r.rarity) || rarity.equalsIgnoreCase(String.valueOf(r.shorthand())))
				return r;
		if (rarity.contains("mythic"))
			return MYTHIC_RARE;
		else if (rarity.contains("basic"))
			return BASIC_LAND;
		else
			throw new IllegalArgumentException("Illegal rarity string \"" + rarity + "\"");
	}
	
	/**
	 * Create a rarity from a shorthand character.
	 * 
	 * @param rarity Character to create a Rarity from.
	 * @return A Rarity representing the specified shorthand character.
	 * @throws IllegalArgumentException If a Rarity cannot be created from the specified character.
	 */
	public static Rarity get(char rarity)
	{
		rarity = Character.toLowerCase(rarity);
		switch (rarity)
		{
		case 'c':
			return COMMON;
		case 'u':
			return UNCOMMON;
		case 'r':
			return RARE;
		case 'm':
			return MYTHIC_RARE;
		case 's':
			return SPECIAL;
		case 'b':
			return BASIC_LAND;
		default:
			throw new IllegalArgumentException("Illegal rarity shorthand");
		}
	}
	
	/**
	 * String representation of this Rarity.
	 */
	private final String rarity;
	
	/**
	 * Create a new Rarity.
	 * 
	 * @param rarity String representation of the new Rarity.
	 */
	private Rarity(final String rarity)
	{
		this.rarity = rarity;
	}
	
	/**
	 * @return A shorthand character representing this Rarity.
	 */
	public char shorthand()
	{
		switch(this)
		{
		case COMMON:
			return 'C';
		case UNCOMMON:
			return 'U';
		case RARE:
			return 'R';
		case MYTHIC_RARE:
			return 'M';
		case SPECIAL:
			return 'S';
		case BASIC_LAND:
			return 'B';
		default:
			return '\0';
		}
	}

	/**
	 * Get the character of this Rarity's String representation at the specified index.
	 * 
	 * @param index Index of the character to get
	 * @return Character at the specified index of this Rarity's String representation
	 * @see java.lang.CharSequence#charAt(int)
	 */
	@Override
	public char charAt(int index)
	{
		return rarity.charAt(index);
	}

	/**
	 * @return The length of this Rarity's String representation.
	 * @see java.lang.CharSequence#length()
	 */
	@Override
	public int length()
	{
		return rarity.length();
	}

	/**
	 * Get a subsequence of this Rarity's String representation.
	 * 
	 * @param start Starting index of the subsequence to get (inclusive)
	 * @param end Ending index of the subsequence to get (noninclusive)
	 * @return The sequence of characters starting at start and ending just before end.
	 * @see java.lang.CharSequence#subSequence(int, int)
	 */
	@Override
	public CharSequence subSequence(int start, int end)
	{
		return rarity.subSequence(start, end);
	}
	
	/**
	 * @return The String representation of this Rarity.
	 */
	@Override
	public String toString()
	{
		return rarity;
	}
}
