package database.symbol;

/**
 * This class represents a mana symbol that can be paid for with any amount
 * of mana and replaces any X's that appear in a card's text with that amount.
 * 
 * @author Alec Roelke
 */
public class XSymbol extends Symbol
{
	/**
	 * An instance of XSymbol.
	 * @see database.symbol.Symbol
	 */
	public static final XSymbol X = new XSymbol();
	
	/**
	 * Create a new XSymbol.
	 */
	private XSymbol()
	{
		super("x_mana.png");
	}
	
	/**
	 * @return This XSymbol's text, which is 'X'.
	 * @see database.symbol.Symbol#getText()
	 */
	@Override
	public String getText()
	{
		return "X";
	}

	/**
	 * @return 0, since X is always resolved to 0 for any card not on the stack.
	 * @see database.symbol.Symbol#value()
	 */
	@Override
	public double value()
	{
		return 0;
	}

	/**
	 * @param o Symbol to compare with
	 * @return 0 if the other Symbol is an XSymbol or if the ordering for the other
	 * Symbol is not defined, otherwise return a negative number (X symbols always come
	 * first in mana costs).
	 * @see database.symbol.Symbol#compareTo(database.symbol.Symbol)
	 */
	@Override
	public int compareTo(Symbol o)
	{
		if (o instanceof XSymbol || o instanceof ChaosSymbol
			|| o instanceof PhiSymbol || o instanceof TapSymbol
			|| o instanceof UntapSymbol)
			return 0;
		else
			return -1;
	}

	/**
	 * @param other Symbol to compare with
	 * @return <code>true</code> if the other Symbol is an XSymbol, and <code>false</code>
	 * otherwise.
	 * @see database.symbol.Symbol#sameSymbol(database.symbol.Symbol)
	 */
	@Override
	public boolean sameSymbol(Symbol other)
	{
		return other instanceof XSymbol;
	}
}
