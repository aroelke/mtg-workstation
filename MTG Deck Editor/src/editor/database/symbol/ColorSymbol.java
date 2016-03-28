package editor.database.symbol;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import editor.database.characteristics.ManaType;

/**
 * This class represents a symbol for a single colored mana.
 * 
 * @author Alec Roelke
 */
public class ColorSymbol extends Symbol
{
	/**
	 * Map of ManaType onto its corresponding symbol.  To get a colored symbol, use this Map.
	 * @see editor.database.symbol.Symbol
	 */
	public static final Map<ManaType, ColorSymbol> SYMBOLS = Collections.unmodifiableMap(
			Arrays.stream(ManaType.values()).collect(Collectors.toMap((t) -> t, ColorSymbol::new)));
	
	/**
	 * Get the symbol corresponding to a color.
	 * 
	 * @param col ManaType to find the symbol for
	 * @return The ColorSymbol corresponding to the given ManaType, or null
	 * if none exists. 
	 */
	public static ColorSymbol get(ManaType col)
	{
		return SYMBOLS.get(col);
	}
	
	/**
	 * Color of this ColorSymbol.
	 */
	private final ManaType color;
	
	/**
	 * Create a new ColorSymbol.
	 * 
	 * @param color Color of the new ColorSymbol.
	 */
	private ColorSymbol(ManaType color)
	{
		super(color.toString().toLowerCase() + "_mana.png");
		this.color = color;
	}
	
	/**
	 * @return The ManaType representing the color of this ColorSymbol.
	 */
	public ManaType color()
	{
		return color;
	}
	
	/**
	 * @return The shorthand character representing the color of this ColorSymbol.
	 * @see editor.database.symbol.Symbol#getText()
	 */
	@Override
	public String getText()
	{
		return String.valueOf(color.shorthand());
	}

	/**
	 * @return The value of this symbol for converted mana costs: 1.
	 * @see editor.database.symbol.Symbol#value()
	 */
	@Override
	public double value()
	{
		return 1;
	}

	/**
	 * @return A Map containing this ColorSymbol's color weight.  All values will be 0 except for
	 * this ColorSymbol's color, which will be 1.
	 * @see editor.database.symbol.Symbol#colorWeights()
	 */
	@Override
	public Map<ManaType, Double> colorWeights()
	{
		return createWeights(new ColorWeight(color, 1.0));
	}

	/**
	 * @param o Symbol to compare with
	 * @return A positive number if the other symbol can appear in mana costs but is not
	 * a ColorSymbol; the color-ordering of this symbol and the other symbol if they are
	 * both ColorSymbols; or 0 otherwise.
	 * @see editor.database.symbol.Symbol#compareTo(Symbol)
	 */
	@Override
	public int compareTo(Symbol o)
	{
		if (o instanceof ColorSymbol)
			return color.colorOrder(((ColorSymbol)o).color);
		else
			return super.compareTo(o);
	}

	/**
	 * @return <code>true</code> if the other Symbol is a ColorSymbol and the colors are the same
	 * or <code>false</code> otherwise.
	 * @see editor.database.symbol.Symbol#sameSymbol(Symbol)
	 */
	@Override
	public boolean sameSymbol(Symbol other)
	{
		return other instanceof ColorSymbol && color.equals(((ColorSymbol)other).color);
	}
}
