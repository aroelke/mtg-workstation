package editor.database.attributes

import editor.unicode.{_, given}

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
case class CombatStat private(expression: String, value: Double) extends Ordered[CombatStat] {
  /** Whether or not the combat stat can vary throughout the game (i.e. has a * in it) */
  lazy val variable = expression.contains('*')

  override def compare(that: CombatStat) = {
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

  override def toString = expression
}

object CombatStat {
  /**
   * Create a new combat stat, inferring its expression from its value.
   * 
   * @param value numeric value of the combat stat
   * @return a new combat stat
   */
  def apply(value: Double): CombatStat = CombatStat(value.toString, value)

  /**
   * Create a new combat stat, inferring its value from its expression.
   * 
   * @param expression string expression determining the combat stat
   * @return a new combat stat
   */
  def apply(expression: String): CombatStat = {
    val e = expression.replaceAll("\\s+", "")
    val value = {
      val x = e.replaceAll(s"[*?$SuperscriptTwo]+", "").replaceAll("[+-]$", "").replace(OneHalf, ".5")
      if (x.isEmpty)
        0
      else if (x == Infinity)
        Double.PositiveInfinity
      else
        x.toDouble
    }
    CombatStat(e, value)
  }
}