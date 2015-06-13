package database.symbol;


/**
 * This class represents a snow mana symbol.
 * 
 * @author Alec Roelke
 */
public class SnowSymbol extends Symbol
{
	/**
	 * An instance of SnowSymbol.
	 * @see database.symbol.Symbol
	 */
	public static final SnowSymbol SNOW = new SnowSymbol();
	
	/**
	 * Create a new SnowSymbol.
	 */
	private SnowSymbol()
	{
		super("snow_mana.png");
	}
	
	/**
	 * @return This SnowSymbol's text, which is 'S'.
	 * @see database.symbol.Symbol#getText()
	 */
	@Override
	public String getText()
	{
		return "S";
	}

	/**
	 * @return This SnowSymbol's value for converted mana cost, which is 1.
	 * @see database.symbol.Symbol#value()
	 */
	@Override
	public double value()
	{
		return 1;
	}

	/**
	 * @param o Symbol to compare with
	 * @return A negative number if this SnowSymbol should come before the other in costs,
	 * 0 if the other is also a SnowSymbol or ordering is not defined, and a positive number if
	 * it should come after. 
	 * @see database.symbol.Symbol#compareTo(Symbol)
	 */
	@Override
	public int compareTo(Symbol o)
	{
		if (o instanceof SnowSymbol)
			return 0;
		else if (o instanceof ColorSymbol || o instanceof HybridSymbol
				 || o instanceof PhyrexianSymbol || o instanceof TwobridSymbol)
			return -1;
		else if (o instanceof ColorlessSymbol || o instanceof HalfColorSymbol
				 || o instanceof HalfManaSymbol || o instanceof XSymbol
				 || o instanceof YSymbol || o instanceof ZSymbol)
			return 1;
		else
			return 0;
	}

	/**
	 * @return <code>true</code> if the other Symbol is a SnowSymbol, and <code>false</code>
	 * otherwise.
	 * @see database.symbol.Symbol#sameSymbol(Symbol)
	 */
	@Override
	public boolean sameSymbol(Symbol other)
	{
		return other instanceof SnowSymbol;
	}
}
