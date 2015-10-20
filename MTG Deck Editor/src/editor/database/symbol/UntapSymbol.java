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

	/**
	 * @param o Symbol to compare with
	 * @return 0, since the ordering of this Symbol is not defined (like the untap
	 * symbol, it is separated from mana costs by a comma).
	 * @see editor.database.symbol.Symbol#compareTo(editor.database.symbol.Symbol)
	 */
	@Override
	public int compareTo(Symbol o)
	{
		return 0;
	}

	/**
	 * @return <code>true</code> if the other Symbol is an UntapSymbol and <code>false</code>
	 * otherwise.
	 * @see editor.database.symbol.Symbol#sameSymbol(editor.database.symbol.Symbol)
	 */
	@Override
	public boolean sameSymbol(Symbol other)
	{
		return other instanceof UntapSymbol;
	}
}
