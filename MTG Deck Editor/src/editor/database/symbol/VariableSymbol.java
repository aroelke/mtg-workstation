package editor.database.symbol;

import java.util.HashMap;
import java.util.Map;

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
	public static final Map<Character, VariableSymbol> SYMBOLS = new HashMap<Character, VariableSymbol>();
	static
	{
		SYMBOLS.put('X', new VariableSymbol('X'));
		SYMBOLS.put('x', SYMBOLS.get('X'));
		SYMBOLS.put('Y', new VariableSymbol('Y'));
		SYMBOLS.put('y', SYMBOLS.get('Y'));
		SYMBOLS.put('Z', new VariableSymbol('Z'));
		SYMBOLS.put('z', SYMBOLS.get('Z'));
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

	/**
	 * @param other Symbol to compare with
	 * @return <code>true</code> if this VariableSymbol and the other Symbol
	 * are the same, and <code>false</code> otherwise.
	 */
	@Override
	public boolean sameSymbol(Symbol other)
	{
		if (other instanceof VariableSymbol)
			return var == ((VariableSymbol)other).var;
		else
			return false;
	}

}
