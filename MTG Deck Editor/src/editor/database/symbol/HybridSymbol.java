package editor.database.symbol;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import editor.database.characteristics.ManaType;

/**
 * This class represents a two-color hybrid symbol.  It will sort its colors so that they
 * appear in the correct order.
 * 
 * @author Alec Roelke
 */
public class HybridSymbol extends ManaSymbol
{
	/**
	 * Map mapping each pair of colors to their corresponding hybrid symbols.
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
					SYMBOLS.put(tuple, new HybridSymbol(tuple));
				}
			}
		}
	}
	
	/**
	 * Get the HybridSymbol corresponding to the given colors.
	 * 
	 * @param color1 first color
	 * @param color2 second color
	 * @return The HybridSymbol corresponding to the given colors, or null if no such symbol exists.
	 */
	public static HybridSymbol get(ManaType color1, ManaType color2)
	{
		return SYMBOLS.get(new ManaType.Tuple(color1, color2));
	}
	
	/**
	 * Get the HybridSymbol corresponding to the String, which is two color characters
	 * separated by a "/".
	 * 
	 * @param pair the String to look up
	 * @return the HybridSymbol corresponding to the given String, or null if no
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
				return SYMBOLS.get(new ManaType.Tuple(colors));
		}
		catch (IllegalArgumentException e)
		{
			return null;
		}
	}
	
	/**
	 * This HybridSymbol's pair of colors.
	 */
	private final ManaType.Tuple colors;
	
	/**
	 * Create a new HybridSymbol
	 * 
	 * @param colors Tuple containing the colors of the new HybridSymbol
	 */
	private HybridSymbol(ManaType.Tuple colors)
	{
		super(colors.get(0).toString().toLowerCase() + "_" + colors.get(1).toString().toLowerCase() + "_mana.png",
				colors.get(0).shorthand() + "/" + colors.get(1).shorthand(), 1);
		this.colors = colors;
	}

	/**
	 * {@inheritDoc}
	 * This HybridSymbol's weights are 0.5 for its colors and 0 for the rest.
	 */
	@Override
	public Map<ManaType, Double> colorWeights()
	{
		return createWeights(new ColorWeight(colors.get(0), 0.5),
							 new ColorWeight(colors.get(1), 0.5));
	}

	@Override
	public int compareTo(ManaSymbol o)
	{
		if (o instanceof HybridSymbol)
			return colors.compareTo(((HybridSymbol)o).colors);
		else
			return super.compareTo(o);
	}
}
