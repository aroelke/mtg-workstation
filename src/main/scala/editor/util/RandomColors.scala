package editor.util

import java.awt.Color
import scala.util.Random

/**
 * Class for generating random colors.
 * 
 * @param r random number generator
 * @author Alec Roelke
 */
class RandomColors(r: Random) {
  /** @return a random color. */
  def nextColor = Color(r.nextFloat, r.nextFloat, math.sqrt(r.nextFloat).toFloat)
}