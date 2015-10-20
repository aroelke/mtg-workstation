package editor.database.symbol;


/**
 * This class represents an amount of colorless mana that might appear in a mana cost
 * on a Magic: The Gathering card.
 * 
 * @author Alec Roelke
 */
public class ColorlessSymbol extends Symbol
{
	/**
	 * Highest consecutive value that a colorless symbol might attain.
	 * @see editor.database.symbol.Symbol
	 */
	public static int HIGHEST = 20;
	/**
	 * Array of consecutive ColorlessSymbols.
	 */
	public static final ColorlessSymbol[] N = new ColorlessSymbol[HIGHEST + 1];
	static
	{
		for (int i = 0; i <= HIGHEST; i++)
			N[i] = new ColorlessSymbol(i);
	}
	/**
	 * ColorlessSymbol representing 100 mana.
	 */
	public static final ColorlessSymbol HUNDRED = new ColorlessSymbol(100);
	/**
	 * ColorlessSymbol representing 1,000,000 mana.
	 */
	public static final ColorlessSymbol MILLION = new ColorlessSymbol(1000000);
	
	/**
	 * Amount of mana this ColorlessSymbol represents.
	 */
	private final int amount;
	
	/**
	 * Create a new ColorlessSymbol.
	 * 
	 * @param amount Amount of mana the new ColorlessSymbol represents.
	 */
	private ColorlessSymbol(int amount)
	{
		super(amount + "_mana.png");
		this.amount = amount;
	}
	
	/**
	 * @return The number corresponding to the amount of mana this ColorlessSymbol represents.
	 * @see editor.database.symbol.Symbol#getText()
	 */
	@Override
	public String getText()
	{
		return String.valueOf(amount);
	}

	/**
	 * @return The amount of mana this symbol represents.
	 * @see editor.database.symbol.Symbol#value()
	 */
	@Override
	public double value()
	{
		return amount;
	}

	/**
	 * @param o Symbol to compare to
	 * @return A negative number if the other symbol is a color symbol, half symbol, hybrid symbol,
	 * or snow symbol; the difference between amounts if the other symbol is a colorless symbol,
	 * and a positive number otherwise.
	 * @see editor.database.symbol.Symbol#compareTo(Symbol)
	 */
	@Override
	public int compareTo(Symbol o)
	{
		if (o instanceof ColorlessSymbol)
			return amount - ((ColorlessSymbol)o).amount;
		else if (o instanceof ColorSymbol || o instanceof HalfColorSymbol
				 || o instanceof HybridSymbol || o instanceof PhyrexianSymbol
				 || o instanceof SnowSymbol || o instanceof TwobridSymbol)
			return -1;
		else if (o instanceof HalfManaSymbol || o instanceof XSymbol
				 || o instanceof YSymbol || o instanceof ZSymbol)
			return 1;
		else
			return 0;
	}

	/**
	 * @return <code>true</code> if the other Symbol is a ColorlessSymbol with the same amount and
	 * <code>false</code> otherwise.
	 * @see editor.database.symbol.Symbol#sameSymbol(Symbol)
	 */
	@Override
	public boolean sameSymbol(Symbol other)
	{
		return other instanceof ColorlessSymbol && amount == ((ColorlessSymbol)other).amount;
	}
}
