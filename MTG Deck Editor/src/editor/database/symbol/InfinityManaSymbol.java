package editor.database.symbol;

/**
 * This class represents a symbol for infinite colorless mana. 
 * 
 * @author Alec Roelke
 */
public class InfinityManaSymbol extends Symbol
{
	/**
	 * An instance of the Infinity symbol.
	 * @see editor.database.symbol.Symbol
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
	 * @see editor.database.symbol.Symbol#getText()
	 */
	@Override
	public String getText()
	{
		return "∞";
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
