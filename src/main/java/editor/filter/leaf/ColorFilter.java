package editor.filter.leaf;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import editor.database.attributes.CardAttribute;
import editor.database.attributes.ManaType;
import editor.database.card.Card;
import editor.util.Containment;

/**
 * This class represents a filter to group cards by color characteristic.
 *
 * @author Alec Roelke
 */
public class ColorFilter extends FilterLeaf<List<ManaType>>
{
    /**
     * Set of colors that should match cards.
     */
    public Set<ManaType> colors;
    /**
     * Containment of this ColorFilter.
     */
    public Containment contain;
    /**
     * Whether or not cards should have multiple colors.
     */
    public boolean multicolored;

    /**
     * Create a ColorFilter without a type or function.  Should be used only for
     * deserialization.
     */
    public ColorFilter()
    {
        this(null, null);
    }

    /**
     * Create a new ColorFilter.
     *
     * @param t type of the new ColorFilter
     * @param f function for the new ColorFilter
     */
    public ColorFilter(CardAttribute t, Function<Card, List<ManaType>> f)
    {
        super(t, f);
        contain = Containment.CONTAINS_ANY_OF;
        colors = new HashSet<>();
        multicolored = false;
    }

    @Override
    public FilterLeaf<List<ManaType>> subCopy()
    {
        ColorFilter filter = (ColorFilter)CardAttribute.createFilter(type());
        filter.colors = new HashSet<>(colors);
        filter.contain = contain;
        filter.multicolored = multicolored;
        return filter;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null)
            return false;
        if (other == this)
            return true;
        if (other.getClass() != getClass())
            return false;
        ColorFilter o = (ColorFilter)other;
        return o.type().equals(type()) && o.colors.equals(colors) && o.contain == contain && o.multicolored == multicolored;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(type(), function(), colors, contain, multicolored);
    }

    /**
     * {@inheritDoc}
     * Filter cards according to the colors in a color characteristic.
     */
    @Override
    public boolean testFace(Card c)
    {
        return contain.test(function().apply(c), colors)
                && (!multicolored || function().apply(c).size() > 1);
    }

    @Override
    protected void serializeFields(JsonObject fields)
    {
        JsonArray array = new JsonArray();
        for (ManaType c : colors)
            array.add(c.toString());

        fields.addProperty("contains", contain.toString());
        fields.add("colors", array);
        fields.addProperty("multicolored", multicolored);
    }

    @Override
    protected void deserializeFields(JsonObject fields)
    {
        contain = Containment.parseContainment(fields.get("contains").getAsString());
        for (JsonElement element : fields.get("colors").getAsJsonArray())
            colors.add(ManaType.parseManaType(element.getAsString()));
        multicolored = fields.get("multicolored").getAsBoolean();
    }
}