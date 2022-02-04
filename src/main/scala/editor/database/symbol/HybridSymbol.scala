package editor.database.symbol

import editor.database.attributes.ManaType

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

/**
 * Companion to [[HybridSymbol]] that contains all possible symbols and methods for parsing them from strings.
 * @author Alec Roelke
 */
object HybridSymbol {
  @deprecated val SYMBOLS = ManaType.colors.map((c1) => c1 -> ManaType.colors.collect{ case (c2) if c2 != c1 => c2 -> HybridSymbolGenerator.values((c1, c2)) })
  @deprecated def tryParseHybridSymbol(s: String) = HybridSymbolGenerator.parse(s).toJava
  @deprecated def parseHybridSymbol(s: String) = {
    val colors = s.split("/").map(ManaType.parseManaType)
    HybridSymbolGenerator.values((colors(0), colors(1)))
  }
}

/**
 * A mana symbol that can be paid with either of two colors of mana. Its color intensity is 0.5 for each of its two colors, and 0 for all the
 * rest of the types, as there are two ways to pay for it, and its text representation is the shorthand of its two [[ManaType]]s separated by
 * a slash ("/").
 * 
 * @constructor create a new hybrid mana symbol
 * @param first first color that can be used to pay
 * @param second second color that can be used to pay
 * 
 * @author Alec Roelke
 */
class HybridSymbol private[symbol](private val first: ManaType, private val second: ManaType) extends ManaSymbol(s"${first.toString.toLowerCase}_${second.toString.toLowerCase}_mana.png", s"${first.shorthand.toUpper}/${second.shorthand.toUpper}", 1) {
  override def colorIntensity = ManaSymbol.createIntensity(Map(first -> 0.5, second -> 0.5))

  override def compareTo(o: ManaSymbol) = o match {
    case h: HybridSymbol => first.compareTo(h.first)*10 + second.compareTo(h.second)
    case _ => super.compareTo(o)
  }
}