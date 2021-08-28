package editor.database.attributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * This class represents an expansion set of Magic: The Gathering cards.
 *
 * @param name name of the expansion
 * @param block name of the block the expansion belongs to ({@link NO_BLOCK} if there is none)
 * @param code the expansion's code
 * @param count number of cards in the expansion
 * @param released date the expansion was released
 * 
 * @author Alec Roelke
 * 
 * ~~RECORD~~
 */
public class Expansion/*(String name, String block, String code, int count, LocalDate released)*/ implements Comparable<Expansion>
{
    /**
     * Array containing all block names.
     */
    public static String[] blocks = {};
    /**
     * Text to show when an expansion isn't part of a block.
     */
    public static final String NO_BLOCK = "<No Block>";
    /**
     * Date format used to decode expansion release dates.
     */
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /**
     * Array containing all expansion names.
     */
    public static Expansion[] expansions = {};

    private final String name;
    private final String block;
    private final String code;
    private final int count;
    private final LocalDate released;

    public Expansion(String n, String b, String c, int x, LocalDate r)
    {
        name = n;
        block = b;
        code = c;
        count = x;
        released = r;
    }

    public String name()
    {
        return name;
    }

    public String block()
    {
        return block;
    }

    public String code()
    {
        return code;
    }

    public int count()
    {
        return count;
    }

    public LocalDate released()
    {
        return released;
    }

    @Override
    public int compareTo(Expansion other)
    {
        return name.compareTo(other.name);
    }

    /**
     * @return A String representation of this Expansion.
     */
    @Override
    public String toString()
    {
        return name;
    }
}
