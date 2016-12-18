package editor.filter.leaf;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

import editor.database.card.Card;
import editor.database.characteristics.ManaType;
import editor.filter.Filter;
import editor.filter.FilterFactory;
import editor.util.Containment;
import editor.util.SerializableFunction;

/**
 * This class represents a filter to group cards by color characteristic.
 * 
 * @author Alec Roelke
 */
public class ColorFilter extends FilterLeaf<List<ManaType>>
{
	/**
	 * Set of colors that should match cards.
	 */
	public Set<ManaType> colors;
	/**
	 * Containment of this ColorFilter.
	 */
	public Containment contain;
	/**
	 * Whether or not cards should have multiple colors.
	 */
	public boolean multicolored;

	/**
	 * Create a ColorFilter without a type or function.  Should be used only for
	 * deserialization.
	 */
	public ColorFilter()
	{
		this("", null);
	}
	
	/**
	 * Create a new ColorFilter.
	 * 
	 * @param t type of the new ColorFilter
	 * @param f function for the new ColorFilter
	 */
	public ColorFilter(String t, SerializableFunction<Card, List<ManaType>> f)
	{
		super(t, f);
		contain = Containment.CONTAINS_ANY_OF;
		colors = new HashSet<ManaType>();
		multicolored = false;
	}
	
	/**
	 * {@inheritDoc}
	 * This ColorFilter's content is each of its color's characters, followed
	 * optionally by an 'M' to indicate multicolored, surrounded by quotes.
	 */
	@Override
	public String content()
	{
		StringJoiner join = new StringJoiner("", "\"", "\"");
		for (ManaType color: colors)
			join.add(String.valueOf(color.shorthand()));
		if (multicolored)
			join.add("M");
		return contain.toString() + join.toString();
	}

	@Override
	public Filter copy()
	{
		ColorFilter filter = (ColorFilter)FilterFactory.createFilter(type());
		filter.colors = new HashSet<ManaType>(colors);
		filter.contain = contain;
		filter.multicolored = multicolored;
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
		ColorFilter o = (ColorFilter)other;
		return o.type().equals(type()) && o.colors.equals(colors) && o.contain == contain && o.multicolored == multicolored;
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(type(), function(), colors, contain, multicolored);
	}
	
	@Override
	public void parse(String s)
	{
		String content = checkContents(s, type());
		int delim = content.indexOf('"');
		contain = Containment.parseContainment(content.substring(0, delim));
		for (char c: content.substring(delim + 1, content.lastIndexOf('"')).toCharArray())
		{
			if (Character.toUpperCase(c) == 'M')
				multicolored = true;
			else
				colors.add(ManaType.parseManaType(c));
		}
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		super.readExternal(in);
		contain = (Containment)in.readObject();
		int n = in.readInt();
		for (int i = 0; i < n; i++)
			colors.add((ManaType)in.readObject());
		multicolored = in.readBoolean();
	}
	
	/**
	 * {@inheritDoc}
	 * Filter cards according to the colors in a color characteristic.
	 */
	@Override
	public boolean test(Card c)
	{
		return contain.test(function().apply(c), colors)
				&& (!multicolored || function().apply(c).size() > 1);
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		super.writeExternal(out);
		out.writeObject(contain);
		out.writeInt(colors.size());
		for (ManaType type: colors)
			out.writeObject(type);
		out.writeBoolean(multicolored);
	}
}
