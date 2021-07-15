package editor.filter.leaf.options.single;

import java.util.HashSet;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import editor.database.attributes.CardAttribute;
import editor.database.card.Card;
import editor.database.card.CardLayout;
import editor.filter.leaf.FilterLeaf;

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
        super(CardAttribute.LAYOUT, Card::layout);
    }

    @Override
    protected CardLayout convertFromString(String str)
    {
        return CardLayout.valueOf(str.replace(' ', '_').toUpperCase());
    }

    @Override
    protected FilterLeaf<CardLayout> subCopy()
    {
        LayoutFilter filter = (LayoutFilter)CardAttribute.createFilter(CardAttribute.LAYOUT);
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
