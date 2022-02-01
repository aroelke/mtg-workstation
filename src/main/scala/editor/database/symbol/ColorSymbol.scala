package editor.database.symbol

import editor.database.attributes.ManaType

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

object ColorSymbol {
  val values = ManaType.values.map(s => s -> ColorSymbol(s)).toMap

  def parse(s: String) = Option(ManaType.tryParseManaType(s)).map(values)

  @deprecated val SYMBOLS = values.asJava
  @deprecated def tryParseColorSymbol(s: String) = parse(s).toJava
  @deprecated def parseColorSymbol(s: String) = values(ManaType.parseManaType(s))
}

class ColorSymbol private(private val color: ManaType) extends ManaSymbol(s"${color.toString.toLowerCase}_mana.png", color.shorthand.toString, 1) {
  override def colorIntensity = ManaSymbol.createIntensity(ColorIntensity(color, 1))

  override def compareTo(o: ManaSymbol) = o match {
    case c: ColorSymbol => color.compareTo(c.color)
    case _ => super.compareTo(o)
  }
}