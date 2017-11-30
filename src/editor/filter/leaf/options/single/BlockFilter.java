package editor.filter.leaf.options.single;

import java.util.HashSet;

import editor.filter.Filter;
import editor.filter.FilterFactory;

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
		super(FilterFactory.BLOCK, (c) -> c.expansion().block);
	}
	
	@Override
	public String convertFromString(String str)
	{
		return str;
	}
	
	@Override
	public Filter copy()
	{
		BlockFilter filter = (BlockFilter)FilterFactory.createFilter(FilterFactory.BLOCK);
		filter.contain = contain;
		filter.selected = new HashSet<String>(selected);
		return filter;
	}
}