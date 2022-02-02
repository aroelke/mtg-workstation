package editor.database.symbol

import editor.database.attributes.ManaType

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

object TwobridSymbol {
  val values = ManaType.colors.map(c => c -> TwobridSymbol(c)).toMap

  def parse(s: String) = if (s.size == 3 && s(0) == '2' && s(1) == '/') Option(ManaType.tryParseManaType(s(2))).map(values) else None

  @deprecated val SYMBOLS = values.asJava
  @deprecated def tryParseTwobridSymbol(s: String) = parse(s).toJava
  @deprecated def parseTwobridSymbol(s: String) = values(ManaType.parseManaType(s))
}

class TwobridSymbol private(private val color: ManaType) extends ManaSymbol(s"2_${color.toString.toLowerCase}_mana.png", s"2/${color.shorthand.toUpper}", 2) {
  override def colorIntensity = ManaSymbol.createIntensity(ColorIntensity(color, 0.5))

  override def compareTo(o: ManaSymbol) = o match {
    case t: TwobridSymbol => color.compareTo(t.color)
    case _ => super.compareTo(o)
  }
}