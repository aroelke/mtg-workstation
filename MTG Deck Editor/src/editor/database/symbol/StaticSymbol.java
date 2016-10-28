package editor.database.symbol;

import java.util.HashMap;
import java.util.Map;

import editor.database.characteristics.ManaType;
import editor.util.UnicodeSymbols;

/**
 * This class represents a symbol that has no variations (like based on color or number).
 * Each one has a different use.
 * 
 * @author Alec Roelke
 */
public class StaticSymbol extends ManaSymbol
{
	/**
	 * Map of symbol texts onto their respective symbols.
	 */
	private static final Map<String, StaticSymbol> SYMBOLS = new HashMap<String, StaticSymbol>();
	static
	{
		// Half-mana symbol.  Represents one half of a generic mana.
		SYMBOLS["1/2"] = new StaticSymbol("half_mana.png", "1/2", 0.5);
		SYMBOLS[String.valueOf(UnicodeSymbols.ONE_HALF)] = SYMBOLS["1/2"];
		// Infinity mana symbol.  Represents infinity generic mana.
		SYMBOLS[String.valueOf(UnicodeSymbols.INFINITY)] = new StaticSymbol("infinity_mana.png", String.valueOf(UnicodeSymbols.INFINITY), Double.POSITIVE_INFINITY);
		// Snow mana symbol.  Can only be paid with snow mana.
		SYMBOLS["S"] = new StaticSymbol("snow_mana.png", "S", 1);
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
		return SYMBOLS[s.toUpperCase()];
	}
	
	/**
	 * Create a new StaticSymbol.
	 * 
	 * @param iconName Icon name of the new symbol
	 * @param text Text representation of the new symbol
	 * @param v sorting value of the new StaticSymbol
	 */
	private StaticSymbol(String iconName, String text, double value)
	{
		super(iconName, text, value);
	}

	/**
	 * @return The color weights of this StaticSymbol, which are none.
	 */
	@Override
	public Map<ManaType, Double> colorWeights()
	{
		return createWeights();
	}
}
