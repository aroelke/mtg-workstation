package editor.database.attributes;

import java.util.Objects;

import editor.database.card.Card;

/**
 * This class represents the starting loyalty of a planeswalker.  All {@link Card}s have them, but they
 * are invisible for non-planeswalkers.
 *
 * @author Alec Roelke
 */
public class Loyalty implements Comparable<Loyalty>
{
    /**
     * Constant for an arbitrary card with no loyalty.
     */
    public static final Loyalty NO_LOYALTY = new Loyalty(0);

    /**
     * Numerical value of the starting loyalty.  Zero means there is no loyalty and
     * -1 means it's X.
     */
    public final int value;

    /**
     * Create a new Loyalty with the given value.
     *
     * @param v value of the new Loyalty
     */
    public Loyalty(int v)
    {
        value = Math.max(-1, v);
    }

    /**
     * Parse a String for a loyalty value.  It can either be a number or "X," and if
     * anything else is used it is considered to not exist.
     *
     * @param s
     */
    public Loyalty(String s)
    {
        int v;
        try
        {
            v = Integer.valueOf(s);
        }
        catch (NumberFormatException x)
        {
            v = s.compareToIgnoreCase("X") == 0 ? -1 : 0;
        }
        value = v;
    }

    @Override
    public int compareTo(Loyalty other)
    {
        return Integer.compare(value, other.value);
    }

    @Override
    public boolean equals(Object other)
    {
        return other != null && (other == this || other instanceof Loyalty && value == ((Loyalty)other).value);
    }

    /**
     * @return <code>true</code> if this Loyalty exists (is nonzero),
     * and <code>false</code> otherwise.
     */
    public boolean exists()
    {
        return value != 0;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(value);
    }

    @Override
    public String toString()
    {
        if (value < 0)
            return "X";
        else if (value == 0)
            return "";
        else return Integer.toString(value);
    }

    /**
     * @return <code>true</code> if this Loyalty is variable (X),
     * and <code>false</code> otherwise.
     */
    public boolean variable()
    {
        return value < 0;
    }
}
