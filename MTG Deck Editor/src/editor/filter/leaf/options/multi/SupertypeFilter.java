package editor.filter.leaf.options.multi;

import java.util.Arrays;

import editor.database.Card;
import editor.filter.FilterType;
import editor.util.Containment;

/**
 * TODO: Comment this class
 * @author Alec Roelke
 */
public class SupertypeFilter extends MultiOptionsFilter<String>
{
	public SupertypeFilter()
	{
		super(FilterType.SUPERTYPE, Card::supertypes);
	}
	
	@Override
	public void parse(String s)
	{
		String content = checkContents(s, FilterType.SUPERTYPE);
		int delim = content.indexOf('{');
		contain = Containment.get(content.substring(0, delim));
		if (content.charAt(delim + 1) != '}')
			selected.addAll(Arrays.asList(content.substring(delim + 1, content.length() - 1).split(",")));
	}
}
