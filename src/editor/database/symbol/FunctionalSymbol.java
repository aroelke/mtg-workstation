package editor.database.symbol;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a symbol that has a specific meaning that isn't related to mana costs.
 *
 * @author Alec Roelke
 */
public class FunctionalSymbol extends Symbol
{
    /**
     * The Chaos symbol that appears on Plane cards.  Because it has a special
     * text representation that doens't use {}, it is exposed as a separate constant.
     */
    public static final FunctionalSymbol CHAOS = new FunctionalSymbol("chaos.png", "CHAOS")
    {
        @Override
        public String toString()
        {
            return "CHAOS";
        }
    };

    /**
     * Map of symbol texts onto their respective symbols.
     */
    public static final Map<String, FunctionalSymbol> SYMBOLS;

    static
    {
        Map<String, FunctionalSymbol> symbols = new HashMap<>();
        // Chaos symbol.
        symbols.put(CHAOS.toString(), CHAOS);
        // Phyrexian phi symbol.
        symbols.put("P", new FunctionalSymbol("phyrexia.png", "P"));
        // Tap symbol.  Used in costs in card text.
        symbols.put("T", new FunctionalSymbol("tap.png", "T"));
        symbols.put("TAP", symbols.get("T"));
        // Untap symbol.  Used in costs in card text.
        symbols.put("Q", new FunctionalSymbol("untap.png", "Q"));
        // Energy counter symbol.
        symbols.put("E", new FunctionalSymbol("energy.png", "E"));

        SYMBOLS = Collections.unmodifiableMap(symbols);
    }

    /**
     * Get the FunctionalSymbol corresponding to the given String.
     *
     * @param s String to look up
     * @return the FunctionalSymbol corresponding to the given String
     * @throws IllegalArgumentException if the String doesn't correspond to a functional symbol
     */
    public static FunctionalSymbol parseFunctionalSymbol(String s) throws IllegalArgumentException
    {
        FunctionalSymbol symbol = tryParseFunctionalSymbol(s);
        if (symbol == null)
            throw new IllegalArgumentException('"' + s + "\" is not a functional symbol");
        return symbol;
    }

    /**
     * Get the FunctionalSymbol corresponding to the given String.
     *
     * @param s String to look up
     * @return the FunctionalSymbol corresponding to the given String, or null if there is none
     */
    public static FunctionalSymbol tryParseFunctionalSymbol(String s)
    {
        return SYMBOLS.get(s.toUpperCase());
    }

    /**
     * Create a new FunctionalSymbol.
     *
     * @param iconName name of the new symbol's icon
     * @param text     text representation of the new symbol
     */
    private FunctionalSymbol(String iconName, String text)
    {
        super(iconName, text);
    }
}
