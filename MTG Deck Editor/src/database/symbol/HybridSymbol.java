package database.symbol;

import java.util.HashMap;
import java.util.Map;

import database.characteristics.MTGColor;

/**
 * This class represents a two-color hybrid symbol.  It will sort its colors so that they appear in the correct order.
 * 
 * @author Alec Roelke
 * @see database.characteristics.Color.Pair
 */
public class HybridSymbol extends Symbol
{
	/**
	 * Map mapping each pair of colors to their corresponding hybrid symbols.
	 * @see database.symbol.Symbol
	 */
	public static final Map<MTGColor.Pair, HybridSymbol> SYMBOLS = new HashMap<MTGColor.Pair, HybridSymbol>();
	static
	{
		SYMBOLS.put(new MTGColor.Pair(MTGColor.WHITE, MTGColor.BLUE), new HybridSymbol(new MTGColor.Pair(MTGColor.WHITE, MTGColor.BLUE)));
		SYMBOLS.put(new MTGColor.Pair(MTGColor.WHITE, MTGColor.BLACK), new HybridSymbol(new MTGColor.Pair(MTGColor.WHITE, MTGColor.BLACK)));
		SYMBOLS.put(new MTGColor.Pair(MTGColor.BLUE, MTGColor.BLACK), new HybridSymbol(new MTGColor.Pair(MTGColor.BLUE, MTGColor.BLACK)));
		SYMBOLS.put(new MTGColor.Pair(MTGColor.BLUE, MTGColor.RED), new HybridSymbol(new MTGColor.Pair(MTGColor.BLUE, MTGColor.RED)));
		SYMBOLS.put(new MTGColor.Pair(MTGColor.BLACK, MTGColor.RED), new HybridSymbol(new MTGColor.Pair(MTGColor.BLACK, MTGColor.RED)));
		SYMBOLS.put(new MTGColor.Pair(MTGColor.BLACK, MTGColor.GREEN), new HybridSymbol(new MTGColor.Pair(MTGColor.BLACK, MTGColor.GREEN)));
		SYMBOLS.put(new MTGColor.Pair(MTGColor.RED, MTGColor.GREEN), new HybridSymbol(new MTGColor.Pair(MTGColor.RED, MTGColor.GREEN)));
		SYMBOLS.put(new MTGColor.Pair(MTGColor.RED, MTGColor.WHITE), new HybridSymbol(new MTGColor.Pair(MTGColor.RED, MTGColor.WHITE)));
		SYMBOLS.put(new MTGColor.Pair(MTGColor.GREEN, MTGColor.WHITE), new HybridSymbol(new MTGColor.Pair(MTGColor.GREEN, MTGColor.WHITE)));
		SYMBOLS.put(new MTGColor.Pair(MTGColor.GREEN, MTGColor.BLUE), new HybridSymbol(new MTGColor.Pair(MTGColor.GREEN, MTGColor.BLUE)));
	}
	
	/**
	 * This HybridSymbol's pair of colors.
	 */
	private final MTGColor.Pair colors;
	
	private HybridSymbol(MTGColor.Pair colors)
	{
		super(colors.first.toString().toLowerCase() + "_" + colors.last.toString().toLowerCase() + "_mana.png");
		this.colors = colors;
	}
	
	/**
	 * @return This HybridSymbol's text, which is the shorthand for its two colors separated by a /.
	 * @see database.symbol.Symbol#getText()
	 */
	@Override
	public String getText()
	{
		return colors.first.shorthand() + "/" + colors.last.shorthand();
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
		weights.put(colors.first, 0.5);
		weights.put(colors.last, 0.5);
		return weights;
	}

	/**
	 * @param o Symbol to compare with
	 * @return A negative number if the other symbol should come after this HybridSymbol,
	 * the two symbols' Pairs' difference if they are both HybridSymbols, 0 if color order
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
	 * @return <code>true</code> if the other Symbol is a HybridSymbol with the same Pair of MTGColors,
	 * and <code>false</code>otherwise.
	 * @see database.symbol.Symbol#sameSymbol(Symbol)
	 */
	@Override
	public boolean sameSymbol(Symbol other)
	{
		return other instanceof HybridSymbol
			   && colors.first.equals(((HybridSymbol)other).colors.first)
			   && colors.last.equals(((HybridSymbol)other).colors.last);
	}
}
