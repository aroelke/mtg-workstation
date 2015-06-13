package gui.filter;

import java.util.function.Predicate;

import database.Card;

public class CardFilter implements Predicate<Card>
{
	private Predicate<Card> filter;
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
}
