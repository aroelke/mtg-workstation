package editor.database.attributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * This class represents an expansion set of Magic: The Gathering cards.
 *
 * @author Alec
 */
public class Expansion implements Comparable<Expansion>
{
    /**
     * Array containing all block names.
     */
    public static String[] blocks = {};
    /**
     * Date format used to decode expansion release dates.
     */
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /**
     * Array containing all expansion names.
     */
    public static Expansion[] expansions = {};

    /**
     * Name of the block this Expansion belongs to (empty if there is none).
     */
    public final String block;
    /**
     * This Expansion's code.
     */
    public final String code;
    /**
     * Number of cards in this Expansion.
     */
    public final int count;
    /**
     * This Expansion's name.
     */
    public final String name;
    /**
     * The date the expansion was released.
     */
    public final LocalDate releaseDate;

    /**
     * Create a new Expansion.
     *
     * @param name name of the new expansion
     * @param block name of the block the new Expansion belongs to
     * @param code code of the new Expansion
     * @param count number of cards in the new Expansion
     */
    public Expansion(String name, String block, String code, int count, LocalDate date)
    {
        this.name = name;
        this.block = block;
        this.code = code;
        this.count = count;
        this.releaseDate = date;
    }

    @Override
    public int compareTo(Expansion other)
    {
        return name.compareTo(other.name);
    }

    /**
     * {@inheritDoc}
     * Expansions are equal if they have the same name.
     */
    @Override
    public boolean equals(Object other)
    {
        if (other == null)
            return false;
        if (other.getClass() != getClass())
            return false;
        if (other == this)
            return true;

        Expansion o = (Expansion)other;
        return name.equals(o.name);
    }

    @Override
    public int hashCode()
    {
        return name.hashCode();
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
