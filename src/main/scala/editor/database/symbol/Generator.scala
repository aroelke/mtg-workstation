package editor.database.symbol

import editor.database.attributes.ManaType
import editor.util.UnicodeSymbols
import scala.reflect.ClassTag

trait SymbolParser[S <: Symbol] {
  def parse(s: String): Option[S]
}

trait HasDiscreteValues[K, S <: Symbol] {
  def values: Map[K, S]
  def apply(key: K) = values(key)
}

enum Generator[K, S <: ManaSymbol](map: => Map[K, S], keygen: (String) => Option[K], comparator: Ordering[ManaSymbol]) extends SymbolParser[ManaSymbol] with HasDiscreteValues[K, S] with Ordering[ManaSymbol] {
  override lazy val values = map
  override def parse(s: String) = keygen(s).flatMap(values.get)
  override def compare(a: ManaSymbol, b: ManaSymbol) = comparator.compare(a, b)

  case VariableSymbol extends Generator[Char, editor.database.symbol.VariableSymbol](
    map = Seq('X', 'Y', 'Z').map((v) => v -> new VariableSymbol(v)).toMap,
    keygen = (s) => Option.when(s.size == 1)(s(0).toUpper),
    comparator = (a, b) => (a, b) match {
      case (va: VariableSymbol, vb: VariableSymbol) => va.variable.toUpper - vb.variable.toUpper
      case _ => throw IllegalArgumentException(s"either $a or $b is not a VariableSymbol")
    }
  )

  case StaticSymbol extends Generator(
    map = Map(
      "1/2" -> new StaticSymbol("half_mana.png", "1/2", 0.5),
      UnicodeSymbols.ONE_HALF.toString -> new StaticSymbol("half_mana.png", "1/2", 0.5),
      UnicodeSymbols.INFINITY.toString -> new StaticSymbol("infinity_mana.png", UnicodeSymbols.INFINITY.toString, Double.PositiveInfinity),
      "S" -> new StaticSymbol("snow_mana.png", "S", 1, 1),
      "M" -> new StaticSymbol("multicolored.png", "M", 0)
    ),
    keygen = Some(_),
    comparator = (a, b) => (a, b) match {
      case (sa: StaticSymbol, sb: StaticSymbol) => (sa.value - sb.value).toInt
      case _ => throw IllegalArgumentException(s"either $a or $b is not a StaticSymbol")
    }
  )

  case GenericSymbol extends Generator(
    map = ((0 to 20).toSeq ++ Seq(100, 1000000)).map((n) => n -> new GenericSymbol(n)).toMap,
    keygen = (s) => try Some(s.toInt) catch case _: NumberFormatException => None,
    comparator = (a, b) => (a, b) match {
      case (ga: GenericSymbol, gb: GenericSymbol) => (ga.value - gb.value).toInt
      case _ => throw IllegalArgumentException(s"either $a or $b is not a GenericSymbol")
    }
  )

  case HalfColorSymbol extends Generator(
    map = ManaType.values.map(s => s -> new HalfColorSymbol(s)).toMap,
    keygen = (s) => if (s.size == 3 && s.startsWith("2/")) Option(ManaType.tryParseManaType(s(2).toUpper)) else None,
    comparator = (a, b) => (a, b) match {
      case (ha: HalfColorSymbol, hb: HalfColorSymbol) => ha.color.compareTo(hb.color)
      case _ => throw IllegalArgumentException(s"either $a or $b is not a HalfColorSymbol")
    }
  )

  case TwobridSymbol extends Generator(
    map = ManaType.colors.map(s => s -> new TwobridSymbol(s)).toMap,
    keygen = (s) => if (s.size == 3 && s.startsWith("2/")) Option(ManaType.tryParseManaType(s(2).toUpper)) else None,
    comparator = (a, b) => (a, b) match {
      case (ta: TwobridSymbol, tb: TwobridSymbol) => ta.color.compareTo(tb.color)
      case _ => throw IllegalArgumentException(s"either $a or $b is not a TwobridSymbol")
    }
  )

  case PhyrexianHybridSymbol extends Generator(
    map = (for (c1 <- ManaType.colors; c2 <- ManaType.colors if c1 != c2) yield (c1, c2) -> (if (c1.colorOrder(c2) < 0) new PhyrexianHybridSymbol(c1, c2) else new PhyrexianHybridSymbol(c2, c1))).toMap,
    keygen = (s) => {
      val tokens = s.split("/")
      if (tokens.size == 3 && tokens(2) == "P") {
        tokens.flatMap((t) => Option(ManaType.tryParseManaType(t))) match {
          case Array(c1, c2) => Some((c1, c2))
          case _ => None
        }
      } else None
    },
    comparator = (a, b) => (a, b) match {
      case (pa: PhyrexianHybridSymbol, pb: PhyrexianHybridSymbol) => pa.first.compareTo(pb.first)*10 + pa.second.compareTo(pb.second)
      case _ => throw IllegalArgumentException(s"either $a or $b is not a PhyrexianHybridSymbol")
    }
  )

  case HybridSymbol extends Generator(
    map = (for (c1 <- ManaType.colors; c2 <- ManaType.colors if c1 != c2) yield (c1, c2) -> (if (c1.colorOrder(c2) < 0) new HybridSymbol(c1, c2) else new HybridSymbol(c2, c1))).toMap,
    keygen = (s) => {
      val tokens = s.split("/")
      if (tokens.size == 2) {
        tokens.flatMap((t) => Option(ManaType.tryParseManaType(t))) match {
          case Array(c1, c2) => Some((c1, c2))
          case _ => None
        }
      } else None
    },
    comparator = (a, b) => (a, b) match {
      case (ha: HybridSymbol, hb: HybridSymbol) => ha.first.compareTo(hb.first)*10 + ha.second.compareTo(hb.second)
      case _ => throw IllegalArgumentException(s"either $a or $b is not a HybridSymbol")
    }
  )

  case PhyrexianSymbol extends Generator(
    map = (for (c1 <- ManaType.colors; c2 <- ManaType.colors if c1 != c2) yield (c1, c2) -> (if (c1.colorOrder(c2) < 0) new PhyrexianHybridSymbol(c1, c2) else new PhyrexianHybridSymbol(c2, c1))).toMap,
    keygen = (s) => if (s.size == 3 && s.toUpperCase.endsWith("/P")) Option(ManaType.tryParseManaType(s(0).toUpper)) else None,
    comparator = (a, b) => (a, b) match {
      case (pa: PhyrexianSymbol, pb: PhyrexianSymbol) => pa.color.compareTo(pb.color)
      case _ => throw IllegalArgumentException(s"either $a or $b is not a PhyrexianSymbol")
    }
  )
  
  case ColorSymbol extends Generator(
    map = ManaType.values.map(s => s -> new ColorSymbol(s)).toMap,
    keygen = (s) => Option(ManaType.tryParseManaType(s)),
    comparator = (a, b) => (a, b) match {
      case (ca: ColorSymbol, cb: ColorSymbol) => ca.color.compareTo(cb.color)
      case _ => throw IllegalArgumentException(s"either $a or $b is not a ColorSymbol")
    }
  )
}

/**
 * A symbol that can be paid using any amount of mana, indicating using a letter like X.  Its color intensity is 0 for all mana types, as
 * any type of mana can be spent to pay for it, and its text representation is the letter used as the variable.
 * 
 * @constructor create a new variable symbol using a particular variable name
 * @param variable letter to use as the variable name
 * 
 * @author Alec Roelke
 */
case class VariableSymbol private[symbol](variable: Char) extends ManaSymbol(s"${variable.toLower}_mana.png", variable.toString.toUpperCase, 0, Map(ManaType.COLORLESS -> 0.5), Generator.VariableSymbol)

/**
 * A mana symbol with a special, specific meaning that isn't captured by any of the other generalized versions of mana symbols. Its color
 * intensity and text representation depend on the symbol.
 * 
 * @constructor create a new statically-specified symbol
 * @param icon icon for the new symbol
 * @param text text used for parsing the symbol
 * @param value mana value of the symbol
 * 
 * @author Alec Roelke
 */
case class StaticSymbol private[symbol](iconName: String, text: String, override val value: Double, intensity: Double = 0) extends ManaSymbol(iconName, text, value, Map(ManaType.COLORLESS -> intensity), Generator.StaticSymbol)

/**
 * A mana symbol representing an amount of any combination of mana. Its color intensity is 0 for all mana types, as any type
 * of mana can pay for it, and its text representation is simply the amount of mana it represents.
 * 
 * @constructor create a new generic mana symbol for a particular amount of mana
 * @param amount amount of mana needed to pay for the symbol
 * 
 * @author Alec Roelke
 */
case class GenericSymbol private[symbol](amount: Int) extends ManaSymbol(s"${amount}_mana.png", amount.toString, amount, Map.empty, Generator.GenericSymbol)

/**
 * Mana symbol representing half of a specific type of mana (not to be confused with the generic 1/2 mana symbol, which is a [[StaticSymbol]]). Its
 * color intensity is 0.5 for its type of mana, and 0 for all others, as it is paid for by only half of that type of mana, and its text
 * representation is an H followed by its [[ManaType]]'s shorthand.
 * 
 * @constructor create a new half-mana symbol
 * @param color type of mana the symbol represents
 * 
 * @author Alec Roelke
 */
case class HalfColorSymbol private[symbol](color: ManaType) extends ManaSymbol(s"half_${color.toString.toLowerCase}_mana.png", s"H${color.shorthand}", 0.5, Map(color -> 0.5), Generator.HalfColorSymbol)

/**
 * A mana symbol that can be paid either with two mana of any type or one mana of a specific color. Its color intensity is 0.5 for its color, and 0
 * for the rest of the types, as there are two ways to pay for it (a specific color of mana or two of any type), and its text representation is a 2
 * and its [[ManaType]]'s shorthand separated by a slash ("/").
 * 
 * @constructor create a new "twobrid" symbol
 * @param color color to use to get the discounted price
 * 
 * @author Alec Roelke
 */
case class TwobridSymbol private[symbol](color: ManaType) extends ManaSymbol(s"2_${color.toString.toLowerCase}_mana.png", s"2/${color.shorthand.toUpper}", 2, Map(color -> 0.5), Generator.TwobridSymbol)

/**
 * A "Phyrexian" mana symbol that can be paid with either of two colors of mana or 2 life. Its color intensity is 1/3 for each of its two colors of mana,
 * and 0 for the rest, as it has three ways to pay for it (one of two colors or 2 life), and its text representation is the shorthand of its colors and a P
 * separated by a slash ("/").
 * 
 * @constructor create a new Phyrexian hybrid mana symbol
 * @param first first color that can be used to pay
 * @param second second color that can be used to pay
 * 
 * @author Alec Roelke
 */
case class PhyrexianHybridSymbol private[symbol](first: ManaType, second: ManaType) extends ManaSymbol(
  s"phyrexian_${first.toString.toLowerCase}_${second.toString.toLowerCase}_mana.png",
  s"${first.shorthand.toUpper}/${second.shorthand.toUpper}/P",
  1,
  Map(first -> 1.0/3.0, second -> 1.0/3.0),
  Generator.PhyrexianHybridSymbol
)

/**
 * A mana symbol that can be paid with either of two colors of mana. Its color intensity is 0.5 for each of its two colors, and 0 for all the
 * rest of the types, as there are two ways to pay for it, and its text representation is the shorthand of its two [[ManaType]]s separated by
 * a slash ("/").
 * 
 * @constructor create a new hybrid mana symbol
 * @param first first color that can be used to pay
 * @param second second color that can be used to pay
 * 
 * @author Alec Roelke
 */
case class HybridSymbol private[symbol](first: ManaType, second: ManaType) extends ManaSymbol(
  s"${first.toString.toLowerCase}_${second.toString.toLowerCase}_mana.png",
  s"${first.shorthand.toUpper}/${second.shorthand.toUpper}",
  1,
  Map(first -> 0.5, second -> 0.5),
  Generator.HybridSymbol
)

/**
 * A "Phyrexian" mana symbol that can be paid either with a color of mana or 2 life. Its color intensity is 0.5 for its color of mana, and 0 for
 * the rest, as there are two ways to pay for it (its color and 2 life), and its text representation is the shorthand of its [[ManaType]] and a P
 * separated by a slash ("/").
 * 
 * @constructor create a new Phyrexian mana symbol
 * @param color color of the new symbol
 * 
 * @author Alec Roelke
 */
case class PhyrexianSymbol private[symbol](color: ManaType) extends ManaSymbol(s"phyrexian_${color.toString.toLowerCase}_mana.png", s"${color.shorthand.toUpper}/P", 1, Map(color -> 1), Generator.PhyrexianSymbol)

/**
 * Mana symbol representing a specific type of mana. Its intensity is 1 for its mana type, and 0 for all others, as it can only be paid for by that type,
 * and its text representation is just the shorthand of its [[ManaType]].
 * 
 * @constructor create a new symbol for a mana type
 * @param color type of mana the symbol represents
 * 
 * @author Alec Roelke
 */
case class ColorSymbol private[symbol](color: ManaType) extends ManaSymbol(s"${color.toString.toLowerCase}_mana.png", color.shorthand.toString, 1, Map(color -> 1), Generator.ColorSymbol)