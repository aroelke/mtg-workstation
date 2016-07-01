package editor.filter;

import java.util.function.Predicate;

import editor.database.card.Card;


/**
 * This class represents a filter to group a collection of Cards
 * by various characteristics.  Each filter has a String representation
 * and can parse a String for its values.  The overall filter is
 * represented by a tree whose internal nodes are groups and whose leaf
 * nodes filter out characteristics.  A filter can test if a Card matches
 * its characteristics.
 * 
 * Note that because filters are mutable, they do not make good keys or Set
 * members.
 * 
 * @author Alec Roelke
 */
public abstract class Filter implements Predicate<Card>
{
	/**
	 * Character marking the end of a group.
	 */
	public static final char END_GROUP = '»';
	/**
	 * Character marking the beginning of a group.
	 */
	public static final char BEGIN_GROUP = '«';

	/**
	 * Parent of this Filter in the tree (null if this is the
	 * root Filter).
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
	 * @return The String representation of this Filter's subtree,
	 * excluding beginning and ending markers.
	 */
	public abstract String representation();
	
	/**
	 * Parse a String to determine the contents of this Filter.  How
	 * the String is parsed is Filter-dependent.
	 * 
	 * @param s String to parse
	 */
	public abstract void parse(String s);
	
	/**
	 * @return A new Filter that is a copy of this Filter.
	 */
	public abstract Filter copy();
	
	/**
	 * @return A String representation of this Filter.
	 * @see Filter#representation()
	 */
	@Override
	public String toString()
	{
		return BEGIN_GROUP + representation() + END_GROUP;
	}
	
	/**
	 * @param other Object to compare with
	 * @return <code>true</code> if the other Object is the same kind of
	 * Filter as this one and all of its fields are the same.
	 */
	@Override
	public abstract boolean equals(Object other);
	
	/**
	 * @return The hash code of this Filter, which is composed of the hash codes
	 * of all of its fields.
	 */
	@Override
	public abstract int hashCode();
}
