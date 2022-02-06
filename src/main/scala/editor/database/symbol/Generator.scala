package editor.database.symbol

import editor.database.attributes.ManaType
import editor.util.UnicodeSymbols
import scala.reflect.ClassTag

trait SymbolParser[S <: Symbol] {
  def parse(s: String): Option[S]
}

trait HasDiscreteValues[K, S <: Symbol] {
  def values: Map[K, S]
}

sealed trait Generator[K, S <: ManaSymbol : ClassTag] extends SymbolParser[S] with HasDiscreteValues[K, S] with Ordering[ManaSymbol] {
  def ordinal = {
    val i = Generator.values.indexOf(this)
    if (i >= 0) i else throw IllegalAccessException(this.toString)
  }
}

case object VariableSymbolGenerator extends Generator[Char, VariableSymbol] {
  override val values = Seq('X', 'Y', 'Z').map((v) => v -> VariableSymbol(v)).toMap
  override def parse(s: String) = Option.when(s.size == 1)(s(0).toUpper).flatMap(values.get)
  override def compare(a: ManaSymbol, b: ManaSymbol) = (a, b) match {
    case (va: VariableSymbol, vb: VariableSymbol) => va.variable.toUpper - vb.variable.toUpper
    case _ => throw IllegalArgumentException(s"either $a or $b is not a VariableSymbol")
  }
}

case object StaticSymbolGenerator extends Generator[String, StaticSymbol] {
  override val values = Map(
    "1/2" -> StaticSymbol("half_mana.png", "1/2", 0.5),
    UnicodeSymbols.ONE_HALF.toString -> StaticSymbol("half_mana.png", "1/2", 0.5),
    UnicodeSymbols.INFINITY.toString -> StaticSymbol("infinity_mana.png", UnicodeSymbols.INFINITY.toString, Double.PositiveInfinity),
    "S" -> StaticSymbol("snow_mana.png", "S", 1),
    "M" -> StaticSymbol("multicolored.png", "M", 0)
  )
  override def parse(s: String) = values.get(s)
  override def compare(a: ManaSymbol, b: ManaSymbol) = (a, b) match {
    case (sa: StaticSymbol, sb: StaticSymbol) => (sa.value - sb.value).toInt
    case _ => throw IllegalArgumentException(s"either $a or $b is not a StaticSymbol")
  }
}

case object GenericSymbolGenerator extends Generator[Int, GenericSymbol] {
  override val values = ((0 to 20).toSeq ++ Seq(100, 1000000)).map((n) => n -> new GenericSymbol(n)).toMap
  override def parse(s: String) = {
    try {
      values.get(s.toInt)
    } catch {
      case _: NumberFormatException => None
      case _: ArrayIndexOutOfBoundsException => None
    }
  }
  override def compare(a: ManaSymbol, b: ManaSymbol) = (a, b) match {
    case (ga: GenericSymbol, gb: GenericSymbol) => (ga.value - gb.value).toInt
    case _ => throw IllegalArgumentException(s"either $a or $b is not a GenericSymbol")
  }
}

case object HalfColorSymbolGenerator extends Generator[ManaType, HalfColorSymbol] {
  override val values = ManaType.values.map(s => s -> HalfColorSymbol(s)).toMap
  override def parse(s: String) = if (s.size == 2 && s(0).toUpper == 'H') Option(ManaType.tryParseManaType(s(1).toUpper)).map(values) else None
  override def compare(a: ManaSymbol, b: ManaSymbol) = (a, b) match {
    case (ha: HalfColorSymbol, hb: HalfColorSymbol) => ha.color.compareTo(hb.color)
    case _ => throw IllegalArgumentException(s"either $a or $b is not a HalfColorSymbol")
  }
}

case object TwobridSymbolGenerator extends Generator[ManaType, TwobridSymbol] {
  override val values = ManaType.colors.map(s => s -> TwobridSymbol(s)).toMap
  override def parse(s: String) = if (s.size == 3 && s.startsWith("2/")) Option(ManaType.tryParseManaType(s(2).toUpper)).map(values) else None
  override def compare(a: ManaSymbol, b: ManaSymbol) = (a, b) match {
    case (ta: TwobridSymbol, tb: TwobridSymbol) => ta.color.compareTo(tb.color)
    case _ => throw IllegalArgumentException(s"either $a or $b is not a TwobridSymbol")
  }
}

case object PhyrexianHybridSymbolGenerator extends Generator[(ManaType, ManaType), PhyrexianHybridSymbol] {
  override val values = (for (c1 <- ManaType.colors; c2 <- ManaType.colors if c1 != c2) yield (c1, c2) -> (if (c1.colorOrder(c2) < 0) PhyrexianHybridSymbol(c1, c2) else PhyrexianHybridSymbol(c2, c1))).toMap
  override def parse(s: String) = {
    val tokens = s.split("/")
    if (tokens.size == 3 && tokens(2) == "P") {
      tokens.flatMap((t) => Option(ManaType.tryParseManaType(t))) match {
        case Array(c1, c2) => values.get((c1, c2))
        case _ => None
      }
    } else None
  }
  override def compare(a: ManaSymbol, b: ManaSymbol) = (a, b) match {
    case (pa: PhyrexianHybridSymbol, pb: PhyrexianHybridSymbol) => pa.first.compareTo(pb.first)*10 + pa.second.compareTo(pb.second)
    case _ => throw IllegalArgumentException(s"either $a or $b is not a PhyrexianHybridSymbol")
  }
}

case object HybridSymbolGenerator extends Generator[(ManaType, ManaType), HybridSymbol] {
  override val values = (for (c1 <- ManaType.colors; c2 <- ManaType.colors if c1 != c2) yield (c1, c2) -> (if (c1.colorOrder(c2) < 0) HybridSymbol(c1, c2) else HybridSymbol(c2, c1))).toMap
  override def parse(s: String) = {
    val tokens = s.split("/")
    if (tokens.size == 2) {
      tokens.flatMap((t) => Option(ManaType.tryParseManaType(t))) match {
        case Array(c1, c2) => values.get((c1, c2))
        case _ => None
      }
    } else None
  }
  override def compare(a: ManaSymbol, b: ManaSymbol) = (a, b) match {
    case (ha: HybridSymbol, hb: HybridSymbol) => ha.first.compareTo(hb.first)*10 + ha.second.compareTo(hb.second)
    case _ => throw IllegalArgumentException(s"either $a or $b is not a HybridSymbol")
  }
}

case object PhyrexianSymbolGenerator extends Generator[ManaType, PhyrexianSymbol] {
  override val values = ManaType.colors.map(s => s -> PhyrexianSymbol(s)).toMap
  override def parse(s: String) = if (s.size == 3 && s.toUpperCase.endsWith("/P")) Option(ManaType.tryParseManaType(s(0).toUpper)).map(values) else None
  override def compare(a: ManaSymbol, b: ManaSymbol) = (a, b) match {
    case (pa: PhyrexianSymbol, pb: PhyrexianSymbol) => pa.color.compareTo(pb.color)
    case _ => throw IllegalArgumentException(s"either $a or $b is not a PhyrexianSymbol")
  }
}

case object ColorSymbolGenerator extends Generator[ManaType, ColorSymbol] {
  override val values = ManaType.values.map(s => s -> ColorSymbol(s)).toMap
  override def parse(s: String) = Option(ManaType.tryParseManaType(s)).map(values)
  override def compare(a: ManaSymbol, b: ManaSymbol) = (a, b) match {
    case (ca: ColorSymbol, cb: ColorSymbol) => ca.color.compareTo(cb.color)
    case _ => throw IllegalArgumentException(s"either $a or $b is not a ColorSymbol")
  }
}

object Generator {
  val values = Seq(VariableSymbolGenerator, StaticSymbolGenerator, GenericSymbolGenerator, HalfColorSymbolGenerator, TwobridSymbolGenerator, PhyrexianHybridSymbolGenerator, HybridSymbolGenerator, PhyrexianSymbolGenerator, ColorSymbolGenerator)
}