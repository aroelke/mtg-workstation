package editor.database.symbol;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import editor.database.characteristics.ManaType;

/**
 * TODO: Comment this class
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
	 * Create a map of color weights for a Symbol, where the keys are ManaTypes and the values
	 * are their weights (Doubles).
	 * 
	 * @param weights Initial weights to use.
	 * @return The Map of ManaTypes onto weights.
	 */
	public static Map<ManaType, Double> createWeights(ColorWeight... weights)
	{
		Map<ManaType, Double> weightsMap = new HashMap<ManaType, Double>();
		weightsMap.put(ManaType.COLORLESS, 0.0);
		weightsMap.put(ManaType.WHITE, 0.0);
		weightsMap.put(ManaType.BLUE, 0.0);
		weightsMap.put(ManaType.BLACK, 0.0);
		weightsMap.put(ManaType.RED, 0.0);
		weightsMap.put(ManaType.GREEN, 0.0);
		for (ColorWeight w: weights)
			weightsMap.put(w.color, w.weight);
		return weightsMap;
	}
	
	/**
	 * Create a ManaSymbol from a String.
	 * 
	 * @param s String representation of the new ManaSymbol, not surrounded by {}
	 * @return A new ManaSymbol that the specified String represents, or null if there
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
	
	public static ManaSymbol valueOf(String s)
	{
		ManaSymbol symbol = get(s);
		if (symbol == null)
			throw new IllegalArgumentException("Illegal mana symbol string \"" + s + "\"");
		else
			return symbol;
	}
	
	public static void sort(List<ManaSymbol> symbols)
	{
		// TODO: Implement this
	}
	
	private final double value;
	
	protected ManaSymbol(String iconName, double v)
	{
		super(iconName);
		value = v;
	}
	
	/**
	 * @return How much mana this Symbol is worth for calculating converted mana costs.
	 */
	public double value()
	{
		return value;
	}
	
	/**
	 * @return This Symbol's color weight map.
	 */
	public abstract Map<ManaType, Double> colorWeights();
	
	/**
	 * @param other Symbol to compare with
	 * @return A negative number if this Symbol should appear before the other in a list,
	 * 0 if the two symbols are the same or if their order doesn't matter,
	 * and a positive number if this Symbol should appear after it.
	 */
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
}
