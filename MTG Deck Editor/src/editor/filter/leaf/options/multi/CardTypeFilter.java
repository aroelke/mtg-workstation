package editor.filter.leaf.options.multi;

import java.util.Arrays;

import editor.database.Card;
import editor.filter.FilterType;
import editor.util.Containment;

/**
 * TODO: Comment this class
 * @author Alec Roelke
 */
public class CardTypeFilter extends MultiOptionsFilter<String>
{
	public CardTypeFilter()
	{
		super(FilterType.TYPE, Card::types);
	}
	
	@Override
	public void parse(String s)
	{
		String content = checkContents(s, FilterType.TYPE);
		int delim = s.indexOf('{');
		contain = Containment.get(content.substring(0, delim));
		if (content.charAt(delim + 1) != '}')
			selected.addAll(Arrays.asList(content.substring(delim + 1, content.length() - 1).split(",")));
	}
}
