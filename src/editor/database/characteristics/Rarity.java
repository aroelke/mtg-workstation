package editor.database.characteristics;

import org.apache.commons.codec.binary.Base32;

/**
 * This enum represents a rarity a Magic: The Gathering card can have.  Rarities
 * are basically Strings, so they implement {@link CharSequence}.  All of the
 * implemented methods operate on a Rarity's String representation from
 * {@link #toString()}.
 *
 * @author Alec Roelke
 */
public enum Rarity implements CharSequence
{
    /**
     * Rarity for basic lands.
     */
    BASIC_LAND("Basic Land"),
    /**
     * Common rarity.
     */
    COMMON("Common"),
    /**
     * Uncommon rarity.
     */
    UNCOMMON("Uncommon"),
    /**
     * Rare rarity.
     */
    RARE("Rare"),
    /**
     * Mythic rare rarity.
     */
    MYTHIC_RARE("Mythic Rare"),
    /**
     * "Special" rarity, such as timeshifted.
     */
    SPECIAL("Special");

    /**
     * Create a rarity from a shorthand character.
     *
     * @param rarity Character to create a Rarity from
     * @return a Rarity representing the specified shorthand character
     * @throws IllegalArgumentException if a Rarity cannot be created from the specified character
     */
    public static Rarity parseRarity(char rarity)
    {
        return switch (Character.toLowerCase(rarity)) {
            case 'c' -> COMMON;
            case 'u' -> UNCOMMON;
            case 'r' -> RARE;
            case 'm' -> MYTHIC_RARE;
            case 's' -> SPECIAL;
            case 'b' -> BASIC_LAND;
            default -> throw new IllegalArgumentException("Illegal rarity shorthand");
        };
    }

    /**
     * Create a Rarity from the specified String.
     *
     * @param rarity String to create a Rarity from
     * @return a Rarity representing the specified String
     * @throws IllegalArgumentException if a Rarity cannot be created from the String
     */
    public static Rarity parseRarity(String rarity)
    {
        if (rarity.contains("mythic"))
            return MYTHIC_RARE;
        else if (rarity.contains("rare"))
            return RARE;
        else if (rarity.contains("uncommon"))
            return UNCOMMON;
        else if (rarity.contains("common"))
            return COMMON;
        else if (rarity.contains("basic"))
            return BASIC_LAND;
        else
        {
            System.err.println("warning: Could not determine rarity of \"" + rarity + '"');
            return SPECIAL;
        }
    }

    /**
     * String representation of this Rarity.
     */
    private final String rarity;

    /**
     * Create a new Rarity.
     *
     * @param rarity String representation of the new Rarity.
     */
    Rarity(final String rarity)
    {
        this.rarity = rarity;
    }

    @Override
    public char charAt(int index)
    {
        return rarity.charAt(index);
    }

    @Override
    public int length()
    {
        return rarity.length();
    }

    /**
     * Get the shorthand character for this Rarity.
     *
     * @return A shorthand character representing this Rarity.
     */
    public char shorthand()
    {
        return switch (this) {
            case COMMON      -> 'C';
            case UNCOMMON    -> 'U';
            case RARE        -> 'R';
            case MYTHIC_RARE -> 'M';
            case SPECIAL     -> 'S';
            case BASIC_LAND  -> 'B';
        };
    }

    @Override
    public CharSequence subSequence(int start, int end)
    {
        return rarity.subSequence(start, end);
    }

    @Override
    public String toString()
    {
        return rarity;
    }
}
