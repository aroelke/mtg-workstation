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
 */
public record Expansion(String name, String block, String code, int count, LocalDate released) implements Comparable<Expansion>
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
