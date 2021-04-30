package editor.filter.leaf.options.single;

import java.util.Arrays;
import java.util.HashSet;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import editor.database.attributes.CardAttribute;
import editor.database.attributes.Expansion;
import editor.database.card.Card;
import editor.filter.Filter;

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
        super(CardAttribute.EXPANSION, Card::expansion);
    }

    @Override
    protected Expansion convertFromString(String str)
    {
        for (Expansion expansion : Expansion.expansions)
            if (str.equalsIgnoreCase(expansion.name()))
                return expansion;
        throw new IllegalArgumentException("Unknown expansion name \"" + str + "\"");
    }

    @Override
    public Filter copy()
    {
        ExpansionFilter filter = (ExpansionFilter)CardAttribute.createFilter(CardAttribute.EXPANSION);
        filter.contain = contain;
        filter.selected = new HashSet<>(selected);
        return filter;
    }

    @Override
    protected JsonElement convertToJson(Expansion item)
    {
        return new JsonPrimitive(item.name());
    }

    @Override
    protected Expansion convertFromJson(JsonElement item)
    {
        return Arrays.stream(Expansion.expansions).filter((e) -> e.name().equalsIgnoreCase(item.getAsString())).findAny().get();
    }
}