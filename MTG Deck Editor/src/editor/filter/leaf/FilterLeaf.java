package editor.filter.leaf;

import java.util.Arrays;
import java.util.function.Function;

import editor.database.Card;
import editor.filter.Filter;
import editor.filter.FilterType;

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
		
		@Override
		public String representation()
		{
			return FilterType.ALL.code + ":";
		}
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
		
		@Override
		public String representation()
		{
			return FilterType.NONE.code + ":";
		}
	};
	
	protected final Function<Card, T> function;
	public final FilterType type;
	
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
		String code = s.substring(1, delim);
		if (!Arrays.asList(correct).contains(FilterType.fromCode(code)))
			throw new IllegalArgumentException("Illegal filter type '" + code + "' found in string \"" + s + "\"");
		return s.substring(delim + 1, s.length() - 1);
	}
	
	@Override
	public String representation()
	{
		return type.code + ":" + content();
	}
}
