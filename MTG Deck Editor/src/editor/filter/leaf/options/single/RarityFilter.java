package editor.filter.leaf.options.single;

import java.util.Arrays;
import java.util.stream.Collectors;

import editor.database.Card;
import editor.database.characteristics.Rarity;
import editor.filter.FilterType;
import editor.util.Containment;

/**
 * TODO: Comment this class
 * @author Alec Roelke
 */
public class RarityFilter extends SingletonOptionsFilter<Rarity>
{
	public RarityFilter()
	{
		super(FilterType.RARITY, Card::rarity);
	}
	
	@Override
	public void parse(String s)
	{
		String content = checkContents(s, FilterType.RARITY);
		int delim = content.indexOf('{');
		contain = Containment.get(content.substring(0, delim));
		if (content.charAt(delim + 1) != '}')
			selected = Arrays.stream(content.substring(delim + 1, content.length() - 1).split(",")).map(Rarity::get).collect(Collectors.toSet());
	}
}
