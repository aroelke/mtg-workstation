package editor.database.symbol

import editor.database.attributes.ManaType

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

/**
 * Companion to [[TwobridSymbol]] which contains all possible symbols and methods to parse them from strings.
 * @author Alec Roelke
 */
object TwobridSymbol {
  /** All possible [[TwobridSymbol]]s; one for each color. @see [[ManaType.color]] */
  val values = ManaType.colors.map(c => c -> TwobridSymbol(c)).toMap

  /**
   * Parse a [[TwobridSymbol]] from a string.
   * 
   * @param s string to parse
   * @return the [[TwobridSymbol]] represented by the string, or None if there isn't one
   */
  def parse(s: String) = if (s.size == 3 && s(0) == '2' && s(1) == '/') Option(ManaType.tryParseManaType(s(2))).map(values) else None

  @deprecated val SYMBOLS = values.asJava
  @deprecated def tryParseTwobridSymbol(s: String) = parse(s).toJava
  @deprecated def parseTwobridSymbol(s: String) = values(ManaType.parseManaType(s))
}

/**
 * A mana symbol that can be paid either with two mana of any type or one mana of a specific color. Its color intensity is 0.5 for its color, and 0
 * for the rest of the types, as there are two ways to pay for it (a specific color of mana or two of any type), and its text representation is a 2
 * and its [[ManaType]]'s shorthand separated by a slash ("/").
 * 
 * @constructor create a new "twobrid" symbol
 * @param color color to use to get the discounted price
 * 
 * @author Alec Roelke
 */
class TwobridSymbol private(private val color: ManaType) extends ManaSymbol(s"2_${color.toString.toLowerCase}_mana.png", s"2/${color.shorthand.toUpper}", 2) {
  override def colorIntensity = ManaSymbol.createIntensity(Map(color -> 0.5))

  override def compareTo(o: ManaSymbol) = o match {
    case t: TwobridSymbol => color.compareTo(t.color)
    case _ => super.compareTo(o)
  }
}