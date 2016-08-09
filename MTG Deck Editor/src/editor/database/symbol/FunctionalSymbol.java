package editor.database.symbol;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a symbol that has no variations (like based on color or number).
 * Each one has a different use.
 * 
 * @author Alec Roelke
 */
public class FunctionalSymbol extends Symbol
{
	/**
	 * The Chaos symbol that appears on Plane cards.  Because it has a special
	 * text representation that doens't use {}, it is exposed as a separate constant.
	 */
	public static final FunctionalSymbol CHAOS = new FunctionalSymbol("chaos.png", "CHAOS")
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
	private static final Map<String, FunctionalSymbol> SYMBOLS = new HashMap<String, FunctionalSymbol>();
	static
	{
		// Chaos symbol.
		SYMBOLS.put(CHAOS.toString(), CHAOS);
		// Half-mana symbol.  Represents one half of a generic mana.
		SYMBOLS.put("1/2", new FunctionalSymbol("half_mana.png", "1/2"));
		SYMBOLS.put("½", SYMBOLS.get("1/2"));
		// Infinity mana symbol.  Represents infinity generic mana.
		SYMBOLS.put("∞", new FunctionalSymbol("infinity_mana.png", "∞"));
		// Phyrexian phi symbol.
		SYMBOLS.put("P", new FunctionalSymbol("phyrexia.png", "P"));
		// Snow mana symbol.  Can only be paid with snow mana.
		SYMBOLS.put("S", new FunctionalSymbol("snow_mana.png", "S"));
		// Tap symbol.  Used in costs in card text.
		SYMBOLS.put("T", new FunctionalSymbol("tap.png", "T"));
		SYMBOLS.put("TAP", SYMBOLS.get("T"));
		// Untap symbol.  Used in costs in card text.
		SYMBOLS.put("Q", new FunctionalSymbol("untap.png", "Q"));
	}
	
	/**
	 * Get the StaticSymbol corresponding to the given String.
	 * 
	 * @param s String to look up
	 * @return The StaticSymbol corresponding to the given String,
	 * or null if there is none.
	 */
	public static FunctionalSymbol get(String s)
	{
		return SYMBOLS.get(s.toUpperCase());
	}
	
	/**
	 * Create a new StaticSymbol.
	 * 
	 * @param iconName Icon name of the new symbol
	 * @param text Text representation of the new symbol
	 */
	private FunctionalSymbol(String iconName, String text)
	{
		super(iconName, text);
	}
}
