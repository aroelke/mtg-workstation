package editor.database.symbol;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import editor.database.characteristics.ManaType;

/**
 * This class represents a Phyrexian mana symbol, which can be paid for either with mana of the
 * same color or two life.
 * 
 * @author Alec Roelke
 */
public class PhyrexianSymbol extends ManaSymbol
{
	/**
	 * Map of ManaTypes onto their corresponding Phyrexian symbols.
	 * @see editor.database.symbol.Symbol
	 */
	private static final Map<ManaType, PhyrexianSymbol> SYMBOLS = Collections.unmodifiableMap(
			Arrays.stream(ManaType.colors()).collect(Collectors.toMap(Function.identity(), PhyrexianSymbol::new)));
	
	/**
	 * Get the PhyrexianSymbol corresponding to the given color.
	 * 
	 * @param col ManaType corresponding to the symbol to get
	 * @return The PhyrexianSymbol corresponding to the given ManaType, or
	 * null if no such symbol exists.
	 */
	public static PhyrexianSymbol get(ManaType col)
	{
		return SYMBOLS[col];
	}
	
	/**
	 * Get the Phyrexian symbol corresponding to the given String, which should be
	 * a color character followed by either /p or /P.
	 * 
	 * @param col String to parse
	 * @return The corresponding Phyrexian sybol.
	 */
	public static PhyrexianSymbol get(String col)
	{
		try
		{
			if (col.length() == 3 && col.charAt(1) == '/' && Character.toUpperCase(col.charAt(2)) == 'P')
				return get(ManaType.get(col.charAt(0)));
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
		super("phyrexian_" + color.toString().toLowerCase() + "_mana.png", String.valueOf(color.shorthand()) + "/P", 1);
		this.color = color;
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
	public int compareTo(ManaSymbol o)
	{
		if (o instanceof PhyrexianSymbol)
			return color.colorOrder(((PhyrexianSymbol)o).color);
		else
			return super.compareTo(o);
	}
}
