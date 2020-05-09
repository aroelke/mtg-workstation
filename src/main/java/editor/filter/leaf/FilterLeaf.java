package editor.filter.leaf;

import editor.database.card.Card;
import editor.filter.Filter;
import editor.filter.FilterAttribute;

import java.util.function.Function;

/**
 * This class represents a leaf in the filter tree, which filters a single characteristic of a Card.
 *
 * @param <T> Type of characteristic being filtered
 * @author Alec Roelke
 */
public abstract class FilterLeaf<T> extends Filter
{
    /**
     * Function representing the characteristic of the cards to be filtered.
     */
    private Function<Card, T> function;

    /**
     * Create a new FilterLeaf.
     *
     * @param t type of the new FilterLeaf
     * @param f function of the new FilterLeaf
     */
    public FilterLeaf(FilterAttribute t, Function<Card, T> f)
    {
        super(t);
        function = f;
    }

    /**
     * Get the attribute to be filtered.
     *
     * @return a {@link Function} representing the attribute of a card to be filtered.
     */
    protected Function<Card, T> function()
    {
        return function;
    }
}
