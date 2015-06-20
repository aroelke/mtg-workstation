package database.symbol;

/**
 * This class represents a symbol for infinite colorless mana. 
 * 
 * @author Alec Roelke
 */
public class InfinityManaSymbol extends Symbol
{
	/**
	 * An instance of the Infinity symbol.
	 * @see database.symbol.Symbol
	 */
	public static final InfinityManaSymbol INFINITY_MANA = new InfinityManaSymbol();
	
	/**
	 * Create a new Infinity symbol.
	 */
	private InfinityManaSymbol()
	{
		super("infinity_mana.png");
	}
	
	/**
	 * @return this InfinityManaSymbol's text:  An infinity sign.
	 * @see database.symbol.Symbol#getText()
	 */
	@Override
	public String getText()
	{
		return "\u221E";
	}

	/**
	 * @return 0, since no ordering has been defined for infinity mana (it could
	 * probably go with other colorless mana, but it can't appear in costs anyway).
	 */
	@Override
	public int compareTo(Symbol other)
	{
		return 0;
	}

	/**
	 * @param other Symbol to compare with
	 * @return <code>true</code> if the other symbol is an InfinityManaSymbol
	 * and <code>false</code> otherwise.
	 */
	@Override
	public boolean sameSymbol(Symbol other)
	{
		return other instanceof InfinityManaSymbol;
	}
}
