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
	/**
	 * Card is not legal in the format.
	 */
	BANNED("Banned"),
	/**
	 * A normal number of cards is legal to have in a deck.
	 */
	LEGAL("Legal"),
	/**
	 * Only one copy of the card is legal in a deck.
	 */
	RESTRICTED("Restricted");
	
	/**
	 * Parse a String for a Legality.
	 * 
	 * @param s String to parse
	 * @return the Legality corresponding to the contents of the specified String.
	 * @throws IllegalArgumentException if there is no corresponding Legality
	 */
	public static Legality parseLegality(String s)
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
	Legality(final String legality)
	{
		this.legality = legality;
	}
	
	@Override
	public String toString()
	{
		return legality;
	}
}
