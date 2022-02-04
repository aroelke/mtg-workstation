package editor.database.symbol

import editor.database.attributes.ManaType
import editor.util.UnicodeSymbols

trait SymbolParser[S <: Symbol] {
  def parse(s: String): Option[S]
}

trait HasDiscreteValues[K, S <: Symbol] {
  def values: Map[K, S]
}

sealed trait Generator[K, S <: ManaSymbol] extends SymbolParser[S] with HasDiscreteValues[K, S]

case object VariableSymbolGenerator extends Generator[Char, VariableSymbol] {
  override val values = Seq('X', 'Y', 'Z').map((v) => v -> VariableSymbol(v)).toMap
  override def parse(s: String) = Option.when(s.size == 1)(s(0).toUpper).flatMap(values.get)
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
}

case object HalfColorSymbolGenerator extends Generator[ManaType, HalfColorSymbol] {
  override val values = ManaType.values.map(s => s -> HalfColorSymbol(s)).toMap
  override def parse(s: String) = if (s.size == 2 && s(0).toUpper == 'H') Option(ManaType.tryParseManaType(s(1).toUpper)).map(values) else None
}

case object TwobridSymbolGenerator extends Generator[ManaType, TwobridSymbol] {
  override val values = ManaType.colors.map(s => s -> TwobridSymbol(s)).toMap
  override def parse(s: String) = if (s.size == 3 && s.startsWith("2/")) Option(ManaType.tryParseManaType(s(2).toUpper)).map(values) else None
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
}

case object PhyrexianSymbolGenerator extends Generator[ManaType, PhyrexianSymbol] {
  override val values = ManaType.colors.map(s => s -> PhyrexianSymbol(s)).toMap
  override def parse(s: String) = if (s.size == 3 && s.toUpperCase.endsWith("/P")) Option(ManaType.tryParseManaType(s(0).toUpper)).map(values) else None
}

case object ColorSymbolGenerator extends Generator[ManaType, ColorSymbol] {
  override val values = ManaType.values.map(s => s -> ColorSymbol(s)).toMap
  override def parse(s: String) = Option(ManaType.tryParseManaType(s)).map(values)
}

object Generator {
  val values = Seq(VariableSymbolGenerator, StaticSymbolGenerator, GenericSymbolGenerator, HalfColorSymbolGenerator, TwobridSymbolGenerator, PhyrexianHybridSymbolGenerator, HybridSymbolGenerator, PhyrexianSymbolGenerator, ColorSymbolGenerator)
}