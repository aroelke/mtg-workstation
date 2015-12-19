package editor.database.symbol;


/**
 * This class represents an amount of generic mana that might appear in a mana cost
 * on a Magic: The Gathering card.
 * 
 * @author Alec Roelke
 */
public class GenericSymbol extends Symbol
{
	/**
	 * Highest consecutive value that a generic symbol might attain.
	 * @see editor.database.symbol.Symbol
	 */
	public static final int HIGHEST = 20;
	/**
	 * Array of consecutive GenericSymbols.
	 */
	public static final GenericSymbol[] N = new GenericSymbol[HIGHEST + 1];
	static
	{
		for (int i = 0; i <= HIGHEST; i++)
			N[i] = new GenericSymbol(i);
	}
	/**
	 * GenericSymbol representing 100 mana.
	 */
	public static final GenericSymbol HUNDRED = new GenericSymbol(100);
	/**
	 * GenericSymbol representing 1,000,000 mana.
	 */
	public static final GenericSymbol MILLION = new GenericSymbol(1000000);
	
	/**
	 * Amount of mana this GenericSymbol represents.
	 */
	private final int amount;
	
	/**
	 * Create a new GenericSymbol.
	 * 
	 * @param amount Amount of mana the new ColorlessSymbol represents.
	 */
	private GenericSymbol(int amount)
	{
		super(amount + "_mana.png");
		this.amount = amount;
	}
	
	/**
	 * @return The number corresponding to the amount of mana this GenericSymbol represents.
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
		if (o instanceof GenericSymbol)
			return amount - ((GenericSymbol)o).amount;
		else if (o instanceof ColorSymbol || o instanceof HalfColorSymbol
				 || o instanceof HybridSymbol || o instanceof PhyrexianSymbol
				 || o instanceof SnowSymbol || o instanceof TwobridSymbol
				 || o instanceof ColorlessSymbol || o instanceof HalfColorlessSymbol)
			return -1;
		else if (o instanceof HalfManaSymbol || o instanceof VariableSymbol)
			return 1;
		else
			return 0;
	}

	/**
	 * @return <code>true</code> if the other Symbol is a GenericSymbol with the same amount and
	 * <code>false</code> otherwise.
	 * @see editor.database.symbol.Symbol#sameSymbol(Symbol)
	 */
	@Override
	public boolean sameSymbol(Symbol other)
	{
		return other instanceof GenericSymbol && amount == ((GenericSymbol)other).amount;
	}
}
