package editor.database.symbol

import editor.database.attributes.ManaType

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

/**
 * Companion to [[HalfColorSymbol]] containing all possible [[HalfColorSymbol]]s and methods for parsing them from strings.
 * @author Alec Roelke
 */
object HalfColorSymbol {
  @deprecated val SYMBOLS = HalfColorSymbolGenerator.values.asJava
  @deprecated def tryParseHalfColorSymbol(s: String) = HalfColorSymbolGenerator.parse(s).toJava
  @deprecated def parseHalfColorSymbol(s: String) = HalfColorSymbolGenerator.values(ManaType.parseManaType(s))
}

/**
 * Mana symbol representing half of a specific type of mana (not to be confused with the generic 1/2 mana symbol, which is a [[StaticSymbol]]). Its
 * color intensity is 0.5 for its type of mana, and 0 for all others, as it is paid for by only half of that type of mana, and its text
 * representation is an H followed by its [[ManaType]]'s shorthand.
 * 
 * @constructor create a new half-mana symbol
 * @param color type of mana the symbol represents
 * 
 * @author Alec Roelke
 */
class HalfColorSymbol private[symbol](private val color: ManaType) extends ManaSymbol(s"half_${color.toString.toLowerCase}_mana.png", s"H${color.shorthand}", 0.5) {
  override def colorIntensity = ManaSymbol.createIntensity(Map(color -> 0.5))

  override def compareTo(o: ManaSymbol) = o match {
    case h: HalfColorSymbol => color.compareTo(h.color)
    case _ => super.compareTo(o)
  }
}