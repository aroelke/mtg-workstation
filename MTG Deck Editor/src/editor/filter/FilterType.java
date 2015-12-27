package editor.filter;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import editor.database.Card;
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
import editor.filter.leaf.options.single.BlockFilter;
import editor.filter.leaf.options.single.ExpansionFilter;
import editor.filter.leaf.options.single.RarityFilter;

/**
 * This enum represents a type of filter that can be used to filter Cards.
 * 
 * @author Alec Roelke
 */
public enum FilterType
{
	NAME("Name", "n"),
	MANA_COST("Mana Cost", "m"),
	CMC("CMC", "cmc"),
	COLOR("Color", "c"),
	COLOR_IDENTITY("Color Identity", "ci"),
	TYPE_LINE("Type Line", "type"),
	SUPERTYPE("Supertype", "super"),
	TYPE("Card Type", "cardtype"),
	SUBTYPE("Subtype", "sub"),
	EXPANSION("Expansion", "x"),
	BLOCK("Block", "b"),
	RARITY("Rarity", "r"),
	RULES_TEXT("Rules Text", "o"),
	FLAVOR_TEXT("Flavor Text", "f"),
	POWER("Power", "p"),
	TOUGHNESS("Toughness", "t"),
	LOYALTY("Loyalty", "l"),
	ARTIST("Artist", "a"),
	CARD_NUMBER("Card Number", "#"),
	FORMAT_LEGALITY("Format Legality", "legal"),
	DEFAULTS("Defaults", ""),
	NONE("<No Card>", "0"),
	ALL("<Any Card>", "*");
	
	static
	{
		NAME.supplier = () -> new TextFilter(NAME, Card::names);
		MANA_COST.supplier = () -> new ManaCostFilter();
		CMC.supplier = () -> new NumberFilter(CMC, Card::cmc);
		COLOR.supplier = () -> new ColorFilter(COLOR, Card::colors);
		COLOR_IDENTITY.supplier = () -> new ColorFilter(COLOR_IDENTITY, Card::colorIdentity);
		TYPE_LINE.supplier = () -> new TypeLineFilter();
		SUPERTYPE.supplier = () -> new SupertypeFilter();
		TYPE.supplier = () -> new CardTypeFilter();
		SUBTYPE.supplier = () -> new SubtypeFilter();
		EXPANSION.supplier = () -> new ExpansionFilter();
		BLOCK.supplier = () -> new BlockFilter();
		RARITY.supplier = () -> new RarityFilter();
		RULES_TEXT.supplier = () -> new TextFilter(RULES_TEXT, Card::normalizedText);
		FLAVOR_TEXT.supplier = () -> new TextFilter(FLAVOR_TEXT, Card::normalizedFlavor);
		POWER.supplier = () -> new VariableNumberFilter(POWER, (c) -> c.power().stream().map((p) -> (double)p.value).collect(Collectors.toList()), Card::powerVariable);
		TOUGHNESS.supplier = () -> new VariableNumberFilter(TOUGHNESS, (c) -> c.toughness().stream().map((p) -> (double)p.value).collect(Collectors.toList()), Card::toughnessVariable);
		LOYALTY.supplier = () -> new NumberFilter(LOYALTY, (c) -> c.loyalty().stream().map((l) -> (double)l.value).collect(Collectors.toList()));
		ARTIST.supplier = () -> new TextFilter(ARTIST, Card::artists);
		CARD_NUMBER.supplier = () -> new NumberFilter(CARD_NUMBER, (c) -> Arrays.stream(c.number()).map((v) -> Double.valueOf(v.replace("--", "0").replaceAll("[\\D]", ""))).collect(Collectors.toList()));
		FORMAT_LEGALITY.supplier = () -> new LegalityFilter();
		DEFAULTS.supplier = () -> null;
		NONE.supplier = () -> FilterLeaf.NO_CARDS;
		ALL.supplier = () -> FilterLeaf.ALL_CARDS;
	}
	
	/**
	 * Get a FilterType from a String.
	 * 
	 * @param c String to parse
	 * @return The FilterType corresponding to the String.
	 */
	public static FilterType fromCode(String c)
	{
		for (FilterType filterType: FilterType.values())
			if (c.equalsIgnoreCase(filterType.code))
				return filterType;
		throw new IllegalArgumentException("Illegal filter type string " + c);
	}
	
	/**
	 * Name of this FilterType.
	 */
	private final String name;
	/**
	 * Code for this FilterType to figure out which panel to set content for.
	 */
	public final String code;
	/**
	 * TODO: Comment this
	 */
	private Supplier<FilterLeaf<?>> supplier;
	
	/**
	 * Create a new FilterType.
	 * 
	 * @param n Name of the new FilterType
	 * @param c Code of the new FilterType
	 */
	private FilterType(String n, String c)
	{
		name = n;
		code = c;
	}
	
	/**
	 * Create a new Filter with the default values.
	 * 
	 * @return The new Filter.
	 */
	public FilterLeaf<?> createFilter()
	{
		if (this == DEFAULTS)
			return null;
		return supplier.get();
	}
	
	/**
	 * TODO: Comment this
	 * @param content
	 * @return
	 * @throws InstantiationException
	 */
	public FilterLeaf<?> createFilter(String content) throws InstantiationException
	{
		FilterLeaf<?> filter = createFilter();
		filter.parse(content);
		return filter;
	}
	
	/**
	 * @return A String representation of this FilterType, which is it's name.
	 */
	@Override
	public String toString()
	{
		return name;
	}
}
