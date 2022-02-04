package editor.database.symbol

import editor.database.attributes.ManaType

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

/**
 * Companion to [[PhyrexianSymbol]] that contains all the possible symbols and methods for parsing them from strings.
 * @author Alec Roelke
 */
object PhyrexianSymbol {
  @deprecated val SYMBOLS = PhyrexianSymbolGenerator.values.asJava
  @deprecated def tryParsePhyrexianSymbol(s: String) = PhyrexianSymbolGenerator.parse(s).toJava
  @deprecated def parsePhyrexianSymbol(s: String) = PhyrexianSymbolGenerator.values(ManaType.parseManaType(s))
}

/**
 * A "Phyrexian" mana symbol that can be paid either with a color of mana or 2 life. Its color intensity is 0.5 for its color of mana, and 0 for
 * the rest, as there are two ways to pay for it (its color and 2 life), and its text representation is the shorthand of its [[ManaType]] and a P
 * separated by a slash ("/").
 * 
 * @constructor create a new Phyrexian mana symbol
 * @param color color of the new symbol
 * 
 * @author Alec Roelke
 */
class PhyrexianSymbol private[symbol](private val color: ManaType) extends ManaSymbol(s"phyrexian_${color.toString.toLowerCase}_mana.png", s"${color.shorthand.toUpper}/P", 1) {
  override def colorIntensity = ManaSymbol.createIntensity(Map(color -> 0.5))

  override def compareTo(o: ManaSymbol) = o match {
    case p: PhyrexianSymbol => color.compareTo(p.color)
    case _ => super.compareTo(o)
  }
}
