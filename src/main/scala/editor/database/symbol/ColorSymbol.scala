package editor.database.symbol

import editor.database.attributes.ManaType

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

/**
 * Companion to [[ColorSymbol]] containing all (currently) possible mana type symbols, as well as functions for
 * parsing them from strings.
 * @author Alec Roelke
 */
object ColorSymbol {
  /** All possible mana type symbols. @see [[ManaType]] */
  val values = ManaType.values.map(s => s -> ColorSymbol(s)).toMap

  /**
   * Parse a [[ColorSymbol]] from a string.
   * 
   * @param s string to parse
   * @return the [[ColorSymbol]] corresponding to the string, or None if there isn't one
   */
  def parse(s: String) = Option(ManaType.tryParseManaType(s)).map(values)

  @deprecated val SYMBOLS = values.asJava
  @deprecated def tryParseColorSymbol(s: String) = parse(s).toJava
  @deprecated def parseColorSymbol(s: String) = values(ManaType.parseManaType(s))
}

/**
 * Mana symbol representing a specific type of mana.
 * 
 * @constructor create a new symbol for a mana type
 * @param color type of mana the symbol represents
 * 
 * @author Alec Roelke
 */
class ColorSymbol private(private val color: ManaType) extends ManaSymbol(s"${color.toString.toLowerCase}_mana.png", color.shorthand.toString, 1) {
  override def colorIntensity = ManaSymbol.createIntensity(Map(color -> 1))

  override def compareTo(o: ManaSymbol) = o match {
    case c: ColorSymbol => color.compareTo(c.color)
    case _ => super.compareTo(o)
  }
}