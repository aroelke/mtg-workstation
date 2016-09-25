package editor.database.symbol;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import editor.database.characteristics.ManaType;

/**
 * This class represents half of a colored mana symbol.
 * 
 * @author Alec Roelke
 */
public class HalfColorSymbol extends ManaSymbol
{
	/**
	 * Map of ManaType onto its corresponding half-symbol.  To get a colored half-symbol, use this Map.
	 * @see editor.database.symbol.Symbol
	 */
	private static final Map<ManaType, HalfColorSymbol> SYMBOLS = Collections.unmodifiableMap(
			Arrays.stream(ManaType.values()).collect(Collectors.toMap(Function.identity(), HalfColorSymbol::new)));
	
	/**
	 * Get the HalfColorSymbol corresponding to the given ManaType.
	 * 
	 * @param col ManaType to get the symbol for
	 * @return The HalfColorSymbol corresponding to the given ManaType, or null
	 * if no such symbol exists.
	 */
	public static HalfColorSymbol get(ManaType col)
	{
		return SYMBOLS[col];
	}
	
	/**
	 * Get the HalfColorSymbol corresponding to the given String.
	 * 
	 * @param col String to get the symbol for
	 * @return The HalfColorSymbol corresponding to the given ManaType, or
	 * null otherwise.
	 */
	public static HalfColorSymbol get(String col)
	{
		try
		{
			if (col.length() == 2 && Character.toUpperCase(col.charAt(0)) == 'H')
				return get(ManaType.get(col.charAt(1)));
			else
				return null;
		}
		catch (IllegalArgumentException | StringIndexOutOfBoundsException e)
		{
			return null;
		}
	}
	
	/**
	 * This HalfColorSymbol's color.
	 */
	private final ManaType color;
	
	/**
	 * Create a new HalfColorSymbol.
	 * 
	 * @param color Color of the new HalfColorSymbol
	 */
	private HalfColorSymbol(ManaType color)
	{
		super("half_" + color.toString().toLowerCase() + "_mana.png", "H" + String.valueOf(color.shorthand()), 0.5);
		this.color = color;
	}

	/**
	 * @return A Map containing this HalfColorSymbol's color weight.  All values will be 0 except for
	 * this HalfColorSymbol's color, which will be 0.5.
	 * @see editor.database.symbol.Symbol#colorWeights()
	 */
	@Override
	public Map<ManaType, Double> colorWeights()
	{
		return createWeights(new ColorWeight(color, 0.5));
	}

	/**
	 * @param o Symbol to compare with
	 * @return A negative number if the other symbol is a colored symbol or a hybrid symbol,
	 * a positive number if it is a colorless symbol, the color-order difference between the two
	 * symbols if the other symbol is a colored half-mana symbol, or 0 otherwise.
	 * @see editor.database.symbol.Symbol#compareTo(Symbol)
	 */
	@Override
	public int compareTo(ManaSymbol o)
	{
		if (o instanceof HalfColorSymbol)
			return color.colorOrder(((HalfColorSymbol)o).color);
		else
			return super.compareTo(o);
	}
}
