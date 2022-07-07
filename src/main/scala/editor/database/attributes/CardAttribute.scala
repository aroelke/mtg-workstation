package editor.database.attributes

import editor.filter.leaf.FilterLeaf
import scala.reflect.ClassTag
import editor.collection.CardListEntry
import java.text.Collator
import editor.filter.leaf.TextFilter
import editor.filter.leaf.ManaCostFilter
import editor.filter.leaf.NumberFilter
import editor.filter.leaf.ColorFilter
import editor.filter.leaf.TypeLineFilter
import editor.filter.leaf.options.multi.CardTypeFilter
import editor.filter.leaf.options.multi.LegalityFilter
import editor.filter.leaf.options.multi.SubtypeFilter
import editor.filter.leaf.options.multi.SupertypeFilter
import editor.filter.leaf.options.multi.TagsFilter
import editor.filter.leaf.options.single.BlockFilter
import editor.filter.leaf.options.single.ExpansionFilter
import editor.filter.leaf.options.single.LayoutFilter
import editor.filter.leaf.options.single.RarityFilter
import editor.filter.leaf.VariableNumberFilter
import editor.database.card.CardLayout
import editor.collection.Categorization
import scala.jdk.CollectionConverters._
import java.time.LocalDate
import editor.filter.leaf.BinaryFilter

// D: type returned from CardTableEntry.apply
sealed trait CardAttribute[D : ClassTag](name: String, val description: String) extends Ordering[D] {
  def filter: Option[FilterLeaf]

  def ordinal = CardAttribute.values.indexOf(this)
  def any_compare(x: Any, y: Any): Int = (x, y) match {
    case (a: D, b: D) => compare(a, b)
    case (a: D, b) => throw ClassCastException(s"$b: ${b.getClass} is not an instance of ${dataType}")
    case (a, b: D) => throw ClassCastException(s"$a: ${a.getClass} is not an instance of ${dataType}")
    case (a, b) => throw ClassCastException(s"$a: ${a.getClass} is not an instance of ${dataType} and $b: ${b.getClass} is not an instance of ${dataType}")
  }
  def comparingCard: Ordering[CardListEntry] = (a: CardListEntry, b: CardListEntry) => compare(a(this) match { case v: D => v }, b(this) match { case v: D => v})
  def dataType = implicitly[ClassTag[D]].runtimeClass.asInstanceOf[Class[D]]

  override def toString = name
}

object CardAttribute {
  case object Name extends CardAttribute[String]("Name", "Card Name") {
    override def compare(x: String, y: String) = Collator.getInstance.compare(x, y)
    override def filter = Some(TextFilter(this, _.normalizedName))
  }

  case object RulesText extends CardAttribute[Unit]("Rules Text", "Up-to-date Oracle text") {
    override def compare(x: Unit, y: Unit) = throw UnsupportedOperationException()
    override def filter = Some(TextFilter(this, _.normalizedOracle))
  }

  case object FlavorText extends CardAttribute[Unit]("Flavor Text", "Flavor text") {
    override def compare(x: Unit, y: Unit) = throw UnsupportedOperationException()
    override def filter = Some(TextFilter(this, _.normalizedFlavor))
  }

  case object PrintedText extends CardAttribute[Unit]("Printed Text", "Rules text as printed on the card") {
    override def compare(x: Unit, y: Unit) = throw UnsupportedOperationException()
    override def filter = Some(TextFilter(this, _.normalizedPrinted))
  }

  case object ManaCost extends CardAttribute[Seq[ManaCost]]("Mana Cost", "Mana cost, including symbols") {
    override def compare(x: Seq[ManaCost], y: Seq[ManaCost]) = x(0).compare(y(0))
    override def filter = Some(ManaCostFilter())
  }

  case object RealManaValue extends CardAttribute[Double]("Real Mana Value", "Card mana value as defined by the rules") {
    override def compare(x: Double, y: Double) = x.compare(y)
    override def filter = Some(NumberFilter(this, true, _.manaValue))
  }

  case object EffManaValue extends CardAttribute[Seq[Double]]("Eff. Mana Value", "Spell or permament mana value on the stack or battlefield of each face") {
    override def compare(x: Seq[Double], y: Seq[Double]) = x(0).compare(y(0))
    override def filter = Some(NumberFilter(this, false, _.manaValue))
  }

  case object Colors extends CardAttribute[Seq[ManaType]]("Colors", "Card colors derived from mana cost or color indicator") {
    override def compare(x: Seq[ManaType], y: Seq[ManaType]) = {
      val diff = x.size - y.size
      if (diff == 0)
        (x zip y).map{ case (a, b) => a.compare(b) }.zipWithIndex.map{ case (d, i) => d*math.pow(10, x.size - i).toInt }.reduce(_ + _)
      else
        diff
    }
    override def filter = Some(ColorFilter(this, _.colors))
  }

  case object ColorIdentity extends CardAttribute[Seq[ManaType]]("Color Identity", "Card colors plus the colors of any mana symbols that appear in its Oracle text") {
    override def compare(x: Seq[ManaType], y: Seq[ManaType]) = {
      val diff = x.size - y.size
      if (diff == 0)
        (x zip y).map{ case (a, b) => a.compare(b) }.zipWithIndex.map{ case (d, i) => d*math.pow(10, x.size - i).toInt }.reduce(_ + _)
      else
        diff
    }
    override def filter = Some(ColorFilter(this, _.colorIdentity))
  }

  case object TypeLine extends CardAttribute[Seq[TypeLine]]("Type Line", "Full type line, including supertypes, card types, and subtypes") {
    override def compare(x: Seq[TypeLine], y: Seq[TypeLine]) = {
      if (x.size > y.size)
        1
      else if (y.size > x.size)
        -1
      else
        (x zip y).map(_.compare(_)).find(_ > 0).headOption.getOrElse(0)
    }
    override def filter = Some(TypeLineFilter())
  }

  case object PrintedTypes extends CardAttribute[Unit]("Printed Type Line", "Type line as printed on the card") {
    override def compare(x: Unit, y: Unit) = throw UnsupportedOperationException()
    override def filter = Some(TextFilter(this, _.faces.map(_.printedTypes)))
  }

  case object CardType extends CardAttribute[Unit]("Card Type", "Card types only") {
    override def compare(x: Unit, y: Unit) = throw UnsupportedOperationException()
    override def filter = Some(CardTypeFilter())
  }

  case object Subtype extends CardAttribute[Unit]("Subtype", "Subtypes only") {
    override def compare(x: Unit, y: Unit) = throw UnsupportedOperationException()
    override def filter = Some(SubtypeFilter())
  }

  case object Supertype extends CardAttribute[Unit]("Supertype", "Supertypes only") {
    override def compare(x: Unit, y: Unit) = throw UnsupportedOperationException()
    override def filter = Some(SupertypeFilter())
  }

  case object Power extends CardAttribute[Seq[CombatStat]]("Power", "Creature power") {
    override def compare(x: Seq[CombatStat], y: Seq[CombatStat]) = x(0).compare(y(0))
    override def filter = Some(VariableNumberFilter(this, _.power.value, _.powerVariable))
  }

  case object Toughness extends CardAttribute[Seq[CombatStat]]("Toughness", "Creature toughness") {
    override def compare(x: Seq[CombatStat], y: Seq[CombatStat]) = x(0).compare(y(0))
    override def filter = Some(VariableNumberFilter(this, _.toughness.value, _.toughnessVariable))
  }

  case object Loyalty extends CardAttribute[java.util.List[Loyalty]]("Loyalty", "Planeswalker starting loyalty") {
    override def compare(x: java.util.List[Loyalty], y: java.util.List[Loyalty]) = x.get(0).compare(y.get(0))
    override def filter = Some(VariableNumberFilter(this, _.loyalty.value, _.loyaltyVariable))
  }

  case object Layout extends CardAttribute[CardLayout]("Layout", "Layout of card faces") {
    override def compare(x: CardLayout, y: CardLayout) = x.compare(y)
    override def filter = Some(LayoutFilter())
  }

  case object Expansion extends CardAttribute[String]("Expansion", "Expansion a card belongs to") {
    override def compare(x: String, y: String) = Collator.getInstance.compare(x, y)
    override def filter = Some(ExpansionFilter())
  }

  case object Block extends CardAttribute[String]("Block", "Block of expansions, if any, a card's expansion belongs to") {
    override def compare(x: String, y: String) = Collator.getInstance.compare(x, y)
    override def filter = Some(BlockFilter())
  }

  case object Rarity extends CardAttribute[Rarity]("Rarity", "Printed rarity") {
    override def compare(x: Rarity, y: Rarity) = x.compare(y)
    override def filter = Some(RarityFilter())
  }

  case object Artist extends CardAttribute[String]("Artist", "Credited artist") {
    override def compare(x: String, y: String) = Collator.getInstance.compare(x, y)
    override def filter = Some(TextFilter(this, _.faces.map(_.artist)))
  }

  case object CardNumber extends CardAttribute[String]("Card Number", "Collector number in expansion") {
    override def compare(x: String, y: String) = Collator.getInstance.compare(x, y)
    override def filter = Some(NumberFilter(this, false, (f) => {
      try {
        f.number.replace("--", "0").replaceAll(raw"[\D]", "").toDouble
      } catch case e: NumberFormatException => 0.0
    }))
  }

  case object LegalIn extends CardAttribute[java.util.List[String]]("Format Legality", "Formats a card can be legally be played in and if it is restricted") {
    override def compare(x: java.util.List[String], y: java.util.List[String]) = Collator.getInstance.compare(x.asScala.mkString(","), y.asScala.mkString(","))
    override def filter = Some(LegalityFilter())
  }

  case object Tags extends CardAttribute[java.util.Set[String]]("Tags", "Tags you have created and assigned") {
    override def compare(x: java.util.Set[String], y: java.util.Set[String]) = Collator.getInstance.compare(x.asScala.mkString(","), y.asScala.mkString(","))
    override def filter = Some(TagsFilter())
  }

  case object Categories extends CardAttribute[Set[Categorization]]("Categories", "") {
    override def compare(x: Set[Categorization], y: Set[Categorization]) = {
      (x.toSeq.sortBy(_.name) zip y.toSeq.sortBy(_.name)).filter(_.name != _.name).map{ case (a, b) => a.name.compare(b.name) }.headOption.getOrElse(x.size - y.size)
    }
    override def filter = None
  }

  case object Count extends CardAttribute[Int]("Count", "") {
    override def compare(x: Int, y: Int) = x.compare(y)
    override def filter = None
  }

  case object DateAdded extends CardAttribute[LocalDate]("Date Added", "") {
    override def compare(x: LocalDate, y: LocalDate) = x.compareTo(y)
    override def filter = None
  }

  case object AnyCard extends CardAttribute[Unit]("<Any Card>", "Match any card") {
    override def compare(x: Unit, y: Unit) = throw UnsupportedOperationException()
    override def filter = Some(BinaryFilter(true))
  }

  case object NoCard extends CardAttribute[Unit]("<No Card>", "Match no card") {
    override def compare(x: Unit, y: Unit) = throw UnsupportedOperationException()
    override def filter = Some(BinaryFilter(false))
  }

  case object Defaults extends CardAttribute[Unit]("Defaults", "Filters of predefined categories") {
    override def compare(x: Unit, y: Unit) = throw UnsupportedOperationException()
    override def filter = None
  }

  case object Group extends CardAttribute[Unit]("Group", "") {
    override def compare(x: Unit, y: Unit) = throw UnsupportedOperationException()
    override def filter = None
  }

  val values: IndexedSeq[CardAttribute[?]] = IndexedSeq(Name, RulesText, FlavorText, PrintedText, ManaCost, RealManaValue, EffManaValue, Colors, ColorIdentity, TypeLine, PrintedTypes, CardType, Subtype, Supertype, Power, Toughness, Loyalty, Layout, Expansion, Block, Rarity, Artist, CardNumber, LegalIn, Tags, Categories, Count, DateAdded, AnyCard, NoCard, Defaults, Group)
  lazy val filterableValues = values.filter(_.filter.isDefined)
  lazy val displayableValues = values.filter(_.dataType != classOf[Unit])
  lazy val inventoryValues = displayableValues.filter(!Seq(Categories, Count).contains(_))

  def parse(s: String) = {
    if (s.equalsIgnoreCase("cmc") || s.equalsIgnoreCase("mana value"))
      Some(RealManaValue)
    else
      values.find(_.toString.equalsIgnoreCase(s)).headOption
  }
}