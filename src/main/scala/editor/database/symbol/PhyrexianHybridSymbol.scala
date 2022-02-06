package editor.database.symbol

import editor.database.attributes.ManaType

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

/**
 * Companion to [[PhyrexianHybridSymbol]] that contains all possible symbols and methods for parsing them from strings.
 * @author Alec Roelke
 */
object PhyrexianHybridSymbol {
  @deprecated def SYMBOLS = ManaType.colors.map((c1) => c1 -> ManaType.colors.collect{ case (c2) if c2 != c1 => c2 -> PhyrexianHybridSymbolGenerator.values((c1, c2)) })
  @deprecated def tryParsePhyrexianHybridSymbol(s: String) = PhyrexianHybridSymbolGenerator.parse(s).toJava
  @deprecated def parsePhyrexianHybridSymbol(s: String) = {
    val colors = s.split("/").map(ManaType.parseManaType)
    PhyrexianHybridSymbolGenerator.values((colors(0), colors(1)))
  }
}

/**
 * A "Phyrexian" mana symbol that can be paid with either of two colors of mana or 2 life. Its color intensity is 1/3 for each of its two colors of mana,
 * and 0 for the rest, as it has three ways to pay for it (one of two colors or 2 life), and its text representation is the shorthand of its colors and a P
 * separated by a slash ("/").
 * 
 * @constructor create a new Phyrexian hybrid mana symbol
 * @param first first color that can be used to pay
 * @param second second color that can be used to pay
 * 
 * @author Alec Roelke
 */
class PhyrexianHybridSymbol private[symbol](val first: ManaType, val second: ManaType) extends ManaSymbol(s"phyrexian_${first.toString.toLowerCase}_${second.toString.toLowerCase}_mana.png", s"${first.shorthand.toUpper}/${second.shorthand.toUpper}/P", 1, PhyrexianHybridSymbolGenerator) {
  override def colorIntensity = ManaSymbol.createIntensity(Map(first -> 1.0/3.0, second -> 1.0/3.0))
}
