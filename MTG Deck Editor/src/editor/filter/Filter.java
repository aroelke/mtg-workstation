package editor.filter;

import java.util.function.Predicate;

import editor.database.Card;


/**
 * TODO: Comment this class
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

	protected FilterGroup parent;
	
	public Filter()
	{
		parent = null;
	}
	
	public abstract String representation();
	
	public abstract void parse(String s);
	
	public void group()
	{
		FilterGroup group = new FilterGroup();
		group.parent = parent;
		group.addChild(this);
	}
	
	@Override
	public String toString()
	{
		return BEGIN_GROUP + representation() + END_GROUP;
	}
}
