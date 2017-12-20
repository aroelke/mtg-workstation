package editor.filter.leaf.options.single;

import editor.database.card.Card;
import editor.database.card.CardLayout;
import editor.filter.Filter;
import editor.filter.FilterAttribute;

import java.util.HashSet;

/**
 * This class represents a filter that groups cards by layout.
 *
 * @author Alec Roelke
 */
public class LayoutFilter extends SingletonOptionsFilter<CardLayout>
{
    /**
     * Create a new LayoutFilter.
     */
    public LayoutFilter()
    {
        super(FilterAttribute.LAYOUT, Card::layout);
    }

    @Override
    public CardLayout convertFromString(String str)
    {
        return CardLayout.valueOf(str.replace(' ', '_').toUpperCase());
    }

    @Override
    public Filter copy()
    {
        LayoutFilter filter = (LayoutFilter)FilterAttribute.createFilter(FilterAttribute.LAYOUT);
        filter.contain = contain;
        filter.selected = new HashSet<>(selected);
        return filter;
    }
}
