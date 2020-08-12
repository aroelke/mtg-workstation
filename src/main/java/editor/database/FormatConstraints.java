package editor.database;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class FormatConstraints
{
    public static final Map<String, FormatConstraints> CONSTRAINTS = Map.ofEntries(
        new SimpleImmutableEntry<>("brawl", new FormatConstraints(60, true, 1, true)),
        new SimpleImmutableEntry<>("commander", new FormatConstraints(100, true, 1, true)),
        new SimpleImmutableEntry<>("duel", new FormatConstraints(100, true, 1, true)),
        new SimpleImmutableEntry<>("future", new FormatConstraints()),
        new SimpleImmutableEntry<>("historic", new FormatConstraints()),
        new SimpleImmutableEntry<>("legacy", new FormatConstraints()),
        new SimpleImmutableEntry<>("modern", new FormatConstraints()),
        new SimpleImmutableEntry<>("oldschool", new FormatConstraints()),
        new SimpleImmutableEntry<>("pauper", new FormatConstraints()),
        new SimpleImmutableEntry<>("penny", new FormatConstraints()),
        new SimpleImmutableEntry<>("pioneer", new FormatConstraints()),
        new SimpleImmutableEntry<>("standard", new FormatConstraints()),
        new SimpleImmutableEntry<>("vintage", new FormatConstraints())
    );

    public static final List<String> FORMAT_NAMES = CONSTRAINTS.keySet().stream().sorted().collect(Collectors.toList());

    public static final List<Class<?>> CLASSES = List.of(
        String.class,
        Integer.class,
        Boolean.class,
        Integer.class,
        Boolean.class
    );

    public static final List<String> DATA_NAMES = List.of(
        "Name",
        "Deck Size",
        "Exact?",
        "Max Card Count",
        "Has Commander?"
    );

    public final int deckSize;
    public final boolean isExact;
    public final int maxCopies;
    public final boolean hasCommander;
    
    private FormatConstraints(int size, boolean exact, int copies, boolean commander)
    {
        deckSize = size;
        isExact = exact;
        maxCopies = copies;
        hasCommander = commander;
    }

    public FormatConstraints()
    {
        this(60, false, 4, false);
    }

    public Object[] toArray(String name)
    {
        return new Object[] { name, deckSize, isExact, maxCopies, hasCommander };
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null)
            return false;
        if (other == this)
            return true;
        if (!(other instanceof FormatConstraints))
            return false;
        FormatConstraints o = (FormatConstraints)other;
        return o.deckSize == deckSize && o.isExact == isExact && o.maxCopies == maxCopies && o.hasCommander == hasCommander;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(deckSize, isExact, maxCopies, hasCommander);
    }
}