package editor.filter.leaf.options.single;

import java.util.HashSet;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import editor.database.card.Card;
import editor.database.characteristics.Rarity;
import editor.filter.Filter;
import editor.filter.FilterAttribute;

/**
 * This class represents a filter that groups cards by rarity.
 *
 * @author Alec Roelke
 */
public class RarityFilter extends SingletonOptionsFilter<Rarity>
{
    /**
     * Create a new RarityFilter.
     */
    public RarityFilter()
    {
        super(FilterAttribute.RARITY, Card::rarity);
    }

    @Override
    public Filter copy()
    {
        RarityFilter filter = (RarityFilter)FilterAttribute.createFilter(FilterAttribute.RARITY);
        filter.contain = contain;
        filter.selected = new HashSet<>(selected);
        return filter;
    }

    @Override
    protected Rarity convertFromString(String str)
    {
        return Rarity.parseRarity(str);
    }

    @Override
    protected JsonElement convertToJson(Rarity item)
    {
        return new JsonPrimitive(item.toString());
    }

    @Override
    protected Rarity convertFromJson(JsonElement item)
    {
        return Rarity.parseRarity(item.getAsString());
    }
}