package editor.filter.leaf;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import editor.database.Card;
import editor.filter.FilterType;
import editor.util.Containment;

/**
 * TODO: Comment this class
 * @author Alec Roelke
 */
public class TypeLineFilter extends FilterLeaf<List<List<String>>>
{
	public Containment contain;
	public String line;
	
	public TypeLineFilter()
	{
		super(FilterType.TYPE_LINE, Card::allTypes);
		contain = Containment.CONTAINS_ANY_OF;
		line = "";
	}

	@Override
	public boolean test(Card c)
	{
		return !line.isEmpty()
				&& c.allTypes().stream().anyMatch((f) ->
				contain.test(f.stream().map(String::toLowerCase).collect(Collectors.toList()),
						Arrays.asList(line.toLowerCase().split("\\s"))));
	}

	@Override
	public String content()
	{
		return contain.toString() + "\"" + line + "\"";
	}
	
	@Override
	public void parse(String s)
	{
		String content = checkContents(s, FilterType.TYPE_LINE);
		int delim = content.indexOf('"');
		contain = Containment.get(content.substring(0, delim));
		line = content.substring(delim + 1, content.lastIndexOf('"'));
	}
}
