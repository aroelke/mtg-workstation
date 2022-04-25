package editor.database.attributes

import java.awt.Color

object ManaType {
  val colors = Seq(White, Blue, Black, Red, Green)

  def valueOf(s: Char) = values.find(_.shorthand == s.toUpper).getOrElse(throw IllegalArgumentException(s"unknown mana type $s"))

  def parse(s: String) = values.find((c) => c.toString.equalsIgnoreCase(s) || c.shorthand.toString.equalsIgnoreCase(s))

  def sorted(types: Iterable[ManaType]) = {
    val counts = types.groupBy(identity).map{ case (c, cs) => c -> cs.size }
    val sorted = (counts - Colorless).keys.toSeq.sorted match {
      case Seq(a, b) => if (a.colorOrder(b) > 0) Seq(a, b) else Seq(b, a)
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
    Seq.fill(counts(Colorless))(Colorless) ++ sorted.flatMap((c) => Seq.fill(counts(c))(c))
  }

  @deprecated def COLORLESS() = Colorless
}

enum ManaType(val shorthand: Char, val color: Option[Color]) extends Ordered[ManaType] {
  import ManaType._

  def colorOrder(other: ManaType) = {
    if (this == Colorless && other == Colorless) 0
    else if (this == Colorless) -1
    else if (other == Colorless) 1
    else {
      val diff = ordinal - other.ordinal
      if (math.abs(diff) <= 2) diff else -diff
    }
  }

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