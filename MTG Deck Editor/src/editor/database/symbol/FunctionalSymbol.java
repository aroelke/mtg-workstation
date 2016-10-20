package editor.database.symbol;

import java.util.Collections;
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
	private static final Map<String, FunctionalSymbol> SYMBOLS;
	static
	{
		Map<String, FunctionalSymbol> symbols = new HashMap<String, FunctionalSymbol>();
		// Chaos symbol.
		symbols[CHAOS.toString()] = CHAOS;
		// Phyrexian phi symbol.
		symbols["P"] = new FunctionalSymbol("phyrexia.png", "P");
		// Tap symbol.  Used in costs in card text.
		symbols["T"] = new FunctionalSymbol("tap.png", "T");
		symbols["TAP"] = symbols["T"];
		// Untap symbol.  Used in costs in card text.
		symbols["Q"] = new FunctionalSymbol("untap.png", "Q");
		// Energy counter symbol.
		symbols["E"] = new FunctionalSymbol("energy.png", "E");
		
		SYMBOLS = Collections.unmodifiableMap(symbols);
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
		return SYMBOLS[s.toUpperCase()];
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
