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
  /** All the possible [[VariableSymbols]]: X, Y, and Z. */
  val values = Seq('X', 'Y', 'Z').map((v) => v.toString -> VariableSymbol(v)).toMap

  /**
   * Parse a [[VariableSymbol]] from a string.
   * 
   * @param s string to parse
   * @return a [[VariableSymbol]] using the letter in the string as its variable, or None if there isn't one
   */
  def parse(s: String) = values.get(s)

  @deprecated val SYMBOLS = values.asJava
  @deprecated def parseVariableSymbol(s: String) = values(s)
  @deprecated def tryParseVariableSymbol(s: String) = parse(s).toJava
}

/**
 * A symbol that can be paid using any amount of mana, indicating using a letter like X.
 * 
 * @constructor create a new variable symbol using a particular variable name
 * @param variable letter to use as the variable name
 * 
 * @author Alec Roelke
 */
class VariableSymbol private(private val variable: Char) extends ManaSymbol(s"${variable.toLower}_mana.png", variable.toString.toUpperCase, 0) {
  override def colorIntensity = ManaSymbol.createIntensity(Map(ManaType.COLORLESS -> 0.5))

  override def compareTo(other: ManaSymbol) = other match {
    case s: VariableSymbol => variable.toUpper - s.variable.toUpper
    case _ => super.compareTo(other)
  }
}