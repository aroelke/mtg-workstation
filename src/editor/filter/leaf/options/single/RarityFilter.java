package editor.filter.leaf.options.single;

import editor.database.card.Card;
import editor.database.characteristics.Rarity;
import editor.filter.Filter;
import editor.filter.FilterAttribute;

import java.util.HashSet;

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
    public Rarity convertFromString(String str)
    {
        return Rarity.parseRarity(str);
    }
}
