package editor.database.symbol

import editor.database.attributes.ManaType

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

object ManaSymbol {
  private val Order = Seq(
    classOf[VariableSymbol],
    classOf[StaticSymbol],
    classOf[GenericSymbol],
    classOf[HalfColorSymbol],
    classOf[TwobridSymbol],
    classOf[PhyrexianHybridSymbol],
    classOf[HybridSymbol],
    classOf[PhyrexianSymbol],
    classOf[ColorSymbol]
  )

  def createIntensity(weights: Map[ManaType, Double]) = ManaType.values.map((c) => c -> weights.getOrElse(c, 0.0)).toMap

  def parse(s: String) = {
    ColorSymbol.parse(s).orElse(
    GenericSymbol.parse(s).orElse(
    HalfColorSymbol.parse(s).orElse(
    PhyrexianHybridSymbol.parse(s).orElse(
    HybridSymbol.parse(s).orElse(
    PhyrexianSymbol.parse(s).orElse(
    TwobridSymbol.parse(s).orElse(
    VariableSymbol.parse(s).orElse(
    StaticSymbol.parse(s)
    ))))))))
  }

  def sort(symbols: collection.mutable.Seq[ManaSymbol]): Unit = {}

  @deprecated def createIntensity(): java.util.Map[ManaType, java.lang.Double] = collection.mutable.Map.from(createIntensity(Map.empty).map{ case (c, n) => c -> new java.lang.Double(n) }).asJava
  @deprecated def tryParseManaSymbol(s: String) = parse(s).toJava
  @deprecated def parseManaSymbol(s: String) = parse(s).getOrElse(throw IllegalArgumentException(s"$s is not a mana symbol"))
  @deprecated def sort(symbols: java.util.List[ManaSymbol]): Unit = sort(symbols.asScala)
}

abstract class ManaSymbol private[symbol](icon: String, text: String, val value: Double) extends Symbol(icon, text) with Comparable[ManaSymbol] {
  import ManaSymbol.Order

  def colorIntensity: Map[ManaType, Double]

  override def compareTo(other: ManaSymbol) = {
    if (Order.contains(getClass) && Order.contains(other.getClass))
      Order.indexOf(getClass) - Order.indexOf(other.getClass)
    else if (!Order.contains(getClass))
      1
    else if (!Order.contains(other.getClass))
      -1
    else
      0
  }
}