package editor.database.symbol

import editor.database.attributes.ManaType

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

object HybridSymbol {
  val values = ManaType.colors.map((c1) => c1 -> ManaType.colors.collect{ case c2 if c2 != c1 => c2 -> (if (c1.colorOrder(c2) < 0) HybridSymbol(c1, c2) else HybridSymbol(c2, c1)) }.toMap).toMap

  def parse(s: String) = {
    val colors = s.split("/").flatMap((t) => Option(ManaType.tryParseManaType(t)))
    colors match {
      case Array(col1, col2) => Some(values(col1)(col2))
      case _ => None
    }
  }

  @deprecated def SYMBOLS = values.map{ case (c, s) => c -> s.asJava }.asJava
  @deprecated def tryParseHybridSymbol(s: String) = parse(s).toJava
  @deprecated def parseHybridSymbol(s: String) = {
    val colors = s.split("/").map(ManaType.parseManaType)
    values(colors(0))(colors(1))
  }
}

class HybridSymbol private(private val first: ManaType, private val second: ManaType) extends ManaSymbol(s"${first.toString.toLowerCase}_${second.toString.toLowerCase}_mana.png", s"${first.shorthand.toUpper}/${second.shorthand.toUpper}", 1) {
  override def colorIntensity = ManaSymbol.createIntensity(ColorIntensity(first, 0.5), ColorIntensity(second, 0.5))

  override def compareTo(o: ManaSymbol) = o match {
    case h: HybridSymbol => first.compareTo(h.first)*10 + second.compareTo(h.second)
    case _ => super.compareTo(o)
  }
}