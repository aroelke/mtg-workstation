package editor.database.symbol;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import editor.database.characteristics.ManaType;

/**
 * This class represents a symbol for a single type of mana.
 * 
 * @author Alec Roelke
 */
public class ColorSymbol extends ManaSymbol
{
	/**
	 * Map of mana type onto its corresponding ColorSymbol.
	 */
	private static final Map<ManaType, ColorSymbol> SYMBOLS = Collections.unmodifiableMap(
			Arrays.stream(ManaType.values()).collect(Collectors.toMap(Function.identity(), ColorSymbol::new)));
	
	/**
	 * Get the symbol corresponding to a type of mana.
	 * 
	 * @param col type of mana to find the symbol for
	 * @return the ColorSymbol corresponding to the given mana type , or null if none exists. 
	 */
	public static ColorSymbol get(ManaType col)
	{
		return SYMBOLS[col];
	}
	
	/**
	 * Get the symbol corresponding to a color string.
	 * 
	 * @param col String to find the symbol for
	 * @return the ColorSymbol corresponding to the given String, or null if none exists.
	 */
	public static ColorSymbol get(String col)
	{
		try
		{
			return get(ManaType.get(col));
		}
		catch (IllegalArgumentException e)
		{
			return null;
		}
	}
	
	/**
	 * Color of this ColorSymbol.
	 */
	private final ManaType color;
	
	/**
	 * Create a new ColorSymbol.
	 * 
	 * @param color mana type of the new ColorSymbol.
	 */
	private ColorSymbol(ManaType color)
	{
		super(color.toString().toLowerCase() + "_mana.png", String.valueOf(color.shorthand()), 1);
		this.color = color;
	}

	/**
	 * {@inheritDoc}
	 * This ColorSymbol's weights are 1 for its mana type and 0 for all the others.
	 */
	@Override
	public Map<ManaType, Double> colorWeights()
	{
		return createWeights(new ColorWeight(color, 1.0));
	}

	@Override
	public int compareTo(ManaSymbol o)
	{
		if (o instanceof ColorSymbol)
			return color.colorOrder(((ColorSymbol)o).color);
		else
			return super.compareTo(o);
	}
}
