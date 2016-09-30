package editor.filter.leaf.options.multi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

import editor.database.card.Card;
import editor.database.characteristics.Legality;
import editor.filter.Filter;
import editor.filter.FilterFactory;
import editor.util.Containment;

/**
 * This class represents a filter that groups cards by format legality.
 * 
 * @author Alec Roelke
 */
public class LegalityFilter extends MultiOptionsFilter<String>
{
	/**
	 * Whether or not the card should be restricted in the formats
	 * selected.
	 */
	public boolean restricted;
	/**
	 * List of all formats cards can be played in.
	 */
	public static String[] formatList = {};
	
	/**
	 * Create a new LegalityFilter.
	 */
	public LegalityFilter()
	{
		super(FilterFactory.FORMAT_LEGALITY, Card::legalIn);
		restricted = false;
	}
	
	/**
	 * @param c Card to test
	 * @return <code>true</code> if the given Card is legal in the selected
	 * formats with this LegalityFilter's containments, or is restricted in
	 * them if desired, and <code>false</code> otherwise.
	 */
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
				if (c.legality()[format] != Legality.RESTRICTED)
					return false;
			return true;
		}
		else
			return true;
	}
	
	/**
	 * @return The String representation of this LegalityFilter's content,
	 * which is the same as that of OptionsFilter, but with an r afterward
	 * if the card is restricted and a u otherwise.
	 * @see editor.filter.leaf.options.OptionsFilter#content()
	 * @see editor.filter.leaf.FilterLeaf#content()
	 */
	@Override
	public String content()
	{
		return super.content() + (restricted ? 'r' : 'u');
	}
	
	/**
	 * Parse a String to determine this LegalityFilter's containment, 
	 * formats, and restricted flag.
	 * 
	 * @param s String to parse
	 * @see editor.filter.Filter#parse(String)
	 */
	@Override
	public void parse(String s)
	{
		String content = checkContents(s, FilterFactory.FORMAT_LEGALITY);
		int delim = content.indexOf('{');
		contain = Containment.fromString(content.substring(0, delim));
		if (content.charAt(delim + 1) != '}')
			selected.addAll(Arrays.asList(content.substring(delim + 1, content.length() - 2).split(",")));
		restricted = content.charAt(content.length() - 1) == 'r';
	}
	
	/**
	 * @return A new LegalityFilter that is a copy of this one.
	 */
	@Override
	public Filter copy()
	{
		LegalityFilter filter = (LegalityFilter)FilterFactory.createFilter(FilterFactory.FORMAT_LEGALITY);
		filter.contain = contain;
		filter.selected = new HashSet<String>(selected);
		filter.restricted = restricted;
		return filter;
	}
	
	/**
	 * @param other Object to compare with
	 * @return <code>true</code> if the other Object is a LegalityFilter and its
	 * containment, selected legalities, and restricted flag are all the same.
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
		LegalityFilter o = (LegalityFilter)other;
		return o.contain == contain && o.selected.equals(selected) && o.restricted == restricted;
	}
	
	/**
	 * @return The hash code of this LegalityFilter, which is composed from its
	 * selected list and restricted flag.
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash(contain, function, selected, restricted);
	}
}
