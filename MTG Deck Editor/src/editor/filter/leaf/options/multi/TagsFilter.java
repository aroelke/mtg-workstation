package editor.filter.leaf.options.multi;

import java.util.Arrays;
import java.util.HashSet;

import editor.database.card.Card;
import editor.filter.Filter;
import editor.filter.FilterFactory;
import editor.util.Containment;

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
		super(FilterFactory.TAGS, (c) -> Card.tags.getOrDefault(c, new HashSet<String>()));
	}

	@Override
	public String convertFromString(String str)
	{
		return str;
	}

	@Override
	public Filter copy()
	{
		TagsFilter filter = (TagsFilter)FilterFactory.createFilter(FilterFactory.TAGS);
		filter.contain = contain;
		filter.selected = new HashSet<String>(selected);
		return filter;
	}

	@Override
	public void parse(String s)
	{
		String content = checkContents(s, FilterFactory.TAGS);
		int delim = content.indexOf('{');
		contain = Containment.fromString(content.substring(0, delim));
		if (content.charAt(delim + 1) != '}')
			selected.addAll(Arrays.asList(content.substring(delim + 1, content.length() - 1).split(",")));
	}
}
