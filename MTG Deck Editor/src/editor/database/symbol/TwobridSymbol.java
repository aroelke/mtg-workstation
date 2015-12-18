package editor.database.symbol;

import java.util.HashMap;
import java.util.Map;

import editor.database.characteristics.MTGColor;

/**
 * This class represents a colorless-colored hybrid mana symbol, which can be paid for either by one
 * mana of the corresponding color, or two mana of any color.  These are referred to as "twobrid"
 * symbols.
 * 
 * @author Alec Roelke
 */
public class TwobridSymbol extends Symbol
{
	/**
	 * Map of colors onto their corresponding twobrid symbols.
	 * @see editor.database.symbol.Symbol
	 */
	public static final Map<MTGColor, TwobridSymbol> SYMBOLS = new HashMap<MTGColor, TwobridSymbol>();
	static
	{
		SYMBOLS.put(MTGColor.WHITE, new TwobridSymbol(MTGColor.WHITE));
		SYMBOLS.put(MTGColor.BLUE, new TwobridSymbol(MTGColor.BLUE));
		SYMBOLS.put(MTGColor.BLACK, new TwobridSymbol(MTGColor.BLACK));
		SYMBOLS.put(MTGColor.RED, new TwobridSymbol(MTGColor.RED));
		SYMBOLS.put(MTGColor.GREEN, new TwobridSymbol(MTGColor.GREEN));
	}
	
	/**
	 * Get the TwobridSymbol corresponding to the given color.
	 * 
	 * @param col Color to look up
	 * @return The TwobridSymbol corresponding to the given MTGColor, or
	 * null if no such symbol exists.
	 */
	public static TwobridSymbol get(MTGColor col)
	{
		return SYMBOLS.get(col);
	}
	
	/**
	 * This TwobridSymbol's color.
	 */
	private final MTGColor color;
	
	/**
	 * Create a TwobridSymbol
	 * 
	 * @param color The new TwobridSymbol's color.
	 */
	private TwobridSymbol(MTGColor color)
	{
		super("2_" + color.toString().toLowerCase() + "_mana.png");
		this.color = color;
	}
	
	/**
	 * @return The MTGColor of this TwobridSymbol.
	 */
	public MTGColor color()
	{
		return color;
	}
	
	/**
	 * @return This TwobridSymbol's text, which is a "2/" followed by its color shorthand.
	 * @see editor.database.symbol.Symbol#getText()
	 */
	@Override
	public String getText()
	{
		return "2/" + String.valueOf(color.shorthand());
	}

	/**
	 * @return This TwobridSymbol's value in converted mana costs, which is 2.
	 * @see editor.database.symbol.Symbol#value()
	 */
	@Override
	public double value()
	{
		return 2;
	}

	/**
	 * @return This TwobridSymbol's color weight, which is 0.5 for its color and 0
	 * for the others.
	 * @see editor.database.symbol.Symbol#colorWeights()
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
	 * @return A negative number if this TwobridSymbol should come before the other
	 * Symbol in costs, the color ordering of the two symbols if they are both
	 * TwobridSymbols, a postive number if it should come after, and 0 if ordering
	 * is not defined.
	 * @see editor.database.symbol.Symbol#compareTo(Symbol)
	 */
	@Override
	public int compareTo(Symbol o)
	{
		if (o instanceof TwobridSymbol)
			return color.colorOrder(((TwobridSymbol)o).color);
		else if (o instanceof ColorSymbol || o instanceof HybridSymbol
				 || o instanceof PhyrexianSymbol || o instanceof ColorlessSymbol)
			return -1;
		else if (o instanceof GenericSymbol || o instanceof HalfColorSymbol
				 || o instanceof HalfManaSymbol || o instanceof SnowSymbol
				 || o instanceof VariableSymbol || o instanceof HalfColorlessSymbol)
			return 1;
		else
			return 0;
	}

	/**
	 * @return <code>true</code> if the other Symbol is a TwobridSymbol of the same color
	 * and <code>false</code> otherwise.
	 * @see editor.database.symbol.Symbol#sameSymbol(Symbol)
	 */
	@Override
	public boolean sameSymbol(Symbol other)
	{
		return other instanceof TwobridSymbol && color.equals(((TwobridSymbol)other).color);
	}
}
