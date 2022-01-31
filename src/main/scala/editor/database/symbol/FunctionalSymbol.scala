package editor.database.symbol

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

/**
 * Companion to [[FunctionalSymbol]] that contains all the existing non-mana symbols and functions for parsing strings
 * into [[FunctionalSymbol]]s.
 * @author Alec Roelke
 */
object FunctionalSymbol {
  /** The Chaos symbol used on Planes, which has a special string representation without braces. */
  val Chaos = new FunctionalSymbol("chaos.png", "CHAOS") { override val toString = "CHAOS" }

  /**
   * All of the possible [[FunctionalSymbol]]s:
   * - The Chaos symbol
   * - The Phyrexia symbol
   * - The tap symbol
   * - The untap symbol
   * - The energy symbol
   */
  val values = Map(
    Chaos.toString -> Chaos,
    "PW" -> Chaos,
    "P" -> FunctionalSymbol("phyrexia.png", "P"),
    "T" -> FunctionalSymbol("tap.png", "T"),
    "TAP" -> FunctionalSymbol("tap.png", "T"),
    "Q" -> FunctionalSymbol("untap.png", "Q"),
    "E" -> FunctionalSymbol("energy.png", "E")
  )

  /**
   * Parse a string to get the corresponding [[FunctionalSymbol]].
   * 
   * @param s string to parse
   * @return the [[FunctionalSymbol]] represented by the string, or None if there isn't one
   */
  def parse(s: String) = values.get(s.toUpperCase)

  @deprecated val CHAOS = Chaos
  @deprecated val SYMBOLS = values.asJava
  @deprecated def tryParseFunctionalSymbol(s: String) = parse(s).toJava
  @deprecated def parseFunctionalSymbol(s: String) = values(s)
}

/**
 * A symbol with a specific meaning that's not used in mana costs.
 * 
 * @constructor create a new non-mana symbol
 * @param icon icon for the symbol
 * @param text text representation of the symbol
 * 
 * @author Alec Roelke
 */
class FunctionalSymbol private(icon: String, text: String) extends Symbol(icon, text)