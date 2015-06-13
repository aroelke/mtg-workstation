package database.symbol;


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
	 * @see database.symbol.Symbol
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
	 * @see database.symbol.Symbol#getText()
	 */
	@Override
	public String getText()
	{
		return "P";
	}

	/**
	 * @param o Symbol to compare with
	 * @return 0, since ordering with other Symbols is not defined for a PhiSymbol.
	 * @see database.symbol.Symbol#compareTo(database.symbol.Symbol)
	 */
	@Override
	public int compareTo(Symbol o)
	{
		return 0;
	}

	/**
	 * @return <code>true</code> if the other Symbol is a PhiSymbol, and <code>false</code>
	 * otherwise.
	 * @see database.symbol.Symbol#sameSymbol(database.symbol.Symbol)
	 */
	@Override
	public boolean sameSymbol(Symbol other)
	{
		return other instanceof PhiSymbol;
	}
}
