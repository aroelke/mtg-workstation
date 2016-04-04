package editor.database.symbol;

/**
 * This class represents a tap symbol.
 * 
 * @author Alec Roelke
 */
public class TapSymbol extends Symbol
{
	/**
	 * An instance of TapSymbol.
	 * @see editor.database.symbol.Symbol
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
	 * @see editor.database.symbol.Symbol#getText()
	 */
	@Override
	public String getText()
	{
		return "T";
	}
}
