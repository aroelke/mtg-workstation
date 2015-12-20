package editor.database.symbol;

import java.util.HashMap;
import java.util.Map;

import editor.database.characteristics.MTGColor;

/**
 * This class represents a symbol for a single colored mana.
 * 
 * @author Alec Roelke
 */
public class ColorSymbol extends Symbol
{
	/**
	 * Map of MTGColor onto its corresponding symbol.  To get a colored symbol, use this Map.
	 * @see editor.database.symbol.Symbol
	 */
	public static final Map<MTGColor, ColorSymbol> SYMBOLS = new HashMap<MTGColor, ColorSymbol>();
	static
	{
		SYMBOLS.put(MTGColor.WHITE, new ColorSymbol(MTGColor.WHITE));
		SYMBOLS.put(MTGColor.BLUE, new ColorSymbol(MTGColor.BLUE));
		SYMBOLS.put(MTGColor.BLACK, new ColorSymbol(MTGColor.BLACK));
		SYMBOLS.put(MTGColor.RED, new ColorSymbol(MTGColor.RED));
		SYMBOLS.put(MTGColor.GREEN, new ColorSymbol(MTGColor.GREEN));
	}
	
	/**
	 * Get the symbol corresponding to a color.
	 * 
	 * @param col MTGColor to find the symbol for
	 * @return The ColorSymbol corresponding to the given MTGColor, or null
	 * if none exists. 
	 */
	public static ColorSymbol get(MTGColor col)
	{
		return SYMBOLS.get(col);
	}
	
	/**
	 * Color of this ColorSymbol.
	 */
	private final MTGColor color;
	
	/**
	 * Create a new ColorSymbol.
	 * 
	 * @param color Color of the new ColorSymbol.
	 */
	private ColorSymbol(MTGColor color)
	{
		super(color.toString().toLowerCase() + "_mana.png");
		this.color = color;
	}
	
	/**
	 * @return The MTGColor representing the color of this ColorSymbol.
	 */
	public MTGColor color()
	{
		return color;
	}
	
	/**
	 * @return The shorthand character representing the color of this ColorSymbol.
	 * @see editor.database.symbol.Symbol#getText()
	 */
	@Override
	public String getText()
	{
		return String.valueOf(color.shorthand());
	}

	/**
	 * @return The value of this symbol for converted mana costs: 1.
	 * @see editor.database.symbol.Symbol#value()
	 */
	@Override
	public double value()
	{
		return 1;
	}

	/**
	 * @return A Map containing this ColorSymbol's color weight.  All values will be 0 except for
	 * this ColorSymbol's color, which will be 1.
	 * @see editor.database.symbol.Symbol#colorWeights()
	 */
	@Override
	public Map<MTGColor, Double> colorWeights()
	{
		Map<MTGColor, Double> weights = createWeights(0, 0, 0, 0, 0);
		weights.put(color, 1.0);
		return weights;
	}

	/**
	 * @param o Symbol to compare with
	 * @return A positive number if the other symbol can appear in mana costs but is not
	 * a ColorSymbol; the color-ordering of this symbol and the other symbol if they are
	 * both ColorSymbols; or 0 otherwise.
	 * @see editor.database.symbol.Symbol#compareTo(Symbol)
	 */
	@Override
	public int compareTo(Symbol o)
	{
		if (o instanceof ColorSymbol)
			return color.colorOrder(((ColorSymbol)o).color);
		else
			return super.compareTo(o);
	}

	/**
	 * @return <code>true</code> if the other Symbol is a ColorSymbol and the colors are the same
	 * or <code>false</code> otherwise.
	 * @see editor.database.symbol.Symbol#sameSymbol(Symbol)
	 */
	@Override
	public boolean sameSymbol(Symbol other)
	{
		return other instanceof ColorSymbol && color.equals(((ColorSymbol)other).color);
	}
}
