package editor.filter.leaf;

import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;

import editor.database.Card;
import editor.database.characteristics.ManaType;
import editor.filter.FilterType;
import editor.util.Containment;

/**
 * This class represents a filter to group cards by color characteristic.
 * 
 * @author Alec Roelke
 */
public class ColorFilter extends FilterLeaf<ManaType.Tuple>
{
	/**
	 * Containment of this ColorFilter.
	 */
	public Containment contain;
	/**
	 * Set of colors that should match cards.
	 */
	public Set<ManaType> colors;
	/**
	 * Whether or not cards should have multiple colors.
	 */
	public boolean multicolored;

	/**
	 * Create a new ColorFilter.
	 * 
	 * @param t Type of the new ColorFilter
	 * @param f Function for the new ColorFilter
	 */
	public ColorFilter(FilterType t, Function<Card, ManaType.Tuple> f)
	{
		super(t, f);
		contain = Containment.CONTAINS_ANY_OF;
		colors = new HashSet<ManaType>();
		multicolored = false;
	}
	
	/**
	 * @param c Card to test
	 * @return <code>true</code> if the given Card's color characteristic
	 * matches this ColorFilter's colors and containment, and <code>false</code>
	 * otherwise.
	 */
	@Override
	public boolean test(Card c)
	{
		return contain.test(function.apply(c), colors)
				&& (!multicolored || function.apply(c).size() > 1);
	}

	/**
	 * @return The String representation of this ColorFilter's content,
	 * which is its containment's String representation followed by
	 * a list of characters representing its colors and finally an
	 * M if cards should be multicolored.
	 * @see FilterLeaf#content()
	 */
	@Override
	public String content()
	{
		StringJoiner join = new StringJoiner("", "\"", "\"");
		for (ManaType color: new ManaType.Tuple(colors))
			join.add(String.valueOf(color.shorthand()));
		if (multicolored)
			join.add("M");
		return contain.toString() + join.toString();
	}
	
	/**
	 * Parse a String to determine this ColorFilter's containment, 
	 * colors, and multicolored status.
	 * 
	 * @param s String to parse
	 * @see editor.filter.Filter#parse(String)
	 */
	@Override
	public void parse(String s)
	{
		String content = checkContents(s, type);
		int delim = content.indexOf('"');
		contain = Containment.get(content.substring(0, delim));
		for (char c: content.substring(delim + 1, content.lastIndexOf('"')).toCharArray())
		{
			if (Character.toUpperCase(c) == 'M')
				multicolored = true;
			else
				colors.add(ManaType.get(c));
		}
	}
}
