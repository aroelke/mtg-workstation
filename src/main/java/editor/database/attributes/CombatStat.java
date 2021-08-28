package editor.database.attributes;

import editor.util.UnicodeSymbols;

/**
 * This class represents a power value or a toughness value.
 *
 * @param expression string expression showing the power or toughness value (may contain *)
 * @param value numeric value of the power or toughness for sorting
 * 
 * @author Alec Roelke
 * 
 * ~~RECORD~~
 */
public class CombatStat/*(String expression, double value)*/ implements OptionalAttribute, Comparable<CombatStat>
{
    /**
     * Representation for a combat stat that doesn't exist.
     */
    public static final CombatStat NO_COMBAT = new CombatStat(Double.NaN);

    private final String expression;
    private final double value;

    public CombatStat(String e, double v)
    {
        expression = e;
        value = v;
    }

    /**
     * Create a new PowerToughness from a number.
     *
     * @param v Numeric value of the CombatStat
     */
    public CombatStat(double v)
    {
        this(Double.isNaN(v) ? "" : String.valueOf(v), v);
    }

    public String expression()
    {
        return expression;
    }

    public double value()
    {
        return value;
    }

    /**
     * Create a new PowerToughness from an expression.
     *
     * @param e expression for the new CombatStat
     */
    public CombatStat(String e)
    {
        this(e.replaceAll("\\s+", ""), switch (e) {
            case "" -> Double.NaN;
            default -> {
                e = e.replaceAll("[*?" + UnicodeSymbols.SUPERSCRIPT_TWO + "]+", "").replaceAll("[+-]$", "").replace(String.valueOf(UnicodeSymbols.ONE_HALF), ".5");
                if (e.isEmpty())
                    yield 0;
                else if (e.equals(String.valueOf(UnicodeSymbols.INFINITY)))
                    yield Double.POSITIVE_INFINITY;
                else
                    yield Double.valueOf(e);
            }
        });
    }

    @Override
    public int compareTo(CombatStat o)
    {
        if (!exists() && !o.exists())
            return 0;
        else if (!exists())
            return 1;
        else if (!o.exists())
            return -1;
        else
            return (int)(2*value - 2*o.value);
    }

    @Override
    public boolean exists()
    {
        return !Double.isNaN(value);
    }

    /**
     * {@inheritDoc}
     * If this CombatStat doesn't exist according to {@link #exists()}, this is
     * an empty String.
     */
    @Override
    public String toString()
    {
        return expression;
    }

    /**
     * If this CombatStat's expression contains a *, it is variable.  Otherwise,
     * it is not.
     *
     * @return true if this CombatStat is variable, and false otherwise
     */
    public boolean variable()
    {
        return expression.contains("*");
    }
}
