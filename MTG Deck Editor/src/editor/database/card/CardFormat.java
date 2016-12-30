package editor.database.card;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

import editor.collection.CardList;
import editor.collection.category.CategorySpec;
import editor.database.characteristics.CardData;

public class CardFormat
{
	private static class FakeEntry implements CardList.Entry
	{
		private final Card card;
		
		public FakeEntry(Card c)
		{
			card = c;
		}

		@Override
		public Card card()
		{
			return card;
		}

		@Override
		public Set<CategorySpec> categories()
		{
			return Collections.emptySet();
		}

		@Override
		public int count()
		{
			return 1;
		}

		@Override
		public Date dateAdded()
		{
			return card.expansion().releaseDate;
		}
		
	}
	
	private String format;
	
	public CardFormat(String pattern)
	{
		format = pattern;
	}
	
	public String format(Card card)
	{
		FakeEntry temp = new FakeEntry(card);
		return format(temp);
	}
	
	public String format(CardList.Entry card)
	{
		String pattern = format;
		for (CardData type: CardData.values())
			pattern = pattern.replace('{' + type.toString().toLowerCase() + '}', String.valueOf(card.get(type)));
		return pattern;
	}
}
