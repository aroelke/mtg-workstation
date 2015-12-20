package editor.filter.leaf;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import editor.database.Card;
import editor.filter.FilterType;
import editor.util.Comparison;

/**
 * TODO: Comment this class
 * @author Alec Roelke
 */
public class VariableNumberFilter extends NumberFilter
{
	public static VariableNumberFilter createFilter(FilterType type)
	{
		switch (type)
		{
		case POWER:
			return new VariableNumberFilter(type, (c) -> c.power().stream().map((p) -> (double)p.value).collect(Collectors.toList()), Card::powerVariable);
		case TOUGHNESS:
			return new VariableNumberFilter(type, (c) -> c.toughness().stream().map((p) -> (double)p.value).collect(Collectors.toList()), Card::toughnessVariable);
		default:
			throw new IllegalArgumentException("Illegal variable number filter type " + type.name());
		}
	}
	
	public boolean varies;
	private Predicate<Card> variable;
	
	public VariableNumberFilter(FilterType t, Function<Card, Collection<Double>> f, Predicate<Card> v)
	{
		super(t, f);
		varies = false;
		variable = v;
	}
	
	@Override
	public boolean test(Card c)
	{
		return varies ? variable.test(c) : super.test(c);
	}

	@Override
	public String content()
	{
		return varies ? "*" : super.content();
	}

	@Override
	public void parse(String s)
	{
		String content = checkContents(s, FilterType.POWER, FilterType.TOUGHNESS);
		if (content.equals("*"))
			varies = true;
		else
		{
			varies = false;
			compare = Comparison.get(content.charAt(0));
			operand = Double.valueOf(content.substring(1));
		}
	}
}
