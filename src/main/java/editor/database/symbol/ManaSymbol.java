package editor.database.symbol;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import editor.database.attributes.ManaType;

/**
 * This class represents a symbol that represents one or more mana.
 *
 * @author Alec Roelke
 */
public abstract class ManaSymbol extends Symbol implements Comparable<ManaSymbol>
{
    /**
     * List of symbol types in the order they should appear in.
     */
    private static final List<Class<? extends ManaSymbol>> ORDER = List.of(
            VariableSymbol.class,
            StaticSymbol.class,
            GenericSymbol.class,
            HalfColorSymbol.class,
            TwobridSymbol.class,
            HybridSymbol.class,
            PhyrexianSymbol.class,
            ColorSymbol.class);

    /**
     * Create a map of color weights for a Symbol, where the keys are {@link ManaType}s and
     * the values are their weights (Doubles).
     *
     * @param weights initial weights to use.
     * @return the map of {@link ManaType}s onto weights.
     */
    public static Map<ManaType, Double> createIntensity(ColorIntensity... weights)
    {
        var weightsMap = new EnumMap<ManaType, Double>(Arrays.stream(ManaType.values()).collect(Collectors.toMap(Function.identity(), (m) -> 0.0)));
        for (ColorIntensity w : weights)
            weightsMap.put(w.color, w.intensity);
        return weightsMap;
    }

    /**
     * Create a ManaSymbol from a String.
     *
     * @param s String representation of the new ManaSymbol, not surrounded by {}
     * @return a new ManaSymbol that the specified String represents
     * @throws IllegalArgumentException if the String doesn't correspond to a mana symbol
     */
    public static ManaSymbol parseManaSymbol(String s) throws IllegalArgumentException
    {
        return tryParseManaSymbol(s).orElseThrow(() -> new IllegalArgumentException('"' + s + "\" is not a mana symbol"));
    }

    /**
     * Create a ManaSymbol from a String.
     *
     * @param s String representation of the new ManaSymbol, not surrounded by {}
     * @return a new ManaSymbol that the specified String represents, or null if there is none
     */
    public static Optional<? extends ManaSymbol> tryParseManaSymbol(String s)
    {
        var color = ColorSymbol.tryParseColorSymbol(s);
        if (color.isPresent())
            return color;
        var generic = GenericSymbol.tryParseGenericSymbol(s);
        if (generic.isPresent())
            return generic;
        var half = HalfColorSymbol.tryParseHalfColorSymbol(s);
        if (half.isPresent())
            return half;
        var hybrid = HybridSymbol.tryParseHybridSymbol(s);
        if (hybrid.isPresent())
            return hybrid;
        var phyrexian = PhyrexianSymbol.tryParsePhyrexianSymbol(s);
        if (phyrexian.isPresent())
            return phyrexian;
        var twobrid = TwobridSymbol.tryParseTwobridSymbol(s);
        if (twobrid.isPresent())
            return twobrid;
        var variable = VariableSymbol.tryParseVariableSymbol(s);
        if (variable.isPresent())
            return variable;
        var constant = StaticSymbol.tryParseStaticSymbol(s);
        if (constant.isPresent())
            return constant;
        return Optional.empty();
    }

    /**
     * Sort a list of ManaSymbols according to their ordering in the color wheel.
     * (Not yet implemented)
     *
     * @param symbols List of ManaSymbols to sort.
     */
    public static void sort(List<ManaSymbol> symbols)
    {}

    /**
     * How much this ManaSymbols is worth for calculating converted mana costs.
     */
    private final double value;

    /**
     * Create a new ManaSymbol.
     *
     * @param iconName name of the new ManaSymbol
     * @param text     String representation of the new ManaSymbol
     * @param v        value of the new ManaSymbol in a mana cost.
     */
    protected ManaSymbol(String iconName, String text, double v)
    {
        super(iconName, text);
        value = v;
    }

    /**
     * Get a map of each {@link ManaType} onto this ManaSymbol's weight for that type.
     * Each weight should always be between 0 and 1.
     *
     * @return this ManaSymbol's color weight map
     */
    public abstract Map<ManaType, Double> colorIntensity();

    @Override
    public int compareTo(ManaSymbol other)
    {
        if (ORDER.contains(getClass()) && ORDER.contains(other.getClass()))
            return ORDER.indexOf(getClass()) - ORDER.indexOf(other.getClass());
        else if (!ORDER.contains(getClass()))
            return 1;
        else if (!ORDER.contains(other.getClass()))
            return -1;
        else
            return 0;
    }

    /**
     * Get the value of this ManaSymbol in a mana cost.
     *
     * @return this ManaSymbol's value
     */
    public double value()
    {
        return value;
    }
}
