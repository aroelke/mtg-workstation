package editor.database.attributes

import editor.util.UnicodeSymbols

/**
 * A creature's power or toughness value on a card. Sometimes power and/or toughness is determined using an expression,
 * like "1 + *." In such cases, the numeric value of the stat is determined by treating the variable as 0.
 * 
 * @constructor create a new power/toughness value
 * @param expression string version of the combat stat
 * @param value numeric value of the combat stat
 * 
 * @author Alec Roelke
 */
case class CombatStat(expression: String, value: Double) extends OptionalAttribute with Ordered[CombatStat] {
  @deprecated def this(v: Double) = this(if (v.isNaN) "" else v.toString, v)
  @deprecated def this(e: String) = this(e.replaceAll("\\s+", ""), if (e.isEmpty) Double.NaN else {
    val x = e.replaceAll("[*?" + UnicodeSymbols.SUPERSCRIPT_TWO + "]+", "").replaceAll("[+-]$", "").replace(String.valueOf(UnicodeSymbols.ONE_HALF), ".5")
    if (x.isEmpty)
      0
    else if (x == UnicodeSymbols.INFINITY.toString)
      Double.PositiveInfinity
    else
      x.toDouble
  })

  /** Whether or not the card has power or toughness. */
  override val exists = !value.isNaN

  /** Whether or not the combat stat can vary throughout the game (i.e. has a * in it) */
  lazy val variable = expression.contains('*')

  override def compare(that: CombatStat) = {
    if (!exists && !that.exists)
      return 0;
    else if (!exists)
      return 1;
    else if (!that.exists)
      return -1;
    else
      return (2*value - 2*that.value).toInt;
  }

  override val toString = expression
}

object CombatStat {
  /**
   * Create a new combat stat, inferring its expression from its value.
   * 
   * @param value numeric value of the combat stat
   * @return a new combat stat
   */
  def apply(value: Double): CombatStat = CombatStat(if (value.isNaN) "" else value.toString, value)

  /**
   * Create a new combat stat, inferring its value from its expression.
   * 
   * @param expression string expression determining the combat stat
   * @return a new combat stat
   */
  def apply(expression: String): CombatStat = {
    val e = expression.replaceAll("\\s+", "")
    val value = if (e.isEmpty) Double.NaN else {
      val x = e.replaceAll("[*?" + UnicodeSymbols.SUPERSCRIPT_TWO + "]+", "").replaceAll("[+-]$", "").replace(String.valueOf(UnicodeSymbols.ONE_HALF), ".5")
      if (x.isEmpty)
        0
      else if (x == UnicodeSymbols.INFINITY.toString)
        Double.PositiveInfinity
      else
        x.toDouble
    }
    CombatStat(e, value)
  }

  /** Combat stat value used to indicate that a card doesn't have it. */
  val NoCombat = CombatStat(Double.NaN)
  @deprecated def NO_COMBAT() = NoCombat
}