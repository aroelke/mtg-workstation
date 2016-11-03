package editor.filter.leaf.options.multi;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Objects;

import editor.database.card.Card;
import editor.filter.leaf.options.OptionsFilter;
import editor.util.SerializableFunction;

/**
 * This class represents a filter that groups cards by a characteristic that
 * can contain zero or more of a set of values.
 * 
 * @author Alec Roelke
 *
 * @param <T> Type of the characteristic being filtered
 */
public abstract class MultiOptionsFilter<T> extends OptionsFilter<T>
{
	/**
	 * Function representing the characteristic being filtered that hides
	 * the superclass's function.
	 */
	private SerializableFunction<Card, Collection<T>> function;
	
	/**
	 * Create a new MultiOptionsFilter.
	 * 
	 * @param t Type of the new MultiOptionsFilter
	 * @param f Function for the new MultiOptionsFilter
	 */
	public MultiOptionsFilter(String t, SerializableFunction<Card, Collection<T>> f)
	{
		super(t, null);
		function = f;
	}
	
	protected SerializableFunction<Card, Collection<T>> multifunction()
	{
		return function;
	}
	
	/**
	 * @param c Card to test
	 * @return <code>true</code> if the values in the Card's characteristic
	 * match this MultiOptionsFilter's selected values with its containment.
	 */
	@Override
	public boolean test(Card c)
	{
		return contain.test(function.apply(c), selected);
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(type(), function, contain, selected);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		super.readExternal(in);
		function = (SerializableFunction<Card, Collection<T>>)in.readObject();
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		super.writeExternal(out);
		out.writeObject(function);
	}
}
