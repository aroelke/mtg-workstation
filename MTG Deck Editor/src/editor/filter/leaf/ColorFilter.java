package editor.filter.leaf;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;

import editor.database.card.Card;
import editor.database.characteristics.ManaType;
import editor.filter.Filter;
import editor.filter.FilterFactory;
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
	
	/**
	 * @return A new ColorFilter that is a copy of this ColorFilter.
	 */
	@Override
	public Filter copy()
	{
		ColorFilter filter = (ColorFilter)FilterFactory.createFilter(type);
		filter.colors = new HashSet<ManaType>(colors);
		filter.contain = contain;
		filter.multicolored = multicolored;
		return filter;
	}
	
	/**
	 * @param other Object to compare with
	 * @return <code>true</code> if the other Object is a ColorFilter, its
	 * set of ManaTypes is the same as this one's, the characteristic it filters
	 * is the same, its containment is the same, and whether or not Cards should
	 * be multicolored is the same.
	 */
	@Override
	public boolean equals(Object other)
	{
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (other.getClass() != getClass())
			return false;
		ColorFilter o = (ColorFilter)other;
		return o.type == type && o.colors.equals(colors) && o.contain == contain && o.multicolored == multicolored;
	}
	
	/**
	 * @return The hash code of this ColorFilter, which is composed of the hash
	 * codes of its type, color set, containment, and multicolored flag.
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash(type, function, colors, contain, multicolored);
	}
}
