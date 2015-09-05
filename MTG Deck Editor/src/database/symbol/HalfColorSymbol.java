package database.symbol;

import java.util.HashMap;
import java.util.Map;

import database.characteristics.MTGColor;

/**
 * This class represents half of a colored mana symbol.
 * 
 * @author Alec Roelke
 */
public class HalfColorSymbol extends Symbol
{
	/**
	 * Map of MTGColor onto its corresponding half-symbol.  To get a colored half-symbol, use this Map.
	 * @see database.symbol.Symbol
	 */
	public static final Map<MTGColor, HalfColorSymbol> SYMBOLS = new HashMap<MTGColor, HalfColorSymbol>();
	static
	{
		SYMBOLS.put(MTGColor.WHITE, new HalfColorSymbol(MTGColor.WHITE));
		SYMBOLS.put(MTGColor.BLUE, new HalfColorSymbol(MTGColor.BLUE));
		SYMBOLS.put(MTGColor.BLACK, new HalfColorSymbol(MTGColor.BLACK));
		SYMBOLS.put(MTGColor.RED, new HalfColorSymbol(MTGColor.RED));
		SYMBOLS.put(MTGColor.GREEN, new HalfColorSymbol(MTGColor.GREEN));
	}
	
	/**
	 * Get the HalfColorSymbol corresponding to the given MTGColor.
	 * 
	 * @param col MTGColor to get the symbol for
	 * @return The HalfColorSymbol corresponding to the given MTGColor, or null
	 * if no such symbol exists.
	 */
	public static HalfColorSymbol get(MTGColor col)
	{
		return SYMBOLS.get(col);
	}
	
	/**
	 * This HalfColorSymbol's color.
	 */
	private final MTGColor color;
	
	/**
	 * Create a new HalfColorSymbol.
	 * 
	 * @param color Color of the new HalfColorSymbol
	 */
	private HalfColorSymbol(MTGColor color)
	{
		super("half_" + color.toString().toLowerCase() + "_mana.png");
		this.color = color;
	}
	
	/**
	 * @return An 'H' followed by the shorthand representation of this HalfColorSymbol's color.
	 * @see database.symbol.Symbol#getText()
	 */
	@Override
	public String getText()
	{
		return "H" + String.valueOf(color.shorthand());
	}

	/**
	 * @return The value of this symbol for converted mana costs: 0.5.
	 * @see database.symbol.Symbol#value()
	 */
	@Override
	public double value()
	{
		return 0.5;
	}

	/**
	 * @return A Map containing this HalfColorSymbol's color weight.  All values will be 0 except for
	 * this HalfColorSymbol's color, which will be 0.5.
	 * @see database.symbol.Symbol#colorWeights()
	 */
	@Override
	public Map<MTGColor, Double> colorWeights()
	{
		Map<MTGColor, Double> weights = createWeights(0, 0, 0, 0, 0);
		weights.put(color, 0.5);
		return weights;
	}

	/**
	 * @param o Symbol to compare with
	 * @return A negative number if the other symbol is a colored symbol or a hybrid symbol,
	 * a positive number if it is a colorless symbol, the color-order difference between the two
	 * symbols if the other symbol is a colored half-mana symbol, or 0 otherwise.
	 * @see database.symbol.Symbol#compareTo(Symbol)
	 */
	@Override
	public int compareTo(Symbol o)
	{
		if (o instanceof HalfColorSymbol)
			return color.colorOrder(((HalfColorSymbol)o).color);
		else if (o instanceof ColorSymbol || o instanceof HybridSymbol
				 || o instanceof PhyrexianSymbol || o instanceof SnowSymbol
				 || o instanceof TwobridSymbol)
			return -1;
		else if (o instanceof HalfManaSymbol || o instanceof ColorlessSymbol
				 || o instanceof XSymbol || o instanceof YSymbol || o instanceof ZSymbol)
			return 1;
		else
			return 0;
	}

	/**
	 * @return <code>true</code> if the other Symbol is a HalfColorSymbol and the colors are the same
	 * or <code>false</code> otherwise.
	 * @see database.symbol.Symbol#sameSymbol(Symbol)
	 */
	@Override
	public boolean sameSymbol(Symbol other)
	{
		return other instanceof HalfColorSymbol && color.equals(((HalfColorSymbol)other).color);
	}
}
