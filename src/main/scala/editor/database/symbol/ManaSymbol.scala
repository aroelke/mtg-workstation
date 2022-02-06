package editor.database.symbol

import editor.database.attributes.ManaType

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

/**
 * Companion to [[ManaSymbol]] containing methods for parsing them from strings and other utility.
 * @author Alec Roelke
 */
object ManaSymbol extends SymbolParser[ManaSymbol] {
  /**
   * Convenience method for creating color intensity maps, as they should have all colors in them.  Mana types not explicitly
   * given weights will be set to 0 weight.
   * 
   * @param weights mapping of applicable mana types onto their weights for the symbol
   * @return a complete color intensity map including weights for all mana types
   */
  def createIntensity(weights: Map[ManaType, Double]) = ManaType.values.map((c) => c -> weights.getOrElse(c, 0.0)).toMap

  /**
   * Parse a [[ManaSymbol]] from a string.  The should contain only the symbol's text, not any braces.
   * 
   * @param s string to parse
   * @return the [[ManaSymbol]] corresponding to the string, or None if there isn't one
   */
  def parse(s: String) = {
/*
    for (generator <- Generator.values)
      println(s"  ${generator.toString}: $s -> ${generator.parse(s)}")
*/
    val result = Generator.values.flatMap(_.parse(s)).headOption
//    println(s"parsed $s into $result")
    result
  }

  /**
   * Not yet implemented.
   * 
   * @param symbols collection of symbols to sort in place
   */
  def sort(symbols: collection.mutable.Seq[ManaSymbol]): Unit = {}

  @deprecated def createIntensity(): java.util.Map[ManaType, java.lang.Double] = collection.mutable.Map.from(createIntensity(Map.empty).map{ case (c, n) => c -> new java.lang.Double(n) }).asJava
  @deprecated def tryParseManaSymbol(s: String) = parse(s).toJava
  @deprecated def parseManaSymbol(s: String) = parse(s).getOrElse(throw IllegalArgumentException(s"$s is not a mana symbol"))
  @deprecated def sort(symbols: java.util.List[ManaSymbol]): Unit = sort(symbols.asScala)
}

/**
 * A symbol representing an amount and type of mana that can be paid.
 * 
 * @constructor create a new mana symbol
 * @param icon file name of the icon to display for the symbol
 * @param text shorthand text of the symbol to show between braces when printing it out
 * @param value mana value of the symbol
 * 
 * @author Alec Roelke
 */
abstract class ManaSymbol private[symbol](icon: String, text: String, val value: Double, val generator: Generator[?, ? <: ManaSymbol]) extends Symbol(icon, text) with Ordered[ManaSymbol] {
  /**
   * Get the color intensity map of this symbol. Each mana type is mapped onto an "intensity," which is a value that loosely represents the fraction of
   * the number of ways the symbol can be paid for that the mana type is. This is used mainly for sorting symbols and mana costs. See each symbol for an
   * explanation on its color intensity.
   * 
   * @return the color intensity of this symbol
   * @see [[ColorIntensity]]
   */
  def colorIntensity: Map[ManaType, Double]

  override def compare(other: ManaSymbol) = if (generator.ordinal == other.generator.ordinal) generator.compare(this, other) else generator.ordinal - other.generator.ordinal
}