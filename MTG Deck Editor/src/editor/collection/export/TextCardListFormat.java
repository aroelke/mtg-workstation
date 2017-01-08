package editor.collection.export;

import java.util.stream.Collectors;

import editor.collection.CardList;
import editor.database.card.CardFormat;

/**
 * This class represents a formatter that formats a card list according to a
 * {@link CardFormat}.
 * 
 * @author Alec Roelke
 */
public class TextCardListFormat implements CardListFormat
{
	/**
	 * Default format for exporting card lists, which is {@value #DEFAULT_FORMAT}.
	 */
	public static final String DEFAULT_FORMAT = "{count}x {name} ({expansion})";
	
	/**
	 * Format to use for formatting a card list
	 */
	private CardFormat format;
	
	/**
	 * Create a new TextCardListFormat.
	 * 
	 * @param pattern pattern to use for formatting a card list
	 */
	public TextCardListFormat(String pattern)
	{
		format = new CardFormat(pattern);
	}
	
	/**
	 * Create a new TextCardListFormat with the default format specifier.
	 */
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
