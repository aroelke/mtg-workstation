package editor.database.symbol;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Comment this class
 * @author Alec Roelke
 */
public class StaticSymbol extends Symbol
{
	private static final Map<String, StaticSymbol> SYMBOLS = new HashMap<String, StaticSymbol>();
	static
	{
		SYMBOLS.put("CHAOS", new StaticSymbol("chaos.png", "CHAOS")
		{
			@Override
			public String toString()
			{
				return "CHAOS";
			}
		});
		SYMBOLS.put("1/2", new StaticSymbol("half_mana.png", "1/2"));
		SYMBOLS.put("½", SYMBOLS.get("1/2"));
		SYMBOLS.put("∞", new StaticSymbol("infinity_mana.png", "∞"));
		SYMBOLS.put("P", new StaticSymbol("phyrexia.png", "P"));
		SYMBOLS.put("S", new StaticSymbol("snow_mana.png", "S"));
		SYMBOLS.put("T", new StaticSymbol("tap.png", "T"));
		SYMBOLS.put("Q", new StaticSymbol("untap.png", "Q"));
	}
	
	public static StaticSymbol get(String s)
	{
		return SYMBOLS.get(s.toUpperCase());
	}
	
	private String text;
	
	private StaticSymbol(String iconName, String t)
	{
		super(iconName);
		text = t;
	}

	@Override
	public String getText()
	{
		return text;
	}
}
