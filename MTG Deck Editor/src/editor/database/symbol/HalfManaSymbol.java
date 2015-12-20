package editor.database.symbol;


/**
 * This class represents a symbol for half of a generic mana.
 * 
 * @author Alec Roelke
 */
public class HalfManaSymbol extends Symbol
{
	/**
	 * Instance of a HalfManaSymbol.
	 * @see editor.database.symbol.Symbol
	 */
	public static final HalfManaSymbol HALF_MANA = new HalfManaSymbol();
	
	/**
	 * Create a new HalfManaSymbol.
	 */
	private HalfManaSymbol()
	{
		super("half_mana.png");
	}

	/**
	 * @return This HalfManaSymbol's text: "1/2."
	 * @see editor.database.symbol.Symbol#getText()
	 */
	@Override
	public String getText()
	{
		return "1/2";
	}

	/**
	 * @return The value of this symbol for converted mana costs: 0.5.
	 * @see editor.database.symbol.Symbol#value()
	 */
	@Override
	public double value()
	{
		return 0.5;
	}

	/**
	 * @return <code>true</code> if the other Symbol is a HalfManaSymbol or <code>false</code> otherwise.
	 * @see editor.database.symbol.Symbol#sameSymbol(Symbol)
	 */
	@Override
	public boolean sameSymbol(Symbol other)
	{
		return other instanceof HalfColorSymbol;
	}
}
