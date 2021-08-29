package editor.gui.settings

import java.awt.Color

/**
 * Settings structure containing information about analyzing opening hands.
 * 
 * @constructor create a new hand analysis structure
 * @param size starting hand size
 * @param rounding how to round probabilities
 * @param background background color for visual hand display
 * 
 * @author Alec Roelke
 */
case class HandSettings(size: Int = 7, rounding: String = "No Rounding", background: Color = Color.WHITE) {
  def this() = this(7, "No Rounding", Color.WHITE)
}