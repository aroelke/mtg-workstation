package editor.filter.leaf.options.multi;

import java.util.Collection;
import java.util.function.Function;

import editor.database.card.Card;
import editor.filter.FilterType;
import editor.filter.leaf.options.OptionsFilter;

/**
 * This class represents a filter that groups cards by a characteristic that
 * can contain zero or more of a set of values.
 * 
 * @author Alec Roelk
 *
 * @param <T> Type of the characteristic being filtered
 */
public abstract class MultiOptionsFilter<T> extends OptionsFilter<T>
{
	/**
	 * Function representing the characteristic being filtered that hides
	 * the superclass's function.
	 */
	protected final Function<Card, Collection<T>> function;
	
	/**
	 * Create a new MultiOptionsFilter.
	 * 
	 * @param t Type of the new MultiOptionsFilter
	 * @param f Function for the new MultiOptionsFilter
	 */
	public MultiOptionsFilter(FilterType t, Function<Card, Collection<T>> f)
	{
		super(t, null);
		function = f;
	}
	
	/**
	 * @param c Card to test
	 * @return <code>true</code> if the values in the Card's characteristic
	 * match this MultiOptionsFilter's selected values with its containment.
	 */
	@Override
	public boolean test(Card c)
	{
		return contain.test(function.apply(c), selected);
	}
}
