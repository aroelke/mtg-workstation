package editor.util

import java.text.Normalizer

/**
 * Collection of constants naming various Unicode symbols used in the application.
 * @author Alec Roelke
 */
object UnicodeSymbols {
  val Substitute = '\u001A'
  val SuperscriptTwo = '\u00B2'
  val OneHalf = '\u00BD'
  val Multiply = '\u00D7'
  val AeLower = '\u00E6'
  val EmDash = '\u2014'
  val Bullet = '\u2022'
  val Ellipsis = '\u2026'
  val Minus = '\u2212'
  val NotEqual = '\u2260'
  val LessOrEqual = '\u2264'
  val GreaterOrEqual = '\u2265'
  val Infinity = '\u221E'
  val LeftArrow = '\u2B05'
  val UpArrow = '\u2B06'
  val DownArrow = '\u2B07'
  val RightArrow = '\u2B95'

  /**
   * "Normalize" a string to replace special characters with approximately-equvalent ASCII strings, and also convert it to
   * lower case.
   * 
   * @param str string to normalize
   * @return the normalized, lower-case string
   */
  def normalize(str: String) = Normalizer.normalize(str.toLowerCase, Normalizer.Form.NFD).replaceAll("\\p{M}", "").replace(AeLower.toString, "ae")
}
