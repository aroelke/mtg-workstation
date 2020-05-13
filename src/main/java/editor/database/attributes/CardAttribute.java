package editor.database.attributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import editor.collection.Inventory;
import editor.database.card.Card;
import editor.database.card.CardLayout;
import editor.filter.Filter;
import editor.filter.leaf.BinaryFilter;
import editor.filter.leaf.ColorFilter;
import editor.filter.leaf.FilterLeaf;
import editor.filter.leaf.ManaCostFilter;
import editor.filter.leaf.NumberFilter;
import editor.filter.leaf.TextFilter;
import editor.filter.leaf.TypeLineFilter;
import editor.filter.leaf.VariableNumberFilter;
import editor.filter.leaf.options.multi.CardTypeFilter;
import editor.filter.leaf.options.multi.LegalityFilter;
import editor.filter.leaf.options.multi.SubtypeFilter;
import editor.filter.leaf.options.multi.SupertypeFilter;
import editor.filter.leaf.options.multi.TagsFilter;
import editor.filter.leaf.options.single.BlockFilter;
import editor.filter.leaf.options.single.ExpansionFilter;
import editor.filter.leaf.options.single.LayoutFilter;
import editor.filter.leaf.options.single.RarityFilter;

/**
 * This enum represents a characteristic of a Magic: The Gathering card such as name, power, toughness,
 * etc.
 *
 * @author Alec Roelke
 */
public enum CardAttribute implements Supplier<FilterLeaf<?>>
{
    /** Name of a card. */
    NAME("Name", String.class, (a) -> new TextFilter(a, Card::normalizedName)),
    /** A card's Oracle text. */
    RULES_TEXT("Rules Text", (a) -> new TextFilter(a, Card::normalizedOracle)),
    /** A card's flavor text. */
    FLAVOR_TEXT("Flavor Text", (a) -> new TextFilter(a, Card::normalizedFlavor)),
    /** The text physically printed on a card. */
    PRINTED_TEXT("Printed Text", (a) -> new TextFilter(a, Card::normalizedPrinted)),
    /** Mana cost of a card. */
    MANA_COST("Mana Cost", List.class, (a) -> new ManaCostFilter()),
    /** Converted mana cost of a card. */
    CMC("CMC", List.class, (a) -> new NumberFilter(a, Card::cmc)),
    /** Colors of all faces of a card. */
    COLORS("Colors", List.class, (a) -> new ColorFilter(a, Card::colors)),
    /** Color identity of a card. */
    COLOR_IDENTITY("Color Identity", List.class, (a) -> new ColorFilter(a, Card::colorIdentity)),
    /** Type line of a card. */
    TYPE_LINE("Type Line", String.class, (a) -> new TypeLineFilter()),
    /** The type line physically printed on a card. */
    PRINTED_TYPES("Printed Type Line", (a) -> new TextFilter(a, Card::printedTypes)),
    /** A card's types. */
    CARD_TYPE("Card Type", (a) -> new CardTypeFilter()),
    /** A card's subtypes. */
    SUBTYPE("Subtype", (a) -> new SubtypeFilter()),
    /** A card's supertypes. */
    SUPERTYPE("Supertype", (a) -> new SupertypeFilter()),
    /** Power of a creature card. */
    POWER("Power", List.class, (a) -> new VariableNumberFilter(a, (c) -> c.power().stream().map((p) -> p.value).collect(Collectors.toList()), Card::powerVariable)),
    /** Toughness of a creature card. */
    TOUGHNESS("Toughness", List.class, (a) -> new VariableNumberFilter(a, (c) -> c.toughness().stream().map((p) -> p.value).collect(Collectors.toList()), Card::toughnessVariable)),
    /** Loyalty of a planeswalker card. */
    LOYALTY("Loyalty", List.class, (a) -> new VariableNumberFilter(a, (Card c) -> c.loyalty().stream().map((l) -> (double)l.value).collect(Collectors.toList()), Card::loyaltyVariable)),
    /** {@link CardLayout} of a card. */
    LAYOUT("Layout", CardLayout.class, (a) -> new LayoutFilter()),
    /** Name of the expansion a card was released in. */
    EXPANSION("Expansion", String.class, (a) -> new ExpansionFilter()),
    /** Name of the block containing the expansion the card was released in. */
    BLOCK("Block", String.class, (a) -> new BlockFilter()),
    /** Rarity of a card in its expansion. */
    RARITY("Rarity", Rarity.class, (a) -> new RarityFilter()),
    /** Artist of a card. */
    ARTIST("Artist", String.class,(a) -> new TextFilter(a, Card::artist)),
    /** Collector number of a card. */
    CARD_NUMBER("Card Number", List.class, (a) -> new NumberFilter(a, (c) -> c.number().stream().map((v) -> Double.valueOf(v.replace("--", "0").replaceAll("[\\D]", ""))).collect(Collectors.toList()))),
    /** Set of formats a card is legal in. */
    LEGAL_IN("Format Legality", List.class, (a) -> new LegalityFilter()),
    /** Tags that have been applied to a card. */
    TAGS("Tags", Set.class, (a) -> new TagsFilter()),

    /** Categories in a deck in which a card belongs.*/
    CATEGORIES("Categories", Set.class),
    /** Number of copies of a card in a deck. */
    COUNT("Count", Integer.class),
    /** Date a card was added to a deck. */
    DATE_ADDED("Date Added", LocalDate.class),

    /** No filter applied. */
    ANY("<Any Card>", (a) -> new BinaryFilter(true)),
    /** Filter all cards. */
    NONE("<No Card>", (a) -> new BinaryFilter(false)),
    /** Filter using one of the predefined filters.  This should not be used to create a filter. */
    DEFAULTS("Defaults", (a) -> null),
    /** Group of filters.  This should not be used to create a filter. */
    GROUP("Group", Optional.empty(), Optional.empty());

    /**
     * Parse a String for a CardAttribute.
     *
     * @param s String to parse
     * @return the CardAttribute that corresponds to the given String.
     * @throws IllegalArgumentException if no such CardAttribute exists
     */
    public static CardAttribute fromString(String s) throws IllegalArgumentException
    {
        for (CardAttribute c : CardAttribute.values())
            if (c.toString().equalsIgnoreCase(s))
                return c;
        throw new IllegalArgumentException("Unknown attribute \"" + s + "\"");
    }

    /**
     * Create a new filter that filters the specified attribute.
     *
     * @param attribute attribute to filter by
     * @return a {@link Filter} that filters by the chosen attribute
     */
    public static Filter createFilter(CardAttribute attribute)
    {
        return attribute.get();
    }

    /**
     * @return an array containing the attributes that can be filtered.
     */
    public static CardAttribute[] filterableValues()
    {
        return Arrays.stream(values()).filter((f) -> f.filter.isPresent()).toArray(CardAttribute[]::new);
    }

    /**
     * @return an array containing the attributes that can be displayed in a table.
     */
    public static CardAttribute[] displayableValues()
    {
        return Arrays.stream(values()).filter((c) -> c.dataType.isPresent()).toArray(CardAttribute[]::new);
    }

    /**
     * Get the types of CardAttribute that can be returned by an {@link Inventory}.
     *
     * @return An array containing the CardAttribute that can be shown in the inventory table.
     */
    public static CardAttribute[] inventoryValues()
    {
        var displayable = new ArrayList<>(Arrays.asList(displayableValues()));
        displayable.removeAll(List.of(CATEGORIES, COUNT));
        return displayable.toArray(new CardAttribute[displayable.size()]);
    }

    /**
     * Name of the characteristic
     */
    private final String name;
    /**
     * Class of the data that will appear in table columns containing data of this characteristic
     * if it can be displayed.
     */
    private final Optional<Class<?>> dataType;
    /**
     * Function for creating a new filter for the attribute if it can be filtered.
     */
    private final Optional<Function<CardAttribute, FilterLeaf<?>>> filter;

    /**
     * Create a CardCharacteristic with the specified name, column class, and filter generator.
     *
     * @param n name of the new CardAttribute
     * @param c class of the corresponding information on a card, if it can be displayed in a table
     * @param f function for generating a filter on the attribute, if it can be filtered
     */
    private CardAttribute(String n, Optional<Class<?>> c, Optional<Function<CardAttribute, FilterLeaf<?>>> f)
    {
        name = n;
        dataType = c;
        filter = f;
    }

    /**
     * Create a CardCharacteristic with the specified name, column class, and filter generator.
     *
     * @param n name of the new CardAttribute
     * @param f function for generating a filter on the attribute
     */
    CardAttribute(String n, Class<?> c, Function<CardAttribute, FilterLeaf<?>> f)
    {
        this(n, Optional.of(c), Optional.of(f));
    }

    /**
     * Create a CardCharacteristic with the specified name and column class that shouldn't be
     * used to create a filter.
     *
     * @param n name of the new CardAttribute
     * @param c class of the corresponding information on a card, if it can be displayed in a table
     */
    CardAttribute(String n, Class<?> c)
    {
        this(n, Optional.of(c), Optional.empty());
    }

    /**
     * Create a CardCharacteristic with the specified name and filter generator that shouldn't be
     * displayed in a table
     *
     * @param n name of the new CardAttribute
     * @param f function for generating a filter on the attribute
     */
    CardAttribute(String n, Function<CardAttribute, FilterLeaf<?>> f)
    {
        this(n, Optional.empty(), Optional.of(f));
    }

    @Override
    public String toString()
    {
        return name;
    }

    /**
     * @return the data type used for displaying values of this attribute in a table column.
     * @throws NoSuchElementException if this attribute shouldn't be displayed in a table
     */
    public Class<?> dataType() throws NoSuchElementException
    {
        return dataType.orElseThrow();
    }

    /**
     * @return a new filter that filters by this attribute.
     * @throws NoSuchElementException if this attribute shouldn't be used for filtering
     */
    @Override
    public FilterLeaf<?> get() throws NoSuchElementException
    {
        return filter.orElseThrow().apply(this);
    }
}
