package editor.database.symbol;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Comment this class
 * @author Alec Roelke
 */
public class VariableSymbol extends Symbol
{
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
	
	private char var;
	
	private VariableSymbol(char v)
	{
		super(Character.toLowerCase(v) + "_mana.png");
		var = Character.toUpperCase(v);
	}
	
	@Override
	public String getText()
	{
		return String.valueOf(var);
	}

	@Override
	public int compareTo(Symbol other)
	{
		if (other instanceof VariableSymbol)
			return var - ((VariableSymbol)other).var;
		else
			return -1;
	}

	@Override
	public boolean sameSymbol(Symbol other)
	{
		if (other instanceof VariableSymbol)
			return var == ((VariableSymbol)other).var;
		else
			return false;
	}

}
