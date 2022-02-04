package editor.database.symbol

import editor.database.attributes.ManaType
import editor.util.UnicodeSymbols

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

/**
 * Companion containing all of the possible [[StaticSymbol]]s and method for parsing them from strings.
 * @author Alec Roelke
 */
object StaticSymbol {
  @deprecated val SYMBOLS = StaticSymbolGenerator.values.asJava
  @deprecated def parseStaticSymbol(s: String) = StaticSymbolGenerator.values(s)
  @deprecated def tryParseStaticSymbol(s: String) = StaticSymbolGenerator.parse(s).toJava
}

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
class StaticSymbol private[symbol](icon: String, text: String, value: Double) extends ManaSymbol(icon, text, value) {
  override def colorIntensity = ManaSymbol.createIntensity(Map(ManaType.COLORLESS -> value))
}