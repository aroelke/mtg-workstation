package editor.database.symbol;

import java.util.HashMap;
import java.util.Map;

import editor.database.characteristics.ManaType;

/**
 * This class represents a Phyrexian mana symbol, which can be paid for either with mana of the
 * same color or two life.
 * 
 * @author Alec Roelke
 */
public class PhyrexianSymbol extends Symbol
{
	/**
	 * Map of ManaTypes onto their corresponding Phyrexian symbols.
	 * @see editor.database.symbol.Symbol
	 */
	private static final Map<ManaType, PhyrexianSymbol> SYMBOLS = new HashMap<ManaType, PhyrexianSymbol>();
	static
	{
		SYMBOLS.put(ManaType.WHITE, new PhyrexianSymbol(ManaType.WHITE));
		SYMBOLS.put(ManaType.BLUE, new PhyrexianSymbol(ManaType.BLUE));
		SYMBOLS.put(ManaType.BLACK, new PhyrexianSymbol(ManaType.BLACK));
		SYMBOLS.put(ManaType.RED, new PhyrexianSymbol(ManaType.RED));
		SYMBOLS.put(ManaType.GREEN, new PhyrexianSymbol(ManaType.GREEN));
	}
	
	/**
	 * Get the PhyrexianSymbol corresponding to the given color.
	 * 
	 * @param col ManaType corresponding to the symbol to get
	 * @return The PhyrexianSymbol corresponding to the given ManaType, or
	 * null if no such symbol exists.
	 */
	public static PhyrexianSymbol get(ManaType col)
	{
		return SYMBOLS.get(col);
	}
	
	public static PhyrexianSymbol get(String col)
	{
		try
		{
			int index = col.indexOf('/');
			if (index > 0 && Character.toUpperCase(col.charAt(index + 1)) == 'P')
				return get(ManaType.get(col.charAt(index - 1)));
			else
				return null;
		}
		catch (IllegalArgumentException | StringIndexOutOfBoundsException e)
		{
			return null;
		}
	}
	
	/**
	 * This PhyrexianSymbol's color.
	 */
	public final ManaType color;
	
	/**
	 * Create a new PhyrexianSymbol.
	 * 
	 * @param color The new PhyrexianSymbol's color.
	 */
	private PhyrexianSymbol(ManaType color)
	{
		super("phyrexian_" + color.toString().toLowerCase() + "_mana.png");
		this.color = color;
	}
	
	/**
	 * @return This PhyrexianSymbol's text, which is its color's shorhand followed by "/P".
	 * @see editor.database.symbol.Symbol#getText()
	 */
	@Override
	public String getText()
	{
		return String.valueOf(color.shorthand()) + "/P";
	}

	/**
	 * @return This PhyrexianSymbol's value in mana costs, which is 1.
	 * @see editor.database.symbol.Symbol#value()
	 */
	@Override
	public double value()
	{
		return 1;
	}

	/**
	 * @return This PhyrexianSymbol's color weights, which is 1 for its color and 0 for
	 * the rest.
	 * @see editor.database.symbol.Symbol#colorWeights()
	 */
	@Override
	public Map<ManaType, Double> colorWeights()
	{
		return createWeights(new ColorWeight(color, 0.5));
	}

	/**
	 * @param o Symbol to compare with
	 * @return A negative number if this PhyrexianSymbol should come before the other Symbol
	 * in a cost, the ordering of colors if the other Symbol is a PhyrexianSymbol, a positive
	 * number if its should come after, and 0 if ordering is undefined.
	 * @see editor.database.symbol.Symbol#compareTo(editor.database.symbol.Symbol)
	 */
	@Override
	public int compareTo(Symbol o)
	{
		if (o instanceof PhyrexianSymbol)
			return color.colorOrder(((PhyrexianSymbol)o).color);
		else
			return super.compareTo(o);
	}
}
