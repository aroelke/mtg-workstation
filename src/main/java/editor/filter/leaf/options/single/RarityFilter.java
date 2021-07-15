package editor.filter.leaf.options.single;

import java.util.HashSet;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import editor.database.attributes.CardAttribute;
import editor.database.attributes.Rarity;
import editor.database.card.Card;
import editor.filter.leaf.FilterLeaf;

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
        super(CardAttribute.RARITY, Card::rarity);
    }

    @Override
    protected FilterLeaf<Rarity> subCopy()
    {
        RarityFilter filter = (RarityFilter)CardAttribute.createFilter(CardAttribute.RARITY);
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