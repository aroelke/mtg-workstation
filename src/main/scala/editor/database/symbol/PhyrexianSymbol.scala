package editor.database.symbol

import editor.database.attributes.ManaType

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

/**
 * Companion to [[PhyrexianSymbol]] that contains all the possible symbols and methods for parsing them from strings.
 * @author Alec Roelke
 */
object PhyrexianSymbol {
  /** All possible [[PhyrexianSymbol]]s. Currently only colored ones exist. @see [[ManaType.color]] */
  val values = ManaType.colors.map(c => c -> PhyrexianSymbol(c)).toMap

  /**
   * Parse a [[PhyrexianSymbol]] from a string.
   * 
   * @param s string to parse
   * @return the [[PhyrexianSymbol]] represented by the string, or None if there isn't one
   */
  def parse(s: String) = if (s.size == 3 && s(1) == '/' && s(2).toUpper == 'P') Option(ManaType.tryParseManaType(s(0))).map(values) else None

  @deprecated val SYMBOLS = values.asJava
  @deprecated def tryParsePhyrexianSymbol(s: String) = parse(s).toJava
  @deprecated def parsePhyrexianSymbol(s: String) = values(ManaType.parseManaType(s))
}

/**
 * A "Phyrexian" mana symbol that can be paid either with a color of mana or 2 life.
 * 
 * @constructor create a new Phyrexian mana symbol
 * @param color color of the new symbol
 * 
 * @author Alec Roelke
 */
class PhyrexianSymbol private(private val color: ManaType) extends ManaSymbol(s"phyrexian_${color.toString.toLowerCase}_mana.png", s"${color.shorthand.toUpper}/P", 1) {
  override def colorIntensity = ManaSymbol.createIntensity(ColorIntensity(color, 0.5))

  override def compareTo(o: ManaSymbol) = o match {
    case p: PhyrexianSymbol => color.compareTo(p.color)
    case _ => super.compareTo(o)
  }
}
