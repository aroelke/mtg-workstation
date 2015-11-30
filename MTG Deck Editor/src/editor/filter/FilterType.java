package editor.filter;

/**
 * This enum represents a type of filter that can be used to filter Cards.
 * 
 * @author Alec Roelke
 */
public enum FilterType
{
	NAME("Name", "n"),
	MANA_COST("Mana Cost", "m"),
	CMC("CMC", "cmc"),
	COLOR("Color", "c"),
	COLOR_IDENTITY("Color Identity", "ci"),
	TYPE_LINE("Type Line", "type"),
	SUPERTYPE("Supertype", "super"),
	TYPE("Card Type", "cardtype"),
	SUBTYPE("Subtype", "sub"),
	EXPANSION("Expansion", "x"),
	BLOCK("Block", "b"),
	RARITY("Rarity", "r"),
	RULES_TEXT("Rules Text", "o"),
	FLAVOR_TEXT("Flavor Text", "f"),
	POWER("Power", "p"),
	TOUGHNESS("Toughness", "t"),
	LOYALTY("Loyalty", "l"),
	ARTIST("Artist", "a"),
	CARD_NUMBER("Card Number", "#"),
	FORMAT_LEGALITY("Format Legality", "legal"),
	DEFAULTS("Defaults", ""),
	NONE("<No Card>", "0"),
	ALL("<Any Card>", "*");
	
	/**
	 * Get a FilterType from a String.
	 * 
	 * @param c String to parse
	 * @return The FilterType corresponding to the String.
	 */
	public static FilterType fromCode(String c)
	{
		for (FilterType filterType: FilterType.values())
			if (c.equalsIgnoreCase(filterType.code))
				return filterType;
		throw new IllegalArgumentException("Illegal filter type string " + c);
	}
	
	/**
	 * Name of this FilterType.
	 */
	private final String name;
	/**
	 * Code for this FilterType to figure out which panel to set content for.
	 */
	public final String code;
	
	/**
	 * Create a new FilterType.
	 * 
	 * @param n Name of the new FilterType.
	 * @param c Code of the new FilterType.
	 */
	private FilterType(String n, String c)
	{
		name = n;
		code = c;
	}
	
	/**
	 * @return A String representation of this FilterType, which is it's name.
	 */
	@Override
	public String toString()
	{
		return name;
	}
}
