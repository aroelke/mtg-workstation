package editor.database.symbol

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

/**
 * Companion to [[GenericSymbol]] containing all the currently-used generic mana symbols and functions for parsing them
 * from strings.
 * @author Alec Roelke
 */
object GenericSymbol {
  @deprecated val HIGHEST_CONSECUTIVE = 20
  @deprecated val N = GenericSymbolGenerator.values.slice(0, HIGHEST_CONSECUTIVE + 1).asJava
  @deprecated val HUNDRED = GenericSymbolGenerator.values(100)
  @deprecated val MILLION = GenericSymbolGenerator.values(1000000)
  @deprecated def get(n: Int) = GenericSymbolGenerator.values(n)
  @deprecated def tryParseGenericSymbol(s: String) = GenericSymbolGenerator.parse(s).toJava
  @deprecated def parseGenericSymbol(s: String) = get(s.toInt)
}

/**
 * A mana symbol representing an amount of any combination of mana. Its color intensity is 0 for all mana types, as any type
 * of mana can pay for it, and its text representation is simply the amount of mana it represents.
 * 
 * @constructor create a new generic mana symbol for a particular amount of mana
 * @param amount amount of mana needed to pay for the symbol
 * 
 * @author Alec Roelke
 */
class GenericSymbol private[symbol](amount: Int) extends ManaSymbol(s"${amount}_mana.png", amount.toString, amount) {
  override def colorIntensity = ManaSymbol.createIntensity(Map.empty)

  override def compareTo(o: ManaSymbol) = o match {
    case g: GenericSymbol => value.toInt - o.value.toInt
    case _ => super.compareTo(o)
  }
}