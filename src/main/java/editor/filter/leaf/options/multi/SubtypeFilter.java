package editor.filter.leaf.options.multi;

import java.util.HashSet;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import editor.database.attributes.CardAttribute;
import editor.database.card.Card;
import editor.filter.leaf.FilterLeaf;

/**
 * This class represents a filter that groups cards by subtype.
 *
 * @author Alec Roelke
 */
public class SubtypeFilter extends MultiOptionsFilter<String>
{
    /**
     * List of all subtypes that appear on cards.
     */
    public static String[] subtypeList = {};

    /**
     * Create a new SubtypeFilter.
     */
    public SubtypeFilter()
    {
        super(CardAttribute.SUBTYPE, Card::subtypes);
    }

    @Override
    protected String convertFromString(String str)
    {
        return str;
    }

    @Override
    public FilterLeaf<String> subCopy()
    {
        SubtypeFilter filter = (SubtypeFilter)CardAttribute.createFilter(CardAttribute.SUBTYPE);
        filter.contain = contain;
        filter.selected = new HashSet<>(selected);
        return filter;
    }

    @Override
    protected JsonElement convertToJson(String item)
    {
        return new JsonPrimitive(item);
    }

    @Override
    protected String convertFromJson(JsonElement item)
    {
        return item.getAsString();
    }
}
