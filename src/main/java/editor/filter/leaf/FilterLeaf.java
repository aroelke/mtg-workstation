package editor.filter.leaf;

import java.util.function.Function;

import com.google.gson.JsonObject;

import editor.database.attributes.CardAttribute;
import editor.database.card.Card;
import editor.database.card.MultiCard;
import editor.database.card.SingleCard;
import editor.filter.FaceSearchOptions;
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
    /**
     * Which face(s) to search on a card when filtering.
     */
    public FaceSearchOptions faces;

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
        faces = FaceSearchOptions.ANY;
    }

    /**
     * Create a copy of this FilterLeaf to be used in {@link #copy()}, containing
     * only fields that are unique to it.  Common fields are copied by {@link #copy()}.
     * 
     * @return A copy of this FilterLeaf containing only unique fields.
     */
    protected abstract FilterLeaf<T> copyLeaf();

    @Override
    public final Filter copy()
    {
        var filter = copyLeaf();
        filter.faces = faces;
        return filter;
    }

    /**
     * Test an individual face of a card.  Called by {@link #copy()} on each face, and
     * then combined according to {@link #faces} to determine if the test passes.
     * 
     * @param c card face to test
     * @return <code>true</code> if the card face passes the filter, and <code>false</code>
     * otherwise.
     */
    protected abstract boolean testFace(Card c);

    @Override
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

    protected abstract boolean leafEquals(Object other);

    @Override
    public boolean equals(Object other)
    {
        return other != null && (other == this || other.getClass() == getClass() && faces == ((FilterLeaf<?>)other).faces && leafEquals(other));
    }

    /**
     * 
     * 
     * @param fields
     */
    protected abstract void serializeLeaf(JsonObject fields);

    @Override
    protected final void serializeFields(JsonObject fields)
    {
        serializeLeaf(fields);
        fields.addProperty("faces", faces.toString());
    }

    protected abstract void deserializeLeaf(JsonObject fields);

    @Override
    protected final void deserializeFields(JsonObject fields)
    {
        deserializeLeaf(fields);
        faces = fields.has("faces") ? FaceSearchOptions.valueOf(fields.get("faces").getAsString()) : FaceSearchOptions.ANY;
    }
}
