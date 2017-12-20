package editor.filter.leaf.options.multi;

import editor.database.card.Card;
import editor.filter.Filter;
import editor.filter.FilterAttribute;

import java.util.HashSet;

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
        super(FilterAttribute.TAGS, (c) -> Card.tags.getOrDefault(c, new HashSet<String>()));
    }

    @Override
    public String convertFromString(String str)
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
}
