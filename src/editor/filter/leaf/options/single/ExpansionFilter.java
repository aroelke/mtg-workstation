package editor.filter.leaf.options.single;

import java.util.HashSet;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import editor.database.card.Card;
import editor.database.characteristics.Expansion;
import editor.filter.Filter;
import editor.filter.FilterAttribute;

/**
 * This class represents a filter that groups cards by expansion.
 *
 * @author Alec Roelke
 */
public class ExpansionFilter extends SingletonOptionsFilter<Expansion>
{
    /**
     * Create a new ExpansionFilter.
     */
    public ExpansionFilter()
    {
        super(FilterAttribute.EXPANSION, Card::expansion);
    }

    @Override
    protected Expansion convertFromString(String str)
    {
        for (Expansion expansion : Expansion.expansions)
            if (str.equalsIgnoreCase(expansion.name))
                return expansion;
        throw new IllegalArgumentException("Unknown expansion name \"" + str + "\"");
    }

    @Override
    public Filter copy()
    {
        ExpansionFilter filter = (ExpansionFilter)FilterAttribute.createFilter(FilterAttribute.EXPANSION);
        filter.contain = contain;
        filter.selected = new HashSet<>(selected);
        return filter;
    }

    @Override
    protected JsonElement convertToJson(Expansion item)
    {
        return new JsonPrimitive(item.name);
    }
}