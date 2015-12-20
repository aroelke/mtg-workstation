package editor.database.symbol;


/**
 * This class represents a Phi symbol that might appear in the text box of a Magic: the Gathering
 * card.
 * 
 * @author Alec Roelke
 */
public class PhiSymbol extends Symbol
{
	/**
	 * Instance of a PhiSymbol.
	 * @see editor.database.symbol.Symbol
	 */
	public static final PhiSymbol PHI = new PhiSymbol();
	
	/**
	 * Create a new PhiSymbol.
	 */
	private PhiSymbol()
	{
		super("phyrexia.png");
	}

	/**
	 * @return This PhiSymbol's text: 'P'.
	 * @see editor.database.symbol.Symbol#getText()
	 */
	@Override
	public String getText()
	{
		return "P";
	}

	/**
	 * @return <code>true</code> if the other Symbol is a PhiSymbol, and <code>false</code>
	 * otherwise.
	 * @see editor.database.symbol.Symbol#sameSymbol(editor.database.symbol.Symbol)
	 */
	@Override
	public boolean sameSymbol(Symbol other)
	{
		return other instanceof PhiSymbol;
	}
}
