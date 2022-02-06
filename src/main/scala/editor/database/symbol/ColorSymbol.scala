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
  @deprecated val SYMBOLS = ColorSymbolGenerator.values.asJava
  @deprecated def tryParseColorSymbol(s: String) = ColorSymbolGenerator.parse(s).toJava
  @deprecated def parseColorSymbol(s: String) = ColorSymbolGenerator.values(ManaType.parseManaType(s))
}

/**
 * Mana symbol representing a specific type of mana. Its intensity is 1 for its mana type, and 0 for all others, as it can only be paid for by that type,
 * and its text representation is just the shorthand of its [[ManaType]].
 * 
 * @constructor create a new symbol for a mana type
 * @param color type of mana the symbol represents
 * 
 * @author Alec Roelke
 */
class ColorSymbol private[symbol](val color: ManaType) extends ManaSymbol(s"${color.toString.toLowerCase}_mana.png", color.shorthand.toString, 1, ColorSymbolGenerator) {
  override def colorIntensity = ManaSymbol.createIntensity(Map(color -> 1))
}