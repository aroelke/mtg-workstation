package editor.filter.leaf.options.single;

import java.util.Arrays;
import java.util.function.Function;

import editor.database.Card;
import editor.filter.FilterType;
import editor.filter.leaf.options.OptionsFilter;

/**
 * TODO: Comment this class
 * @author Alec Roelke
 *
 * @param <T>
 */
public abstract class SingletonOptionsFilter<T> extends OptionsFilter<T>
{
	public SingletonOptionsFilter(FilterType t, Function<Card, T> f)
	{
		super(t, f);
	}

	@Override
	public boolean test(Card c)
	{
		return contain.test(selected, Arrays.asList(function.apply(c)));
	}
}