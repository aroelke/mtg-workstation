package editor.database.symbol;


/**
 * This class represents a snow mana symbol.
 * 
 * @author Alec Roelke
 */
public class SnowSymbol extends Symbol
{
	/**
	 * An instance of SnowSymbol.
	 * @see editor.database.symbol.Symbol
	 */
	public static final SnowSymbol SNOW = new SnowSymbol();
	
	/**
	 * Create a new SnowSymbol.
	 */
	private SnowSymbol()
	{
		super("snow_mana.png");
	}
	
	/**
	 * @return This SnowSymbol's text, which is 'S'.
	 * @see editor.database.symbol.Symbol#getText()
	 */
	@Override
	public String getText()
	{
		return "S";
	}

	/**
	 * @return This SnowSymbol's value for converted mana cost, which is 1.
	 * @see editor.database.symbol.Symbol#value()
	 */
	@Override
	public double value()
	{
		return 1;
	}

	/**
	 * @return <code>true</code> if the other Symbol is a SnowSymbol, and <code>false</code>
	 * otherwise.
	 * @see editor.database.symbol.Symbol#sameSymbol(Symbol)
	 */
	@Override
	public boolean sameSymbol(Symbol other)
	{
		return other instanceof SnowSymbol;
	}
}
