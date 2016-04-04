package editor.database.symbol;


/**
 * This class represents the Chaos symbol that appears on Plane cards.
 * 
 * @author Alec Roelke
 */
public class ChaosSymbol extends Symbol
{
	/**
	 * Instance of the Chaos symbol.
	 * @see editor.database.symbol.Symbol
	 */
	public static final ChaosSymbol CHAOS = new ChaosSymbol();
	
	/**
	 * Create a new Chaos symbol.
	 */
	private ChaosSymbol()
	{
		super("chaos.png");
	}

	/**
	 * @return This ChaosSymbol's text: a "C."
	 * @see editor.database.symbol.Symbol#getText()
	 */
	@Override
	public String getText()
	{
		return "CHAOS";
	}
	
	/**
	 * @return A String representation of this ChaosSymbol: The string "CHAOS".
	 */
	@Override
	public String toString()
	{
		return "CHAOS";
	}
}
