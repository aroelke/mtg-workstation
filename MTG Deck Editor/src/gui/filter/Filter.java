package gui.filter;

import java.util.function.Predicate;

import database.Card;

public class Filter implements Predicate<Card>
{
	@Override
	public boolean test(Card arg0)
	{
		return false;
	}
}
