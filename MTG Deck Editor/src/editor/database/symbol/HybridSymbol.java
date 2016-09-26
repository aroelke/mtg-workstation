package editor.database.symbol;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import editor.database.characteristics.ManaType;

/**
 * This class represents a two-color hybrid symbol.  It will sort its colors
 * so that they appear in the correct order.
 * 
 * @author Alec Roelke
 */
public class HybridSymbol extends ManaSymbol
{
	/**
	 * Map mapping each Tuple of colors to their corresponding hybrid symbols.
	 * @see editor.database.symbol.Symbol
	 */
	private static final Map<ManaType.Tuple, HybridSymbol> SYMBOLS = new HashMap<ManaType.Tuple, HybridSymbol>();
	static
	{
		for (int i = 0; i < ManaType.colors().length; i++)
		{
			for (int j = i; j < ManaType.colors().length; j++)
			{
				if (i != j)
				{
					ManaType.Tuple tuple = new ManaType.Tuple(ManaType.colors()[i], ManaType.colors()[j]);
					SYMBOLS[tuple] = new HybridSymbol(tuple);
				}
			}
		}
	}
	
	/**
	 * Get the HybridSymbol corresponding to the given ManaTypes.
	 * 
	 * @param colors ManaTypes to look up
	 * @return The HybridSymbol corresponding to the given ManaTypes, or
	 * null if no such symbol exists.
	 */
	public static HybridSymbol get(ManaType color1, ManaType color2)
	{
		return SYMBOLS[new ManaType.Tuple(color1, color2)];
	}
	
	/**
	 * Get the HybridSymbol corresponding to the String, which is two color characters
	 * separated by a "/".
	 * 
	 * @param pair The String to look up
	 * @return The HybridSymbol corresponding to the given String, or null if no
	 * such symbol exists.
	 */
	public static HybridSymbol get(String pair)
	{
		try
		{
			List<ManaType> colors = Arrays.stream(pair.split("/")).map(ManaType::get).collect(Collectors.toList());
			if (colors.size() != 2)
				return null;
			else
				return SYMBOLS[new ManaType.Tuple(colors)];
		}
		catch (IllegalArgumentException e)
		{
			return null;
		}
	}
	
	/**
	 * This HybridSymbol's Tuple of colors.
	 */
	private final ManaType.Tuple colors;
	
	/**
	 * Create a new HybridSymbol
	 * 
	 * @param colors Tuple containing the colors of the new HybridSymbol
	 */
	private HybridSymbol(ManaType.Tuple colors)
	{
		super(colors[0].toString().toLowerCase() + "_" + colors[1].toString().toLowerCase() + "_mana.png",
				colors[0].shorthand() + "/" + colors[1].shorthand(), 1);
		this.colors = colors;
	}

	/**
	 * @return This HybridSymbol's color weight, which is 0.5 for each of its two colors and
	 * 0 for the other three.
	 * @see editor.database.symbol.Symbol#colorWeights()
	 */
	@Override
	public Map<ManaType, Double> colorWeights()
	{
		return createWeights(new ColorWeight(colors[0], 0.5),
							 new ColorWeight(colors[1], 0.5));
	}

	/**
	 * @param o Symbol to compare with
	 * @return A negative number if the other symbol should come after this HybridSymbol,
	 * the two symbols' Tuples' difference if they are both HybridSymbols, 0 if color order
	 * doesn't matter, and a postive number if the other symbol should come before.
	 * @see editor.database.symbol.Symbol#compareTo(Symbol)
	 */
	@Override
	public int compareTo(ManaSymbol o)
	{
		if (o instanceof HybridSymbol)
			return colors.compareTo(((HybridSymbol)o).colors);
		else
			return super.compareTo(o);
	}
}
