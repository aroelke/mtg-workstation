package editor.database.attributes;

import editor.database.card.Card;
import editor.util.Parsers;

/**
 * This class represents the starting loyalty of a planeswalker.  All {@link Card}s have them, but they
 * are invisible for non-planeswalkers. A value of {@link Double#NaN} means there is no loyalty, -1 means
 * X loyalty (set by something else such as X in mana cost), and -2 means variable loyalty (*).
 *
 * @param value numerical value of the starting loyalty
 * 
 * @author Alec Roelke
 */
public record Loyalty(double value) implements OptionalAttribute, Comparable<Loyalty>
{
    /**
     * Constant for an arbitrary card with no loyalty.
     */
    public static final Loyalty NO_LOYALTY = new Loyalty(Double.NaN);

    private static final double X = -1;
    private static final double STAR = -2;

    /**
     * Create a new Loyalty with the given value. Loyalty creatd this way can't vary.
     *
     * @param v value of the new Loyalty
     */
    public Loyalty(int v)
    {
        this((double)Math.max(0, v));
    }

    /**
     * Parse a String for a loyalty value.  It can either be a number, "X," or "*,"
     * and if anything else is used it is considered to not exist.
     *
     * @param s
     */
    public Loyalty(String s)
    {
        this (switch (s.toUpperCase()) {
            case "X" -> X;
            case "*" -> STAR;
            case ""  -> Double.NaN;
            default  -> Parsers.tryParseDouble(s).orElse(Double.NaN);
        });
    }

    @Override
    public int compareTo(Loyalty other)
    {
        return Double.compare(value, other.value);
    }

    @Override
    public boolean exists()
    {
        return !Double.isNaN(value);
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
     * @return <code>true</code> if this Loyalty is variable (X, *), and <code>false</code> otherwise.
     */
    public boolean variable()
    {
        return value < 0;
    }
}
