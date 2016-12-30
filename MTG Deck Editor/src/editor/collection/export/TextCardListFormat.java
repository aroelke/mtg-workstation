package editor.collection.export;

import java.util.stream.Collectors;

import editor.collection.CardList;
import editor.database.card.CardFormat;

public class TextCardListFormat implements CardListFormat
{
	public static final String DEFAULT_FORMAT = "{count}x {name} ({expansion})";
	
	private CardFormat format;
	
	public TextCardListFormat(String pattern)
	{
		format = new CardFormat(pattern);
	}
	
	public TextCardListFormat()
	{
		this(DEFAULT_FORMAT);
	}
	
	@Override
	public String format(CardList list)
	{
		return String.join(System.lineSeparator(),
				list.stream().map((c) -> format.format(list.getData(c))).collect(Collectors.toList()));
	}
	
	@Override
	public CardList parse(String source)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
}
