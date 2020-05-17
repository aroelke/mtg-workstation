package editor.collection.export;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.joestelmach.natty.Parser;

import editor.collection.CardList;
import editor.collection.deck.Deck;
import editor.database.card.CardFormat;
import editor.gui.MainFrame;

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

    public static final Pattern COUNT_PATTERN = Pattern.compile("(?:^(?:\\d+x|x\\d+|\\d+)|(?:\\d+x|x\\d+|\\d+)$)");

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

    @Override
    public String format(CardList list)
    {
        return String.join(System.lineSeparator(),
                list.stream().map((c) -> format.format(list.getEntry(c))).collect(Collectors.toList()));
    }

    @Override
    public String header()
    {
        return "";
    }

    private void parseLine(Deck deck, String line) throws ParseException
    {
        var possibilities = MainFrame.inventory().stream().filter((c) -> line.contains(c.unifiedName().toLowerCase()) || c.name().stream().anyMatch((n) -> line.contains(n.toLowerCase()))).collect(Collectors.toList());
        if (possibilities.isEmpty())
            throw new ParseException("Can't parse card name from \"" + line.trim() + '"', 0);

        var filtered = possibilities.stream().filter((c) -> line.contains(c.expansion().name.toLowerCase())).collect(Collectors.toList());
        if (!filtered.isEmpty())
            possibilities = filtered;
        filtered = possibilities.stream().filter((c) -> !c.unifiedName().toLowerCase().equals(c.expansion().name.toLowerCase())).collect(Collectors.toList());
        if (!filtered.isEmpty())
            possibilities = filtered;

        if (possibilities.size() > 1)
            System.err.println("Multiple matches for \"" + line.trim() + '"');

        Matcher countMatcher = COUNT_PATTERN.matcher(line);
        LocalDate date = new Parser().parse(line).stream().flatMap((g) -> g.getDates().stream()).findFirst().orElse(new Date()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        deck.add(possibilities.get(0), countMatcher.find() ? Integer.parseInt(countMatcher.group().replace("x", "")) : 1, date);
    }

    @Override
    public CardList parse(List<String> source) throws ParseException
    {
        Deck deck = new Deck();
        for (String line : source)
            parseLine(deck, line.trim().toLowerCase());
        return deck;
    }

    @Override
    public CardList parse(InputStream source) throws ParseException, IOException
    {
        Deck deck = new Deck();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(source)))
        {
            String line;
            while ((line = reader.readLine()) != null)
                parseLine(deck, line.trim().toLowerCase());
        }
        return null;
    }
}
