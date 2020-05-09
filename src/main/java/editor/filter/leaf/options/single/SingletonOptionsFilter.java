package editor.filter.leaf.options.single;

import editor.database.card.Card;
import editor.filter.FilterAttribute;
import editor.filter.leaf.options.OptionsFilter;

import java.util.Collections;
import java.util.function.Function;

/**
 * This class is an {@link OptionsFilter} for which characteristics only have one
 * value from the list of options.
 *
 * @param <T> Type of the characteristic to be filtered
 * @author Alec Roelke
 */
public abstract class SingletonOptionsFilter<T> extends OptionsFilter<T>
{
    /**
     * Create a new SingletonOptionsFilter.
     *
     * @param t type of the new SingletonOptionsFilter
     * @param f function for the new SingletonOptionsFilter
     */
    public SingletonOptionsFilter(FilterAttribute t, Function<Card, T> f)
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