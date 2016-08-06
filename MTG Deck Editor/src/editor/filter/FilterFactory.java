package editor.filter;

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
public interface FilterFactory
{
	/**
	 * Create a new filter.
	 * 
	 * @param type Type of filter to create
	 * @return A new filter of the given type with default values.  To edit those values,
	 * access them directly.
	 */
	public static FilterLeaf<?> createFilter(FilterType type)
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
}
