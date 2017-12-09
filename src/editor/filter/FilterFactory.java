package editor.filter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
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
 * This class is a factory for creating new filters.
 * 
 * @author Alec Roelke
 */
public abstract class FilterFactory
{
	/**
	 * Code for an "all cards" filter.
	 */
	public static final String ALL = "*";
	/**
	 * Code for an artist filter.
	 */
	public static final String ARTIST = "a";
	/**
	 * Code for a block filter.
	 */
	public static final String BLOCK = "b";
	/**
	 * Code for a collector's number filter.
	 */
	public static final String CARD_NUMBER = "#";
	/**
	 * Code for a converted mana cost filter.
	 */
	public static final String CMC = "cmc";
	/**
	 * Code for a color filter.
	 */
	public static final String COLOR = "c";
	/**
	 * Code for a color identity filter.
	 */
	public static final String COLOR_IDENTITY = "ci";
	/**
	 * Code (or lack thereof) for a defaults filter.  This should never be used, and only
	 * exists for completeness.
	 */
	public static final String DEFAULTS = "";
	/**
	 * Code for an expansion filter.
	 */
	public static final String EXPANSION = "x";
	/**
	 * Map of each filter type's code onto its name.
	 */
	public static final Map<String, String> FILTER_TYPES;
	/**
	 * Code for a flavor text filter.
	 */
	public static final String FLAVOR_TEXT = "f";
	/**
	 * Code for a format legality filter.
	 */
	public static final String FORMAT_LEGALITY = "legal";
	/**
	 * Code for a layout filter.
	 */
	public static final String LAYOUT = "L";
	/**
	 * Code for a loyalty filter.
	 */
	public static final String LOYALTY = "l";
	/**
	 * Code for a mana cost filter.
	 */
	public static final String MANA_COST = "m";
	/**
	 * Code for a name filter.
	 */
	public static final String NAME = "n";
	/**
	 * Code for a "no cards" filter.
	 */
	public static final String NONE = "0";
	/**
	 * Code for a power filter.
	 */
	public static final String POWER = "p";
	/**
	 * Code for a rarity filter.
	 */
	public static final String RARITY = "r";
	/**
	 * Code for a rules text filter.
	 */
	public static final String RULES_TEXT = "o";
	/**
	 * Code for a subtype filter.
	 */
	public static final String SUBTYPE = "sub";
	/**
	 * Code for a supertype filter.
	 */
	public static final String SUPERTYPE = "super";
	/**
	 * Code for a user tags filter.
	 */
	public static final String TAGS = "tag";
	/**
	 * Code for a toughness filter.
	 */
	public static final String TOUGHNESS = "t";
	/**
	 * Code for a type filter.
	 */
	public static final String TYPE = "cardtype";
	/**
	 * Code for a type line filter.
	 */
	public static final String TYPE_LINE = "type";
	static
	{
		Map<String, String> filterTypes = new LinkedHashMap<String, String>();
		filterTypes.put(NAME, "Name");
		filterTypes.put(LAYOUT, "Layout");
		filterTypes.put(MANA_COST, "Mana Cost");
		filterTypes.put(CMC, "CMC");
		filterTypes.put(COLOR, "Color");
		filterTypes.put(COLOR_IDENTITY, "Color Identity");
		filterTypes.put(TYPE_LINE, "Type Line");
		filterTypes.put(SUPERTYPE, "Supertype");
		filterTypes.put(TYPE, "Card Type");
		filterTypes.put(SUBTYPE, "Subtype");
		filterTypes.put(EXPANSION, "Expansion");
		filterTypes.put(BLOCK, "Block");
		filterTypes.put(RARITY, "Rarity");
		filterTypes.put(RULES_TEXT, "Rules Text");
		filterTypes.put(FLAVOR_TEXT, "Flavor Text");
		filterTypes.put(POWER, "Power");
		filterTypes.put(TOUGHNESS, "Toughness");
		filterTypes.put(LOYALTY, "Loyalty");
		filterTypes.put(ARTIST, "Artist");
		filterTypes.put(CARD_NUMBER, "Card Number");
		filterTypes.put(FORMAT_LEGALITY, "Format Legality");
		filterTypes.put(TAGS, "Tags");
		filterTypes.put(DEFAULTS, "Defaults");
		filterTypes.put(NONE, "<No Card>");
		filterTypes.put(ALL, "<Any Card>");
		FILTER_TYPES = Collections.unmodifiableMap(filterTypes);
	}
	/**
	 * Code for a group of filters.
	 */
	public static final String GROUP = "group";
	
	/**
	 * Create a new filter.
	 * 
	 * @param type type of filter to create
	 * @return a new filter of the given type with default values.  To edit those values,
	 * access them directly.
	 */
	public static FilterLeaf<?> createFilter(String type)
	{
		switch (type)
		{
		case NAME:
			return new TextFilter(type, Card::normalizedName);
		case LAYOUT:
			return new LayoutFilter();
		case MANA_COST:
			return new ManaCostFilter();
		case CMC:
			return new NumberFilter(type, Card::cmc);
		case COLOR:
			return new ColorFilter(type, Card::colors);
		case COLOR_IDENTITY:
			return new ColorFilter(type, Card::colorIdentity);
		case TYPE_LINE:
			return new TypeLineFilter();
		case SUPERTYPE:
			return new SupertypeFilter();
		case TYPE:
			return new CardTypeFilter();
		case SUBTYPE:
			return new SubtypeFilter();
		case EXPANSION:
			return new ExpansionFilter();
		case BLOCK:
			return new BlockFilter();
		case RARITY:
			return new RarityFilter();
		case RULES_TEXT:
			return new TextFilter(type, Card::normalizedOracle);
		case FLAVOR_TEXT:
			return new TextFilter(type, Card::normalizedFlavor);
		case POWER:
			return new VariableNumberFilter(type, (c) -> c.power().stream().map((p) -> p.value).collect(Collectors.toList()), Card::powerVariable);
		case TOUGHNESS:
			return new VariableNumberFilter(type, (c) -> c.toughness().stream().map((p) -> p.value).collect(Collectors.toList()), Card::toughnessVariable);
		case LOYALTY:
			return new VariableNumberFilter(type, (Card c) -> c.loyalty().stream().map((l) -> (double)l.value).collect(Collectors.toList()), Card::loyaltyVariable);
		case ARTIST:
			return new TextFilter(type, Card::artist);
		case CARD_NUMBER:
			return new NumberFilter(type, (c) -> c.number().stream().map((v) -> Double.valueOf(v.replace("--", "0").replaceAll("[\\D]", ""))).collect(Collectors.toList()));
		case FORMAT_LEGALITY:
			return new LegalityFilter();
		case TAGS:
			return new TagsFilter();
		case DEFAULTS:
			return null;
		case NONE:
			return new BinaryFilter(false);
		case ALL:
			return new BinaryFilter(true);
		default:
			return null;
		}
	}
	
	/**
	 * FilterFactory cannot be instantiated and should not be extended.
	 */
	private FilterFactory()
	{}
}
