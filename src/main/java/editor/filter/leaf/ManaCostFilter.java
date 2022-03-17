package editor.filter.leaf;

import java.util.Objects;

import com.google.gson.JsonObject;

import editor.database.attributes.CardAttribute;
import editor.database.attributes.ManaCost;
import editor.database.card.Card;
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
        super(CardAttribute.MANA_COST, null);
        contain = Containment.CONTAINS_ANY_OF;
        cost = new ManaCost();
    }

    @Override
    public FilterLeaf<ManaCost> copyLeaf()
    {
        ManaCostFilter filter = (ManaCostFilter)CardAttribute.createFilter(CardAttribute.MANA_COST);
        filter.contain = contain;
        filter.cost = cost;
        return filter;
    }

    @Override
    public boolean leafEquals(Object other)
    {
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
    public boolean testFace(Card c)
    {
        return switch (contain) {
            case CONTAINS_ANY_OF -> Containment.CONTAINS_ANY_OF.test(c.manaCost(), cost);
            case CONTAINS_NONE_OF -> Containment.CONTAINS_NONE_OF.test(c.manaCost(), cost);
            case CONTAINS_ALL_OF -> c.manaCost().isSuperset(cost);
            case CONTAINS_NOT_ALL_OF -> !c.manaCost().isSuperset(cost);
            case CONTAINS_EXACTLY -> c.manaCost().equals(cost);
            case CONTAINS_NOT_EXACTLY -> !c.manaCost().equals(cost);
        };
    }

    @Override
    protected void serializeLeaf(JsonObject fields)
    {
        fields.addProperty("contains", contain.toString());
        fields.addProperty("cost", cost.toString());
    }

    @Override
    protected void deserializeLeaf(JsonObject fields)
    {
        contain = Containment.parseContainment(fields.get("contains").getAsString());
        cost = ManaCost.parseManaCost(fields.get("cost").getAsString());
    }
}
