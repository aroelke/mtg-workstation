package editor.filter.leaf;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

import com.google.gson.JsonObject;

import editor.database.attributes.CardAttribute;
import editor.database.card.Card;
import editor.util.Comparison;

/**
 * This class represents a filter for a card characteristic that is a number.
 *
 * @author Alec Roelke
 */
public class NumberFilter extends FilterLeaf
{
    private Function<Card, Collection<Double>> function;
    /**
     * Operation to compare the characteristic with this NumberFilter's
     * operand.
     */
    public Comparison operation;
    /**
     * Operand to perform the operation on.
     */
    public double operand;

    /**
     * Create a new NumberFilter.
     *
     * @param t type of the new NumberFilter
     * @param f function for the new NumberFilter
     */
    public NumberFilter(CardAttribute t, Function<Card, Collection<Double>> f)
    {
        super(t);
        function = f;
        operation = Comparison.EQ;
        operand = 0.0;
    }

    /**
     * Create a new NumberFilter without a type or function.  Should only be used for
     * deserialization.
     */
    public NumberFilter()
    {
        this(null, null);
    }

    /**
     * {@inheritDoc}
     * Filter cards by a numerical value according to this NumberFilter's operation and operand.
     */
    @Override
    protected boolean testFace(Card c)
    {
        return function.apply(c).stream().anyMatch((v) -> !v.isNaN() && operation.test(v, operand));
    }

    /**
     * @return A new NumberFilter that is a copy of this one.
     */
    @Override
    protected FilterLeaf copyLeaf()
    {
        NumberFilter filter = (NumberFilter)CardAttribute.createFilter(type());
        filter.operation = operation;
        filter.operand = operand;
        return filter;
    }

    /**
     * @param other Object to compare with
     * @return <code>true</code> if the other Object is a NumberFilter and its type,
     * comparison, and operand is the same.
     */
    @Override
    public boolean leafEquals(Object other)
    {
        if (other.getClass() != getClass())
            return false;
        NumberFilter o = (NumberFilter)other;
        return o.type().equals(type()) && o.operation.equals(operation) && o.operand == operand;
    }

    /**
     * @return The hash code of this NumberFilter, which is composed of its comparison and
     * operand.
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(type(), operation, operand);
    }

    @Override
    protected void serializeLeaf(JsonObject fields)
    {
        fields.addProperty("operation", operation.toString());
        fields.addProperty("operand", operand);
    }

    @Override
    protected void deserializeLeaf(JsonObject fields)
    {
        operation = Comparison.valueOf(fields.get("operation").getAsString().charAt(0));
        operand = fields.get("operand").getAsDouble();
    }
}