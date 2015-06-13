package database.symbol;


/**
 * This class represents the Chaos symbol that appears on Plane cards.
 * 
 * @author Alec Roelke
 */
public class ChaosSymbol extends Symbol
{
	/**
	 * Instance of the Chaos symbol.
	 * @see database.symbol.Symbol
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
	 * @see database.symbol.Symbol#getText()
	 */
	@Override
	public String getText()
	{
		return "C";
	}

	/**
	 * @param o Symbol to compare with
	 * @return 0, since ordering with other symbols is not defined for chaos symbol.
	 * @see database.symbol.Symbol#compareTo(database.symbol.Symbol)
	 */
	@Override
	public int compareTo(Symbol o)
	{
		return 0;
	}

	/**
	 * @param other Symbol to compare to
	 * @return 0, since this symbol's ordering relative to other ones is
	 * not defined.
	 */
	@Override
	public boolean sameSymbol(Symbol other)
	{
		return other instanceof ChaosSymbol;
	}
}
