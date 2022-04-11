package editor.filter.leaf;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.gson.JsonObject;

import editor.database.attributes.CardAttribute;
import editor.database.card.Card;

/**
 * This class represents a filter for a numeric card characteristic that can be variable.
 *
 * @author Alec Roelke
 */
public class VariableNumberFilter extends NumberFilter
{
    /**
     * The function that determines if the filter's characteristic is variable.
     */
    private Predicate<Card> variable;
    /**
     * Whether or not the filter should search for varying values of its characteristic.
     */
    public boolean varies;

    /**
     * Create a new VariableNumberFilter with no filter type and no functions.  This constructor should
     * only be used for deserialization.
     */
    public VariableNumberFilter()
    {
        this(null, null, null);
    }

    /**
     * Create a new VariableNumberFilter.
     *
     * @param t type of the new VariableNumberFilter
     * @param f function representing the card characteristic
     * @param v function checking if the card characteristic is variable
     */
    public VariableNumberFilter(CardAttribute t, Function<Card, Collection<Double>> f, Predicate<Card> v)
    {
        super(t, f);
        varies = false;
        variable = v;
    }

    @Override
    protected FilterLeaf copyLeaf()
    {
        VariableNumberFilter filter = (VariableNumberFilter)CardAttribute.createFilter(type());
        filter.varies = varies;
        filter.variable = variable;
        filter.operation = operation;
        filter.operand = operand;
        return filter;
    }

    @Override
    public boolean leafEquals(Object other)
    {
        if (other.getClass() != getClass())
            return false;
        VariableNumberFilter o = (VariableNumberFilter)other;
        return o.type().equals(type()) && o.varies == varies && o.operation.equals(operation) && o.operand == operand;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(type(), varies, variable, operation, operand);
    }

    /**
     * {@inheritDoc}
     * Only return true if the given card's value from this filter's function passes this filter's
     * operation, or if it is variable and a variable value is desired.
     */
    @Override
    protected boolean testFace(Card c)
    {
        return varies ? variable.test(c) : super.testFace(c);
    }

    @Override
    protected void serializeLeaf(JsonObject fields)
    {
        super.serializeLeaf(fields);
        fields.addProperty("varies", varies);
    }

    @Override
    protected void deserializeLeaf(JsonObject fields)
    {
        super.deserializeLeaf(fields);
        varies = fields.get("varies").getAsBoolean();
    }
}
