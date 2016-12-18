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
 * This class represents a filter that filters a card by its entire type line.
 * 
 * @author Alec Roelke
 */
public class TypeLineFilter extends FilterLeaf<List<Set<String>>>
{
	/**
	 * Containment specification for the terms in the filter's text.
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
	 * {@inheritDoc}
	 * This TypeLineFilter's content String is its containment's String value followed
	 * by its line in quotes.
	 */
	@Override
	public String content()
	{
		return contain.toString() + "\"" + line + "\"";
	}
	
	@Override
	public Filter copy()
	{
		TypeLineFilter filter = (TypeLineFilter)FilterFactory.createFilter(FilterFactory.TYPE_LINE);
		filter.contain = contain;
		filter.line = line;
		return filter;
	}
	
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
	
	@Override
	public int hashCode()
	{
		return Objects.hash(type(), function(), contain, line);
	}
	
	@Override
	public void parse(String s)
	{
		String content = checkContents(s, FilterFactory.TYPE_LINE);
		int delim = content.indexOf('"');
		contain = Containment.parseContainment(content.substring(0, delim));
		line = content.substring(delim + 1, content.lastIndexOf('"'));
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		super.readExternal(in);
		contain = (Containment)in.readObject();
		line = in.readUTF();
	}
	
	/**
	 * {@inheritDoc}
	 * Filter cards whose type lines match the specified values.
	 */
	@Override
	public boolean test(Card c)
	{
		return !line.isEmpty()
				&& c.allTypes().stream().anyMatch((f) ->
				contain.test(f.stream().map(String::toLowerCase).collect(Collectors.toList()),
						Arrays.asList(line.toLowerCase().split("\\s"))));
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		super.writeExternal(out);
		out.writeObject(contain);
		out.writeUTF(line);
	}
}
