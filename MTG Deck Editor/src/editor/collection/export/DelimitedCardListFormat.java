package editor.collection.export;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import editor.collection.CardList;
import editor.collection.deck.Deck;
import editor.database.card.Card;
import editor.database.card.CardFormat;
import editor.database.characteristics.CardData;
import editor.gui.MainFrame;

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
	 * @param headers whether or not to include column headers
	 */
	public DelimitedCardListFormat(String delim, List<CardData> data, boolean headers)
	{
		delimiter = delim;
		types = data;
		escape = "\"";
		include = headers;
	}
	
	/**
	 * Create a new DelimitedCardListFormat using the default values and
	 * including headers.
	 */
	public DelimitedCardListFormat()
	{
		this(DEFAULT_DELIMITER, DEFAULT_DATA, true);
	}
	
	/**
	 * Clean a cell that is surrounded by escape characters so that it is no
	 * longer surrounded by them.
	 * 
	 * @param cell string to clean
	 * @return a string that is not surrounded by escape characters
	 */
	private String cleanEscape(String cell)
	{
		if (cell.substring(0, escape.length()).equals(escape) || cell.substring(cell.length() - escape.length()).equals(escape))
			return cell.substring(1, cell.length() - 1);
		return cell;
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
				String value = format.format(list.getData(card));
				if (value.contains(delimiter))
					value = escape + value.replace(escape, escape + escape) + escape;
				line.add(value);
			}
			join.add(line.toString());
		}
		return join.toString();
	}
	
	/**
	 * {@inheritDoc}
	 * @throws IllegalStateException if card name isn't a column
	 */
	@Override
	public CardList parse(String source) throws ParseException, IllegalStateException
	{
		List<String> lines = Arrays.stream(source.split(System.lineSeparator())).collect(Collectors.toList());
		
		int pos = 0;
		if (!include)
		{
			String[] headers = lines.get(0).split(delimiter);
			types = new ArrayList<CardData>(headers.length);
			for (String header: headers)
			{
				boolean success = false;
				for (CardData type: CardData.values())
				{
					if (header.compareToIgnoreCase(type.toString()) == 0)
					{
						types.add(type);
						success = true;
						break;
					}
				}
				if (!success)
					throw new ParseException("unknown data type " + header, pos);
				pos += header.length();
			}
			lines.remove(0);
		}
		
		int nameIndex = types.indexOf(CardData.NAME);
		if (nameIndex < 0)
			throw new IllegalStateException("can't parse cards without names");
		int expansionIndex = types.indexOf(CardData.EXPANSION_NAME);
		int numberIndex = types.indexOf(CardData.CARD_NUMBER);
		int countIndex = types.indexOf(CardData.COUNT);
		if (countIndex < 0)
			System.err.println("warning: missing card count in parse; assuming one copy of each card");
		int dateIndex = types.indexOf(CardData.DATE_ADDED);
		
		Deck deck = new Deck();
		for (String line: lines)
		{
			line = line.replace(escape + escape, escape);
			String[] cells = line.split(delimiter + "(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
			
			List<Card> possibilities = MainFrame.inventory().stream().filter((c) -> c.unifiedName().equalsIgnoreCase(cleanEscape(cells[nameIndex]))).collect(Collectors.toList());
			if (possibilities.size() > 1 && expansionIndex > -1)
				possibilities.removeIf((c) -> !c.expansion().name.equalsIgnoreCase(cleanEscape(cells[expansionIndex])));
			if (possibilities.size() > 1 && numberIndex > -1)
				possibilities.removeIf((c) -> !String.join(' ' + Card.FACE_SEPARATOR + ' ', c.number()).equals(cleanEscape(cells[numberIndex])));
			
			if (possibilities.size() > 1)
				System.err.println("warning: cannot determine printing of " + possibilities.get(0).unifiedName());
			if (possibilities.isEmpty())
				throw new ParseException("can't find card named " + cells[nameIndex], pos);
			deck.add(possibilities.get(0), countIndex < 0 ? 1 : Integer.parseInt(cells[countIndex]), dateIndex < 0 ? new Date() : Deck.DATE_FORMAT.parse(cleanEscape(cells[dateIndex])));
			pos += line.length();
		}
		
		return deck;
	}
	
}
