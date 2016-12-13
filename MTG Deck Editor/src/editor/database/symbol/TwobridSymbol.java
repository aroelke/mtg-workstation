package editor.database.symbol;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import editor.database.characteristics.ManaType;

/**
 * This class represents a colorless-colored hybrid mana symbol, which can be paid for either by one
 * mana of the corresponding color, or two mana of any color.  These are referred to as "twobrid"
 * symbols.
 * 
 * @author Alec Roelke
 */
public class TwobridSymbol extends ManaSymbol
{
	/**
	 * Map of colors onto their corresponding twobrid symbols.
	 */
	private static final Map<ManaType, TwobridSymbol> SYMBOLS = Collections.unmodifiableMap(
			Arrays.stream(ManaType.colors()).collect(Collectors.toMap(Function.identity(), TwobridSymbol::new)));
	
	/**
	 * Get the TwobridSymbol corresponding to the given color.
	 * 
	 * @param col color to look up
	 * @return the TwobridSymbol corresponding to the given ManaType, or null if no such symbol exists.
	 */
	public static TwobridSymbol get(ManaType col)
	{
		return SYMBOLS.get(col);
	}
	
	/**
	 * Get the TwobridSymbol corresponding to the given String.
	 * 
	 * @param col Color to look up
	 * @return The TwobridSymbol corresponding to the given String, or
	 * null if no such symbol exists.
	 */
	public static TwobridSymbol get(String col)
	{
		try
		{
			int index = col.indexOf('/');
			if (index > 0 && col.charAt(index - 1) == '2')
				return get(ManaType.get(col.charAt(index + 1)));
			else
				return null;
		}
		catch (IllegalArgumentException | StringIndexOutOfBoundsException e)
		{
			return null;
		}
	}
	
	/**
	 * This TwobridSymbol's color.
	 */
	private final ManaType color;
	
	/**
	 * Create a TwobridSymbol.
	 * 
	 * @param color the new TwobridSymbol's color
	 */
	private TwobridSymbol(ManaType color)
	{
		super("2_" + color.toString().toLowerCase() + "_mana.png", "2/" + String.valueOf(color.shorthand()), 2);
		this.color = color;
	}

	/**
	 * {@inheritDoc}
	 * This TwobridSymbols' color weight is 0.5 for its color and 0 for everything else.
	 */
	@Override
	public Map<ManaType, Double> colorWeights()
	{
		return createWeights(new ColorWeight(color, 0.5));
	}

	@Override
	public int compareTo(ManaSymbol o)
	{
		if (o instanceof TwobridSymbol)
			return color.colorOrder(((TwobridSymbol)o).color);
		else
			return super.compareTo(o);
	}
}
