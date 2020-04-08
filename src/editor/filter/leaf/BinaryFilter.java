package editor.filter.leaf;

import java.util.Objects;

import com.google.gson.JsonObject;

import editor.database.card.Card;
import editor.filter.Filter;
import editor.filter.FilterAttribute;

/**
 * This class represents a filter with only two options: All cards or no cards.
 *
 * @author Alec Roelke
 */
public class BinaryFilter extends FilterLeaf<Void>
{
    /**
     * Whether or not to let all Cards through the filter.
     */
    private boolean all;

    /**
     * Create a new BinaryFilter that lets all cards through.  Should only be used
     * for deserialization.
     */
    public BinaryFilter()
    {
        this(true);
    }

    /**
     * Create a new BinaryFilter.
     *
     * @param a whether or not to let all Cards through the filter.
     */
    public BinaryFilter(boolean a)
    {
        super(a ? FilterAttribute.ANY : FilterAttribute.NONE, null);
        all = a;
    }

    @Override
    public Filter copy()
    {
        return FilterAttribute.createFilter(type());
    }

    @Override
    public boolean equals(Object other)
    {
        return other != null && (other == this || other.getClass() == getClass() && ((BinaryFilter)other).all == all);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(type(), function(), all);
    }

    /**
     * {@inheritDoc}
     * Either let all cards through or none of them.
     */
    @Override
    public boolean test(Card c)
    {
        return all;
    }

    @Override
    protected void serializeFields(JsonObject fields)
    {
        fields.addProperty("all", all);
    }

    @Override
    protected void deserializeFields(JsonObject fields)
    {
        all = fields.get("all").getAsBoolean();
    }
}
