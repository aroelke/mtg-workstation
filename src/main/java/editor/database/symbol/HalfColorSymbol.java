package editor.database.symbol;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import editor.database.attributes.ManaType;

/**
 * This class represents half of a colored mana symbol.
 *
 * @author Alec Roelke
 */
public class HalfColorSymbol extends ManaSymbol
{
    /**
     * Map of color onto its corresponding half-symbol.
     */
    public static final Map<ManaType, HalfColorSymbol> SYMBOLS = Collections.unmodifiableMap(
            Arrays.stream(ManaType.values()).collect(Collectors.toMap(Function.identity(), HalfColorSymbol::new)));

    /**
     * Get the HalfColorSymbol corresponding to the given String.
     *
     * @param col String to get the symbol for
     * @return the HalfColorSymbol corresponding to the given color string
     * @throws IllegalArgumentException if the string does not describe a half color symbol
     */
    public static HalfColorSymbol parseHalfColorSymbol(String col) throws IllegalArgumentException
    {
        return tryParseHalfColorSymbol(String.valueOf(col.charAt(1))).orElseThrow(() -> new IllegalArgumentException('"' + col + "\" is not a half color symbol"));
    }

    /**
     * Get the HalfColorSymbol corresponding to the given String.
     *
     * @param col String to get the symbol for
     * @return the HalfColorSymbol corresponding to the given color string, or null if no such
     * symbol exists
     */
    public static Optional<HalfColorSymbol> tryParseHalfColorSymbol(String col)
    {
        if (col.length() == 2 && Character.toUpperCase(col.charAt(0)) == 'H')
            return Optional.ofNullable(SYMBOLS.get(ManaType.tryParseManaType(col.charAt(1))));
        return Optional.empty();
    }

    /**
     * This HalfColorSymbol's color.
     */
    private final ManaType color;

    /**
     * Create a new HalfColorSymbol.
     *
     * @param color color of the new HalfColorSymbol
     */
    private HalfColorSymbol(ManaType color)
    {
        super("half_" + color.toString().toLowerCase() + "_mana.png", "H" + String.valueOf(color.shorthand()), 0.5);
        this.color = color;
    }

    /**
     * {@inheritDoc}
     * This HalfColorSymbol's weights are 0.5 for its color and 0 for all the others.
     */
    @Override
    public Map<ManaType, Double> colorIntensity()
    {
        return createIntensity(new ColorIntensity(color, 0.5));
    }

    @Override
    public int compareTo(ManaSymbol o)
    {
        if (o instanceof HalfColorSymbol)
            return color.colorOrder(((HalfColorSymbol)o).color);
        else
            return super.compareTo(o);
    }
}
