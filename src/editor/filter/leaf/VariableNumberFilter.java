package editor.filter.leaf;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.gson.JsonObject;

import editor.database.card.Card;
import editor.filter.Filter;
import editor.filter.FilterAttribute;

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
    public VariableNumberFilter(FilterAttribute t, Function<Card, Collection<Double>> f, Predicate<Card> v)
    {
        super(t, f);
        varies = false;
        variable = v;
    }

    @Override
    public Filter copy()
    {
        VariableNumberFilter filter = (VariableNumberFilter)FilterAttribute.createFilter(type());
        filter.varies = varies;
        filter.variable = variable;
        filter.operation = operation;
        filter.operand = operand;
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
        VariableNumberFilter o = (VariableNumberFilter)other;
        return o.type().equals(type()) && o.varies == varies && o.variable.equals(variable)
                && o.operation.equals(operation)&o.operand == operand;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(type(), function(), varies, variable, operation, operand);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
        super.readExternal(in);
        varies = in.readBoolean();
    }

    /**
     * {@inheritDoc}
     * Only return true if the given card's value from this filter's function passes this filter's
     * operation, or if it is variable and a variable value is desired.
     */
    @Override
    public boolean test(Card c)
    {
        return varies ? variable.test(c) : super.test(c);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException
    {
        super.writeExternal(out);
        out.writeBoolean(varies);
    }

    @Override
    public void serializeFields(JsonObject fields)
    {
        super.serializeFields(fields);
        fields.addProperty("varies", varies);
    }
}
