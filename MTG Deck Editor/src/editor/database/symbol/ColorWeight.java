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
	public final ManaType color;
	public final double weight;
	
	public ColorWeight(ManaType c, double w)
	{
		color = c;
		weight = w;
	}
}
