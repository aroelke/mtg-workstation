package editor.database.symbol;

/**
 * This class represents a mana symbol that can be paid for with any amount
 * of mana and replaces any Y's that appear in a card's text with that amount.
 * 
 * @author Alec Roelke
 */
public class YSymbol extends Symbol
{
	/**
	 * An instance of YSymbol.
	 * @see editor.database.symbol.Symbol
	 */
	public static final YSymbol Y = new YSymbol();
	
	/**
	 * Create a YSymbol
	 */
	private YSymbol()
	{
		super("y_mana.png");
	}
	
	/**
	 * @return This YSymbol's text, which is 'Y'.
	 * @see editor.database.symbol.Symbol#getText()
	 */
	@Override
	public String getText()
	{
		return "Y";
	}
	
	/**
	 * @return 0, since Y is always resolved to 0 for any card not on the stack.
	 * @see editor.database.symbol.Symbol#value()
	 */
	@Override
	public double value()
	{
		return 0;
	}

	/**
	 * @param o Symbol to compare with
	 * @return 0 if the other Symbol is an YSymbol or if the ordering for the other
	 * Symbol is not defined, a positive value if is an XSymbol, and a negative value
	 * otherwise.
	 * @see editor.database.symbol.Symbol#compareTo(editor.database.symbol.Symbol)
	 */
	@Override
	public int compareTo(Symbol o)
	{
		if (o instanceof YSymbol || o instanceof ChaosSymbol
			|| o instanceof  PhiSymbol || o instanceof TapSymbol
			|| o instanceof UntapSymbol)
			return 0;
		else if (o instanceof XSymbol)
			return 1;
		else
			return -1;
	}

	/**
	 * @param other Symbol to compare with
	 * @return <code>true</code> if the other Sybmol is a YSymbol, and <code>false</code>
	 * otherwise.
	 * @see editor.database.symbol.Symbol#sameSymbol(editor.database.symbol.Symbol)
	 */
	@Override
	public boolean sameSymbol(Symbol other)
	{
		return other instanceof YSymbol;
	}
}
