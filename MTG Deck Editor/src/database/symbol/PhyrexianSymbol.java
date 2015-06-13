package database.symbol;

import java.util.HashMap;
import java.util.Map;

import database.characteristics.MTGColor;

/**
 * This class represents a Phyrexian mana symbol, which can be paid for either with mana of the
 * same color or two life.
 * 
 * @author Alec Roelke
 */
public class PhyrexianSymbol extends Symbol
{
	/**
	 * Map of MTGColors onto their corresponding Phyrexian symbols.
	 * @see database.symbol.Symbol
	 */
	public static final Map<MTGColor, PhyrexianSymbol> SYMBOLS = new HashMap<MTGColor, PhyrexianSymbol>();
	static
	{
		SYMBOLS.put(MTGColor.WHITE, new PhyrexianSymbol(MTGColor.WHITE));
		SYMBOLS.put(MTGColor.BLUE, new PhyrexianSymbol(MTGColor.BLUE));
		SYMBOLS.put(MTGColor.BLACK, new PhyrexianSymbol(MTGColor.BLACK));
		SYMBOLS.put(MTGColor.RED, new PhyrexianSymbol(MTGColor.RED));
		SYMBOLS.put(MTGColor.GREEN, new PhyrexianSymbol(MTGColor.GREEN));
	}
	
	/**
	 * This PhyrexianSymbol's color.
	 */
	public final MTGColor color;
	
	/**
	 * Create a new PhyrexianSymbol.
	 * 
	 * @param color The new PhyrexianSymbol's color.
	 */
	private PhyrexianSymbol(MTGColor color)
	{
		super("phyrexian_" + color.toString().toLowerCase() + "_mana.png");
		this.color = color;
	}
	
	/**
	 * @return This PhyrexianSymbol's text, which is its color's shorhand followed by "/P".
	 * @see database.symbol.Symbol#getText()
	 */
	@Override
	public String getText()
	{
		return String.valueOf(color.shorthand()) + "/P";
	}

	/**
	 * @return This PhyrexianSymbol's value in mana costs, which is 1.
	 * @see database.symbol.Symbol#value()
	 */
	@Override
	public double value()
	{
		return 1;
	}

	/**
	 * @return This PhyrexianSymbol's color weights, which is 1 for its color and 0 for
	 * the rest.
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
	 * @return A negative number if this PhyrexianSymbol should come before the other Symbol
	 * in a cost, the ordering of colors if the other Symbol is a PhyrexianSymbol, a positive
	 * number if its should come after, and 0 if ordering is undefined.
	 * @see database.symbol.Symbol#compareTo(database.symbol.Symbol)
	 */
	@Override
	public int compareTo(Symbol o)
	{
		if (o instanceof PhyrexianSymbol)
			return color.colorOrder(((PhyrexianSymbol)o).color);
		else if (o instanceof ColorSymbol)
			return -1;
		else if (o instanceof ColorlessSymbol || o instanceof HalfManaSymbol
				 || o instanceof HalfColorSymbol || o instanceof SnowSymbol
				 || o instanceof TwobridSymbol || o instanceof HybridSymbol
				 || o instanceof XSymbol || o instanceof YSymbol || o instanceof ZSymbol)
			return 1;
		else
			return 0;
	}

	/**
	 * @return <code>true</code> if the other Symbol is a PhyrexianSymbol of the same color and
	 * <code>false</code> otherwise.
	 * @see database.symbol.Symbol#sameSymbol(database.symbol.Symbol)
	 */
	@Override
	public boolean sameSymbol(Symbol other)
	{
		return other instanceof PhyrexianSymbol && color.equals(((PhyrexianSymbol)other).color);
	}
}
