package editor.filter.leaf;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

import editor.database.attributes.CardAttribute;
import editor.database.card.Card;
import editor.filter.Filter;
import editor.util.Containment;

/**
 * This class represents a filter that filters a card by its entire type line.
 *
 * @author Alec Roelke
 */
public class TypeLineFilter extends FilterLeaf<List<Set<String>>>
{
    /**
     * Containment specification for the terms in the filter's text.
     */
    public Containment contain;
    /**
     * Text containing values to search for in a card's type line.
     */
    public String line;

    /**
     * Create a new TypeLineFilter.
     */
    public TypeLineFilter()
    {
        super(CardAttribute.TYPE_LINE, Card::allTypes);
        contain = Containment.CONTAINS_ANY_OF;
        line = "";
    }

    @Override
    public Filter copy()
    {
        TypeLineFilter filter = (TypeLineFilter)CardAttribute.createFilter(CardAttribute.TYPE_LINE);
        filter.contain = contain;
        filter.line = line;
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
        TypeLineFilter o = (TypeLineFilter)other;
        return contain == o.contain && line.equals(o.line);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(type(), function(), contain, line);
    }

    /**
     * {@inheritDoc}
     * Filter cards whose type lines match the specified values.
     */
    @Override
    public boolean test(Card c)
    {
        return !line.isEmpty() && contain.test(c.allTypes().stream().flatMap(Set::stream).map(String::toLowerCase).collect(Collectors.toSet()), Arrays.asList(line.toLowerCase().split("\\s")));
    }

    @Override
    protected void serializeFields(JsonObject fields)
    {
        fields.addProperty("contains", contain.toString());
        fields.addProperty("pattern", line);
    }

    @Override
    protected void deserializeFields(JsonObject fields)
    {
        contain = Containment.parseContainment(fields.get("contains").getAsString());
        line = fields.get("pattern").getAsString();
    }
}
