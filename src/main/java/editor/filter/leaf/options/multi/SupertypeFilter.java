package editor.filter.leaf.options.multi;

import java.util.HashSet;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import editor.database.attributes.CardAttribute;
import editor.database.card.Card;
import editor.filter.leaf.FilterLeaf;

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
        super(CardAttribute.SUPERTYPE, Card::supertypes);
    }

    @Override
    protected String convertFromString(String str)
    {
        return str;
    }

    @Override
    public FilterLeaf<String> subCopy()
    {
        SupertypeFilter filter = (SupertypeFilter)CardAttribute.createFilter(CardAttribute.SUPERTYPE);
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
