package editor.database.attributes

import editor.database.symbol.ManaSymbol

import scala.util.matching._
import editor.database.symbol.ManaSymbolInstances

object ManaCost {
  private val pattern = raw"\{([cwubrgCWUBRG\/phPH\ddDlLsSxXyYzZ]+)\}".r

  /** Regular expression that can be used to extract a mana cost from a string, along with [[parse]]. */
  val Pattern = raw"(?:${pattern.regex})+".r

  /**
   * Parse a string for a mana cost.  The string should consist of one or more string representations of [[ManaSymbol]]s, each of which
   * is surrounded by braces, with no spacing in between.
   * 
   * @param s string to parse
   * @return The [[ManaCost]] parsed from the string, or None if it couldn't be parsed
   */
  def parse(s: String) = {
    val symbols = pattern.findAllMatchIn(s).map(_.group(1)).flatMap(ManaSymbol.parse).toIndexedSeq
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
case class ManaCost(private val cost: IndexedSeq[ManaSymbol] = IndexedSeq.empty) extends IndexedSeq[ManaSymbol] with Ordered[ManaCost] {
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

  /**
   * Calculate the devotion to a set of colors this mana cost contributes. A player's devotion to a
   * set of colors is the number of mana symbols of any color in that set among the mana costs of
   * permanents they control. This has an extended definition for colorless as well that works
   * specifically with the colorless mana type (NOT generic symbols) for potential future
   * compatibility.
   * 
   * @param types set of mana types to calculate devotion for
   * @return the devotion to the set of mana types this mana cost contributes
   */
  def devotionTo(types: Set[ManaType]): Int = count((s) => {
    if (s == ManaSymbolInstances.ColorSymbol(ManaType.Colorless))
      types.contains(ManaType.Colorless)
    else
      (types - ManaType.Colorless).map(s.colorIntensity).sum > 0
  })

  /**
   * Convenience method for calculating the devotion to a single mana type that this mana cost contributes.
   * 
   * @param mana type of mana to calculate devotion for
   * @return the devotion to the mana type this mana cost contributes
   */
  def devotionTo(mana: ManaType): Int = devotionTo(Set(mana))

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

  override lazy val toString = map(_.toString).mkString
}