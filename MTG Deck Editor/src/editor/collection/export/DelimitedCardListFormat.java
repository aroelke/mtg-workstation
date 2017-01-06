package editor.collection.export;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import editor.collection.CardList;
import editor.database.card.Card;
import editor.database.characteristics.CardData;

public class DelimitedCardListFormat implements CardListFormat
{
	public static final String DEFAULT_DELIMITER = ",";
	public static final List<CardData> DEFAULT_DATA = Arrays.asList(CardData.NAME,
			CardData.EXPANSION_NAME,
			CardData.COUNT);
	public static final String DEFAULT_ESCAPE = "\"";
	
	private String delimiter;
	private String escape;
	private boolean include;
	private List<CardData> types;
	
	public DelimitedCardListFormat(String delim, List<CardData> data, String esc, boolean headers)
	{
		delimiter = delim;
		types = data;
		escape = esc;
		include = headers;
	}
	
	public DelimitedCardListFormat()
	{
		this(DEFAULT_DELIMITER, DEFAULT_DATA, DEFAULT_ESCAPE, true);
	}
	
	@Override
	public String format(CardList list)
	{
		StringJoiner join = new StringJoiner(System.lineSeparator());
		if (include)
			join.add(String.join(delimiter, types.stream().map(CardData::toString).collect(Collectors.toList())));
		for (Card card: list)
		{
			StringJoiner line = new StringJoiner(delimiter);
			for (CardData type: types)
			{
				String value = String.valueOf(list.getData(card).get(type));
				if (value.contains(delimiter))
					value = escape + value.replace(escape, escape + escape) + escape;
				line.add(value);
			}
			join.add(line.toString());
		}
		return join.toString();
	}
	
	@Override
	public CardList parse(String source)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
}
