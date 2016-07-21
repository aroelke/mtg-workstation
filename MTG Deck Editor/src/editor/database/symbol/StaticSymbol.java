package editor.database.symbol;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a symbol that has no variations (like based on color or number).
 * Each one has a different use.
 * 
 * @author Alec Roelke
 */
public class StaticSymbol extends Symbol
{
	/**
	 * The Chaos symbol that appears on Plane cards.  Because it has a special
	 * text representation that doens't use {}, it is exposed as a separate constant.
	 */
	public static final StaticSymbol CHAOS = new StaticSymbol("chaos.png", "CHAOS")
	{
		@Override
		public String toString()
		{
			return "CHAOS";
		}
	};
	/**
	 * Map of symbol texts onto their respective symbols.
	 */
	public static final Map<String, StaticSymbol> SYMBOLS = new HashMap<String, StaticSymbol>();
	static
	{
		// Chaos symbol.
		SYMBOLS.put(CHAOS.text, CHAOS);
		// Half-mana symbol.  Represents one half of a generic mana.
		SYMBOLS.put("1/2", new StaticSymbol("half_mana.png", "1/2"));
		SYMBOLS.put("½", SYMBOLS.get("1/2"));
		// Infinity mana symbol.  Represents infinity generic mana.
		SYMBOLS.put("∞", new StaticSymbol("infinity_mana.png", "∞"));
		// Phyrexian phi symbol.
		SYMBOLS.put("P", new StaticSymbol("phyrexia.png", "P"));
		// Snow mana symbol.  Can only be paid with snow mana.
		SYMBOLS.put("S", new StaticSymbol("snow_mana.png", "S"));
		// Tap symbol.  Used in costs in card text.
		SYMBOLS.put("T", new StaticSymbol("tap.png", "T"));
		SYMBOLS.put("TAP", SYMBOLS.get("T"));
		// Untap symbol.  Used in costs in card text.
		SYMBOLS.put("Q", new StaticSymbol("untap.png", "Q"));
	}
	
	/**
	 * Get the StaticSymbol corresponding to the given String.
	 * 
	 * @param s String to look up
	 * @return The StaticSymbol corresponding to the given String,
	 * or null if there is none.
	 */
	public static StaticSymbol get(String s)
	{
		return SYMBOLS.get(s.toUpperCase());
	}
	
	/**
	 * Text representation of this StaticSymbol.
	 */
	private String text;
	
	/**
	 * Create a new StaticSymbol.
	 * 
	 * @param iconName Icon name of the new symbol
	 * @param t Text representation of the new symbol
	 */
	private StaticSymbol(String iconName, String t)
	{
		super(iconName);
		text = t;
	}

	/**
	 * @return The text representation of this StaticSymbol.
	 */
	@Override
	public String getText()
	{
		return text;
	}
}
