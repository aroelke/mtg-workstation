package editor.database.symbol

import editor.database.attributes.ManaType

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

/**
 * Companion to [[HybridSymbol]] that contains all possible symbols and methods for parsing them from strings.
 * @author Alec Roelke
 */
object HybridSymbol {
  /**
   * All possible hybrid symbols in all combinations of different colors, regardless of order. Pairs of colors
   * in different orders will map to the same symbol.
   * @see [[ManaType.colors]]
   */
  val values = ManaType.colors.map((c1) => c1 -> ManaType.colors.collect{ case c2 if c2 != c1 => c2 -> (if (c1.colorOrder(c2) < 0) HybridSymbol(c1, c2) else HybridSymbol(c2, c1)) }.toMap).toMap

  /**
   * Parse a [[HybridSymbol]] from a string.
   * 
   * @param s string to parse
   * @return the [[HybridSymbol]] represented by the string, or None if there isn't one
   */
  def parse(s: String) = {
    val colors = s.split("/").flatMap((t) => Option(ManaType.tryParseManaType(t)))
    colors match {
      case Array(col1, col2) => Some(values(col1)(col2))
      case _ => None
    }
  }

  @deprecated def SYMBOLS = values.map{ case (c, s) => c -> s.asJava }.asJava
  @deprecated def tryParseHybridSymbol(s: String) = parse(s).toJava
  @deprecated def parseHybridSymbol(s: String) = {
    val colors = s.split("/").map(ManaType.parseManaType)
    values(colors(0))(colors(1))
  }
}

/**
 * A mana symbol that can be paid with either of two colors of mana.
 * 
 * @constructor create a new hybrid mana symbol
 * @param first first color that can be used to pay
 * @param second second color that can be used to pay
 * 
 * @author Alec Roelke
 */
class HybridSymbol private(private val first: ManaType, private val second: ManaType) extends ManaSymbol(s"${first.toString.toLowerCase}_${second.toString.toLowerCase}_mana.png", s"${first.shorthand.toUpper}/${second.shorthand.toUpper}", 1) {
  override def colorIntensity = ManaSymbol.createIntensity(ColorIntensity(first, 0.5), ColorIntensity(second, 0.5))

  override def compareTo(o: ManaSymbol) = o match {
    case h: HybridSymbol => first.compareTo(h.first)*10 + second.compareTo(h.second)
    case _ => super.compareTo(o)
  }
}