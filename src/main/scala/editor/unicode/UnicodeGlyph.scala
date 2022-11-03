package editor.unicode

/**
 * Wrapper class around a character, particularly a Unicode symbol without an ASCII equivalent, meant to be easily used
 * as a string or character constant in most contexts. Use [[toString]] for contexts where implicit conversion to string
 * doesn't work.
 *
 * @param toChar explicit character representation for contexts where implicit conversion doesn't work
 */
case class UnicodeGlyph(toChar: Char) {
  override def toString = toChar.toString
  override def equals(x: Any) = x match {
    case s: String => s == toString
    case c: Char => c == toChar
    case g: UnicodeGlyph => toChar == g.toChar
    case _ => false
  }
}