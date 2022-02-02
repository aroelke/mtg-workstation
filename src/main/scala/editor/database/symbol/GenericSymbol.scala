package editor.database.symbol

import scala.jdk.OptionConverters._

/**
 * Companion to [[GenericSymbol]] containing all the currently-used generic mana symbols and functions for parsing them
 * from strings.
 * @author Alec Roelke
 */
object GenericSymbol {
  /** Highest currently-used contiguous generic mana symbol. */
  val Max = 20

  /** All of the contiguous generic mana symbols from {0} through {[[Max]]}. */
  val Contiguous = (0 to Max).map(new GenericSymbol(_)).toSeq

  /** Generic mana symbol for 100 mana. */
  val Hundred = new GenericSymbol(100)

  /** Generic mana symbol for 1000000 mana. */
  val Million = new GenericSymbol(1000000)

  /** All currently-used generic mana symbols: {0} through {[[Max]]}, {100}, and {1000000}. */
  val values = (Contiguous.zipWithIndex.map{ case (a, b) => b -> a } ++ Seq(100 -> Hundred, 1000000 -> Million)).toMap

  /**
   * @param n amount of mana needed to pay for the symbol
   * @return the [[GenericSymbol]] corresponding to the amount of mana needed to pay it
   */
  def apply(n: Int) = n match {
    case 1000000 => Million
    case 100 => Hundred
    case _ if n <= 20 => Contiguous(n)
    case _ => throw ArrayIndexOutOfBoundsException(n)
  }

  /**
   * Parse a [[GenericSymbol]] from a string, which should contain only the amount of mana it represents.
   * 
   * @param s string to parse
   * @return the [[GenericSymbol]] represented by the string, or None if there isn't one
   */
  def parse(s: String) = try {
    Some(apply(s.toInt))
  } catch case _ @ (_: NumberFormatException | _: ArrayIndexOutOfBoundsException) => None

  @deprecated val HIGHEST_CONSECUTIVE = Max
  @deprecated val N = Contiguous.toArray
  @deprecated val HUNDRED = Hundred
  @deprecated val MILLION = Million
  @deprecated def get(n: Int) = apply(n)
  @deprecated def tryParseGenericSymbol(s: String) = parse(s).toJava
  @deprecated def parseGenericSymbol(s: String) = apply(s.toInt)
}

/**
 * A mana symbol that can be paid for using an amount of any type of mana in any combination.
 * 
 * @constructor create a new generic mana symbol for a particular amount of mana
 * @param amount amount of mana needed to pay for the symbol
 * 
 * @author Alec Roelke
 */
class GenericSymbol private(amount: Int) extends ManaSymbol(s"${amount}_mana.png", amount.toString, amount) {
  override def colorIntensity = ManaSymbol.createIntensity(Map.empty)

  override def compareTo(o: ManaSymbol) = o match {
    case g: GenericSymbol => value.toInt - o.value.toInt
    case _ => super.compareTo(o)
  }
}