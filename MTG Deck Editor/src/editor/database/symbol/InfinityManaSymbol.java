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
}
