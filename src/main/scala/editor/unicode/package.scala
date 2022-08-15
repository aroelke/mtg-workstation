package editor

import java.text.Normalizer

/**
 * Collection of constants naming various Unicode symbols used in the application and functions for converting to ASCII text.
 * @author Alec Roelke
 */
package object unicode {
  val Substitute = UnicodeGlyph('\u001A')
  val SuperscriptTwo = UnicodeGlyph('\u00B2')
  val OneHalf = UnicodeGlyph('\u00BD')
  val Multiply = UnicodeGlyph('\u00D7')
  val AeLower = UnicodeGlyph('\u00E6')
  val EmDash = UnicodeGlyph('\u2014')
  val Bullet = UnicodeGlyph('\u2022')
  val Ellipsis = UnicodeGlyph('\u2026')
  val Minus = UnicodeGlyph('\u2212')
  val NotEqual = UnicodeGlyph('\u2260')
  val LessOrEqual = UnicodeGlyph('\u2264')
  val GreaterOrEqual = UnicodeGlyph('\u2265')
  val Infinity = UnicodeGlyph('\u221E')
  val LeftArrow = UnicodeGlyph('\u2B05')
  val UpArrow = UnicodeGlyph('\u2B06')
  val DownArrow = UnicodeGlyph('\u2B07')
  val RightArrow = UnicodeGlyph('\u2B95')

  given Conversion[UnicodeGlyph, Char] = _.toChar
  given CanEqual[UnicodeGlyph, Char] = CanEqual.derived
  given CanEqual[Char, UnicodeGlyph] = CanEqual.derived

  given Conversion[UnicodeGlyph, String] = _.toString
  given CanEqual[UnicodeGlyph, String] = CanEqual.derived
  given CanEqual[String, UnicodeGlyph] = CanEqual.derived

  /**
   * "Normalize" a string to replace special characters with approximately-equvalent ASCII strings, and also convert it to
   * lower case.
   * 
   * @param str string to normalize
   * @return the normalized, lower-case string
   */
  def normalize(str: String) = Normalizer.normalize(str.toLowerCase, Normalizer.Form.NFD).replaceAll("\\p{M}", "").replace(AeLower, "ae")
}