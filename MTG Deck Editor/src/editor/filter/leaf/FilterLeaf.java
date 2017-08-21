package editor.filter.leaf;

import java.util.Arrays;
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
	 * Check to ensure the contents of the given String match this FilterLeaf,
	 * and return a String containing the contents of the filter.
	 * 
	 * @param s String to check
	 * @param correct list of FilterTypes indicating correct ones for this FilterLeaf
	 * @return a String representing the contents of this FilterLeaf for further
	 * parsing.
	 */
	public String checkContents(String s, String... correct)
	{
		int delim = s.indexOf(':');
		String code = s.substring(1, delim);
		if (!Arrays.asList(correct).contains(code))
			throw new IllegalArgumentException("Illegal filter type '" + code + "' found in string \"" + s + "\"");
		return s.substring(delim + 1, s.length() - 1);
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
