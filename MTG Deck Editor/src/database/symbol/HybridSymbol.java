package database.symbol;

import java.util.HashMap;
import java.util.Map;

import database.characteristics.MTGColor;

/**
 * This class represents a two-color hybrid symbol.  It will sort its colors so that they appear in the correct order.
 * 
 * @author Alec Roelke
 * @see database.characteristics.Color.Tuple
 */
public class HybridSymbol extends Symbol
{
	/**
	 * Map mapping each Tuple of colors to their corresponding hybrid symbols.
	 * @see database.symbol.Symbol
	 */
	public static final Map<MTGColor.Tuple, HybridSymbol> SYMBOLS = new HashMap<MTGColor.Tuple, HybridSymbol>();
	static
	{
		SYMBOLS.put(new MTGColor.Tuple(MTGColor.WHITE, MTGColor.BLUE), new HybridSymbol(new MTGColor.Tuple(MTGColor.WHITE, MTGColor.BLUE)));
		SYMBOLS.put(new MTGColor.Tuple(MTGColor.WHITE, MTGColor.BLACK), new HybridSymbol(new MTGColor.Tuple(MTGColor.WHITE, MTGColor.BLACK)));
		SYMBOLS.put(new MTGColor.Tuple(MTGColor.BLUE, MTGColor.BLACK), new HybridSymbol(new MTGColor.Tuple(MTGColor.BLUE, MTGColor.BLACK)));
		SYMBOLS.put(new MTGColor.Tuple(MTGColor.BLUE, MTGColor.RED), new HybridSymbol(new MTGColor.Tuple(MTGColor.BLUE, MTGColor.RED)));
		SYMBOLS.put(new MTGColor.Tuple(MTGColor.BLACK, MTGColor.RED), new HybridSymbol(new MTGColor.Tuple(MTGColor.BLACK, MTGColor.RED)));
		SYMBOLS.put(new MTGColor.Tuple(MTGColor.BLACK, MTGColor.GREEN), new HybridSymbol(new MTGColor.Tuple(MTGColor.BLACK, MTGColor.GREEN)));
		SYMBOLS.put(new MTGColor.Tuple(MTGColor.RED, MTGColor.GREEN), new HybridSymbol(new MTGColor.Tuple(MTGColor.RED, MTGColor.GREEN)));
		SYMBOLS.put(new MTGColor.Tuple(MTGColor.RED, MTGColor.WHITE), new HybridSymbol(new MTGColor.Tuple(MTGColor.RED, MTGColor.WHITE)));
		SYMBOLS.put(new MTGColor.Tuple(MTGColor.GREEN, MTGColor.WHITE), new HybridSymbol(new MTGColor.Tuple(MTGColor.GREEN, MTGColor.WHITE)));
		SYMBOLS.put(new MTGColor.Tuple(MTGColor.GREEN, MTGColor.BLUE), new HybridSymbol(new MTGColor.Tuple(MTGColor.GREEN, MTGColor.BLUE)));
	}
	
	/**
	 * Get the HybridSymbol corresponding to the given MTGColors.
	 * 
	 * @param colors MTGColors to look up
	 * @return The HybridSymbol corresponding to the given MTGColors, or
	 * null if no such symbol exists.
	 */
	public static HybridSymbol get(MTGColor... colors)
	{
		return SYMBOLS.get(new MTGColor.Tuple(colors));
	}
	
	/**
	 * This HybridSymbol's Tuple of colors.
	 */
	private final MTGColor.Tuple colors;
	
	private HybridSymbol(MTGColor.Tuple colors)
	{
		super(colors.get(0).toString().toLowerCase() + "_" + colors.get(1).toString().toLowerCase() + "_mana.png");
		this.colors = colors;
	}
	
	/**
	 * @return This HybridSymbol's text, which is the shorthand for its two colors separated by a /.
	 * @see database.symbol.Symbol#getText()
	 */
	@Override
	public String getText()
	{
		return colors.get(0).shorthand() + "/" + colors.get(1).shorthand();
	}

	/**
	 * @return This HybridSymbol's value for converted cost, which is 1.
	 * @see database.symbol.Symbol#value()
	 */
	@Override
	public double value()
	{
		return 1;
	}

	/**
	 * @return This HybridSymbol's color weight, which is 0.5 for each of its two colors and
	 * 0 for the other three.
	 * @see database.symbol.Symbol#colorWeights()
	 */
	@Override
	public Map<MTGColor, Double> colorWeights()
	{
		Map<MTGColor, Double> weights = createWeights(0, 0, 0, 0, 0);
		weights.put(colors.get(0), 0.5);
		weights.put(colors.get(1), 0.5);
		return weights;
	}

	/**
	 * @param o Symbol to compare with
	 * @return A negative number if the other symbol should come after this HybridSymbol,
	 * the two symbols' Tuples' difference if they are both HybridSymbols, 0 if color order
	 * doesn't matter, and a postive number if the other symbol should come before.
	 * @see database.symbol.Symbol#compareTo(Symbol)
	 */
	@Override
	public int compareTo(Symbol o)
	{
		if (o instanceof HybridSymbol)
			return colors.compareTo(((HybridSymbol)o).colors);
		else if (o instanceof ColorSymbol || o instanceof PhyrexianSymbol)
			return -1;
		else if (o instanceof ColorlessSymbol || o instanceof HalfManaSymbol
				 || o instanceof HalfColorSymbol || o instanceof SnowSymbol
				 || o instanceof TwobridSymbol || o instanceof XSymbol
				 || o instanceof YSymbol || o instanceof ZSymbol)
			return 1;
		else
			return 0;
	}

	/**
	 * @return <code>true</code> if the other Symbol is a HybridSymbol with the same Tuple of MTGColors,
	 * and <code>false</code>otherwise.
	 * @see database.symbol.Symbol#sameSymbol(Symbol)
	 */
	@Override
	public boolean sameSymbol(Symbol other)
	{
		return other instanceof HybridSymbol
			   && colors.get(0).equals(((HybridSymbol)other).colors.get(0))
			   && colors.get(1).equals(((HybridSymbol)other).colors.get(1));
	}
}
