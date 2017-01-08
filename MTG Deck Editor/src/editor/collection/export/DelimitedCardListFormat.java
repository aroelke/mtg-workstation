package editor.collection.export;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import editor.collection.CardList;
import editor.database.card.Card;
import editor.database.card.CardFormat;
import editor.database.characteristics.CardData;

/**
 * This class represents a formatter that creates a table whose columns
 * are card characteristics and whose rows contain the values of those
 * characteristics for the corresponding cards.
 * 
 * @author Alec Roelke
 */
public class DelimitedCardListFormat implements CardListFormat
{
	/**
	 * Default delimiter between table cells.  It is a comma.
	 */
	public static final String DEFAULT_DELIMITER = ",";
	/**
	 * Default data to present in the table.  By default, only a card's name,
	 * expansion, and copy count will be used.
	 */
	public static final List<CardData> DEFAULT_DATA = Arrays.asList(CardData.NAME,
			CardData.EXPANSION_NAME,
			CardData.COUNT);
	/**
	 * When a cell value contains the delimiter, it should indicate where the
	 * value of the cell begins and ends.  By default, such cells are surrounded
	 * by double quotes.
	 */
	public static final String DEFAULT_ESCAPE = "\"";
	
	/**
	 * Delimeter to separate table cells.
	 */
	private String delimiter;
	/**
	 * String to surround cells that contain the delimiter with.
	 */
	private String escape;
	/**
	 * Whether or not to include column headers.  Parsing will require
	 * column headers.
	 */
	private boolean include;
	/**
	 * Data types to include in the table.
	 */
	private List<CardData> types;
	
	/**
	 * Create a new way to format a card list into a table.
	 * 
	 * @param delim delimiter for the table cells
	 * @param data data types to use for the columns
	 * @param esc sequence to enclose values that contain the delimiter
	 * @param headers whether or not to include column headers
	 */
	public DelimitedCardListFormat(String delim, List<CardData> data, String esc, boolean headers)
	{
		delimiter = delim;
		types = data;
		escape = esc;
		include = headers;
	}
	
	/**
	 * Create a new DelimitedCardListFormat using the default values and
	 * including headers.
	 */
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
		List<CardFormat> columnFormats = types.stream().map((t) -> new CardFormat('{' + t.toString().toLowerCase() + '}')).collect(Collectors.toList());
		for (Card card: list)
		{
			StringJoiner line = new StringJoiner(delimiter);
			for (CardFormat format: columnFormats)
			{
				String value = format.format(card);
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
