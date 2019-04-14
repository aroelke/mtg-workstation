package editor.filter.leaf.options.multi;

import java.util.HashSet;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import editor.database.card.Card;
import editor.filter.Filter;
import editor.filter.FilterAttribute;

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
        super(FilterAttribute.SUBTYPE, Card::subtypes);
    }

    @Override
    protected String convertFromString(String str)
    {
        return str;
    }

    @Override
    public Filter copy()
    {
        SubtypeFilter filter = (SubtypeFilter)FilterAttribute.createFilter(FilterAttribute.SUBTYPE);
        filter.contain = contain;
        filter.selected = new HashSet<>(selected);
        return filter;
    }

    @Override
    protected JsonElement convertToJson(String item)
    {
        return new JsonPrimitive(item);
    }
}
