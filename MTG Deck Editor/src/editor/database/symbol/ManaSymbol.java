package editor.database.symbol;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import editor.database.characteristics.ManaType;

/**
 * This class represents a symbol that represents one or more mana.
 * 
 * @author Alec Roelke
 */
public abstract class ManaSymbol extends Symbol implements Comparable<ManaSymbol>
{
	/**
	 * List of symbol types in the order they should appear in.
	 */
	private static final List<Class<? extends ManaSymbol>> ORDER = Arrays.asList(
			VariableSymbol.class,
			StaticSymbol.class,
			GenericSymbol.class,
			HalfColorSymbol.class,
			TwobridSymbol.class,
			HybridSymbol.class,
			PhyrexianSymbol.class,
			ColorSymbol.class);
	
	/**
	 * Create a map of color weights for a Symbol, where the keys are {@link ManaType}s and
	 * the values are their weights (Doubles).
	 * 
	 * @param weights initial weights to use.
	 * @return the map of {@link ManaType}s onto weights.
	 */
	public static Map<ManaType, Double> createWeights(ColorWeight... weights)
	{
		Map<ManaType, Double> weightsMap = new EnumMap<ManaType, Double>(ManaType.class);
		weightsMap[ManaType.COLORLESS] = 0.0;
		weightsMap[ManaType.WHITE] = 0.0;
		weightsMap[ManaType.BLUE] = 0.0;
		weightsMap[ManaType.BLACK] = 0.0;
		weightsMap[ManaType.RED] = 0.0;
		weightsMap[ManaType.GREEN] = 0.0;
		for (ColorWeight w: weights)
			weightsMap[w.color] = w.weight;
		return weightsMap;
	}
	
	/**
	 * Create a ManaSymbol from a String.
	 * 
	 * @param s String representation of the new ManaSymbol, not surrounded by {}
	 * @return a new ManaSymbol that the specified String represents, or null if there
	 * is no such ManaSymbol.
	 */
	public static ManaSymbol get(String s)
	{
		ManaSymbol value;
		if ((value = ColorSymbol.get(s)) != null)
			return value;
		else if ((value = GenericSymbol.get(s)) != null)
			return value;
		else if ((value = HalfColorSymbol.get(s)) != null)
			return value;
		else if ((value = HybridSymbol.get(s)) != null)
			return value;
		else if ((value = PhyrexianSymbol.get(s)) != null)
			return value;
		else if ((value = TwobridSymbol.get(s)) != null)
			return value;
		else if ((value = VariableSymbol.get(s)) != null)
			return value;
		else if ((value = StaticSymbol.get(s)) != null)
			return value;
		else
			return null;
	}
	
	/**
	 * Sort a list of ManaSymbols according to their ordering in the color wheel.
	 * (Not yet implemented)
	 * 
	 * @param symbols List of ManaSymbols to sort.
	 */
	public static void sort(List<ManaSymbol> symbols)
	{
		// TODO: Implement this
	}
	
	/**
	 * Get the ManaSymbol value of the given String.
	 * 
	 * @param s String to parse
	 * @return 5he ManaSymbol that corresponds to the given String
	 * @throws IllegalArgumentException if the String doesn't represent a Symbol
	 */
	public static ManaSymbol valueOf(String s)
	{
		ManaSymbol symbol = get(s);
		if (symbol == null)
			throw new IllegalArgumentException("Illegal mana symbol string \"" + s + "\"");
		else
			return symbol;
	}
	
	/**
	 * How much this ManaSymbols is worth for calculating converted mana costs.
	 */
	private final double value;
	
	/**
	 * Create a new ManaSymbol.
	 * 
	 * @param iconName name of the new ManaSymbol
	 * @param text String representation of the new ManaSymbol
	 * @param v value of the new ManaSymbol in a mana cost.
	 */
	protected ManaSymbol(String iconName, String text, double v)
	{
		super(iconName, text);
		value = v;
	}
	
	/**
	 * Get a map of each {@link ManaType} onto this ManaSymbol's weight for that type.
	 * Each weight should always be between 0 and 1.
	 * 
	 * @return this ManaSymbol's color weight map
	 */
	public abstract Map<ManaType, Double> colorWeights();
	
	@Override
	public int compareTo(ManaSymbol other)
	{
		if (ORDER.contains(getClass()) && ORDER.contains(other.getClass()))
			return ORDER.indexOf(getClass()) - ORDER.indexOf(other.getClass());
		else if (!ORDER.contains(getClass()))
			return 1;
		else if (!ORDER.contains(other.getClass()))
			return -1;
		else
			return 0;
	}
	
	/**
	 * Get the value of this ManaSymbol in a mana cost.
	 * 
	 * @return this ManaSymbol's value
	 */
	public double value()
	{
		return value;
	}
}
