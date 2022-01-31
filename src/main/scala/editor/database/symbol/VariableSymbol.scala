package editor.database.symbol

import editor.database.attributes.ManaType

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

object VariableSymbol {
  val values = Seq('X', 'Y', 'Z').map((v) => v.toString -> VariableSymbol(v)).toMap

  def parse(s: String) = values.get(s)

  @deprecated val SYMBOLS = values.asJava
  @deprecated def parseVariableSymbol(s: String) = values(s)
  @deprecated def tryParseVariableSymbol(s: String) = parse(s).toJava
}

class VariableSymbol private(private val variable: Char) extends ManaSymbol(s"${variable.toLower}_mana.png", variable.toString.toUpperCase, 0) {
  override def colorIntensity = ManaSymbol.createIntensity(ColorIntensity(ManaType.COLORLESS, 0.5))

  override def compareTo(other: ManaSymbol) = other match {
    case s: VariableSymbol => variable.toUpper - s.variable.toUpper
    case _ => super.compareTo(other)
  }
}