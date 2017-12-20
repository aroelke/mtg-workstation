package editor.filter.leaf.options.single;

import editor.filter.Filter;
import editor.filter.FilterAttribute;

import java.util.HashSet;

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
        super(FilterAttribute.BLOCK, (c) -> c.expansion().block);
    }

    @Override
    public String convertFromString(String str)
    {
        return str;
    }

    @Override
    public Filter copy()
    {
        BlockFilter filter = (BlockFilter)FilterAttribute.createFilter(FilterAttribute.BLOCK);
        filter.contain = contain;
        filter.selected = new HashSet<>(selected);
        return filter;
    }
}