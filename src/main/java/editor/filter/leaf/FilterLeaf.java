package editor.filter.leaf;

import java.util.function.Function;

import editor.database.attributes.CardAttribute;
import editor.database.card.Card;
import editor.database.card.MultiCard;
import editor.database.card.SingleCard;
import editor.filter.FacesFilter;
import editor.filter.Filter;

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
    public FacesFilter faces;

    /**
     * Create a new FilterLeaf.
     *
     * @param t type of the new FilterLeaf
     * @param f function of the new FilterLeaf
     */
    public FilterLeaf(CardAttribute t, Function<Card, T> f)
    {
        super(t);
        function = f;
        faces = FacesFilter.ANY;
    }

    public abstract FilterLeaf<T> subCopy();

    public final Filter copy()
    {
        var filter = subCopy();
        filter.faces = faces;
        return filter;
    }

    public abstract boolean testFace(Card c);

    public final boolean test(Card c)
    {
        if (c instanceof SingleCard s)
            return testFace(c);
        else if (c instanceof MultiCard m)
            return switch (faces) {
                case ANY   -> m.faces().stream().anyMatch(this::testFace);
                case ALL   -> m.faces().stream().allMatch(this::testFace);
                case FRONT -> testFace(m.faces().get(0));
                case BACK  -> testFace(m.faces().get(m.faces().size() - 1));
            };
        else
            return false;
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
