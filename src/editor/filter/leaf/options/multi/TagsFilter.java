package editor.filter.leaf.options.multi;

import java.util.HashSet;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import editor.filter.Filter;
import editor.filter.FilterAttribute;
import editor.gui.settings.SettingsDialog;

/**
 * This class represents a filter that filters cards by user-controlled tags.
 *
 * @author Alec Roelke
 */
public class TagsFilter extends MultiOptionsFilter<String>
{
    /**
     * Create a new TagsFilter.
     */
    public TagsFilter()
    {
        super(FilterAttribute.TAGS, (c) -> SettingsDialog.settings().inventory.tags.getOrDefault(c.multiverseid().get(0), new HashSet<String>()));
    }

    @Override
    protected String convertFromString(String str)
    {
        return str;
    }

    @Override
    public Filter copy()
    {
        TagsFilter filter = (TagsFilter)FilterAttribute.createFilter(FilterAttribute.TAGS);
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
