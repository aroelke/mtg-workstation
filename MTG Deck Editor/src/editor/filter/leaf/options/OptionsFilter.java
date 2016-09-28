package editor.filter.leaf.options;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;

import editor.database.card.Card;
import editor.filter.leaf.FilterLeaf;
import editor.util.Containment;

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
	 * @param t Type of this OptionsFilter
	 * @param f Function for this OptionsFilter
	 */
	public OptionsFilter(String t, Function<Card, T> f)
	{
		super(t, f);
		contain = Containment.CONTAINS_ANY_OF;
		selected = new HashSet<T>();
	}

	/**
	 * @return The String representation of this OptionsFilter's content,
	 * which is its containment's String representation followed by the
	 * String representations of its selected options surrounded by
	 * braces and separated by commas.
	 * @see FilterLeaf#content()
	 */
	@Override
	public String content()
	{
		StringJoiner join = new StringJoiner(",", "{", "}");
		for (T option: selected)
			join.add(option.toString());
		return contain.toString() + join.toString();
	}
	
	/**
	 * @param other Object to compare with
	 * @return <code>true</code> if the other Object is the same kind of OptionsFilter
	 * as this one and its selection and containment are the same.
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
		OptionsFilter<?> o = (OptionsFilter<?>)other;
		return o.type.equals(type) && o.contain == contain && o.selected.equals(selected);
	}
	
	/**
	 * @return The hash code of this OptionsFilter, which is composed from the hash
	 * codes of its containment type and selected items set.
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash(type, function, contain, selected);
	}
}
