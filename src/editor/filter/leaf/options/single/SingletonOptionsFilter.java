package editor.filter.leaf.options.single;

import java.util.Collections;
import java.util.function.Function;

import editor.database.card.Card;
import editor.filter.leaf.options.OptionsFilter;

/**
 * This class is an {@link OptionsFilter} for which characteristics only have one
 * value from the list of options.
 *
 * @author Alec Roelke
 *
 * @param <T> Type of the characteristic to be filtered
 */
public abstract class SingletonOptionsFilter<T> extends OptionsFilter<T>
{
	/**
	 * Create a new SingletonOptionsFilter.
	 * 
	 * @param t type of the new SingletonOptionsFilter
	 * @param f function for the new SingletonOptionsFilter
	 */
	public SingletonOptionsFilter(String t, Function<Card, T> f)
	{
		super(t, f);
	}

	/**
	 * {@inheritDoc}
	 * Filter cards according to an attribute that takes exactly one value
	 * according to this SingletonOptionsFilter's selection and containment.
	 */
	@Override
	public boolean test(Card c)
	{
		return contain.test(selected, Collections.singletonList(function().apply(c)));
	}
}