package editor.database.symbol;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import editor.database.attributes.ManaType;

class PhyrexianHybridSymbol extends ManaSymbol {
    public static final Map<ManaType, Map<ManaType, PhyrexianHybridSymbol>> SYMBOLS = Collections.unmodifiableMap(Arrays.stream(ManaType.colors()).collect(
        Collectors.toMap(
            Function.identity(),
            (m) -> Arrays.stream(ManaType.colors()).filter((n) -> n != m).collect(Collectors.toMap(Function.identity(), (n) -> new PhyrexianHybridSymbol(m, n)))
        )
    ));

    public static PhyrexianHybridSymbol parsePhyrexianHybridSymbol(String pair) throws IllegalArgumentException
    {
        return tryParsePhyrexianHybridSymbol(pair).orElseThrow(() -> new IllegalArgumentException('"' + pair + "\" is not a Phyrexian hybrid symbol"));
    }

    public static Optional<PhyrexianHybridSymbol> tryParsePhyrexianHybridSymbol(String pair)
    {
        var tokens = pair.split("/");
        if (tokens.length == 3 && tokens[2].equals("P"))
        {
            var c1 = ManaType.tryParseManaType(tokens[0]);
            var c2 = ManaType.tryParseManaType(tokens[1]);
            if (SYMBOLS.get(c1) != null)
                return Optional.ofNullable(SYMBOLS.get(c1).get(c2));
        }
        return Optional.empty();
    }

    private final ManaType color1;
    private final ManaType color2;

    private PhyrexianHybridSymbol(ManaType col1, ManaType col2)
    {
        super("phyrexian_" + (col1.colorOrder(col2) > 0 ? col2 : col1).toString().toLowerCase() + '_' + (col1.colorOrder(col2) > 0 ? col1 : col2).toString().toLowerCase() + "_mana.png",
                (col1.colorOrder(col2) > 0 ? col2 : col1).shorthand() + "/" + (col1.colorOrder(col2) > 0 ? col1 : col2).shorthand() + "/P",
                1);
        color1 = col1;
        color2 = col2;
    }

    @Override
    public Map<ManaType, Double> colorIntensity()
    {
        return createIntensity(new ColorIntensity(color1, 1.0/3.0), new ColorIntensity(color2, 1.0/3.0));
    }

    @Override
    public int compareTo(ManaSymbol o)
    {
        if (o instanceof PhyrexianHybridSymbol s)
            return color1.compareTo(s.color1)*10 + color2.compareTo(s.color2);
        else
            return super.compareTo(o);
    }
}
