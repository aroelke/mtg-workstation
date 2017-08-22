package editor.filter.leaf.options;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import editor.database.card.Card;
import editor.filter.leaf.FilterLeaf;
import editor.util.Containment;
import editor.util.SerializableFunction;

/**
 * This class represents a filter that groups cards based on characteristics
 * that take on values from a list of options.
 * 
 * @author Alec Roelke
 *
 * @param <T> Type of the options for the characteristic to be filtered
 */
public abstract class OptionsFilter<T> extends FilterLeaf<T>
{
	/**
	 * Containment type of this OptionsFilter.
	 */
	public Containment contain;
	/**
	 * Set of options that have been selected.
	 */
	public Set<T> selected;
	
	/**
	 * Create a new OptionsFilter.
	 * 
	 * @param t type of this OptionsFilter
	 * @param f function for this OptionsFilter
	 */
	public OptionsFilter(String t, SerializableFunction<Card, T> f)
	{
		super(t, f);
		contain = Containment.CONTAINS_ANY_OF;
		selected = new HashSet<T>();
	}
	
	/**
	 * Convert a String to the type that this OptionsFilter's options have.
	 * 
	 * @param str String to convert
	 * @return the option corresponding to the given String
	 */
	public abstract T convertFromString(String str);
	
	@Override
	public boolean equals(Object other)
	{
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (other.getClass() != getClass())
			return false;
		OptionsFilter<?> o = (OptionsFilter<?>)other;
		return o.type().equals(type()) && o.contain == contain && o.selected.equals(selected);
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(type(), function(), contain, selected);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		contain = (Containment)in.readObject();
		int n = in.readInt();
		for (int i = 0; i < n; i++)
		{
			if (in.readBoolean())
				selected.add((T)in.readObject());
			else
				selected.add(convertFromString(in.readUTF()));
		}
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeObject(contain);
		out.writeInt(selected.size());
		for (T item: selected)
		{
			out.writeBoolean(item instanceof Serializable);
			if (item instanceof Serializable)
				out.writeObject(item);
			else
				out.writeUTF(item.toString());
		}
	}
}
