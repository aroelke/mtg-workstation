package database.symbol;

/**
 * This class represents a tap symbol.
 * 
 * @author Alec Roelke
 */
public class TapSymbol extends Symbol
{
	/**
	 * An instance of TapSymbol.
	 * @see database.symbol.Symbol
	 */
	public static final TapSymbol TAP = new TapSymbol();
	
	/**
	 * Create a TapSymbol.
	 */
	private TapSymbol()
	{
		super("tap.png");
	}
	
	/**
	 * @return This TapSymbol's text, which is 'T'.
	 * @see database.symbol.Symbol#getText()
	 */
	@Override
	public String getText()
	{
		return "T";
	}

	/**
	 * @return 0, since the order of where a tap symbol should go in costs is not defined
	 * (it is always separated from a mana cost by a comma).
	 * @see database.symbol.Symbol#compareTo(database.symbol.Symbol)
	 */
	@Override
	public int compareTo(Symbol o)
	{
		return 0;
	}

	/**
	 * @return <code>true</code> if the other Symbol is a TapSymbol, and <code>false</code>
	 * otherwise.
	 * @see database.symbol.Symbol#sameSymbol(database.symbol.Symbol)
	 */
	@Override
	public boolean sameSymbol(Symbol other)
	{
		return other instanceof TapSymbol;
	}
}
