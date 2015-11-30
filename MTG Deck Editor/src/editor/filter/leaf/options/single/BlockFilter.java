package editor.filter.leaf.options.single;

import java.util.Arrays;

import editor.filter.FilterType;
import editor.util.Containment;

/**
 * TODO: Comment this class
 * @author Alec Roelke
 */
public class BlockFilter extends SingletonOptionsFilter<String>
{
	public BlockFilter()
	{
		super(FilterType.BLOCK, (c) -> c.expansion().block);
	}
	
	@Override
	public void parse(String s)
	{
		String content = checkContents(s, FilterType.BLOCK);
		int delim = s.indexOf('{');
		contain = Containment.get(content.substring(0, delim));
		if (content.charAt(delim + 1) != '}')
			selected.addAll(Arrays.asList(content.substring(delim, content.length() - 1).split(",")));
	}
}