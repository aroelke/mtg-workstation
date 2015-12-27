package editor.filter.leaf;

import java.util.Collection;
import java.util.function.Function;

import editor.database.Card;
import editor.filter.FilterType;
import editor.util.Comparison;

/**
 * TODO: Comment this class
 * @author Alec Roelke
 */
public class NumberFilter extends FilterLeaf<Collection<Double>>
{
	public Comparison compare;
	public double operand;
	
	public NumberFilter(FilterType t, Function<Card, Collection<Double>> f)
	{
		super(t, f);
		compare = Comparison.EQ;
		operand = 0.0;
	}

	@Override
	public boolean test(Card c)
	{
		Collection<Double> values = function.apply(c);
		return !values.stream().allMatch((v) -> v.isNaN()) && values.stream().anyMatch((v) -> compare.test(v, operand));
	}

	@Override
	public String content()
	{
		return compare.toString() + operand;
	}
	
	@Override
	public void parse(String s)
	{
		String content = checkContents(s, FilterType.CARD_NUMBER, FilterType.CMC, FilterType.LOYALTY);
		compare = Comparison.get(content.charAt(0));
		operand = Double.valueOf(content.substring(1));
	}
}