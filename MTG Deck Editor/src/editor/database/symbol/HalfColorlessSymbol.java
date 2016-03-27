package editor.database.symbol;

/**
 * TODO: Roll this into the HalfColorSymbol class
 * @author Alec Roelke
 */
public class HalfColorlessSymbol extends Symbol
{
	public static final HalfColorlessSymbol HALF_COLORLESS = new HalfColorlessSymbol();
	
	private HalfColorlessSymbol()
	{
		super("half_colorless_mana.png");
	}
	
	@Override
	public String getText()
	{
		return "C/2";
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

	@Override
	public boolean sameSymbol(Symbol other)
	{
		return other instanceof HalfColorlessSymbol;
	}

}
