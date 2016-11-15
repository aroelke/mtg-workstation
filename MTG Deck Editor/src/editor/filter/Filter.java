package editor.filter;

import java.io.Externalizable;
import java.util.function.Predicate;

import editor.database.card.Card;
import editor.util.UnicodeSymbols;


/**
 * This class represents a filter to group a collection of Cards by various characteristics.
 * Each filter has a String representation and can parse a String for its values.  The overall
 * filter is represented by a tree whose internal nodes are groups and whose leaf nodes filter
 * out characteristics.  A filter can test if a Card matches its characteristics.
 *
 * Note that because filters are mutable, they do not make good keys or Set
 * members.
 *
 * @author Alec Roelke
 */
public abstract class Filter implements Predicate<Card>, Externalizable
{
	/**
	 * Character marking the beginning of a group.
	 */
	public static final char BEGIN_GROUP = UnicodeSymbols.LEFT_ANGLE_DOUBLE_QUOTE;
	/**
	 * Character marking the end of a group.
	 */
	public static final char END_GROUP = UnicodeSymbols.RIGHT_ANGLE_DOUBLE_QUOTE;

	/**
	 * Parent of this Filter in the tree (null if this is the root Filter).
	 */
	protected FilterGroup parent;

	/**
	 * Create a new Filter with no parent.
	 */
	public Filter()
	{
		parent = null;
	}

	/**
	 * Create a copy of this Filter.
	 * 
	 * @return A new Filter that is a copy of this Filter.
	 */
	public abstract Filter copy();

	@Override
	public abstract boolean equals(Object other);

	@Override
	public abstract int hashCode();

	/**
	 * Parse a String to determine the contents of this Filter.  How the String is parsed
	 * is Filter-dependent.
	 *
	 * @param s String to parse
	 */
	public abstract void parse(String s);

	/**
	 * Get the String representation of the contents of this Filter.
	 * 
	 * @return The String representation of this Filter's subtree, excluding beginning
	 * and ending markers.
	 */
	public abstract String representation();

	/**
	 * {@inheritDoc}
	 * The String representation is the String value of its contents obtained from
	 * {@link #representation()} surrounded by {@link #BEGIN_GROUP} and {@link #END_GROUP}.
	 * @see Filter#representation()
	 */
	@Override
	public String toString()
	{
		return BEGIN_GROUP + representation() + END_GROUP;
	}
}
