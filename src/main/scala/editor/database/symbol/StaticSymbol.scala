package editor.database.symbol

import editor.database.attributes.ManaType
import editor.util.UnicodeSymbols

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

/**
 * Companion containing all of the possible [[StaticSymbol]]s and method for parsing them from strings.
 * @author Alec Roelke
 */
object StaticSymbol {
  /**
   * All of the possible [[StaticSymbol]]s:
   * - 1/2 generic mana (both with the string "1/2" and [[UnicodeSymbols.ONE_HALF]])
   * - Infinity generic mana
   * - Snow mana
   * - Multicolored symbol (not actually used in a mana cost)
   */
  val values = Map(
    "1/2" -> StaticSymbol("half_mana.png", "1/2", 0.5),
    UnicodeSymbols.ONE_HALF.toString -> StaticSymbol("half_mana.png", "1/2", 0.5),
    UnicodeSymbols.INFINITY.toString -> StaticSymbol("infinity_mana.png", UnicodeSymbols.INFINITY.toString, Double.PositiveInfinity),
    "S" -> StaticSymbol("snow_mana.png", "S", 1),
    "M" -> StaticSymbol("multicolored.png", "M", 0)
  )

  /**
   * Parse a [[StaticSymbol]] from a string.
   * 
   * @param s string to parse
   * @return either the [[StaticSymbol]] corresponding to the string, or None
   */
  def parse(s: String) = values.get(s.toUpperCase)

  @deprecated val SYMBOLS = values.asJava
  @deprecated def parseStaticSymbol(s: String) = values(s)
  @deprecated def tryParseStaticSymbol(s: String) = parse(s).toJava
}

/**
 * A mana symbol with a special, specific meaning that isn't captured by any of the other generalized versions of mana symbols.
 * 
 * @constructor create a new statically-specified symbol
 * @param icon icon for the new symbol
 * @param text text used for parsing the symbol
 * @param value mana value of the symbol
 * 
 * @author Alec Roelke
 */
class StaticSymbol private(icon: String, text: String, value: Double) extends ManaSymbol(icon, text, value) {
  override def colorIntensity = ManaSymbol.createIntensity(Map(ManaType.COLORLESS -> value))
}