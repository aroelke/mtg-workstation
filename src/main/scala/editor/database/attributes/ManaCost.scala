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

case class ManaCost(private val cost: Seq[ManaSymbol] = Seq.empty) extends Seq[ManaSymbol] with Ordered[ManaCost] {
  lazy val intensity = ManaType.values.map((t) => t -> cost.map(_.colorIntensity(t)).sum).filter{ case (_, c) => c > 0 }.toMap.withDefaultValue(0.0)

  lazy val colors = intensity.keys.toSet - ManaType.Colorless

  lazy val manaValue = cost.map(_.value).sum

  def isSubset(other: ManaCost) = {
    val myCounts = groupBy(identity).map{ case (s, ss) => s -> ss.size }.toMap
    val oCounts = other.groupBy(identity).map{ case (s, ss) => s -> ss.size }.toMap.withDefaultValue(0)
    myCounts.forall{ case (s, n) => n <= oCounts(s) }
  }

  def isSuperset(other: ManaCost) = other.isSubset(this)

  override def apply(i: Int) = cost(i)
  override def iterator = cost.iterator
  override def length = cost.length

  override def compare(other: ManaCost) = {
    if (isEmpty && other.isEmpty) 0
    else if (isEmpty) -1
    else if (other.isEmpty) 1
    else {
      var diff = (2*(manaValue - other.manaValue)).toInt
      if (diff == 0) {
        val sortedIntensities = intensity.values.toSeq.sorted
        val otherIntensities = other.intensity.values.toSeq.sorted
        diff = sortedIntensities.zipAll(otherIntensities, 0.0, 0.0).zipWithIndex.map{ case ((x, y), i) => math.abs(x - y)*math.pow(10, i) }.sum.toInt
      }
      if (diff == 0) {
        diff = zip(other).filter{ case (a, b) => a != b }.map(_.compare(_)).headOption.getOrElse(0)
      }
      diff
    }
  }

  lazy val toHTMLString = cost.map((s) => s"""<img src="${getClass.getResource(s"/images/icons/${s.name}")}" width="${ComponentUtils.TextSize}" height="${ComponentUtils.TextSize}"/>""").mkString

  override lazy val toString = map(_.toString).mkString
}