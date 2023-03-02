package editor.database.symbol

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
  val Chaos = new FunctionalSymbol("chaos", "CHAOS") { override val toString = "CHAOS" }

  override val values = Map(
    Chaos.toString -> Chaos,
    "PW" -> Chaos,
    "P" -> new FunctionalSymbol("phyrexia", "P"),
    "T" -> new FunctionalSymbol("tap", "T"),
    "TAP" -> new FunctionalSymbol("tap", "T"),
    "Q" -> new FunctionalSymbol("untap", "Q"),
    "E" -> new FunctionalSymbol("energy", "E")
  )

  override def parse(s: String) = values.get(s.toUpperCase)
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