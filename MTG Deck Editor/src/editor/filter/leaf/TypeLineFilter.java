package editor.filter.leaf;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import editor.database.card.Card;
import editor.filter.Filter;
import editor.filter.FilterFactory;
import editor.util.Containment;

/**
 * This class represents a filter that filters a card by its entire
 * type line.
 * 
 * @author Alec Roelke
 */
public class TypeLineFilter extends FilterLeaf<List<Set<String>>>
{
	/**
	 * Containment specification for the terms in the filter's
	 * text.
	 */
	public Containment contain;
	/**
	 * Text containing values to search for in a card's type line.
	 */
	public String line;
	
	/**
	 * Create a new TypeLineFilter.
	 */
	public TypeLineFilter()
	{
		super(FilterFactory.TYPE_LINE, Card::allTypes);
		contain = Containment.CONTAINS_ANY_OF;
		line = "";
	}

	/**
	 * @param c Card to test
	 * @return <code>true</code> if the given Card's type line matches
	 * the terms in this TypeLineFilter with the given containment, 
	 * and <code>false</code> otherwise.
	 */
	@Override
	public boolean test(Card c)
	{
		return !line.isEmpty()
				&& c.allTypes().stream().anyMatch((f) ->
				contain.test(f.stream().map(String::toLowerCase).collect(Collectors.toList()),
						Arrays.asList(line.toLowerCase().split("\\s"))));
	}

	/**
	 * @return The String representation of this TypeLineFilter's contents, which
	 * is its containment string followed by its text in quotes.
	 * @see FilterLeaf#content()
	 */
	@Override
	public String content()
	{
		return contain.toString() + "\"" + line + "\"";
	}
	
	/**
	 * Parse a String to determine the containment and text of
	 * this TypeLineFilter.
	 * 
	 * @param s String to parse
	 * @see editor.filter.Filter#parse(String)
	 */
	@Override
	public void parse(String s)
	{
		String content = checkContents(s, FilterFactory.TYPE_LINE);
		int delim = content.indexOf('"');
		contain = Containment.fromString(content.substring(0, delim));
		line = content.substring(delim + 1, content.lastIndexOf('"'));
	}
	
	/**
	 * @return A TypeLineFilter that is a copy of this one.
	 */
	@Override
	public Filter copy()
	{
		TypeLineFilter filter = (TypeLineFilter)FilterFactory.createFilter(FilterFactory.TYPE_LINE);
		filter.contain = contain;
		filter.line = line;
		return filter;
	}
	
	/**
	 * @param other Object to compare with
	 * @return <code>true</code> if the other Object is a TypeLineFilter,
	 * its containment is the same as this one's, and its line is the same
	 * as this one's. 
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
		TypeLineFilter o = (TypeLineFilter)other;
		return contain == o.contain && line.equals(o.line);
	}
	
	/**
	 * @return The hash code of this TypeLineFilter, which composed of the
	 * hash codes of its containment and line.
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash(type(), function(), contain, line);
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		super.readExternal(in);
		contain = (Containment)in.readObject();
		line = in.readUTF();
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		super.writeExternal(out);
		out.writeObject(contain);
		out.writeUTF(line);
	}
}
