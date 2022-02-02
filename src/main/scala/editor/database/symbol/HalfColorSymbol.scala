package editor.database.symbol

import editor.database.attributes.ManaType

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

/**
 * Companion to [[HalfColorSymbol]] containing all possible [[HalfColorSymbol]]s and methods for parsing them from strings.
 * @author Alec Roelke
 */
object HalfColorSymbol {
  /** All possible half-color symbols. @see [[ManaType]] */
  val values = ManaType.values.map((t) => t -> HalfColorSymbol(t)).toMap

  /**
   * Parse a [[HalfColorSymbol]] from a string.
   * 
   * @param s string to parse
   * @return the [[HalfColorSymbol]] represented by the string, or None if there isn't one
   */
  def parse(s: String) = if (s.size == 2 && s(0).toUpper == 'H') Option(ManaType.tryParseManaType(s(1))).map(values) else None

  @deprecated val SYMBOLS = values.asJava
  @deprecated def tryParseHalfColorSymbol(s: String) = parse(s).toJava
  @deprecated def parseHalfColorSymbol(s: String) = values(ManaType.parseManaType(s))
}

/**
 * Mana symbol representing half of a specific type of mana (not to be confused with the generic 1/2 mana symbol, which is a [[StaticSymbol]]).
 * 
 * @constructor create a new half-mana symbol
 * @param color type of mana the symbol represents
 * 
 * @author Alec Roelke
 */
class HalfColorSymbol private(private val color: ManaType) extends ManaSymbol(s"half_${color.toString.toLowerCase}_mana.png", s"H${color.shorthand}", 0.5) {
  override def colorIntensity = ManaSymbol.createIntensity(Map(color -> 0.5))

  override def compareTo(o: ManaSymbol) = o match {
    case h: HalfColorSymbol => color.compareTo(h.color)
    case _ => super.compareTo(o)
  }
}