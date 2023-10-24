package editor.database.attributes

/**
 * Starting stat of a card that is tracked via counters (e.g. loyalty or defense). Some have
 * variable values, either starting (X) or based on game state (*), instead of concrete numbers.
 * 
 * @constructor create a new starting loyalty
 * @param value amount of loyalty
 * 
 * @author Alec Roelke
 */
case class CounterStat(value: Double) extends Ordered[CounterStat] {
  /** Whether or not the card actually has loyalty (i.e. is a planeswalker) */
  val exists = !value.isNaN

  /** Whether or not the starting loyalty is variable. */
  val variable = value < 0

  override def compare(that: CounterStat) = {
    val c = value.compare(that.value)
    if (c == 0) {
      if (variable && !that.variable)
        1
      else if (!variable && that.variable)
        -1
      else // both variable or neither variable
        0
    } else c
  }

  override val toString = value match {
    case CounterStat.X    => "X"
    case CounterStat.STAR => "*"
    case _ => value.toInt.toString
  }
}

object CounterStat {
  /**
   * Create a loyalty value from a string. The only permitted options are integers,
   * "X", "*", and the empty string (to represent no loyalty).
   * 
   * @param s string to parse
   * @return a new loyalty value
   */
  def apply(s: String): CounterStat = CounterStat(s.toUpperCase match {
    case "X" => CounterStat.X
    case "*" => CounterStat.STAR
    case  u  => u.toDouble
  })

  /** Number that represents a starting loyalty of X (determined as the planeswalker enters). */
  val X: Double = -1
  /** Number that represents continuously variable loyalty (based on some other part of the game state). */
  val STAR: Double = -2
}