package editor.database.characteristics;

/**
 * This enum represents a legality in a format.  If a card is banned, it cannot
 * be played in any deck.  If it is restricted, exactly one copy may appear in a deck
 * and its sideboard.  If it is legal, up to four copies (with the exception of basic
 * lands and cards that say otherwise) may appear in a deck and its sideboard.
 * 
 * @author Alec Roelke
 */
public enum Legality
{
	BANNED("Banned"),
	RESTRICTED("Restricted"),
	LEGAL("Legal");
	
	/**
	 * Parse a String for a Legality.
	 * 
	 * @param s String to parse
	 * @return The Legality corresponding to the contents of the specified String.
	 */
	public static Legality get(String s)
	{
		for (Legality l: Legality.values())
			if (s.equalsIgnoreCase(l.toString()))
				return l;
		throw new IllegalArgumentException("Illegal legality string " + s);
	}
	
	/**
	 * Type of legality a card might have in a format.
	 */
	private final String legality;
	
	/**
	 * Create a new Legality.
	 * 
	 * @param legality Type of legality a card might have.
	 */
	private Legality(final String legality)
	{
		this.legality = legality;
	}
	
	/**
	 * @return A String representation of this Legality.
	 */
	@Override
	public String toString()
	{
		return legality;
	}
}
