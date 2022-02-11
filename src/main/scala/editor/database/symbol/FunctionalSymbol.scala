package editor.database.symbol

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

/**
 * Companion to [[FunctionalSymbol]] that contains all the existing non-mana symbols and functions for parsing strings
 * into [[FunctionalSymbol]]s. The possible [[FunctionalSymbol]]s are:
 * - The Chaos symbol
 * - The Phyrexia symbol
 * - The tap symbol
 * - The untap symbol
 * - The energy symbol
 *
 * @author Alec Roelke
 */
object FunctionalSymbol extends SymbolParser[FunctionalSymbol] with HasSymbolValues[String, FunctionalSymbol] {
  /** The Chaos symbol used on Planes, which has a special string representation without braces. */
  val Chaos = new FunctionalSymbol("chaos.png", "CHAOS") { override val toString = "CHAOS" }

  override val values = Map(
    Chaos.toString -> Chaos,
    "PW" -> Chaos,
    "P" -> new FunctionalSymbol("phyrexia.png", "P"),
    "T" -> new FunctionalSymbol("tap.png", "T"),
    "TAP" -> new FunctionalSymbol("tap.png", "T"),
    "Q" -> new FunctionalSymbol("untap.png", "Q"),
    "E" -> new FunctionalSymbol("energy.png", "E")
  )

  override def parse(s: String) = values.get(s.toUpperCase)

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