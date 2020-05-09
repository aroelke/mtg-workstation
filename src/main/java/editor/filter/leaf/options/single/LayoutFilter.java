package editor.filter.leaf.options.single;

import java.util.HashSet;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import editor.database.card.Card;
import editor.database.card.CardLayout;
import editor.filter.Filter;
import editor.filter.FilterAttribute;

/**
 * This class represents a filter that groups cards by layout.
 *
 * @author Alec Roelke
 */
public class LayoutFilter extends SingletonOptionsFilter<CardLayout>
{
    /**
     * Create a new LayoutFilter.
     */
    public LayoutFilter()
    {
        super(FilterAttribute.LAYOUT, Card::layout);
    }

    @Override
    protected CardLayout convertFromString(String str)
    {
        return CardLayout.valueOf(str.replace(' ', '_').toUpperCase());
    }

    @Override
    public Filter copy()
    {
        LayoutFilter filter = (LayoutFilter)FilterAttribute.createFilter(FilterAttribute.LAYOUT);
        filter.contain = contain;
        filter.selected = new HashSet<>(selected);
        return filter;
    }

    @Override
    protected JsonElement convertToJson(CardLayout item)
    {
        return new JsonPrimitive(item.toString());
    }

    @Override
    protected CardLayout convertFromJson(JsonElement item)
    {
        return CardLayout.valueOf(item.getAsString().replace(' ', '_').toUpperCase());
    }
}
