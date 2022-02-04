package editor.database.symbol

import editor.database.attributes.ManaType

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

/**
 * Companion to [[VariableSymbol]] containing all the possible variables used by the game and functions for parsing
 * them from strings.
 *
 * @author Alec Roelke
 */
object VariableSymbol {
  @deprecated val SYMBOLS = VariableSymbolGenerator.values.asJava
  @deprecated def parseVariableSymbol(s: String) = VariableSymbolGenerator.values(s(0).toUpper)
  @deprecated def tryParseVariableSymbol(s: String) = VariableSymbolGenerator.parse(s).toJava
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
class VariableSymbol private[symbol](private val variable: Char) extends ManaSymbol(s"${variable.toLower}_mana.png", variable.toString.toUpperCase, 0) {
  override def colorIntensity = ManaSymbol.createIntensity(Map(ManaType.COLORLESS -> 0.5))

  override def compareTo(other: ManaSymbol) = other match {
    case s: VariableSymbol => variable.toUpper - s.variable.toUpper
    case _ => super.compareTo(other)
  }
}