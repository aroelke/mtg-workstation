package editor.database.symbol;

/**
 * TODO: Wrap this into the ColorSymbol class.
 * TODO: This needs to treat colorless as a sixth color for color weighting purposes
 * @author Alec Roelke
 */
public class ColorlessSymbol extends Symbol
{
	public static final ColorlessSymbol COLORLESS = new ColorlessSymbol();
	
	private ColorlessSymbol()
	{
		super("colorless_mana.png");
	}
	
	@Override
	public String getText()
	{
		return "C";
	}

	/**
	 * @return The value of this symbol for converted mana costs: 1.
	 * @see editor.database.symbol.Symbol#value()
	 */
	@Override
	public double value()
	{
		return 1;
	}

	@Override
	public boolean sameSymbol(Symbol other)
	{
		return other instanceof ColorlessSymbol;
	}

}
