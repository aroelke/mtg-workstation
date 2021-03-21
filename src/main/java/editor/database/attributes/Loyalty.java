package editor.database.attributes;

import java.util.Objects;

import editor.database.card.Card;

/**
 * This class represents the starting loyalty of a planeswalker.  All {@link Card}s have them, but they
 * are invisible for non-planeswalkers.
 *
 * @author Alec Roelke
 */
public class Loyalty implements OptionalAttribute, Comparable<Loyalty>
{
    /**
     * Constant for an arbitrary card with no loyalty.
     */
    public static final Loyalty NO_LOYALTY = new Loyalty(0);

    private static final double X = -1;
    private static final double STAR = -2;

    /**
     * Numerical value of the starting loyalty.  Zero means there is no loyalty and
     * -1 means it's X.
     */
    public final double value;

    /**
     * Create a new Loyalty with the given value. Loyalty creatd this way can't vary.
     *
     * @param v value of the new Loyalty
     */
    public Loyalty(int v)
    {
        value = Math.max(0, v);
    }

    /**
     * Parse a String for a loyalty value.  It can either be a number, "X," or "*,"
     * and if anything else is used it is considered to not exist.
     *
     * @param s
     */
    public Loyalty(String s)
    {
        double v;
        try
        {
            v = Integer.valueOf(s);
        }
        catch (NumberFormatException x)
        {
            v = switch (s.toUpperCase()) {
                case "X" -> X;
                case "*" -> STAR;
                default  -> Double.NaN;
            };
        }
        value = v;
    }

    @Override
    public int compareTo(Loyalty other)
    {
        return Double.compare(value, other.value);
    }

    @Override
    public boolean equals(Object other)
    {
        return other != null && (other == this || other instanceof Loyalty && value == ((Loyalty)other).value);
    }

    @Override
    public boolean exists()
    {
        return !Double.isNaN(value);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(value);
    }

    @Override
    public String toString()
    {
        if (value == X)
            return "X";
        else if (value == STAR)
            return "*";
        else if (Double.isNaN(value))
            return "";
        else
            return Integer.toString((int)value);
    }

    /**
     * @return <code>true</code> if this Loyalty is variable (X, *),
     * and <code>false</code> otherwise.
     */
    public boolean variable()
    {
        return value < 0;
    }
}
