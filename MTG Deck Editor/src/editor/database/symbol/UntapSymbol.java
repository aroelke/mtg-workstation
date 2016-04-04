package editor.database.symbol;

/**
 * This class represents an untap symbol.
 * 
 * @author Alec Roelke
 */
public class UntapSymbol extends Symbol
{
	/**
	 * An instance of UntapSymbol.
	 * @see editor.database.symbol.Symbol
	 */
	public static final UntapSymbol UNTAP = new UntapSymbol();
	
	/**
	 * Create an UntapSymbol.
	 */
	private UntapSymbol()
	{
		super("untap.png");
	}
	
	/**
	 * @return This UntapSymbol's text, which is 'Q'.
	 * @see editor.database.symbol.Symbol#getText()
	 */
	@Override
	public String getText()
	{
		return "Q";
	}
}
