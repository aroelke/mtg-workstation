package editor.filter.leaf.options.multi;

import java.util.HashSet;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import editor.database.card.Card;
import editor.filter.Filter;
import editor.filter.FilterAttribute;

/**
 * This class represents a filter that groups cards by supertype.
 *
 * @author Alec Roelke
 */
public class SupertypeFilter extends MultiOptionsFilter<String>
{
    /**
     * List of all supertypes that appear on cards.
     */
    public static String[] supertypeList = {};

    /**
     * Create a new SupertypeFilter.
     */
    public SupertypeFilter()
    {
        super(FilterAttribute.SUPERTYPE, Card::supertypes);
    }

    @Override
    protected String convertFromString(String str)
    {
        return str;
    }

    @Override
    public Filter copy()
    {
        SupertypeFilter filter = (SupertypeFilter)FilterAttribute.createFilter(FilterAttribute.SUPERTYPE);
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
