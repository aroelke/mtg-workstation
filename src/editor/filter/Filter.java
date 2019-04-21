package editor.filter;

import java.io.Externalizable;
import java.util.function.Predicate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import editor.database.card.Card;


/**
 * This class represents a filter to group a collection of Cards by various characteristics.
 * Each filter has a String representation and can parse a String for its values.  The overall
 * filter is represented by a tree whose internal nodes are groups and whose leaf nodes filter
 * out characteristics.  A filter can test if a Card matches its characteristics.
 * <p>
 * Note that because filters are mutable, they do not make good keys or Set
 * members.
 *
 * @author Alec Roelke
 */
public abstract class Filter implements Predicate<Card>, Externalizable
{
    /**
     * Parent of this Filter in the tree (null if this is the root Filter).
     */
    protected FilterGroup parent;
    /**
     * Code specifying the attribute of a card to be filtered.
     */
    private final FilterAttribute type;

    /**
     * Create a new Filter with no parent.
     *
     * @param t type of filter being created
     */
    public Filter(FilterAttribute t)
    {
        parent = null;
        type = t;
    }

    /**
     * Create a copy of this Filter.
     *
     * @return A new Filter that is a copy of this Filter.
     */
    public abstract Filter copy();

    @Override
    public abstract boolean equals(Object other);

    @Override
    public abstract int hashCode();

    /**
     * Get the type of this Filter.
     *
     * @return the code specifying the attribute to be filtered.
     */
    public final FilterAttribute type()
    {
        return type;
    }

    /**
     * Add the fields of this Filter to the given {@link JsonObject}.
     * 
     * @param fields {@link JsonObject} to add fields to
     */
    protected abstract void serializeFields(JsonObject fields);

    /**
     * Convert this Filter to a JSON.
     * 
     * @return A {@link JsonElement} representing a serialized version of
     * this Filter.
     */
    public final JsonElement toJsonObject()
    {
        JsonObject fields = new JsonObject();
        fields.addProperty("type", type.toString());
        serializeFields(fields);
        return fields;
    }

    protected abstract void deserializeFields(JsonObject fields);

    public final void fromJsonObject(JsonObject object)
    {
        deserializeFields(object);
    }
}
