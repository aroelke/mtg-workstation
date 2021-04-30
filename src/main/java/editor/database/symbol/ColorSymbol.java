package editor.database.symbol;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import editor.database.attributes.ManaType;

/**
 * This class represents a symbol for a single type of mana.
 *
 * @author Alec Roelke
 */
public class ColorSymbol extends ManaSymbol
{
    /**
     * Map of mana type onto its corresponding ColorSymbol.
     */
    public static final Map<ManaType, ColorSymbol> SYMBOLS = Collections.unmodifiableMap(
            Arrays.stream(ManaType.values()).collect(Collectors.toMap(Function.identity(), ColorSymbol::new)));

    /**
     * Get the symbol corresponding to a color string.
     *
     * @param col String to find the symbol for
     * @return the ColorSymbol corresponding to the given String
     * @throws IllegalArgumentException if the string doesn't correspond to a color symbol
     */
    public static ColorSymbol parseColorSymbol(String col) throws IllegalArgumentException
    {
        return tryParseColorSymbol(col).orElseThrow(() -> new IllegalArgumentException('"' + col + "\" is not a color symbol"));
    }

    /**
     * Get the symbol corresponding to a color string.
     *
     * @param col String to find the symbol for
     * @return the ColorSymbol corresponding to the given String, or null if none exists
     */
    public static Optional<ColorSymbol> tryParseColorSymbol(String col)
    {
        return Optional.ofNullable(SYMBOLS.get(ManaType.tryParseManaType(col)));
    }

    /**
     * Color of this ColorSymbol.
     */
    private final ManaType color;

    /**
     * Create a new ColorSymbol.
     *
     * @param color mana type of the new ColorSymbol.
     */
    private ColorSymbol(ManaType color)
    {
        super(color.toString().toLowerCase() + "_mana.png", String.valueOf(color.shorthand()), 1);
        this.color = color;
    }

    /**
     * {@inheritDoc}
     * This ColorSymbol's weights are 1 for its mana type and 0 for all the others.
     */
    @Override
    public Map<ManaType, Double> colorIntensity()
    {
        return createIntensity(new ColorIntensity(color, 1.0));
    }

    @Override
    public int compareTo(ManaSymbol o)
    {
        if (o instanceof ColorSymbol s)
            return color.compareTo(s.color);
        else
            return super.compareTo(o);
    }
}
