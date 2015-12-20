package editor.filter.leaf.options.multi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import editor.database.Card;
import editor.database.characteristics.Legality;
import editor.filter.FilterType;
import editor.util.Containment;

/**
 * TODO: Comment this class
 * @author Alec Roelke
 */
public class LegalityFilter extends MultiOptionsFilter<String>
{
	public boolean restricted;
	
	public LegalityFilter()
	{
		super(FilterType.FORMAT_LEGALITY, Card::legalIn);
		restricted = false;
	}
	
	@Override
	public boolean test(Card c)
	{
		if (!super.test(c))
			return false;
		else if (restricted)
		{
			Collection<String> formats = new ArrayList<String>(c.legalIn());
			formats.retainAll(selected);
			for (String format: formats)
				if (c.legality().get(format) != Legality.RESTRICTED)
					return false;
			return true;
		}
		else
			return true;
	}
	
	@Override
	public String content()
	{
		return super.content() + (restricted ? 'r' : 'u');
	}
	
	@Override
	public void parse(String s)
	{
		String content = checkContents(s, FilterType.FORMAT_LEGALITY);
		int delim = content.indexOf('{');
		contain = Containment.get(content.substring(0, delim));
		if (content.charAt(delim + 1) != '}')
			selected.addAll(Arrays.asList(content.substring(delim + 1, content.length() - 2).split(",")));
		restricted = content.charAt(content.length() - 1) == 'r';
	}
}
