package editor.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import editor.database.attributes.CardAttribute;
import editor.database.card.Card;

/**
 * This class represents a group of filters that are ANDed or ORed together.
 *
 * @author Alec Roelke
 */
public class FilterGroup extends Filter implements Iterable<Filter>
{
    /**
     * This class represents a method of combining filters to test a card with all of
     * them collectively.
     *
     * @author Alec Roelke
     */
    public enum Mode implements BiPredicate<Collection<Filter>, Card>
    {
        /**
         * All of the filters must pass a card.
         */
        AND("all of", Stream::allMatch),
        /**
         * None of the filters can pass a card.
         */
        NOR("none of", Stream::noneMatch),
        /**
         * Any of the filters must pass a card.
         */
        OR("any of", Stream::anyMatch);

        /**
         * Function representing the mode to test a card with a collection of filters.
         */
        private final BiPredicate<Stream<Filter>, Predicate<? super Filter>> function;
        /**
         * String representation of this Mode.
         */
        private final String mode;

        /**
         * Create a new Mode.
         *
         * @param m String representation of the new Mode.
         */
        Mode(String m, BiPredicate<Stream<Filter>, Predicate<? super Filter>> f)
        {
            mode = m;
            function = f;
        }

        @Override
        public boolean test(Collection<Filter> filters, Card c)
        {
            return function.test(filters.stream(), (f) -> f.test(c));
        }

        @Override
        public String toString()
        {
            return mode;
        }
    }

    /**
     * Children of this FilterGroup.
     */
    private List<Filter> children;

    /** Combination mode of this FilterGroup. */
    public Mode mode;
    /** User-defined string associated with the group. */
    public String comment;

    /**
     * Create a new FilterGroup with no children and in AND mode.
     */
    public FilterGroup()
    {
        super(CardAttribute.GROUP);
        children = new ArrayList<>();
        mode = Mode.AND;
        comment = "";
    }

    /**
     * Create a new FilterGroup using the given list of filters
     * as its children.
     *
     * @param c filters that will be the new FilterGroup's children
     */
    public FilterGroup(Filter... c)
    {
        this();
        for (Filter f : c)
            addChild(f);
    }

    /**
     * Add a new child to this FilterGroup.
     *
     * @param filter filter to add
     */
    public void addChild(Filter filter)
    {
        children.add(filter);
        if (filter.parent != null)
            filter.parent.children.remove(filter);
        filter.parent = this;
    }

    /**
     * {@inheritDoc}
     * The copy will be a deep copy in that its new children will also be copies of
     * the original's children.
     */
    @Override
    public Filter copy()
    {
        FilterGroup filter = new FilterGroup();
        for (Filter child : children)
            filter.addChild(child.copy());
        filter.mode = mode;
        filter.comment = comment;
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
        FilterGroup o = (FilterGroup)other;
        if (o.mode != mode)
            return false;
        if (children.size() != o.children.size())
            return false;
        List<Filter> otherChildren = new ArrayList<>(o.children);
        for (Filter child : children)
            otherChildren.remove(child);
        return otherChildren.isEmpty();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(children, mode);
    }

    @Override
    public Iterator<Filter> iterator()
    {
        return children.iterator();
    }

    @Override
    public boolean test(Card c)
    {
        return mode.test(children, c);
    }

    @Override
    protected void serializeFields(JsonObject fields)
    {
        fields.addProperty("mode", mode.toString());
        fields.addProperty("comment", comment);
        fields.add("children", children.stream().collect(Collector.of(
            JsonArray::new,
            (a, i) -> a.add(i.toJsonObject()),
            (l, r) -> { l.addAll(r); return l; }
        )));
    }

    @Override
    protected void deserializeFields(JsonObject fields)
    {
        mode = Arrays.stream(Mode.values()).filter((m) -> m.toString().equals(fields.get("mode").getAsString())).findAny().get();
        comment = fields.has("comment") ? fields.get("comment").getAsString() : "";
        for (JsonElement element : fields.get("children").getAsJsonArray())
        {
            CardAttribute type = CardAttribute.fromString(element.getAsJsonObject().get("type").getAsString());
            Filter child = type == CardAttribute.GROUP ? new FilterGroup() : CardAttribute.createFilter(type);
            child.fromJsonObject(element.getAsJsonObject());
            children.add(child);
        }
    }
}
