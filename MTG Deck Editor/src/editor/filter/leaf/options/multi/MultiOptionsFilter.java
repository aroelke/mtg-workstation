package editor.filter.leaf.options.multi;

import java.util.Collection;
import java.util.function.Function;

import editor.database.Card;
import editor.filter.FilterType;
import editor.filter.leaf.options.OptionsFilter;

/**
 * TODO: Comment this Class
 * @author Alec Roelk
 *
 * @param <T>
 */
public abstract class MultiOptionsFilter<T> extends OptionsFilter<T>
{
	protected final Function<Card, Collection<T>> function;
	
	public MultiOptionsFilter(FilterType t, Function<Card, Collection<T>> f)
	{
		super(t, null);
		function = f;
	}
	
	@Override
	public boolean test(Card c)
	{
		return contain.test(function.apply(c), selected);
	}

}
