package editor.filter.leaf;

import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;

import editor.database.Card;
import editor.database.characteristics.MTGColor;
import editor.filter.FilterType;
import editor.util.Containment;

/**
 * TODO: Comment this
 * @author Alec Roelke
 */
public class ColorFilter extends FilterLeaf<MTGColor.Tuple>
{
	public static ColorFilter createFilter(FilterType type)
	{
		switch (type)
		{
		case COLOR:
			return new ColorFilter(type, Card::colors);
		case COLOR_IDENTITY:
			return new ColorFilter(type, Card::colorIdentity);
		default:
			throw new IllegalArgumentException("Illegal color filter type " + type.name());
		}
	}
	
	public Containment contain;
	public Set<MTGColor> colors;
	public boolean multicolored;

	public ColorFilter(FilterType t, Function<Card, MTGColor.Tuple> f)
	{
		super(t, f);
		contain = Containment.CONTAINS_ANY_OF;
		colors = new HashSet<MTGColor>();
		multicolored = false;
	}
	
	@Override
	public boolean test(Card c)
	{
		return contain.test(function.apply(c), colors)
				&& (!multicolored || function.apply(c).size() > 1);
	}

	@Override
	public String content()
	{
		StringJoiner join = new StringJoiner("", "\"", "\"");
		for (MTGColor color: new MTGColor.Tuple(colors))
			join.add(String.valueOf(color.shorthand()));
		if (multicolored)
			join.add("M");
		return join.toString();
	}
	
	@Override
	public void parse(String s)
	{
		String content = checkContents(s, FilterType.COLOR, FilterType.COLOR_IDENTITY);
		int delim = content.indexOf('"');
		contain = Containment.get(content.substring(0, delim));
		for (char c: content.substring(delim + 1).toCharArray())
		{
			if (Character.toUpperCase(c) == 'M')
				multicolored = true;
			else
				colors.add(MTGColor.get(c));
		}
	}
}
