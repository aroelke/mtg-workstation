package editor.filter.leaf.options.single;

import java.util.Arrays;
import java.util.HashSet;

import editor.filter.Filter;
import editor.filter.FilterFactory;
import editor.filter.FilterType;
import editor.util.Containment;

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
		super(FilterType.BLOCK, (c) -> c.expansion.block);
	}
	
	/**
	 * Parse a String to determine this BlockFilter's containment and 
	 * blocks.
	 * 
	 * @param s String to parse
	 * @see editor.filter.Filter#parse(String)
	 */
	@Override
	public void parse(String s)
	{
		String content = checkContents(s, FilterType.BLOCK);
		int delim = content.indexOf('{');
		contain = Containment.get(content.substring(0, delim));
		if (content.charAt(delim + 1) != '}')
			selected.addAll(Arrays.asList(content.substring(delim + 1, content.length() - 1).split(",")));
	}
	
	/**
	 * @return A new BlockFilter that is a copy of this BlockFilter.
	 */
	@Override
	public Filter copy()
	{
		BlockFilter filter = (BlockFilter)FilterFactory.createFilter(FilterType.BLOCK);
		filter.contain = contain;
		filter.selected = new HashSet<String>(selected);
		return filter;
	}
}