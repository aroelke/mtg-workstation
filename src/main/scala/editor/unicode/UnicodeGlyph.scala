package editor.unicode

class UnicodeGlyph(val toChar: Char) {
  override def toString = toChar.toString
  override def equals(x: Any) = x match {
    case s: String => s == toString
    case c: Char => c == toChar
    case g: UnicodeGlyph => toChar == g.toChar
    case _ => false
  }
}