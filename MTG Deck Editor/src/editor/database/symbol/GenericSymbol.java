package editor.database.symbol;

import java.util.Map;
import java.util.stream.IntStream;

import editor.database.characteristics.ManaType;

/**
 * This class represents an amount of generic mana that might appear in a mana cost
 * on a Magic: The Gathering card.
 * 
 * @author Alec Roelke
 */
public class GenericSymbol extends ManaSymbol
{
	/**
	 * Highest consecutive value that a generic symbol might attain.
	 * @see editor.database.symbol.Symbol
	 */
	public static final int HIGHEST_CONSECUTIVE = 20;
	/**
	 * Array of consecutive GenericSymbols.
	 */
	private static final GenericSymbol[] N = IntStream.range(0, HIGHEST_CONSECUTIVE + 1).mapToObj(GenericSymbol::new).toArray(GenericSymbol[]::new);
	/**
	 * GenericSymbol representing 100 mana.
	 */
	private static final GenericSymbol HUNDRED = new GenericSymbol(100);
	/**
	 * GenericSymbol representing 1,000,000 mana.
	 */
	private static final GenericSymbol MILLION = new GenericSymbol(1000000);
	
	/**
	 * Get the symbol corresponding to a number.
	 * 
	 * @param n Number to get the symbol of
	 * @return The GenericSymbol corresponding to the given number, or
	 * null if none exists.
	 */
	public static GenericSymbol get(int n)
	{
		if (n <= HIGHEST_CONSECUTIVE)
			return N[n];
		else if (n == 100)
			return HUNDRED;
		else if (n == 1000000)
			return MILLION;
		else
			return null;
	}
	
	/**
	 * Get the symbol corresponding to a String.
	 * 
	 * @param n String to get the symbol of
	 * @return The GenericSymbol corresponding to the given String,
	 * or null if none exists.
	 */
	public static GenericSymbol get(String n)
	{
		try
		{
			return get(Integer.parseInt(n));
		}
		catch (NumberFormatException e)
		{
			return null;
		}
	}
	
	/**
	 * Amount of mana this GenericSymbol represents.
	 */
	private final int amount;
	
	/**
	 * Create a new GenericSymbol.
	 * 
	 * @param amount Amount of mana the new ColorlessSymbol represents.
	 */
	private GenericSymbol(int amount)
	{
		super(amount + "_mana.png", String.valueOf(amount), amount);
		this.amount = amount;
	}

	/**
	 * TODO: Comment this
	 */
	@Override
	public Map<ManaType, Double> colorWeights()
	{
		return createWeights();
	}
	
	/**
	 * @param o Symbol to compare to
	 * @return A negative number if the other symbol is a color symbol, half symbol, hybrid symbol,
	 * or snow symbol; the difference between amounts if the other symbol is a colorless symbol,
	 * and a positive number otherwise.
	 * @see editor.database.symbol.Symbol#compareTo(Symbol)
	 */
	@Override
	public int compareTo(ManaSymbol o)
	{
		if (o instanceof GenericSymbol)
			return amount - ((GenericSymbol)o).amount;
		else
			return super.compareTo(o);
	}
}
