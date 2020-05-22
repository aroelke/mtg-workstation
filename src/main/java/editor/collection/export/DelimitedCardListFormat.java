package editor.collection.export;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import editor.collection.CardList;
import editor.collection.deck.Deck;
import editor.database.attributes.CardAttribute;
import editor.database.card.Card;
import editor.database.card.CardFormat;
import editor.gui.MainFrame;
import editor.gui.editor.DeckSerializer;

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
     * Storage structure for the column indices of important card attributes used for identifying
     * cards in a table.  Some of the indices allow for the use of -1 to indicate that that
     * piece of information is not present in the table.
     * 
     * @author Alec Roelke
     */
    private static class Indices
    {
        /** Column index where cards' names can be found. Must be present. */
        public final int name;
        /** Column index where cards' expansions can be found. */
        public final int expansion;
        /** Column index where cards' collector numbers can be found. */
        public final int number;
        /** Column index where cards' counts can be found. */
        public final int count;
        /** Column index where cards' dates added can be found. */
        public final int date;

        /**
         * Create a new set of indices. Throw an exception if the index for cards'
         * names is unknown (-1) and warn if the one for cards' counts is unknown.
         * 
         * @param n index for names
         * @param e index for expansions
         * @param m index for collector numbers
         * @param c index for counts
         * @param d index for dates
         */
        public Indices(int n, int e, int m, int c, int d)
        {
            name = n;
            expansion = e;
            number = m;
            count = c;
            date = d;

            if (name < 0)
                throw new IllegalStateException("can't parse cards without names");
            if (count < 0)
                System.err.println("warning: missing card count in parse; assuming one copy of each card");
        }
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
     * Current position in the text.
     */
    private int pos;
    /**
     * Indices where identifying information can be found.
     */
    private Indices indices;

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
     * Parse a delimited line to find which headers in the table contain which
     * information.
     * 
     * @param line line to parse
     * @throws ParseException if the line can't be parsed
     */
    private void parseHeader(String line) throws ParseException
    {
        if (include)
            throw new IllegalStateException("Headers are already defined");
        else
        {
            if (!include)
            {
                String[] headers = line.split(delimiter);
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
                }
                indices = new Indices(
                    types.indexOf(CardAttribute.NAME),
                    types.indexOf(CardAttribute.EXPANSION),
                    types.indexOf(CardAttribute.CARD_NUMBER),
                    types.indexOf(CardAttribute.COUNT),
                    types.indexOf(CardAttribute.DATE_ADDED)
                );
            }
        }
    }

    /**
     * Attempt to identify a card from a line of delimited text.
     * 
     * @param deck deck to add the parsed card to
     * @param line line to parse
     * @throws ParseException if the line can't be parsed
     */
    private void parseLine(Deck deck, String line) throws ParseException
    {
        line = line.replace(ESCAPE + ESCAPE, ESCAPE);
        String[] cells = split(delimiter, line);

        var possibilities = MainFrame.inventory().stream().filter((c) -> c.unifiedName().equalsIgnoreCase(cells[indices.name])).collect(Collectors.toList());
        if (possibilities.size() > 1 && indices.expansion > -1)
            possibilities.removeIf((c) -> !c.expansion().name.equalsIgnoreCase(cells[indices.expansion]));
        if (possibilities.size() > 1 && indices.number > -1)
            possibilities.removeIf((c) -> !String.join(Card.FACE_SEPARATOR, c.number()).equals(cells[indices.number]));

        if (possibilities.size() > 1)
            System.err.println("warning: cannot determine printing of " + possibilities.get(0).unifiedName());
        if (possibilities.isEmpty())
            throw new ParseException("can't find card named " + cells[indices.name], pos);
        deck.add(possibilities.get(0), indices.count < 0 ? 1 : Integer.parseInt(cells[indices.count]), indices.date < 0 ? LocalDate.now() : LocalDate.parse(cells[indices.date], Deck.DATE_FORMATTER));
    }

    @Override
    public DeckSerializer parse(InputStream source) throws ParseException, IOException
    {
        Deck deck = new Deck();
        Optional<String> extra = Optional.empty();
        var extras = new LinkedHashMap<String, Deck>();
        pos = 0;
        int c;
        StringBuilder line = new StringBuilder(128);
        boolean headed = false;
        do
        {
            c = source.read();
            if (c >= 0 && c != '\r' && c != '\n')
                line.append((char)c);
            if (c == '\n' || c < 0)
            {
                if (!headed && !include)
                {
                    parseHeader(line.toString());
                    headed = true;
                }
                else
                {
                    try
                    {
                        parseLine(extra.map(extras::get).orElse(deck), line.toString());
                    }
                    catch (ParseException e)
                    {
                        extra = Optional.of(line.toString());
                        extras.put(extra.get(), new Deck());
                    }
                }
                line.setLength(0);
            }
            pos++;
        } while (c >= 0);
        return new DeckSerializer(deck, extras, "");
    }
}
