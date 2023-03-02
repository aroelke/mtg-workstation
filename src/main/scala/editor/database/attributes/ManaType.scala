package editor.database.attributes

import java.awt.Color

/**
 * Object containing global data and parsing methods for [[ManaType]]s.
 * @author Alec Roelke
 */
object ManaType {
  /** List of mana types, in order, that have color. */
  val colors = Seq(White, Blue, Black, Red, Green)

  /**
   * Look up the character corresponding to a [[ManaType]]'s shorthand, ignoring case.
   * 
   * @param s character to look up
   * @return the [[ManaType]] whose shorthand matches the character
   */
  @throws[IllegalArgumentException]("if there is no mana type corresponding to the character")
  def valueOf(s: Char) = values.find(_.shorthand == s.toUpper).getOrElse(throw IllegalArgumentException(s"unknown mana type $s"))

  /**
   * Parse a string for a [[ManaType]]. The string should either contain the whole name of the mana type or
   * be just its single-character shorthand, ignoring case.
   * 
   * @param s string to parse
   * @return the [[ManaType]] whose name or shorthand matches the string, or None if there isn't one
   */
  def parse(s: String) = values.find((c) => c.toString.equalsIgnoreCase(s) || c.shorthand.toString.equalsIgnoreCase(s))

  /**
   * Create a sorted version of the input list of [[ManaType]]s based on their order around the color wheel and how they
   * are typically shown on physical cards.  Use this function to sort a list of [[ManaType]]s rather than any built-in sorting
   * functions. The original list is not changed.
   * 
   * @param types list of types to sort
   * @return a new list containing the same types in the same amounts, but sorted
   */
  def sorted(types: Iterable[ManaType]) = {
    val counts = types.groupBy(identity).map{ case (c, cs) => c -> cs.size }
    val sorted = (counts - Colorless).keys.toSeq.sorted match {
      case Seq(a, b) => if (a.colorOrder(b) < 0) Seq(a, b) else Seq(b, a)
      case s if s.size == 3 =>
        var sorted = s
        while (sorted(0).distanceFrom(sorted(1)) != sorted(1).distanceFrom(sorted(2)))
          sorted = sorted.tail :+ sorted.head
        sorted
      case s if s.size == 4 =>
        val missing = (colors.toSet -- s).head
        var sorted = s
        while (missing.distanceFrom(sorted.head) != 1)
          sorted = sorted.tail :+ sorted.head
        sorted
      case s => s
    }
    Seq.fill(counts.getOrElse(Colorless, 0))(Colorless) ++ sorted.flatMap((c) => Seq.fill(counts(c))(c))
  }
}

/**
 * A type of mana in Magic: The Gathering. Colored mana exists in a wheel that creates a relative ordering of the colors,
 * outside of which is colorless mana, that is depicted on the back of all tournament-legal cards. This wheel can be used
 * to sort lists of mana types. The name of each constant is used to create the long-form name of the mana type.
 * 
 * @constructor create a new mana type
 * @param shorthand single-character representation of mana types in text
 * @param color color to use for depicting the type
 * 
 * @author Alec Roelke
 */
enum ManaType(val shorthand: Char, val color: Option[Color]) extends Ordered[ManaType] {
  import ManaType._

  /**
   * Relative order of this mana type compared to the other one in the shortest direction around the color wheel.
   * 
   * @param other mana type to compare with
   * @return 0 if the two types are the same, a negative number if this type should come before the other type, or a positive
   * number if it should come after
   * @note this should not be used for sorting lists, as it can produce different results depending on the order of entries.  Use [[ManaType.sort]] instead.
   */
  def colorOrder(other: ManaType) = {
    if (this == Colorless && other == Colorless) 0
    else if (this == Colorless) -1
    else if (other == Colorless) 1
    else {
      val diff = ordinal - other.ordinal
      if (math.abs(diff) <= 2) diff else -diff
    }
  }

  /** @return the shortest distance around the color wheel between this type and another */
  def distanceFrom(other: ManaType) = if (this == Colorless || other == Colorless) throw IllegalArgumentException("colorless is not a color") else {
    (other.ordinal - ordinal + colors.length) % colors.size
  }

  override def compare(other: ManaType) = ordinal - other.ordinal

  case Colorless extends ManaType('C', None)
  case White extends ManaType('W', Some(Color.YELLOW.darker))
  case Blue  extends ManaType('U', Some(Color.BLUE))
  case Black extends ManaType('B', Some(Color.BLACK))
  case Red   extends ManaType('R', Some(Color.RED))
  case Green extends ManaType('G', Some(Color.GREEN.darker))
}