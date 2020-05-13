package editor.filter.leaf.options.single;

import java.util.HashSet;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import editor.database.attributes.CardAttribute;
import editor.filter.Filter;

/**
 * This class represents a filter that groups cards by block.
 *
 * @author Alec Roelke
 */
public class BlockFilter extends SingletonOptionsFilter<String>
{
    /**
     * Create a new BlockFilter.
     */
    public BlockFilter()
    {
        super(CardAttribute.BLOCK, (c) -> c.expansion().block);
    }

    @Override
    protected String convertFromString(String str)
    {
        return str;
    }

    @Override
    public Filter copy()
    {
        BlockFilter filter = (BlockFilter)CardAttribute.createFilter(CardAttribute.BLOCK);
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