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
   * Parse a [[ManaSymbol]] from a string.  The should contain only the symbol's text, not any braces.
   * 
   * @param s string to parse
   * @return the [[ManaSymbol]] corresponding to the string, or None if there isn't one
   */
  def parse(s: String) = Generator.values.flatMap(_.parse(s)).headOption

  /**
   * Not yet implemented.
   * 
   * @param symbols collection of symbols to sort in place
   */
  def sort(symbols: collection.mutable.Seq[ManaSymbol]): Unit = {}

  @deprecated def createIntensity(): java.util.Map[ManaType, java.lang.Double] = collection.mutable.Map.from(ManaType.values.map(_ -> new java.lang.Double(0))).asJava
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
 * @param intensity defined color intensities of the symbol
 * @param generator object used for generating symbol instances and comparing them
 * 
 * @author Alec Roelke
 */
class ManaSymbol private[symbol](icon: String, text: String, val value: Double, intensity: Map[ManaType, Double], val generator: Generator[?, ? <: ManaSymbol]) extends Symbol(icon, text) with Ordered[ManaSymbol] {
  /**
   * Get the color intensity map of this symbol. Each mana type is mapped onto an "intensity," which is a value that loosely represents the fraction of
   * the number of ways the symbol can be paid for that the mana type is. This is used mainly for sorting symbols and mana costs. See each symbol for an
   * explanation on its color intensity.
   * 
   * @return the color intensity of this symbol
   * @see [[ColorIntensity]]
   */
  val colorIntensity = intensity.withDefaultValue(0)

  override def compare(other: ManaSymbol) = if (generator.ordinal == other.generator.ordinal) generator.compare(this, other) else generator.ordinal - other.generator.ordinal
}