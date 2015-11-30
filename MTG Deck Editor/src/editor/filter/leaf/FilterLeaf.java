package editor.filter.leaf;

import java.util.Arrays;
import java.util.function.Function;

import editor.database.Card;
import editor.filter.Filter;
import editor.filter.FilterType;
import editor.filter.leaf.options.multi.LegalityFilter;
import editor.filter.leaf.options.multi.MultiOptionsFilter;
import editor.filter.leaf.options.single.SingletonOptionsFilter;

/**
 * TODO: Comment this class
 * @author Alec Roelke
 *
 * @param <T>
 */
public abstract class FilterLeaf<T> extends Filter
{
	public static final FilterLeaf<Void> ALL_CARDS = new FilterLeaf<Void>(FilterType.ALL, null)
	{
		@Override
		public boolean test(Card c)
		{
			return true;
		}

		@Override
		public String content()
		{
			return "";
		}
		
		@Override
		public void parse(String s)
		{}
	};
	
	public static final FilterLeaf<Void> NO_CARDS = new FilterLeaf<Void>(FilterType.NONE, null)
	{
		@Override
		public boolean test(Card t)
		{
			return false;
		}

		@Override
		public String content()
		{
			return "";
		}
		
		@Override
		public void parse(String s)
		{}
	};
	
	public static FilterLeaf<?> createFilter(FilterType type)
	{
		switch (type)
		{
		case COLOR: case COLOR_IDENTITY:
			return ColorFilter.createFilter(type);
		case CARD_NUMBER: case CMC: case LOYALTY:
			return NumberFilter.createFilter(type);
		case ARTIST: case FLAVOR_TEXT: case NAME: case RULES_TEXT:
			return TextFilter.createFilter(type);
		case POWER: case TOUGHNESS:
			return VariableNumberFilter.createFilter(type);
		case BLOCK: case EXPANSION: case RARITY:
			return SingletonOptionsFilter.createFilter(type);
		case SUPERTYPE: case TYPE: case SUBTYPE:
			return MultiOptionsFilter.createFilter(type);
		case MANA_COST:
			return new ManaCostFilter();
		case FORMAT_LEGALITY:
			return new LegalityFilter();
		case TYPE_LINE:
			return new TypeLineFilter();
		case ALL:
			return FilterLeaf.ALL_CARDS;
		case NONE:
			return FilterLeaf.NO_CARDS;
		default:
			throw new IllegalArgumentException("Illegal filter type " + type.name());
		}
	}
	
	protected Function<Card, T> function;
	private FilterType type;
	
	public FilterLeaf(FilterType t, Function<Card, T> f)
	{
		super();
		type = t;
		function = f;
	}

	public abstract String content();
	
	public String checkContents(String s, FilterType... correct)
	{
		int delim = s.indexOf(':');
		if (!Arrays.asList(correct).contains(FilterType.fromCode(s.substring(1, delim))))
			throw new IllegalArgumentException("Illegal filter type found in string \"" + s + "\"");
		return s.substring(delim + 1, s.length() - 1);
	}
	
	@Override
	public String representation()
	{
		return type.code + ":" + content();
	}
}
