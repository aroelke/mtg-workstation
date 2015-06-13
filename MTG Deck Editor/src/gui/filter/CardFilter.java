package gui.filter;

import java.util.function.Predicate;

import database.Card;

/**
 * This class represents a filter for filtering lists of Cards.  It is
 * a Predicate<Card> with an extra tag for the String representation
 * of the filter.
 * 
 * TODO: Finish commenting this
 * 
 * @author Alec Roelke
 */
public class CardFilter implements Predicate<Card>
{
	/**
	 * Predicate<Card> to filter cards by.
	 */
	private Predicate<Card> filter;
	/**
	 * 
	 */
	private String repr;
	
	public CardFilter(Predicate<Card> f, String r)
	{
		filter = f;
		repr = r;
	}
	
	@Override
	public boolean test(Card c)
	{
		return filter.test(c);
	}
	
	public String repr()
	{
		return repr;
	}
	
	public CardFilter and(CardFilter other)
	{
		return new CardFilter(filter.and(other.filter), "<" + filter + " AND " + other.filter + ">");
	}
}
