package editor.database.symbol

import editor.database.attributes.ManaType

/**
 * An intensity for a mana type in a [[editor.database.symbol.Symbol]] or [[editor.database.attributes.ManaCost]].
 * 
 * @constructor create a new color intensity for a [[editor.database.attributes.ManaType]]
 * @param color type of mana
 * @param intensity intensity of that mana type
 * @note For any given mana [[editor.database.symbol.Symbol]], the sum of all of its components' intensities should be 1.
 * 
 * @author Alec Roelke
 */
case class ColorIntensity(color: ManaType, intensity: Double)