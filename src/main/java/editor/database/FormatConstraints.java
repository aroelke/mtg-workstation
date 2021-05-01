package editor.database;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class represents the constraints on building a deck for a particular format.
 * 
 * @param deckSize number of cards a deck should have
 * @param isExact whether the deck size is an exact count or an upper bound
 * @param maxCopies maximum number of copies of a single card allowed
 * @param sideboardSize maximum number of cards allowed in a sideboard
 * @param hasCommander whether or not a deck has a commander
 * 
 * @author Alec Roelke
 */
public record FormatConstraints(int deckSize, boolean isExact, int maxCopies, int sideboardSize, boolean hasCommander)
{
    /**
     * Mapping of format names onto their deckbulding constraints.
     */
    public static final Map<String, FormatConstraints> CONSTRAINTS = Map.ofEntries(
        new SimpleImmutableEntry<>("brawl", new FormatConstraints(60, true, 1, 0, true)),
        new SimpleImmutableEntry<>("commander", new FormatConstraints(100, true, 1, 0, true)),
        new SimpleImmutableEntry<>("duel", new FormatConstraints(100, true, 1, 0, true)),
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

    /**
     * List of supported format names, in alphabetical order.
     */
    public static final List<String> FORMAT_NAMES = CONSTRAINTS.keySet().stream().sorted().collect(Collectors.toList());

    /**
     * List of types of each of the deckbuilding constraints.
     */
    public static final List<Class<?>> CLASSES = List.of(
        String.class,
        Integer.class,
        Boolean.class,
        Integer.class,
        Integer.class,
        Boolean.class
    );

    /**
     * The name of each type of deckbuilding constraint: name of the format,
     * number of cards allowed in the deck, whether that number is a minimum
     * number or exact number, number of copies of any card allowed in a deck
     * (not counting restricted cards, basic lands, or other cards that ignore
     * this restriction), and whether or not the format has a commander.
     */
    public static final List<String> DATA_NAMES = List.of(
        "Name",
        "Deck Size",
        "Exact?",
        "Max Card Count",
        "Sideboard size",
        "Has Commander?"
    );

    /**
     * Create a default set of deckbuilding constraints, which is for a 60-card-
     * minimum-sized deck with 4 copies of any card and no commander.
     */
    public FormatConstraints()
    {
        this(60, false, 4, 15, false);
    }

    /**
     * @param name name of the format
     * @return An array containig the elements of this set of deckbuilding constraints
     * with the name of the format prepended, in the order specified by {@link #DATA_NAMES}.
     */
    public Object[] toArray(String name)
    {
        return new Object[] {name, deckSize, isExact, maxCopies, sideboardSize, hasCommander};
    }
}