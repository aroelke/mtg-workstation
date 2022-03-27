package editor.database.attributes;

import java.text.Collator;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import editor.collection.CardList;
import editor.collection.Inventory;
import editor.collection.deck.Category;
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
import editor.util.CollectionUtils;

import scala.jdk.javaapi.CollectionConverters;

/**
 * This enum represents an attribute of a Magic: The Gathering card such as name, power, toughness,
 * etc. It can create a {@link Filter} for values of the attribute and compare two different values
 * of the attribute. Generally an attribute that can be compared can also be displayed in a table.
 *
 * @author Alec Roelke
 */
public enum CardAttribute implements Supplier<FilterLeaf<?>>, Comparator<Object>
{
    /** Name of a card. */
    NAME("Name", String.class, (a) -> new TextFilter(a, (c) -> CollectionConverters.asJava(c.normalizedName())), Collator.getInstance()),
    /** A card's Oracle text. */
    RULES_TEXT("Rules Text", (a) -> new TextFilter(a, (c) -> CollectionConverters.asJava(c.normalizedOracle()))),
    /** A card's flavor text. */
    FLAVOR_TEXT("Flavor Text", (a) -> new TextFilter(a, (c) -> CollectionConverters.asJava(c.normalizedFlavor()))),
    /** The text physically printed on a card. */
    PRINTED_TEXT("Printed Text", (a) -> new TextFilter(a, (c) -> CollectionConverters.asJava(c.normalizedPrinted()))),
    /** Mana cost of a card. */
    MANA_COST("Mana Cost", List.class, (a) -> new ManaCostFilter(), Comparator.comparing((a) -> CollectionUtils.convertToList(a, ManaCost.class).get(0))),
    /** Mana value of a card. */
    MANA_VALUE("Mana Value", Double.class, (a) -> new NumberFilter(a, (c) -> Collections.singleton(c.manaValue())), (a, b) -> ((Double)a).compareTo((Double)b)),
    /** Smallest mana value of a card. */
    MIN_VALUE("Min Mana Value", Double.class, (a) -> new NumberFilter(a, (c) -> Collections.singleton(c.minManaValue())), (a, b) -> ((Double)a).compareTo((Double)b)),
    /** Largest mana value of a card. */
    MAX_VALUE("Max Mana Value", Double.class, (a) -> new NumberFilter(a, (c) -> Collections.singleton(c.maxManaValue())), (a, b) -> ((Double)a).compareTo((Double)b)),
    /** Colors of all faces of a card. */
    COLORS("Colors", List.class, (a) -> new ColorFilter(a, (c) -> CollectionConverters.asJava(c.colors())), (a, b) -> {
        var first = CollectionUtils.convertToList(a, ManaType.class);
        var second = CollectionUtils.convertToList(b, ManaType.class);
        int diff = first.size() - second.size();
        if (diff == 0)
            for (int i = 0; i < first.size(); i++)
                diff += first.get(i).compareTo(second.get(i)) * Math.pow(10, first.size() - i);
        return diff;
    }),
    /** Color identity of a card. */
    COLOR_IDENTITY("Color Identity", List.class, (a) -> new ColorFilter(a, (c) -> CollectionConverters.asJava(c.colorIdentity())), (a, b) -> {
        var first = CollectionUtils.convertToList(a, ManaType.class);
        var second = CollectionUtils.convertToList(b, ManaType.class);
        int diff = first.size() - second.size();
        if (diff == 0)
            for (int i = 0; i < first.size(); i++)
                diff += first.get(i).compareTo(second.get(i)) * Math.pow(10, first.size() - i);
        return diff;
    }),
    /** Type line of a card. */
    TYPE_LINE("Type Line", List.class, (a) -> new TypeLineFilter(), (a, b) -> {
        var first = CollectionUtils.convertToList(a, TypeLine.class);
        var second = CollectionUtils.convertToList(b, TypeLine.class);
        for (int i = 0; i < Math.max(first.size(), second.size()); i++)
        {
            if (i >= first.size())
                return -1;
            else if (i >= second.size())
                return 1;
            else
            {
                int result = first.get(i).compare(second.get(i));
                if (result != 0)
                    return result;
            }
        }
        return 0;
    }),
    /** The type line physically printed on a card. */
    PRINTED_TYPES("Printed Type Line", (a) -> new TextFilter(a, (c) -> CollectionConverters.asJava(c.faces()).stream().map(Card::printedTypes).collect(Collectors.toList()))),
    /** A card's types. */
    CARD_TYPE("Card Type", (a) -> new CardTypeFilter()),
    /** A card's subtypes. */
    SUBTYPE("Subtype", (a) -> new SubtypeFilter()),
    /** A card's supertypes. */
    SUPERTYPE("Supertype", (a) -> new SupertypeFilter()),
    /** Power of a creature card. */
    POWER(
        "Power",
        List.class,
        (a) -> new VariableNumberFilter(a, (c) -> CollectionConverters.asJava(c.faces()).stream().map((f) -> f.power().value()).collect(Collectors.toList()), Card::powerVariable),
        (a, b) -> ((CombatStat)a).compareTo((CombatStat)b)
    ),
    /** Toughness of a creature card. */
    TOUGHNESS(
        "Toughness",
        List.class,
        (a) -> new VariableNumberFilter(a, (c) -> CollectionConverters.asJava(c.faces()).stream().map((f) -> f.toughness().value()).collect(Collectors.toList()), Card::toughnessVariable),
        (a, b) -> ((CombatStat)a).compareTo((CombatStat)b)
    ),
    /** Loyalty of a planeswalker card. */
    LOYALTY(
        "Loyalty",
        List.class,
        (a) -> new VariableNumberFilter(a, (Card c) -> CollectionConverters.asJava(c.faces()).stream().map((f) -> f.loyalty().value()).collect(Collectors.toList()), Card::loyaltyVariable),
        (a, b) -> ((Loyalty)a).compareTo((Loyalty)b)
    ),
    /** {@link CardLayout} of a card. */
    LAYOUT("Layout", CardLayout.class, (a) -> new LayoutFilter(), (a, b) -> ((CardLayout)a).compareTo((CardLayout)b)),
    /** Name of the expansion a card was released in. */
    EXPANSION("Expansion", String.class, (a) -> new ExpansionFilter(), Collator.getInstance()),
    /** Name of the block containing the expansion the card was released in. */
    BLOCK("Block", String.class, (a) -> new BlockFilter(), Collator.getInstance()),
    /** Rarity of a card in its expansion. */
    RARITY("Rarity", Rarity.class, (a) -> new RarityFilter(), (a, b) -> ((Rarity)a).compareTo((Rarity)b)),
    /** Artist of a card. */
    ARTIST("Artist", String.class,(a) -> new TextFilter(a, (c) -> CollectionConverters.asJava(c.faces()).stream().map(Card::artist).collect(Collectors.toList())), Collator.getInstance()),
    /** Collector number of a card. */
    CARD_NUMBER("Card Number", List.class, (a) -> new NumberFilter(a, (c) -> CollectionConverters.asJava(c.faces()).stream().map((f) -> {
        String v = f.number();
        try
        {
            return Double.valueOf(v.replace("--", "0").replaceAll("[\\D]", ""));
        }
        catch (NumberFormatException e)
        {
            return 0.0;
        }
    }).collect(Collectors.toList())), Collator.getInstance()::compare),
    /** Set of formats a card is legal in. */
    LEGAL_IN("Format Legality", List.class, (a) -> new LegalityFilter(), (a, b) -> {
        var first = String.join(",", CollectionUtils.convertToList(a, String.class).stream().sorted().collect(Collectors.toList()));
        var second = String.join(",", CollectionUtils.convertToList(b, String.class).stream().sorted().collect(Collectors.toList()));
        return first.compareTo(second);
    }),
    /** Tags that have been applied to a card. */
    TAGS("Tags", Set.class, (a) -> new TagsFilter(), (a, b) -> {
        var first = String.join(",", CollectionUtils.convertToSet(a, String.class).stream().sorted().collect(Collectors.toList()));
        var second = String.join(",", CollectionUtils.convertToSet(b, String.class).stream().sorted().collect(Collectors.toList()));
        return Collator.getInstance().compare(first, second);
    }),

    /** Categories in a deck in which a card belongs.*/
    CATEGORIES("Categories", Set.class, (a, b) -> {
        var first = CollectionUtils.convertToSet(a, Category.class).stream().sorted(Comparator.comparing(Category::getName)).collect(Collectors.toList());
        var second = CollectionUtils.convertToSet(b, Category.class).stream().sorted(Comparator.comparing(Category::getName)).collect(Collectors.toList());
        for (int i = 0; i < Math.min(first.size(), second.size()); i++)
        {
            int diff = first.get(i).getName().compareTo(second.get(i).getName());
            if (diff != 0)
                return diff;
        }
        return Integer.compare(first.size(), second.size());
    }),
    /** Number of copies of a card in a deck. */
    COUNT("Count", Integer.class, (a, b) -> (Integer)b - (Integer)a),
    /** Date a card was added to a deck. */
    DATE_ADDED("Date Added", LocalDate.class, (a, b) -> ((LocalDate)a).compareTo((LocalDate)b)),

    /** No filter applied. */
    ANY("<Any Card>", (a) -> new BinaryFilter(true)),
    /** Filter all cards. */
    NONE("<No Card>", (a) -> new BinaryFilter(false)),
    /** Filter using one of the predefined filters.  This should not be used to create a filter. */
    DEFAULTS("Defaults", (a) -> null),
    /** Group of filters.  This should not be used to create a filter. */
    GROUP("Group", Optional.empty(), Optional.empty(), Optional.empty());

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
        // Mana value special case for old converted mana cost terminology
        // (since this is the only case of this so far, it's not worth creating
        // a new field for it)
        if (s.equalsIgnoreCase("cmc"))
            return MANA_VALUE;
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
     * Function for comparing the values of two attributes.
     */
    private final Optional<Comparator<Object>> comparing;

    /**
     * Create a CardCharacteristic with the specified name, column class, and filter generator.
     *
     * @param n name of the new CardAttribute
     * @param c class of the corresponding information on a card, if it can be displayed in a table
     * @param f function for generating a filter on the attribute, if it can be filtered
     * @param comp function for comparing attributes, if it can be compared
     */
    private CardAttribute(String n, Optional<Class<?>> c, Optional<Function<CardAttribute, FilterLeaf<?>>> f, Optional<Comparator<Object>> comp)
    {
        name = n;
        dataType = c;
        filter = f;
        comparing = comp;
    }

    /**
     * Create a CardCharacteristic with the specified name, column class, and filter generator.
     *
     * @param n name of the new CardAttribute
     * @param f function for generating a filter on the attribute
     * @param comp function for comparing attributes
     */
    CardAttribute(String n, Class<?> c, Function<CardAttribute, FilterLeaf<?>> f, Comparator<Object> comp)
    {
        this(n, Optional.of(c), Optional.of(f), Optional.of(comp));
    }

    /**
     * Create a CardCharacteristic with the specified name and column class that shouldn't be
     * used to create a filter.
     *
     * @param n name of the new CardAttribute
     * @param c class of the corresponding information on a card, if it can be displayed in a table
     * @param comp function for comparing attributes
     */
    CardAttribute(String n, Class<?> c, Comparator<Object> comp)
    {
        this(n, Optional.of(c), Optional.empty(), Optional.of(comp));
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
        this(n, Optional.empty(), Optional.of(f), Optional.empty());
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

    @Override
    public int compare(Object a, Object b)
    {
        return comparing.map((c) -> c.compare(a, b)).orElse(0);
    }

    /**
     * @return A comparator that compares two card list entries according to this attribute.
     */
    public Comparator<CardList.Entry> comparingCard()
    {
        return (a, b) -> comparing.map((c) -> c.compare(a.get(this), b.get(this))).orElse(0);
    }
}