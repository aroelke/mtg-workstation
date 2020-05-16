package editor.database.symbol;

import java.util.Map;
import java.util.Optional;

import editor.database.attributes.ManaType;
import editor.util.UnicodeSymbols;

/**
 * This class represents a symbol that has no variations (like based on color or number).
 * Each one has a different use.
 *
 * @author Alec Roelke
 */
public class StaticSymbol extends ManaSymbol
{
    /**
     * Map of symbol texts onto their respective symbols.
     */
    public static final Map<String, StaticSymbol> SYMBOLS = Map.of("1/2", new StaticSymbol("half_mana.png", "1/2", 0.5),
            String.valueOf(UnicodeSymbols.ONE_HALF), new StaticSymbol("half_mana.png", "1/2", 0.5),
            String.valueOf(UnicodeSymbols.INFINITY), new StaticSymbol("infinity_mana.png", String.valueOf(UnicodeSymbols.INFINITY), Double.POSITIVE_INFINITY),
            "S", new StaticSymbol("snow_mana.png", "S", 1));

    /**
     * Get the StaticSymbol corresponding to the given String.
     *
     * @param s String to look up
     * @return the StaticSymbol corresponding to the given String
     * @throws IllegalArgumentException if the String doesn't correspond to a static symbol
     */
    public static StaticSymbol parseStaticSymbol(String s) throws IllegalArgumentException
    {
        return tryParseStaticSymbol(s).orElseThrow(() -> new IllegalArgumentException('"' + s + "\" is not a static symbol"));
    }

    /**
     * Get the StaticSymbol corresponding to the given String.
     *
     * @param s String to look up
     * @return the StaticSymbol corresponding to the given String, or null if there is none.
     */
    public static Optional<StaticSymbol> tryParseStaticSymbol(String s)
    {
        return Optional.ofNullable(SYMBOLS.get(s.toUpperCase()));
    }

    /**
     * Create a new StaticSymbol.
     *
     * @param iconName icon name of the new symbol
     * @param text text representation of the new symbol
     * @param value sorting value of the new StaticSymbol
     */
    private StaticSymbol(String iconName, String text, double value)
    {
        super(iconName, text, value);
    }

    /**
     * {@inheritDoc}
     * There are no color weights for a StaticSymbol.
     */
    @Override
    public Map<ManaType, Double> colorWeights()
    {
        return createWeights();
    }
}
