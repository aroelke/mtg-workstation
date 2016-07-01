package editor.database.symbol;

import java.util.HashMap;
import java.util.Map;

import editor.database.characteristics.ManaType;

/**
 * This class represents a symbol representing a variable amount
 * of generic mana using the variables X, Y or Z.
 * 
 * @author Alec Roelke
 */
public class VariableSymbol extends Symbol
{
	/**
	 * Map of variable names onto their corresponding symbols.  This map
	 * is not case-sensitive.
	 */
	private static final Map<String, VariableSymbol> SYMBOLS = new HashMap<String, VariableSymbol>();
	static
	{
		SYMBOLS.put("X", new VariableSymbol('X'));
		SYMBOLS.put("Y", new VariableSymbol('Y'));
		SYMBOLS.put("Z", new VariableSymbol('Z'));
	}
	
	/**
	 * Get the VariableSymbol corresponding to the given String.
	 * 
	 * @param x The String to look up
	 * @return The VariableSymbol corresponding to the String, or
	 * null if there is no such symbol.
	 */
	public static VariableSymbol get(String x)
	{
		return SYMBOLS.get(x.toUpperCase());
	}
	
	/**
	 * Variable name for this VariableSymbol.
	 */
	private char var;
	
	/**
	 * Create a new VariableSymbol with the corresponding variable.
	 * 
	 * @param v Variable name for the new VariableSymobl
	 */
	private VariableSymbol(char v)
	{
		super(Character.toLowerCase(v) + "_mana.png");
		var = Character.toUpperCase(v);
	}
	
	/**
	 * @return The text representation of this VariableSymbol, which is
	 * its variable name.
	 */
	@Override
	public String getText()
	{
		return String.valueOf(var);
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
	public int compareTo(Symbol other)
	{
		if (other instanceof VariableSymbol)
			return var - ((VariableSymbol)other).var;
		else
			return super.compareTo(other);
	}
}
