package editor.filter.leaf.options;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import editor.database.attributes.CardAttribute;
import editor.database.card.Card;
import editor.filter.leaf.FilterLeaf;
import editor.util.Containment;

/**
 * This class represents a filter that groups cards based on characteristics
 * that take on values from a list of options.
 *
 * @param <T> Type of the options for the characteristic to be filtered
 * @author Alec Roelke
 */
public abstract class OptionsFilter<T> extends FilterLeaf<T>
{
    /**
     * Containment type of this OptionsFilter.
     */
    public Containment contain;
    /**
     * Set of options that have been selected.
     */
    public Set<T> selected;

    /**
     * Create a new OptionsFilter.
     *
     * @param t type of this OptionsFilter
     * @param f function for this OptionsFilter
     */
    public OptionsFilter(CardAttribute t, Function<Card, T> f)
    {
        super(t, f);
        contain = Containment.CONTAINS_ANY_OF;
        selected = new HashSet<>();
    }

    /**
     * Convert a String to the type that this OptionsFilter's options have.
     *
     * @param str String to convert
     * @return the option corresponding to the given String
     */
    protected abstract T convertFromString(String str);

    /**
     * Convert an option to JSON.
     * 
     * @param item item to convert
     * @return A serialized version of the item.
     */
    protected abstract JsonElement convertToJson(T item);

    @Override
    protected void serializeLeaf(JsonObject fields)
    {
        fields.addProperty("contains", contain.toString());
        fields.add("selected",
                   selected.stream().collect(Collector.of(
                       JsonArray::new, (a, i) -> a.add(convertToJson(i)),
                       (l, r) -> { l.addAll(r); return l; }
                   )));
    }

    /**
     * Convert JSON to an option
     * 
     * @param item {@link JsonElement} to convert
     * @return The option corresponding to the {@link JsonElement}.
     */
    protected abstract T convertFromJson(JsonElement item);

    @Override
    protected void deserializeLeaf(JsonObject fields)
    {
        contain = Containment.parseContainment(fields.get("contains").getAsString());
        for (JsonElement element : fields.get("selected").getAsJsonArray())
            selected.add(convertFromJson(element));
    }

    @Override
    public boolean leafEquals(Object other)
    {
        if (other.getClass() != getClass())
            return false;
        var o = (OptionsFilter<?>)other;
        return o.type().equals(type()) && o.contain == contain && o.selected.equals(selected);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(type(), function(), contain, selected);
    }
}
