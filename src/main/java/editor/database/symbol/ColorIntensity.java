package editor.database.symbol;

import editor.database.attributes.ManaType;

/**
 * This class represents a color intensity for a Symbol.  Only mana Symbols will have nonzero intensities
 * for any type of mana.  For any given mana Symbol, the sum of all of its color intensities should be
 * 1.
 *
 * @param color color to record intensity of
 * @param intensity intensity of the color
 * 
 * @author Alec Roelke
 */
public class ColorIntensity//(ManaType color, double intensity)
{
    public final ManaType color;
    public final double intensity;

    public ColorIntensity(ManaType c, double i)
    {
        color = c;
        intensity = i;
    }

    public ManaType color()
    {
        return color;
    }

    public double intensity()
    {
        return intensity;
    }
}
