package editor.database.symbol;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import editor.database.characteristics.ManaType;

/**
 * This class represents a two-color hybrid symbol.  It will sort its colors so that they
 * appear in the correct order.
 *
 * @author Alec Roelke
 */
public class HybridSymbol extends ManaSymbol
{
    /**
     * Map mapping each pair of colors to their corresponding hybrid symbols.
     */
    public static final Map<ManaType, Map<ManaType, HybridSymbol>> SYMBOLS = Collections.unmodifiableMap(
            Arrays.stream(ManaType.colors()).collect(Collectors.toMap(Function.identity(), (m) -> Arrays.stream(
                    ManaType.colors()).filter((n) -> n != m).collect(Collectors.toMap(Function.identity(), (n) -> new HybridSymbol(m, n))))));

    /**
     * Get the HybridSymbol corresponding to the String, which is two color characters
     * separated by a "/".
     *
     * @param pair the String to look up
     * @return the HybridSymbol corresponding to the given String
     * @throws IllegalArgumentException if the String does not describe a hybrid symbol
     */
    public static HybridSymbol parseHybridSymbol(String pair) throws IllegalArgumentException
    {
        HybridSymbol symbol = tryParseHybridSymbol(pair);
        if (symbol != null)
            return symbol;
        throw new IllegalArgumentException('"' + pair + "\" is not a hybrid symbol");
    }

    /**
     * Get the HybridSymbol corresponding to the String, which is two color characters
     * separated by a "/".
     *
     * @param pair the String to look up
     * @return the HybridSymbol corresponding to the given String, or null if there is none
     */
    public static HybridSymbol tryParseHybridSymbol(String pair)
    {
        List<ManaType> colors = Arrays.stream(pair.split("/")).map(ManaType::tryParseManaType).collect(Collectors.toList());
        if (colors.size() == 2 && SYMBOLS.get(colors.get(0)) != null)
            return SYMBOLS.get(colors.get(0)).get(colors.get(1));
        return null;
    }

    /**
     * First color of the hybrid symbol.
     */
    private final ManaType color1;
    /**
     * Second color of the hybrid symbol.
     */
    private final ManaType color2;

    /**
     * Create a new hybrid symbol out of the two given colors.
     *
     * @param col1 first color of the new hybrid symbol
     * @param col2 second color of the new hybrid symbol
     */
    private HybridSymbol(ManaType col1, ManaType col2)
    {
        super((col1.colorOrder(col2) > 0 ? col2 : col1).toString().toLowerCase() + '_' + (col1.colorOrder(col2) > 0 ? col1 : col2).toString().toLowerCase() + "_mana.png",
                (col1.colorOrder(col2) > 0 ? col2 : col1).shorthand() + "/" + (col1.colorOrder(col2) > 0 ? col1 : col2).shorthand(),
                1);
        color1 = col1;
        color2 = col2;
    }

    /**
     * {@inheritDoc}
     * This HybridSymbol's weights are 0.5 for its colors and 0 for the rest.
     */
    @Override
    public Map<ManaType, Double> colorWeights()
    {
        return createWeights(new ColorWeight(color1, 0.5),
                new ColorWeight(color2, 0.5));
    }

    @Override
    public int compareTo(ManaSymbol o)
    {
        if (o instanceof HybridSymbol)
        {
            HybridSymbol other = (HybridSymbol)o;
            return color1.compareTo(other.color1) * 10 + color2.compareTo(other.color2);
        }
        else
            return super.compareTo(o);
    }
}
