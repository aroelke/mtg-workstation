package editor.filter.leaf.options.multi;

import java.util.HashSet;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import editor.database.attributes.CardAttribute;
import editor.database.card.Card;
import editor.filter.leaf.FilterLeaf;

import scala.jdk.javaapi.CollectionConverters;

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
        super(CardAttribute.TAGS, (c) -> {
            if (Card.TAGS().contains(c))
                return CollectionConverters.asJava(Card.TAGS().apply(c));
            else
                return new HashSet<String>();
        });
    }

    @Override
    protected String convertFromString(String str)
    {
        return str;
    }

    @Override
    protected FilterLeaf<String> copyLeaf()
    {
        TagsFilter filter = (TagsFilter)CardAttribute.createFilter(CardAttribute.TAGS);
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
