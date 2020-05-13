package editor.filter.leaf.options.multi;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

import editor.database.attributes.CardAttribute;
import editor.database.card.Card;
import editor.filter.leaf.FilterLeaf;
import editor.filter.leaf.options.OptionsFilter;

/**
 * This class represents a filter that groups cards by a characteristic that
 * can contain zero or more of a set of values.
 *
 * @param <T> Type of the characteristic being filtered
 * @author Alec Roelke
 */
public abstract class MultiOptionsFilter<T> extends OptionsFilter<T>
{
    /**
     * Function representing the characteristic being filtered that hides
     * the superclass's function.  Replaces the function inherited from
     * {@link FilterLeaf}.
     */
    private Function<Card, Collection<T>> function;

    /**
     * Create a new MultiOptionsFilter.
     *
     * @param t type of the new MultiOptionsFilter
     * @param f function for the new MultiOptionsFilter
     */
    public MultiOptionsFilter(CardAttribute t, Function<Card, Collection<T>> f)
    {
        super(t, null);
        function = f;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(type(), function, contain, selected);
    }

    /**
     * Get the function representing this MultiOptionsFilter's attribute.  Don't use
     * {@link #function()}, which will return <code>null</code>.
     *
     * @return this MultiOptionsFilter's function
     */
    protected Function<Card, Collection<T>> multifunction()
    {
        return function;
    }

    /**
     * {@inheritDoc}
     * Filter cards by attributes that can take zero or or more of a certain value
     * according to this MultiOptionsFilter's selection and containment.
     */
    @Override
    public boolean test(Card c)
    {
        return contain.test(function.apply(c), selected);
    }
}
