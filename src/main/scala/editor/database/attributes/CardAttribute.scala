package editor.database.attributes

import editor.collection.CardListEntry
import editor.collection.Categorization
import editor.database.FormatConstraints
import editor.database.card.Card
import editor.database.card.CardLayout
import editor.filter.leaf.BinaryFilter
import editor.filter.leaf.ColorFilter
import editor.filter.leaf.FilterLeaf
import editor.filter.leaf.LegalityFilter
import editor.filter.leaf.ManaCostFilter
import editor.filter.leaf.MultiOptionsFilter
import editor.filter.leaf.NumberFilter
import editor.filter.leaf.OptionsFilter
import editor.filter.leaf.SingletonOptionsFilter
import editor.filter.leaf.TextFilter
import editor.filter.leaf.TypeLineFilter

import java.text.Collator
import java.time.LocalDate
import scala.reflect.ClassTag

// D: type returned from CardTableEntry.apply
// F: type of filter that filters by the attribute
sealed trait CardAttribute[D : ClassTag, F <: FilterLeaf](name: String, val description: String) extends Ordering[D] {
  def filter: F

  def ordinal = CardAttribute.values.indexOf(this)
  def any_compare(x: Any, y: Any): Int = (x, y) match {
    case (a: D, b: D) => compare(a, b)
    case (a: D, b) => throw ClassCastException(s"$b: ${b.getClass} is not an instance of ${dataType}")
    case (a, b: D) => throw ClassCastException(s"$a: ${a.getClass} is not an instance of ${dataType}")
    case (a, b) => throw ClassCastException(s"$a: ${a.getClass} is not an instance of ${dataType} and $b: ${b.getClass} is not an instance of ${dataType}")
  }
  def comparingEntry: Ordering[CardListEntry] = (a: CardListEntry, b: CardListEntry) => compare(a(this) match { case v: D => v }, b(this) match { case v: D => v})
  def dataType = implicitly[ClassTag[D]].runtimeClass.asInstanceOf[Class[D]]

  override def toString = name
}

sealed trait ComparesColors { this: CardAttribute[Set[ManaType], ?] =>
  override def compare(x: Set[ManaType], y: Set[ManaType]) = {
    val diff = x.size - y.size
    if (diff == 0)
      (x zip y).map{ case (a, b) => a.compare(b) }.zipWithIndex.map{ case (d, i) => d*math.pow(10, x.size - i).toInt }.reduce(_ + _)
    else
      diff
  }
}

sealed trait CantCompare[T] { this: CardAttribute[T, ?] =>
  override def compare(x: T, y: T) = throw UnsupportedOperationException(s"$toString can't be used to compare attributes")
}

sealed trait HasTextFilter(text: (Card) => Seq[String]) { this: CardAttribute[Seq[String], TextFilter] =>
  override def filter = TextFilter(this, text)
}

sealed trait HasNumberFilter(unified: Boolean, value: (Card) => Double, variable: Option[(Card) => Boolean]) { this: CardAttribute[?, NumberFilter] =>
  override def filter = NumberFilter(this, unified, value, variable)
}

sealed trait HasColorFilter(colors: (Card) => Set[ManaType]) { this: CardAttribute[Set[ManaType], ColorFilter] =>
  override def filter = ColorFilter(this, colors)
}

trait HasOptions[T, F <: OptionsFilter[T, F]] { this: CardAttribute[?, F] =>
  def options: Seq[T]
}

trait HasAssignableOptions[T, F <: OptionsFilter[T, F]] extends HasOptions[T, F] { this: CardAttribute[?, F] =>
  private var values = Seq.empty[T]
  def options_=(v: Iterable[T]) = values = v.toSeq
  override def options = values.toSeq
}

sealed trait HasSingletonOptionsFilter[T](value: (Card) => T) extends HasOptions[T, SingletonOptionsFilter[T]] { this: CardAttribute[T, SingletonOptionsFilter[T]] =>
  override def filter = SingletonOptionsFilter(this, value)
}

sealed trait HasMultiOptionsFilter[T](values: (Card) => Set[T]) extends HasOptions[T, MultiOptionsFilter[T]] { this: CardAttribute[Set[T], MultiOptionsFilter[T]] =>
  override def filter = MultiOptionsFilter(this, values)
}

sealed trait CantBeFiltered { this: CardAttribute[?, Nothing] =>
  override def filter = throw UnsupportedOperationException(s"$toString can't be filtered")
}

object CardAttribute {
  case object Name extends CardAttribute[Seq[String], TextFilter]("Name", "Card Name") with HasTextFilter(_.normalizedName) {
    override def compare(x: Seq[String], y: Seq[String]) = Collator.getInstance.compare(x(0), y(0))
  }

  case object RulesText extends CardAttribute[Seq[String], TextFilter]("Rules Text", "Up-to-date Oracle text")
      with HasTextFilter(_.normalizedOracle)
      with CantCompare[Seq[String]]

  case object FlavorText extends CardAttribute[Seq[String], TextFilter]("Flavor Text", "Flavor text")
    with HasTextFilter(_.normalizedFlavor)
    with CantCompare[Seq[String]]

  case object PrintedText extends CardAttribute[Seq[String], TextFilter]("Printed Text", "Rules text as printed on the card")
    with HasTextFilter(_.normalizedPrinted)
    with CantCompare[Seq[String]]

  case object ManaCost extends CardAttribute[Seq[ManaCost], ManaCostFilter]("Mana Cost", "Mana cost, including symbols") {
    override def compare(x: Seq[ManaCost], y: Seq[ManaCost]) = x(0).compare(y(0))
    override def filter = ManaCostFilter()
  }

  case object RealManaValue extends CardAttribute[Double, NumberFilter]("Real Mana Value", "Card mana value as defined by the rules") with HasNumberFilter(true, _.manaValue, None) {
    override def compare(x: Double, y: Double) = x.compare(y)
  }

  case object EffManaValue extends CardAttribute[Seq[Double], NumberFilter]("Eff. Mana Value", "Spell or permament mana value on the stack or battlefield of each face") with HasNumberFilter(false, _.manaValue, None) {
    override def compare(x: Seq[Double], y: Seq[Double]) = x(0).compare(y(0))
  }

  case object Colors extends CardAttribute[Set[ManaType], ColorFilter]("Colors", "Card colors derived from mana cost or color indicator")
    with ComparesColors
    with HasColorFilter(_.colors)

  case object ColorIdentity extends CardAttribute[Set[ManaType], ColorFilter]("Color Identity", "Card colors plus the colors of any mana symbols that appear in its Oracle text")
    with ComparesColors
    with HasColorFilter(_.colorIdentity)

  case object TypeLine extends CardAttribute[Seq[TypeLine], TypeLineFilter]("Type Line", "Full type line, including supertypes, card types, and subtypes") {
    override def compare(x: Seq[TypeLine], y: Seq[TypeLine]) = {
      if (x.size > y.size)
        1
      else if (y.size > x.size)
        -1
      else
        (x zip y).map(_.compare(_)).find(_ > 0).headOption.getOrElse(0)
    }
    override def filter = TypeLineFilter()
  }

  case object PrintedTypes extends CardAttribute[Seq[String], TextFilter]("Printed Type Line", "Type line as printed on the card")
    with HasTextFilter(_.faces.map(_.printedTypes))
    with CantCompare[Seq[String]]

  case object CardType extends CardAttribute[Set[String], MultiOptionsFilter[String]]("Card Type", "Card types only")
      with CantCompare[Set[String]]
      with HasMultiOptionsFilter[String](_.types)
      with HasAssignableOptions[String, MultiOptionsFilter[String]]

  case object Subtype extends CardAttribute[Set[String], MultiOptionsFilter[String]]("Subtype", "Subtypes only")
      with CantCompare[Set[String]]
      with HasMultiOptionsFilter[String](_.subtypes)
      with HasAssignableOptions[String, MultiOptionsFilter[String]]

  case object Supertype extends CardAttribute[Set[String], MultiOptionsFilter[String]]("Supertype", "Supertypes only")
      with CantCompare[Set[String]]
      with HasMultiOptionsFilter[String](_.supertypes)
      with HasAssignableOptions[String, MultiOptionsFilter[String]]

  case object Power extends CardAttribute[Seq[Option[CombatStat]], NumberFilter]("Power", "Creature power") with HasNumberFilter(false, _.power.map(_.value).getOrElse(Double.NaN), Some(_.powerVariable)) {
    override def compare(x: Seq[Option[CombatStat]], y: Seq[Option[CombatStat]]) = (x.flatten.headOption, y.flatten.headOption) match {
      case (Some(a), Some(b)) => a.compare(b)
      case (Some(a), None) => -1
      case (None, Some(b)) => 1
      case (None, None) => 0
    }
  }

  case object Toughness extends CardAttribute[Seq[Option[CombatStat]], NumberFilter]("Toughness", "Creature toughness") with HasNumberFilter(false, _.toughness.map(_.value).getOrElse(Double.NaN), Some(_.powerVariable)) {
    override def compare(x: Seq[Option[CombatStat]], y: Seq[Option[CombatStat]]) = (x.flatten.headOption, y.flatten.headOption) match {
      case (Some(a), Some(b)) => a.compare(b)
      case (Some(_), None) => -1
      case (None, Some(_)) => 1
      case (None, None) => 0
    }
  }

  case object Loyalty extends CardAttribute[Seq[Option[Loyalty]], NumberFilter]("Loyalty", "Planeswalker starting loyalty") with HasNumberFilter(false, _.loyalty.map(_.value).getOrElse(Double.NaN), Some(_.loyaltyVariable)) {
    override def compare(x: Seq[Option[Loyalty]], y: Seq[Option[Loyalty]]) = (x.flatten.headOption, y.flatten.headOption) match {
      case (Some(a), Some(b)) => a.compare(b)
      case (Some(_), None) => -1
      case (None, Some(_)) => 1
      case (None, None) => 0
    }
  }

  case object Layout extends CardAttribute[CardLayout, SingletonOptionsFilter[CardLayout]]("Layout", "Layout of card faces") with HasSingletonOptionsFilter[CardLayout](_.layout) {
    override def options = CardLayout.values
    override def compare(x: CardLayout, y: CardLayout) = x.compare(y)
  }

  case object Expansion extends CardAttribute[editor.database.attributes.Expansion, SingletonOptionsFilter[editor.database.attributes.Expansion]]("Expansion", "Expansion a card belongs to") with HasSingletonOptionsFilter[editor.database.attributes.Expansion](_.expansion) {
    override def options = editor.database.attributes.Expansion.expansions
    override def compare(x: editor.database.attributes.Expansion, y: editor.database.attributes.Expansion) = Collator.getInstance.compare(x.name, y.name)
  }

  case object Block extends CardAttribute[String, SingletonOptionsFilter[String]]("Block", "Block of expansions, if any, a card's expansion belongs to") with HasSingletonOptionsFilter[String](_.expansion.block) {
    override def options = editor.database.attributes.Expansion.blocks
    override def compare(x: String, y: String) = Collator.getInstance.compare(x, y)
  }

  case object Rarity extends CardAttribute[editor.database.attributes.Rarity, SingletonOptionsFilter[editor.database.attributes.Rarity]]("Rarity", "Printed rarity") with HasSingletonOptionsFilter[editor.database.attributes.Rarity](_.rarity) {
    override def options = editor.database.attributes.Rarity.values
    override def compare(x: Rarity, y: Rarity) = x.compare(y)
  }

  case object Artist extends CardAttribute[Seq[String], TextFilter]("Artist", "Credited artist") with HasTextFilter(_.faces.map(_.artist)) {
    override def compare(x: Seq[String], y: Seq[String]) = Collator.getInstance.compare(x(0), y(0))
  }

  case object CardNumber extends CardAttribute[Seq[String], NumberFilter]("Card Number", "Collector number in expansion") with HasNumberFilter(false, (f) => {
    try {
      f.number.replace("--", "0").replaceAll(raw"[\D]", "").toDouble
    } catch case e: NumberFormatException => 0.0
  }, None) {
    override def compare(x: Seq[String], y: Seq[String]) = Collator.getInstance.compare(x(0), y(0))
  }

  case object LegalIn extends CardAttribute[Set[String], LegalityFilter]("Format Legality", "Formats a card can be legally be played in and if it is restricted") with HasOptions[String, LegalityFilter] {
    override def options = FormatConstraints.FormatNames
    override def compare(x: Set[String], y: Set[String]) = Collator.getInstance.compare(x.toSeq.sorted.mkString(","), y.toSeq.sorted.mkString(","))
    override def filter = LegalityFilter()
  }

  case object Tags extends CardAttribute[Set[String], MultiOptionsFilter[String]]("Tags", "Tags you have created and assigned") with HasMultiOptionsFilter[String](Card.tags.get(_).map(_.toSet).getOrElse(Set.empty)) {
    override def options = Card.tags.flatMap{ case (_, s) => s }.toSeq.sorted
    override def compare(x: Set[String], y: Set[String]) = Collator.getInstance.compare(x.toSeq.sorted.mkString(","), y.toSeq.sorted.mkString(","))
  }

  case object Categories extends CardAttribute[Set[Categorization], Nothing]("Categories", "") with CantBeFiltered {
    override def compare(x: Set[Categorization], y: Set[Categorization]) = {
      (x.toSeq.sortBy(_.name) zip y.toSeq.sortBy(_.name)).filter(_.name != _.name).map{ case (a, b) => a.name.compare(b.name) }.headOption.getOrElse(x.size - y.size)
    }
  }

  case object Count extends CardAttribute[Int, Nothing]("Count", "") with CantBeFiltered {
    override def compare(x: Int, y: Int) = x.compare(y)
  }

  case object DateAdded extends CardAttribute[LocalDate, Nothing]("Date Added", "") with CantBeFiltered {
    override def compare(x: LocalDate, y: LocalDate) = x.compareTo(y)
  }

  case object AnyCard extends CardAttribute[Unit, BinaryFilter]("<Any Card>", "Match any card") with CantCompare[Unit] {
    override def filter = BinaryFilter(true)
  }

  case object NoCard extends CardAttribute[Unit, BinaryFilter]("<No Card>", "Match no card") with CantCompare[Unit] {
    override def filter = BinaryFilter(false)
  }

  case object Group extends CardAttribute[Unit, Nothing]("Group", "")
    with CantBeFiltered
    with CantCompare[Unit]

  val values: IndexedSeq[CardAttribute[?, ?]] = IndexedSeq(Name, RulesText, FlavorText, PrintedText, ManaCost, RealManaValue, EffManaValue, Colors, ColorIdentity, TypeLine, PrintedTypes, CardType, Subtype, Supertype, Power, Toughness, Loyalty, Layout, Expansion, Block, Rarity, Artist, CardNumber, LegalIn, Tags, Categories, Count, DateAdded, AnyCard, NoCard, Group)
  lazy val filterableValues = values.filter(!_.isInstanceOf[CantBeFiltered])
  lazy val displayableValues = values.filter(!_.isInstanceOf[CantCompare[?]])
  lazy val inventoryValues = displayableValues.filter(!Seq(Categories, Count).contains(_))

  def parse(s: String) = {
    if (s.equalsIgnoreCase("cmc") || s.equalsIgnoreCase("mana value"))
      Some(RealManaValue)
    else
      values.find(_.toString.equalsIgnoreCase(s)).headOption
  }
}