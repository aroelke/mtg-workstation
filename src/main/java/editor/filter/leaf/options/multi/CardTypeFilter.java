package editor.filter.leaf.options.multi;

import java.util.HashSet;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import editor.database.attributes.CardAttribute;
import editor.database.card.Card;
import editor.filter.leaf.FilterLeaf;

/**
 * This class represents a filter that groups cards by card type.
 *
 * @author Alec Roelke
 */
public class CardTypeFilter extends MultiOptionsFilter<String>
{
    /**
     * List of all types that appear on cards (including ones that appear on Unglued and Unhinged cards, whose
     * type lines were not updated for the most modern templating).
     */
    public static String[] typeList = {};

    /**
     * Create a new CardTypeFilter.
     */
    public CardTypeFilter()
    {
        super(CardAttribute.CARD_TYPE, Card::types);
    }

    @Override
    protected String convertFromString(String str)
    {
        return str;
    }

    @Override
    public FilterLeaf<String> subCopy()
    {
        CardTypeFilter filter = (CardTypeFilter)CardAttribute.createFilter(CardAttribute.CARD_TYPE);
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
