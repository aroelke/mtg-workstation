package editor.database.symbol

import editor.database.attributes.ManaType

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

object HalfColorSymbol {
  val values = ManaType.values.map((t) => t -> HalfColorSymbol(t)).toMap

  def parse(s: String) = if (s.size == 2 && s(0).toUpper == 'H') Option(ManaType.tryParseManaType(s(1))).map(values) else None

  @deprecated val SYMBOLS = values.asJava
  @deprecated def tryParseHalfColorSymbol(s: String) = parse(s).toJava
  @deprecated def parseHalfColorSymbol(s: String) = values(ManaType.parseManaType(s))
}

class HalfColorSymbol private(private val color: ManaType) extends ManaSymbol(s"half_${color.toString.toLowerCase}_mana.png", s"H${color.shorthand}", 0.5) {
  override def colorIntensity = ManaSymbol.createIntensity(ColorIntensity(color, 0.5))

  override def compareTo(o: ManaSymbol) = o match {
    case h: HalfColorSymbol => color.compareTo(h.color)
    case _ => super.compareTo(o)
  }
}