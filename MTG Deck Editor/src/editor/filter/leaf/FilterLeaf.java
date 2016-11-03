package editor.filter.leaf;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.function.Function;

import editor.database.card.Card;
import editor.filter.Filter;
import editor.util.SerializableFunction;

/**
 * This class represents a leaf in the filter tree, which filters a
 * single characteristic of a Card.
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
	 * FilterType showing the characteristic of the cards to be filtered.
	 */
	private String type;
	
	/**
	 * Create a new FilterLeaf.
	 * 
	 * @param t FilterType of the new FilterLeaf
	 * @param f Function of the new FilterLeaf
	 */
	public FilterLeaf(String t, SerializableFunction<Card, T> f)
	{
		super();
		type = t;
		function = f;
	}

	public FilterLeaf()
	{
		this("", null);
	}
	
	public String type()
	{
		return type;
	}
	
	protected Function<Card, T> function()
	{
		return function;
	}
	
	/**
	 * @return The String representation of the contents of this FilterLeaf,
	 * without its code.
	 */
	public abstract String content();
	
	/**
	 * Check to ensure the contents of the given String match this FilterLeaf,
	 * and return a String containing the contents of the filter.
	 * 
	 * @param s String to check
	 * @param correct List of FilterTypes indicating correct ones for this FilterLeaf
	 * @return A String representing the contents of this FilterLeaf for further
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
	 * @return The String representation of this FilterLeaf, which is its code followed
	 * by a colon (:) followed by that of its contents, without beginning or ending
	 * markers.
	 * @see FilterLeaf#content()
	 * @see Filter#representation()
	 */
	@Override
	public String representation()
	{
		return type + ":" + content();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		function = (SerializableFunction<Card, T>)in.readObject();
		type = in.readUTF();
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeObject(function);
		out.writeUTF(type);
	}
}
