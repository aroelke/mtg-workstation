package editor.database.symbol;

import editor.database.characteristics.ManaType;

/**
 * This class represents a color weighting for a Symbol.  Only mana
 * Symbols will have nonzero weights for any type of mana.  For any
 * given mana Symbol, the sum of all of its color weights should be
 * 1.
 * 
 * This class is simply a data structure that holds a ManaType and
 * its weight for a symbol to make it easier to populate the
 * Symbol's weight map.
 * 
 * @author Alec Roelke
 */
public class ColorWeight
{
	/**
	 * Color for the weight.
	 */
	public final ManaType color;
	/**
	 * The weight of the color.
	 */
	public final double weight;
	
	/**
	 * Create a new ColorWeight.
	 * 
	 * @param c Color of the new ColorWeight
	 * @param w Weight of the new ColorWeight
	 */
	public ColorWeight(ManaType c, double w)
	{
		color = c;
		weight = w;
	}
}
