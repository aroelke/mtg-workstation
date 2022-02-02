package editor.database.symbol

import editor.database.attributes.ManaType

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

object PhyrexianSymbol {
  val values = ManaType.colors.map(c => c -> PhyrexianSymbol(c)).toMap

  def parse(s: String) = if (s.size == 3 && s(1) == '/' && s(2).toUpper == 'P') Option(ManaType.tryParseManaType(s(0))).map(values) else None

  @deprecated val SYMBOLS = values.asJava
  @deprecated def tryParsePhyrexianSymbol(s: String) = parse(s).toJava
  @deprecated def parsePhyrexianSymbol(s: String) = values(ManaType.parseManaType(s))
}

class PhyrexianSymbol private(private val color: ManaType) extends ManaSymbol(s"phyrexian_${color.toString.toLowerCase}_mana.png", s"${color.shorthand.toUpper}/P", 1) {
  override def colorIntensity = ManaSymbol.createIntensity(ColorIntensity(color, 0.5))

  override def compareTo(o: ManaSymbol) = o match {
    case p: PhyrexianSymbol => color.compareTo(p.color)
    case _ => super.compareTo(o)
  }
}
