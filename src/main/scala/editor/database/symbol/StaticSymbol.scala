package editor.database.symbol

import editor.util.UnicodeSymbols

import scala.jdk.OptionConverters._
import scala.jdk.CollectionConverters._

object StaticSymbol {
  val values = Map(
    "1/2" -> StaticSymbol("half_mana.png", "1/2", 0.5),
    UnicodeSymbols.ONE_HALF.toString -> StaticSymbol("half_mana.png", "1/2", 0.5),
    UnicodeSymbols.INFINITY.toString -> StaticSymbol("infinity_mana.png", UnicodeSymbols.INFINITY.toString, Double.PositiveInfinity),
    "S" -> StaticSymbol("snow_mana.png", "S", 1),
    "M" -> StaticSymbol("multicolored.png", "M", 1)
  )

  def parse(s: String) = values.get(s.toUpperCase)

  @deprecated val SYMBOLS = values.asJava
  @deprecated def parseStaticSymbol(s: String) = values(s)
  @deprecated def tryParseStaticSymbol(s: String) = parse(s).toJava
}

class StaticSymbol(icon: String, text: String, value: Double) extends ManaSymbol(icon, text, value) {
  override def colorIntensity = ManaSymbol.createIntensity()
}