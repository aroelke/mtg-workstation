package editor.database.symbol;

import java.util.Map;
import java.util.stream.IntStream;

import editor.database.characteristics.ManaType;

/**
 * This class represents an amount of generic mana that might appear in a mana cost.
 *
 * @author Alec Roelke
 */
public class GenericSymbol extends ManaSymbol
{
    /**
     * Highest consecutive value that a generic symbol might attain.
     *
     * @see editor.database.symbol.Symbol
     */
    public static final int HIGHEST_CONSECUTIVE = 20;
    /**
     * Array of consecutive GenericSymbols.
     */
    public static final GenericSymbol[] N = IntStream.rangeClosed(0, HIGHEST_CONSECUTIVE).mapToObj(GenericSymbol::new).toArray(GenericSymbol[]::new);
    /**
     * GenericSymbol representing 100 mana.
     */
    public static final GenericSymbol HUNDRED = new GenericSymbol(100);
    /**
     * GenericSymbol representing 1,000,000 mana.
     */
    public static final GenericSymbol MILLION = new GenericSymbol(1000000);

    /**
     * Get the symbol corresponding to a number.
     *
     * @param n number to get the symbol of
     * @return the GenericSymbol corresponding to the given number
     * @throws ArrayIndexOutOfBoundsException if there is no symbol with the corresponding number
     */
    public static GenericSymbol get(int n) throws ArrayIndexOutOfBoundsException
    {
        if (n <= HIGHEST_CONSECUTIVE)
            return N[n];
        else if (n == 100)
            return HUNDRED;
        else if (n == 1000000)
            return MILLION;
        else
            throw new ArrayIndexOutOfBoundsException();
    }

    /**
     * Get the symbol corresponding to a String.
     *
     * @param n String to get the symbol of
     * @return the GenericSymbol corresponding to the given String
     * @throws NumberFormatException          if the string doesn't parse to an integer
     * @throws ArrayIndexOutOfBoundsException if the parsed string doens't correspond to a generic symbol
     */
    public static GenericSymbol parseGenericSymbol(String n) throws NumberFormatException, ArrayIndexOutOfBoundsException
    {
        return get(Integer.parseInt(n));
    }

    /**
     * Get the symbol corresponding to a String.
     *
     * @param n String to get the symbol of
     * @return the GenericSymbol corresponding to the given String, or null if none exists
     */
    public static GenericSymbol tryParseGenericSymbol(String n)
    {
        try
        {
            return get(Integer.parseInt(n));
        }
        catch (NumberFormatException|ArrayIndexOutOfBoundsException e)
        {
            return null;
        }
    }

    /**
     * Create a new GenericSymbol.
     *
     * @param amount amount of mana the new ColorlessSymbol represents.
     */
    private GenericSymbol(int amount)
    {
        super(amount + "_mana.png", String.valueOf(amount), amount);
    }

    /**
     * {@inheritDoc}
     * Generic mana symbols have no color weights.
     */
    @Override
    public Map<ManaType, Double> colorWeights()
    {
        return createWeights();
    }

    @Override
    public int compareTo(ManaSymbol o)
    {
        if (o instanceof GenericSymbol)
            return (int)value() - (int)o.value();
        else
            return super.compareTo(o);
    }
}
