package editor.database.characteristics;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
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
    /** {@link CardLayout} of a card. */
    LAYOUT("Layout", CardLayout.class, (a) -> new LayoutFilter()),
    /** Mana cost of a card. */
    MANA_COST("Mana Cost", List.class, (a) -> new ManaCostFilter()),
    /** Converted mana cost of a card. */
    CMC("CMC", List.class, (a) -> new NumberFilter(a, Card::cmc)),
    /** Colors of all faces of a card. */
    COLORS("Colors", List.class, (a) -> new ColorFilter(a, Card::colors)),
    /** Color identity of a card. */
    COLOR_IDENTITY("Color Identity", List.class, (a) -> new ColorFilter(a, Card::colorIdentity)),
    /** Type line of a card. */
    TYPE_LINE("Type", String.class, (a) -> new TypeLineFilter()),
    /** Name of the expansion a card was released in. */
    EXPANSION_NAME("Expansion", String.class, (a) -> new ExpansionFilter()),
    /** Name of the block containing the expansion the card was released in. */
    BLOCK("Block", String.class, (a) -> new BlockFilter()),
    /** Rarity of a card in its expansion. */
    RARITY("Rarity", Rarity.class, (a) -> new RarityFilter()),
    /** Power of a creature card. */
    POWER("Power", List.class, (a) -> new VariableNumberFilter(a, (c) -> c.power().stream().map((p) -> p.value).collect(Collectors.toList()), Card::powerVariable)),
    /** Toughness of a creature card. */
    TOUGHNESS("Toughness", List.class, (a) -> new VariableNumberFilter(a, (c) -> c.toughness().stream().map((p) -> p.value).collect(Collectors.toList()), Card::toughnessVariable)),
    /** Loyalty of a planeswalker card. */
    LOYALTY("Loyalty", List.class, (a) -> new VariableNumberFilter(a, (Card c) -> c.loyalty().stream().map((l) -> (double)l.value).collect(Collectors.toList()), Card::loyaltyVariable)),
    /** Artist of a card. */
    ARTIST("Artist", String.class,(a) -> new TextFilter(a, Card::artist)),
    /** Collector number of a card. */
    CARD_NUMBER("Card Number", List.class, (a) -> new NumberFilter(a, (c) -> c.number().stream().map((v) -> Double.valueOf(v.replace("--", "0").replaceAll("[\\D]", ""))).collect(Collectors.toList()))),
    /** Set of formats a card is legal in. */
    LEGAL_IN("Format Legality", List.class, (a) -> new LegalityFilter()),
    /** Tags that have been applied to a card. */
    TAGS("Tags", Set.class, (a) -> new TagsFilter()),

    /** Categories in a deck in which a card belongs.*/
    CATEGORIES("Categories", Set.class, null),
    /** Number of copies of a card in a deck. */
    COUNT("Count", Integer.class, null),
    /** Date a card was added to a deck. */
    DATE_ADDED("Date Added", LocalDate.class, null),

    /** A card's Oracle text. */
    RULES_TEXT("Rules Text", null, (a) -> new TextFilter(a, Card::normalizedOracle)),
    /** A card's flavor text. */
    FLAVOR_TEXT("Flavor Text", null, (a) -> new TextFilter(a, Card::normalizedFlavor)),
    /** The text physically printed on a card. */
    PRINTED_TEXT("Printed Text", null, (a) -> new TextFilter(a, Card::normalizedPrinted)),
    /** The type line physically printed on a card. */
    PRINTED_TYPES("Printed Type Line", null, (a) -> new TextFilter(a, Card::printedTypes)),
    /** A card's types. */
    CARD_TYPE("Card Type", null, (a) -> new CardTypeFilter()),
    /** A card's subtypes. */
    SUBTYPE("Subtype", null, (a) -> new SubtypeFilter()),
    /** A card's supertypes. */
    SUPERTYPE("Supertype", null, (a) -> new SupertypeFilter()),
    /** No filter applied. */
    ANY("<Any Card>", null, (a) -> new BinaryFilter(true)),
    /** Filter all cards. */
    NONE("<No Card>", null, (a) -> new BinaryFilter(false)),
    /** Filter using one of the predefined filters.  This should not be used to create a filter. */
    DEFAULTS("Defaults", null, (a) -> null),
    /** Group of filters.  This should not be used to create a filter. */
    GROUP("Group", null, (a) -> null);

    /**
     * Parse a String for a CardAttribute.
     *
     * @param s String to parse
     * @return the CardAttribute that corresponds to the given String.
     * @throws IllegalArgumentException if no such CardAttribute exists
     */
    public static CardAttribute parseCardData(String s) throws IllegalArgumentException
    {
        for (CardAttribute c : CardAttribute.values())
            if (c.toString().equalsIgnoreCase(s))
                return c;
        throw new IllegalArgumentException("Illegal characteristic string \"" + s + "\"");
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
     * Get the attributes that can be filtered.
     *
     * @return an array containing the attributes that can be filtered (all of the values except for
     * {@link #DEFAULTS} and {@link #GROUP}).
     */
    public static CardAttribute[] filterableValues()
    {
        return Arrays.stream(values()).filter((f) -> f.filter != null).toArray(CardAttribute[]::new);
    }

    /**
     * Get the types of CardAttribute that can be returned by an {@link Inventory}.
     *
     * @return An array containing the CardAttribute that can be shown in the inventory table.
     */
    public static CardAttribute[] inventoryValues()
    {
        return new CardAttribute[] {
            NAME,
            LAYOUT,
            MANA_COST,
            CMC,
            COLORS,
            COLOR_IDENTITY,
            TYPE_LINE,
            EXPANSION_NAME,
            RARITY,
            POWER,
            TOUGHNESS,
            LOYALTY,
            ARTIST,
            CARD_NUMBER,
            LEGAL_IN,
            DATE_ADDED
        };
    }

    /**
     * Class of the data that will appear in table columns containing data of this characteristic.
     */
    public final Class<?> dataType;
    /**
     * Name of the characteristic
     */
    private final String name;
    /**
     * Function for creating a new filter for the attribute.
     */
    private final Function<CardAttribute, FilterLeaf<?>> filter;

    /**
     * Create a CardCharacteristic with the specified name and column class.
     *
     * @param n name of the new CardAttribute
     * @param c class of the corresponding information on a card
     */
    CardAttribute(String n, Class<?> c, Function<CardAttribute, FilterLeaf<?>> f)
    {
        name = n;
        dataType = c;
        filter = f;
    }

    @Override
    public FilterLeaf<?> get()
    {
        return filter.apply(this);
    }

    @Override
    public String toString()
    {
        return name;
    }
}
