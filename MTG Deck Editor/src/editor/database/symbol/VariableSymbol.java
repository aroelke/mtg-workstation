package editor.database.symbol;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import editor.database.characteristics.ManaType;

/**
 * This class represents a symbol representing a variable amount
 * of generic mana using the variables X, Y or Z.
 * 
 * @author Alec Roelke
 */
public class VariableSymbol extends ManaSymbol
{
	/**
	 * Map of variable names onto their corresponding symbols.  This map
	 * is not case-sensitive.
	 */
	private static final Map<String, VariableSymbol> SYMBOLS = Collections.unmodifiableMap(
			Arrays.asList('X', 'Y', 'Z').stream().collect(Collectors.toMap(String::valueOf, VariableSymbol::new)));
	
	/**
	 * Get the VariableSymbol corresponding to the given String.
	 * 
	 * @param x The String to look up
	 * @return The VariableSymbol corresponding to the String, or
	 * null if there is no such symbol.
	 */
	public static VariableSymbol get(String x)
	{
		return SYMBOLS[x.toUpperCase()];
	}
	
	/**
	 * Variable name for this VariableSymbol.
	 */
	private final char var;
	
	/**
	 * Create a new VariableSymbol with the corresponding variable.
	 * 
	 * @param v Variable name for the new VariableSymbol
	 */
	private VariableSymbol(char v)
	{
		super(Character.toLowerCase(v) + "_mana.png", String.valueOf(Character.toUpperCase(v)), 0);
		var = Character.toUpperCase(v);
	}

	/**
	 * @return A Map containing this ColorSymbol's color weight.  All values will be 0 except for
	 * colorless, which will be 0.5.
	 * @see editor.database.symbol.Symbol#colorWeights()
	 */
	@Override
	public Map<ManaType, Double> colorWeights()
	{
		return createWeights(new ColorWeight(ManaType.COLORLESS, 0.5));
	}
	
	/**
	 * @param other Symbol to compare with
	 * @return A positive number if this VariableSymbol should come after
	 * the given Symbol in an order, and a negative number otherwise
	 * (or 0 if they're the same symbol).
	 */
	@Override
	public int compareTo(ManaSymbol other)
	{
		if (other instanceof VariableSymbol)
			return var - ((VariableSymbol)other).var;
		else
			return super.compareTo(other);
	}
}
