package editor.filter.leaf;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

import editor.database.attributes.CardAttribute;
import editor.database.card.Card;
import editor.util.Containment;

import scala.jdk.javaapi.CollectionConverters;

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
        super(CardAttribute.TYPE_LINE, (c) -> CollectionConverters.asJava(c.faces()).stream().map((s) -> CollectionConverters.asJava(s.allTypes())).collect(Collectors.toList()));
        contain = Containment.CONTAINS_ANY_OF;
        line = "";
    }

    @Override
    protected FilterLeaf<List<Set<String>>> copyLeaf()
    {
        TypeLineFilter filter = (TypeLineFilter)CardAttribute.createFilter(CardAttribute.TYPE_LINE);
        filter.contain = contain;
        filter.line = line;
        return filter;
    }

    @Override
    public boolean leafEquals(Object other)
    {
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
    protected boolean testFace(Card c)
    {
        return !line.isEmpty() && contain.test(CollectionConverters.asJava(c.faces()).stream().flatMap((s) -> CollectionConverters.asJava(s.allTypes()).stream()).map(String::toLowerCase).collect(Collectors.toSet()), Arrays.asList(line.toLowerCase().split("\\s")));
    }

    @Override
    protected void serializeLeaf(JsonObject fields)
    {
        fields.addProperty("contains", contain.toString());
        fields.addProperty("pattern", line);
    }

    @Override
    protected void deserializeLeaf(JsonObject fields)
    {
        contain = Containment.parseContainment(fields.get("contains").getAsString());
        line = fields.get("pattern").getAsString();
    }
}
