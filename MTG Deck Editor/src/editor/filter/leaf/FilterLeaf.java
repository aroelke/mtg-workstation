package editor.filter.leaf;

import java.util.function.Function;

import editor.database.card.Card;
import editor.filter.Filter;
import editor.util.SerializableFunction;

/**
 * This class represents a leaf in the filter tree, which filters a single characteristic of a Card.
 * 
 * @author Alec Roelke
 *
 * @param <T> Type of characteristic being filtered
 */
public abstract class FilterLeaf<T> extends Filter
{
	/**
	 * Function representing the characteristic of the cards to be filtered.
	 */
	private SerializableFunction<Card, T> function;

	/**
	 * Create a new FilterLeaf.
	 * 
	 * @param t type of the new FilterLeaf
	 * @param f function of the new FilterLeaf
	 */
	public FilterLeaf(String t, SerializableFunction<Card, T> f)
	{
		super(t);
		function = f;
	}
	
	/**
	 * Get the String representation of the contents of this FilterLeaf, without
	 * its code from {@link #type()}.
	 * 
	 * @return a String representing the contents of this FilterLeaf.
	 */
	public abstract String content();
	
	/**
	 * Get the attribute to be filtered.
	 * 
	 * @return a {@link Function} representing the attribute of a card to be filtered.
	 */
	protected Function<Card, T> function()
	{
		return function;
	}
}
