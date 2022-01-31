package editor.database.symbol

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

object FunctionalSymbol {
  val Chaos = new FunctionalSymbol("chaos.png", "CHAOS") { override val toString = "CHAOS" }

  val values = Map(
    Chaos.toString -> Chaos,
    "PW" -> Chaos,
    "P" -> FunctionalSymbol("phyrexia.png", "P"),
    "T" -> FunctionalSymbol("tap.png", "T"),
    "TAP" -> FunctionalSymbol("tap.png", "T"),
    "Q" -> FunctionalSymbol("untap.png", "Q"),
    "E" -> FunctionalSymbol("energy.png", "E")
  )

  def parse(s: String) = values.get(s.toUpperCase)

  @deprecated val CHAOS = Chaos
  @deprecated val SYMBOLS = values.asJava
  @deprecated def tryParseFunctionalSymbol(s: String) = parse(s).toJava
  @deprecated def parseFunctionalSymbol(s: String) = values(s)
}

class FunctionalSymbol private(icon: String, text: String) extends Symbol(icon, text)