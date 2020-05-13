package editor.database.symbol;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import editor.database.attributes.ManaType;

/**
 * This class represents a Phyrexian mana symbol, which can be paid for either with mana of the
 * same color or two life.
 *
 * @author Alec Roelke
 */
public class PhyrexianSymbol extends ManaSymbol
{
    /**
     * Map of colors onto their corresponding Phyrexian symbols.
     */
    public static final Map<ManaType, PhyrexianSymbol> SYMBOLS = Collections.unmodifiableMap(
            Arrays.stream(ManaType.colors()).collect(Collectors.toMap(Function.identity(), PhyrexianSymbol::new)));

    /**
     * Get the Phyrexian symbol corresponding to the given String, which should be
     * a color character followed by either /p or /P.
     *
     * @param col String to parse
     * @return the corresponding Phyrexian symbol
     * @throws IllegalArgumentException if the String doesn't correspond to a Phyrexian symbol
     */
    public static PhyrexianSymbol parsePhyrexianSymbol(String col) throws IllegalArgumentException
    {
        PhyrexianSymbol symbol = tryParsePhyrexianSymbol(col);
        if (symbol == null)
            throw new IllegalArgumentException('"' + col + "\" is not a Phyrexian symbol");
        return symbol;
    }

    /**
     * Get the Phyrexian symbol corresponding to the given String, which should be
     * a color character followed by either /p or /P.
     *
     * @param col String to parse
     * @return the corresponding Phyrexian symbol, or null if there is none
     */
    public static PhyrexianSymbol tryParsePhyrexianSymbol(String col)
    {
        if (col.length() == 3 && col.charAt(1) == '/' && Character.toUpperCase(col.charAt(2)) == 'P')
            return SYMBOLS.get(ManaType.tryParseManaType(col.charAt(0)));
        return null;
    }

    /**
     * This PhyrexianSymbol's color.
     */
    public final ManaType color;

    /**
     * Create a new PhyrexianSymbol.
     *
     * @param color the new PhyrexianSymbol's color
     */
    private PhyrexianSymbol(ManaType color)
    {
        super("phyrexian_" + color.toString().toLowerCase() + "_mana.png", String.valueOf(color.shorthand()) + "/P", 1);
        this.color = color;
    }

    /**
     * {@inheritDoc}
     * This PhyrexianSymbols color weights are 0.5 for its color and 0 for all of the others.
     */
    @Override
    public Map<ManaType, Double> colorWeights()
    {
        return createWeights(new ColorWeight(color, 0.5));
    }

    @Override
    public int compareTo(ManaSymbol o)
    {
        if (o instanceof PhyrexianSymbol)
            return color.colorOrder(((PhyrexianSymbol)o).color);
        else
            return super.compareTo(o);
    }
}
