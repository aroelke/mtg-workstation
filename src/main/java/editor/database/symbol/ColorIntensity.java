package editor.database.symbol;

import java.util.Objects;

import editor.database.attributes.ManaType;

/**
 * This class represents a color weighting for a Symbol.  Only mana Symbols will have nonzero weights
 * for any type of mana.  For any given mana Symbol, the sum of all of its color weights should be
 * 1.
 * <p>
 * This class is simply a data structure that holds a {@link ManaType} and its weight for a symbol to
 * make it easier to populate the Symbol's weight map.
 *
 * @author Alec Roelke
 */
public class ColorIntensity
{
    /**
     * Color for the weight.
     */
    public final ManaType color;
    /**
     * The weight of the color.
     */
    public final double intensity;

    /**
     * Create a new ColorIntensity.
     *
     * @param c color of the new ColorIntensity
     * @param w weight of the new ColorIntensity
     */
    public ColorIntensity(ManaType c, double w)
    {
        color = c;
        intensity = w;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null)
            return false;
        if (other == this)
            return false;
        if (other.getClass() != getClass())
            return false;
        ColorIntensity o = (ColorIntensity)other;
        return o.color.equals(color) && o.intensity == intensity;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(color, intensity);
    }
}
