package editor.database.attributes

import editor.database.symbol.ManaSymbol
import editor.gui.generic.ComponentUtils

import scala.util.matching._

object ManaCost {
  val Pattern = raw"\{([cwubrgCWUBRG\/phPH\dsSxXyYzZ]+)\}".r

  def parse(s: String) = {
    val symbols = Pattern.findAllMatchIn(s).map(_.group(1)).flatMap(ManaSymbol.parse).toSeq
    val remainder = symbols.foldLeft(s.toUpperCase)((r, t) => r.replaceFirst(Regex.quote(t.toString.toUpperCase), ""))
    Option.when(remainder.isEmpty)(ManaCost(symbols))
  }
}

/**
 * A list of zero or more [[ManaSymbol]]s which also has a numerical "mana value" which is the sum of those of
 * those symbols.
 * 
 * @constructor create a new mana cost
 * @param cost list of symbols in the new cost
 * 
 * @author Alec Roelke
 */
case class ManaCost(private val cost: Seq[ManaSymbol] = Seq.empty) extends Seq[ManaSymbol] with Ordered[ManaCost] {
  /** Total color intensity of the mana cost. @see [[ManaSymbol.colorIntensity]] */
  lazy val intensity = ManaType.values.map((t) => t -> cost.map(_.colorIntensity(t)).sum).filter{ case (_, c) => c > 0 }.toMap.withDefaultValue(0.0)

  /** Set of all of the colors in the mana cost. */
  lazy val colors = intensity.keys.toSet - ManaType.Colorless

  /** Mana value of the mana cost, determined by the sum of the mana values of its symbols. */
  lazy val manaValue = cost.map(_.value).sum

  /**
   * Determine if this mana cost is a subset of the other one.
   * 
   * @param other mana cost to compare with
   * @return true if the other mana cost contains all of the symbols of this one, including counts of duplicates, or
   * false otherwise
   */
  def isSubset(other: ManaCost) = {
    val myCounts = groupBy(identity).map{ case (s, ss) => s -> ss.size }.toMap
    val oCounts = other.groupBy(identity).map{ case (s, ss) => s -> ss.size }.toMap.withDefaultValue(0)
    myCounts.forall{ case (s, n) => n <= oCounts(s) }
  }

  /**
   * Determine if this mana cost is a superset of the other one.
   * 
   * @param other mana cost to compare with
   * @return true if this mana cost contains all of the symbols of the other one, including counts of duplicates,
   * or false otherwise.
   */
  def isSuperset(other: ManaCost) = other.isSubset(this)

  override def apply(i: Int) = cost(i)
  override def iterator = cost.iterator
  override def length = cost.length

  override def compare(other: ManaCost) = {
    if (isEmpty && other.isEmpty) 0
    else if (isEmpty) -1
    else if (other.isEmpty) 1
    else {
      // First, compare by mana value
      var diff = (2*(manaValue - other.manaValue)).toInt
      if (diff == 0) {
        // If mana value is the same, compare by greatest color intensity
        val sortedIntensities = intensity.values.toSeq.sorted.reverse
        val otherIntensities = other.intensity.values.toSeq.sorted.reverse
        diff = sortedIntensities.zipAll(otherIntensities, 0.0, 0.0).reverse.zipWithIndex.map{ case ((x, y), i) => (x - y)*math.pow(10, i) }.sum.toInt
      }
      // If intensities are the same, compare by color
      if (diff == 0)
        diff = zip(other).collect{ case (a, b) if a != b => a.compare(b) }.headOption.getOrElse(0)
      // If color is the same, the mana values are the same
      diff
    }
  }

  /** String containing HTML code to display this mana cost in HTML documents. */
//  lazy val toHTMLString = cost.map((s) => s"""<img src="${getClass.getResource(s"/images/icons/${s.name}")}" width="${ComponentUtils.TextSize}" height="${ComponentUtils.TextSize}"/>""").mkString

  override lazy val toString = map(_.toString).mkString
}