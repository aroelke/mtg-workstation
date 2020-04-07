package editor.filter.leaf;

import java.io.IOException;
import java.io.ObjectOutput;
import java.util.Objects;

import com.google.gson.JsonObject;

import editor.database.card.Card;
import editor.database.characteristics.ManaCost;
import editor.filter.Filter;
import editor.filter.FilterAttribute;
import editor.util.Containment;

/**
 * This class represents a filter to group cards by mana costs.
 *
 * @author Alec Roelke
 */
public class ManaCostFilter extends FilterLeaf<ManaCost>
{
    /**
     * Containment for this ManaCostFilter.
     */
    public Containment contain;
    /**
     * Mana cost to filter by.
     */
    public ManaCost cost;

    /**
     * Create a new ManaCostFilter.
     */
    public ManaCostFilter()
    {
        super(FilterAttribute.MANA_COST, null);
        contain = Containment.CONTAINS_ANY_OF;
        cost = new ManaCost();
    }

    @Override
    public Filter copy()
    {
        ManaCostFilter filter = (ManaCostFilter)FilterAttribute.createFilter(FilterAttribute.MANA_COST);
        filter.contain = contain;
        filter.cost = cost;
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
        ManaCostFilter o = (ManaCostFilter)other;
        return o.contain == contain && o.cost.equals(cost);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(type(), contain, cost);
    }

    /**
     * {@inheritDoc}
     * Filter cards by their mana costs.
     */
    @Override
    public boolean test(Card c)
    {
        return c.manaCost().stream().anyMatch((m) -> switch (contain) {
            case CONTAINS_ANY_OF -> Containment.CONTAINS_ANY_OF.test(m, cost);
            case CONTAINS_NONE_OF -> Containment.CONTAINS_NONE_OF.test(m, cost);
            case CONTAINS_ALL_OF -> m.isSuperset(cost);
            case CONTAINS_NOT_ALL_OF -> !m.isSuperset(cost);
            case CONTAINS_EXACTLY -> m.equals(cost);
            case CONTAINS_NOT_EXACTLY -> !m.equals(cost);
        });
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException
    {
        out.writeObject(contain);
        out.writeUTF(cost.toString());
    }

    @Override
    protected void serializeFields(JsonObject fields)
    {
        fields.addProperty("contains", contain.toString());
        fields.addProperty("cost", cost.toString());
    }

    @Override
    protected void deserializeFields(JsonObject fields)
    {
        contain = Containment.parseContainment(fields.get("contains").getAsString());
        cost = ManaCost.parseManaCost(fields.get("cost").getAsString());
    }
}
