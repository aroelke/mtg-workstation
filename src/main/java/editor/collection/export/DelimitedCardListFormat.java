package editor.collection.export;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import editor.collection.CardList;
import editor.collection.deck.Deck;
import editor.database.attributes.CardAttribute;
import editor.database.card.Card;
import editor.database.card.CardFormat;
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
    public static final List<CardAttribute> DEFAULT_DATA = List.of(CardAttribute.NAME,
                                                                   CardAttribute.EXPANSION,
                                                                   CardAttribute.COUNT);
    /**
     * List of suggested delimiters.
     */
    public static final String[] DELIMITERS = new String[]{",", ";", ":", "{tab}", "{space}"};
    /**
     * String to surround cells that contain the delimiter with.
     */
    public static final String ESCAPE = "\"";

    /**
     * Split a string along a delimiter, but only if that delimiter is not inside {@value #ESCAPE}.
     * If a split value is surrounded by {@value #ESCAPE}, remove them.
     *
     * @param delimiter delimiter to split with
     * @param line string to split
     * @return an array containing the split string with delimiters and surrounding {@value #ESCAPE} removed
     */
    public static String[] split(String delimiter, String line)
    {
        String[] cells = line.split(delimiter + "(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (int i = 0; i < cells.length; i++)
            if (cells[i].substring(0, ESCAPE.length()).equals(ESCAPE) || cells[i].substring(cells[i].length() - ESCAPE.length()).equals(ESCAPE))
                cells[i] = cells[i].substring(1, cells[i].length() - 1);
        return cells;
    }

    /**
     * Delimiter to separate table cells.
     */
    private String delimiter;
    /**
     * Whether or not to include column headers.  Parsing will require
     * column headers.
     */
    private boolean include;
    /**
     * Data types to include in the table.
     */
    private List<CardAttribute> types;

    /**
     * Create a new way to format a card list into a table.
     *
     * @param delim delimiter for the table cells
     * @param data data types to use for the columns
     * @param headers whether or not to include column headers
     */
    public DelimitedCardListFormat(String delim, List<CardAttribute> data, boolean headers)
    {
        delimiter = delim;
        types = data;
        include = headers;

        if ("{space}".equals(delimiter))
            delimiter = " ";
        else if ("{tab}".equals(delimiter))
            delimiter = "\t";
    }

    @Override
    public String format(CardList list)
    {
        StringJoiner join = new StringJoiner(System.lineSeparator());
        var columnFormats = types.stream().map((t) -> new CardFormat('{' + t.toString().toLowerCase() + '}')).collect(Collectors.toList());
        for (Card card : list)
        {
            StringJoiner line = new StringJoiner(delimiter);
            for (CardFormat format : columnFormats)
            {
                String value = format.format(list.getEntry(card));
                if (value.contains(delimiter))
                    value = ESCAPE + value.replace(ESCAPE, ESCAPE + ESCAPE) + ESCAPE;
                line.add(value);
            }
            join.add(line.toString());
        }
        return join.toString();
    }

    @Override
    public String header()
    {
        if (include)
            return String.join(delimiter, types.stream().map(CardAttribute::toString).toArray(String[]::new));
        else
            return "";
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if card name isn't a column
     */
    @Override
    public CardList parse(String source) throws ParseException, IllegalStateException
    {
        var lines = Arrays.stream(source.split(System.lineSeparator())).collect(Collectors.toList());

        int pos = 0;
        if (!include)
        {
            String[] headers = lines.get(0).split(delimiter);
            types = new ArrayList<>(headers.length);
            for (String header : headers)
            {
                boolean success = false;
                for (CardAttribute type : CardAttribute.displayableValues())
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

        int nameIndex = types.indexOf(CardAttribute.NAME);
        if (nameIndex < 0)
            throw new IllegalStateException("can't parse cards without names");
        int expansionIndex = types.indexOf(CardAttribute.EXPANSION);
        int numberIndex = types.indexOf(CardAttribute.CARD_NUMBER);
        int countIndex = types.indexOf(CardAttribute.COUNT);
        if (countIndex < 0)
            System.err.println("warning: missing card count in parse; assuming one copy of each card");
        int dateIndex = types.indexOf(CardAttribute.DATE_ADDED);

        Deck deck = new Deck();
        for (String line : lines)
        {
            line = line.replace(ESCAPE + ESCAPE, ESCAPE);
            String[] cells = split(delimiter, line);

            var possibilities = MainFrame.inventory().stream().filter((c) -> c.unifiedName().equalsIgnoreCase(cells[nameIndex])).collect(Collectors.toList());
            if (possibilities.size() > 1 && expansionIndex > -1)
                possibilities.removeIf((c) -> !c.expansion().name.equalsIgnoreCase(cells[expansionIndex]));
            if (possibilities.size() > 1 && numberIndex > -1)
                possibilities.removeIf((c) -> !String.join(' ' + Card.FACE_SEPARATOR + ' ', c.number()).equals(cells[numberIndex]));

            if (possibilities.size() > 1)
                System.err.println("warning: cannot determine printing of " + possibilities.get(0).unifiedName());
            if (possibilities.isEmpty())
                throw new ParseException("can't find card named " + cells[nameIndex], pos);
            deck.add(possibilities.get(0), countIndex < 0 ? 1 : Integer.parseInt(cells[countIndex]), dateIndex < 0 ? LocalDate.now() : LocalDate.parse(cells[dateIndex], Deck.DATE_FORMATTER));
            pos += line.length();
        }

        return deck;
    }

}
