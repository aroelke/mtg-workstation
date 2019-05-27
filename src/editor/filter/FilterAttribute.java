package editor.filter;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

import editor.database.card.Card;
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
 * This enum represents an attribute by which a card can be filtered.
 *
 * @author Alec Roelke
 */
public enum FilterAttribute
{
    /** Filter by artist name. */
    ARTIST("Artist", (a) -> new TextFilter(a, Card::artist)),
    /** Filter by block. */
    BLOCK("Block", (a) -> new BlockFilter()),
    /** Filter by card number. */
    CARD_NUMBER("Card Number", (a) -> new NumberFilter(a, (c) -> c.number().stream().map((v) -> Double.valueOf(v.replace("--", "0").replaceAll("[\\D]", ""))).collect(Collectors.toList()))),
    /** Filter by card type. */
    CARD_TYPE("Card Type", (a) -> new CardTypeFilter()),
    /** Filter by converted mana cost. */
    CMC("CMC", (a) -> new NumberFilter(a, Card::cmc)),
    /** Filter by color. */
    COLOR("Color", (a) -> new ColorFilter(a, Card::colors)),
    /** Filter by color identity. */
    COLOR_IDENTITY("Color Identity", (a) -> new ColorFilter(a, Card::colorIdentity)),
    /** Filter by expansion. */
    EXPANSION("Expansion", (a) -> new ExpansionFilter()),
    /** Filter by flavor text. */
    FLAVOR_TEXT("Flavor Text", (a) -> new TextFilter(a, Card::normalizedFlavor)),
    /** Filter by format legality. */
    FORMAT_LEGALITY("Format Legality", (a) -> new LegalityFilter()),
    /** Filter by layout. */
    LAYOUT("Layout", (a) -> new LayoutFilter()),
    /** Filter by loyalty. */
    LOYALTY("Loyalty", (a) -> new VariableNumberFilter(a, (Card c) -> c.loyalty().stream().map((l) -> (double)l.value).collect(Collectors.toList()), Card::loyaltyVariable)),
    /** Filter by mana cost. */
    MANA_COST("Mana Cost", (a) -> new ManaCostFilter()),
    /** Filter by name. */
    NAME("Name", (a) -> new TextFilter(a, Card::normalizedName)),
    /** Filter by power. */
    POWER("Power", (a) -> new VariableNumberFilter(a, (c) -> c.power().stream().map((p) -> p.value).collect(Collectors.toList()), Card::powerVariable)),
    /** Filter by printed text. */
    PRINTED_TEXT("Printed Text", (a) -> new TextFilter(a, Card::normalizedPrinted)),
    /** Filter by printed type line. */
    PRINTED_TYPES("Printed Type Line", (a) -> new TextFilter(a, Card::printedTypes)),
    /** Filter by rarity. */
    RARITY("Rarity", (a) -> new RarityFilter()),
    /** Filter by rules (Oracle) text. */
    RULES_TEXT("Rules Text", (a) -> new TextFilter(a, Card::normalizedOracle)),
    /** Filter by subtype. */
    SUBTYPE("Subtype", (a) -> new SubtypeFilter()),
    /** Filter by supertype. */
    SUPERTYPE("Supertype", (a) -> new SupertypeFilter()),
    /** Filter by user-defined tags. */
    TAGS("Tags", (a) -> new TagsFilter()),
    /** Filter by toughness. */
    TOUGHNESS("Toughness", (a) -> new VariableNumberFilter(a, (c) -> c.toughness().stream().map((p) -> p.value).collect(Collectors.toList()), Card::toughnessVariable)),
    /** Filter by type line. */
    TYPE_LINE("Type Line", (a) -> new TypeLineFilter()),

    /** No filter applied. */
    ANY("<Any Card>", (a) -> new BinaryFilter(true)),
    /** Filter all cards. */
    NONE("<No Card>", (a) -> new BinaryFilter(false)),
    /** Filter using one of the predefined filters.  This should not be used to create a filter. */
    DEFAULTS("Defaults", null),
    /** Group of filters.  This should not be used to create a filter. */
    GROUP("Group", null);

    /**
     * Create a new filter that filters the specified attribute.
     *
     * @param attribute attribute to filter by
     * @return a {@link Filter} that filters by the chosen attribute
     */
    public static Filter createFilter(FilterAttribute attribute)
    {
        switch (attribute)
        {
        case GROUP:
            return new FilterGroup();
        default:
            return attribute.create();
        }
    }

    /**
     * Get the attributes that can be filtered.
     *
     * @return an array containing the attributes that can be filtered (all of the values except for
     * {@link #DEFAULTS} and {@link #GROUP}).
     */
    public static FilterAttribute[] filterableValues()
    {
        return Arrays.stream(values()).filter((f) -> f.filter != null).toArray(FilterAttribute[]::new);
    }

    public static FilterAttribute fromString(String attribute)
    {
        return Arrays.stream(values()).filter((a) -> a.toString().equalsIgnoreCase(attribute)).findAny().get();
    }

    /**
     * Label to display when selecting a filter attribute.
     */
    private final String label;
    /**
     * Function for creating a new filter.
     */
    private final Function<FilterAttribute, FilterLeaf<?>> filter;

    /**
     * Create a new FilterAttribute to filter cards by particular attributes.
     *
     * @param l label to display for this FilterAttribute
     * @param f function for creating a filter
     */
    FilterAttribute(String l, Function<FilterAttribute, FilterLeaf<?>> f)
    {
        label = l;
        filter = f;
    }

    /**
     * @return a new {@link FilterLeaf} that filters this attribute.
     */
    public FilterLeaf<?> create()
    {
        return filter.apply(this);
    }

    @Override
    public String toString()
    {
        return label;
    }
}
