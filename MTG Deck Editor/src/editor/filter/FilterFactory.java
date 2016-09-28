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
	 * TODO: Comment these
	 */
	public static final String NAME = "n";
	public static final String LAYOUT = "L";
	public static final String MANA_COST = "m";
	public static final String CMC = "cmc";
	public static final String COLOR = "c";
	public static final String COLOR_IDENTITY = "ci";
	public static final String TYPE_LINE = "type";
	public static final String SUPERTYPE = "super";
	public static final String TYPE = "cardtype";
	public static final String SUBTYPE = "sub";
	public static final String EXPANSION = "x";
	public static final String BLOCK = "b";
	public static final String RARITY = "r";
	public static final String RULES_TEXT = "o";
	public static final String FLAVOR_TEXT = "f";
	public static final String POWER = "p";
	public static final String TOUGHNESS = "t";
	public static final String LOYALTY = "l";
	public static final String ARTIST = "a";
	public static final String CARD_NUMBER = "#";
	public static final String FORMAT_LEGALITY = "legal";
	public static final String TAGS = "tag";
	public static final String DEFAULTS = "";
	public static final String NONE = "0";
	public static final String ALL = "*";
	
	public static final Map<String, String> FILTER_TYPES;
	static
	{
		Map<String, String> filterTypes = new LinkedHashMap<String, String>();
		filterTypes[NAME] = "Name";
		filterTypes[LAYOUT] = "Layout";
		filterTypes[MANA_COST] = "Mana Cost";
		filterTypes[CMC] = "CMC";
		filterTypes[COLOR] = "Color";
		filterTypes[COLOR_IDENTITY] = "Color Identity";
		filterTypes[TYPE_LINE] = "Type Line";
		filterTypes[SUPERTYPE] = "Supertype";
		filterTypes[TYPE] = "Card Type";
		filterTypes[SUBTYPE] = "Subtype";
		filterTypes[EXPANSION] = "Expansion";
		filterTypes[BLOCK] = "Block";
		filterTypes[RARITY] = "Rarity";
		filterTypes[RULES_TEXT] = "Rules Text";
		filterTypes[FLAVOR_TEXT] = "Flavor Text";
		filterTypes[POWER] = "Power";
		filterTypes[TOUGHNESS] = "Toughness";
		filterTypes[LOYALTY] = "Loyalty";
		filterTypes[ARTIST] = "Artist";
		filterTypes[CARD_NUMBER] = "Card Number";
		filterTypes[FORMAT_LEGALITY] = "Format Legality";
		filterTypes[TAGS] = "Tags";
		filterTypes[DEFAULTS] = "Defaults";
		filterTypes[NONE] = "<No Card>";
		filterTypes[ALL] = "<Any Card>";
		FILTER_TYPES = Collections.unmodifiableMap(filterTypes);
	}
	
	/**
	 * Create a new filter.
	 * 
	 * @param type Type of filter to create
	 * @return A new filter of the given type with default values.  To edit those values,
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
			return new VariableNumberFilter(type, (c) -> c.power().stream().map((p) -> (double)p.value).collect(Collectors.toList()), Card::powerVariable);
		case TOUGHNESS:
			return new VariableNumberFilter(type, (c) -> c.toughness().stream().map((p) -> (double)p.value).collect(Collectors.toList()), Card::toughnessVariable);
		case LOYALTY:
			return new NumberFilter(type, (c) -> c.loyalty().stream().map((l) -> (double)l.value).collect(Collectors.toList()));
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
	
	private FilterFactory()
	{}
}
