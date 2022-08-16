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
import editor.util.OptionOrdering
import editor.util.SeqOrdering

import java.text.Collator
import java.time.LocalDate
import scala.reflect.ClassTag
import java.time.format.DateTimeFormatter

/**
 * An attribute of a Magic: The Gathering card that can be used for filtering or display in a GUI. Generally corresponds to a field of [[Card]].
 * Also represents an ordering on the type of that field.
 * 
 * @constructor create a new representation of a card attribute
 * @param name name of the attribute
 * @param description short description of the attribute
 * @tparam T type of data used to represent the value of the attribute; for cards with multiple faces, should be a sequence type
 * @tparam F type of [[Filter]] used to filter by the attribute
 * 
 * @author Alec Roelke
 */
sealed trait CardAttribute[T : ClassTag, F <: FilterLeaf](name: String, val description: String) extends ((CardListEntry) => T) with Ordering[T] {
  /** @return a new instance of the filter used to filter by the attribute. */
  def filter: F

  /** @return a card's value for the attribute. */
  def apply(c: Card): T = apply(CardListEntry(c))

  /** @return an [[Ordering]] that can be used to compare card list entries by the attribute. */
  def comparingEntry: Ordering[CardListEntry] = (a: CardListEntry, b: CardListEntry) => compare(apply(a), apply(b))

  /** @return an [[Ordering]] that can be used to compare cards by the attribute. */
  def comparingCard: Ordering[Card] = (a: Card, b: Card) => compare(apply(a), apply(b))

  /** @return the type of data represented by the attribute. */
  def dataType = summon[ClassTag[T]].runtimeClass.asInstanceOf[Class[T]]

  /** @return the index of this attribute in the list of attributes. */
  def ordinal = CardAttribute.values.indexOf(this)

  override def toString = name
}

/**
 * A [[CardAttribute]] that does not actually correspond to a card value, so cannot be used to get it from a card.
 * @author Alec Roelke
 */
sealed trait IsVirtualAttribute { this: CardAttribute[Unit, ?] =>
  override def apply(e: CardListEntry) = throw UnsupportedOperationException(s"attribute $this is not a real card attribute")
}

///////////////////////
// COMPARATOR TRAITS //
///////////////////////

/**
 * A [[CardAttribute]] that compares [[Ordered]] values.
 * @tparam T data type of the card attribute
 * @author Alec Roelke
 */
sealed trait ComparesOrdered[T : Ordering] { this: CardAttribute[T, ?] =>
  override def compare(x: T, y: T) = summon[Ordering[T]].compare(x, y)
}

/**
 * A [[CardAttribute]] that compares values by string using a [[Collator]].
 * 
 * @param convert function to convert the value to a string
 * @param collator collator to use for comparing value strings
 * @tparam T data type of the card attribute
 * 
 * @author Alec Roelke
 */
sealed trait ComparesCollator[T](convert: (T) => String, collator: Collator = Collator.getInstance) { this: CardAttribute[T, ?] =>
  override def compare(x: T, y: T) = collator.compare(convert(x), convert(y))
}

/**
 * A [[CardAttribute]] that compares values that are sets of [[ManaType]] according to their canonical order after sorting them.
 * @author Alec Roelke
 */
sealed trait ComparesColors { this: CardAttribute[Set[ManaType], ?] =>
  override def compare(x: Set[ManaType], y: Set[ManaType]) = {
    val diff = x.size - y.size
    if (diff == 0)
      SeqOrdering[ManaType].compare(ManaType.sorted(x), ManaType.sorted(y))
    else
      diff
  }
}

/** A [[CardAttribute]] representing a card attribute not meant to be compared. */
sealed trait CantCompare[T] { this: CardAttribute[T, ?] =>
  override def compare(x: T, y: T) = throw UnsupportedOperationException(s"$toString can't be used to compare attributes")
}

/////////////////////////////
// FILTER GENERATOR TRAITS //
/////////////////////////////

/**
 * A text-based [[CardAttribute]] that is filtered using a [[TextFilter]].
 * @param text function for retrieving the value of the text attribute from a card
 * @author Alec Roelke
 */
sealed trait HasTextFilter(text: (Card) => Seq[String]) { this: CardAttribute[Seq[String], TextFilter] =>
  override def filter = TextFilter(this, text)
}

/**
 * A number-based [[CardAttribute]] that is filtered using a [[NumberFilter]].
 *
 * @param unified true if the number is the same across all card faces, and false otherwise
 * @param value function for retrieving the value of the numeric attribute from the card
 * @param variable function for determining if the value o fhte numeric attribute could vary while the card is in play during a game
 * 
 * @author Alec Roelke
 */
sealed trait HasNumberFilter(unified: Boolean, value: (Card) => Double, variable: Option[(Card) => Boolean]) { this: CardAttribute[?, NumberFilter] =>
  override def filter = NumberFilter(this, unified, value, variable)
}

/**
 * A [[ManaType]]-based [[CardAttribute]] that is filtered using a [[ColorFilter]].
 * @param colors function for retrieving the value of the color set from the card
 * @author Alec Roelke
 */
sealed trait HasColorFilter(colors: (Card) => Set[ManaType]) { this: CardAttribute[Set[ManaType], ColorFilter] =>
  override def filter = ColorFilter(this, colors)
}

/**
 * A [[CardAttribute]] whose card values are drawn from a list of options.
 * 
 * @tparam T data type of each of the options
 * @tparam F type of filter used to filter by the data
 * 
 * @author Alec Roelke
 */
trait HasOptions[T, F <: OptionsFilter[T, F]] { this: CardAttribute[?, F] =>
  /** List of options for the value of the attribute. */
  def options: Seq[T]
}

/**
 * A [[CardAttribute]] whose options can be reassigned.
 */
trait HasAssignableOptions[T, F <: OptionsFilter[T, F]] extends HasOptions[T, F] { this: CardAttribute[?, F] =>
  private var values = Seq.empty[T]

  /** Update the possible options for the attribute. */
  def options_=(v: Iterable[T]) = values = v.toSeq

  override def options = values.toSeq
}

/**
 * A [[CardAttribute]] whose value is just one of a list of possible options.
 * @tparam T data type of the options
 * @author Alec Roelke
 */
sealed trait HasSingletonOptionsFilter[T] extends HasOptions[T, SingletonOptionsFilter[T]] { this: CardAttribute[T, SingletonOptionsFilter[T]] =>
  override def filter = SingletonOptionsFilter(this, apply)
}

/**
 * A [[CardAttribute]] whose value is any number of a list of possible options.
 * @tparam T data type of the options
 * @author Alec Roelke
 */
sealed trait HasMultiOptionsFilter[T] extends HasOptions[T, MultiOptionsFilter[T]] { this: CardAttribute[Set[T], MultiOptionsFilter[T]] =>
  override def filter = MultiOptionsFilter(this, apply)
}

/**
 * A [[CardAttribute]] whose value isn't meant to be filtered by.
 * @author Alec Roelke
 */
sealed trait CantBeFiltered { this: CardAttribute[?, Nothing] =>
  override def filter = throw UnsupportedOperationException(s"$toString can't be filtered")
}


/**
 * Companion object defining all the [[CardAttribute]]s and global data for them.
 * @author Alec Roelke
 */
object CardAttribute {
  /** Card name for each face. */
  case object Name extends CardAttribute[Seq[String], TextFilter]("Name", "Card Name")
      with ComparesCollator[Seq[String]](_.mkString)
      with HasTextFilter(_.normalizedName) {
    override def apply(e: CardListEntry) = e.card.faces.map(_.name)
  }

  /** Rules (Oracle) text for each face. */
  case object RulesText extends CardAttribute[Seq[String], TextFilter]("Rules Text", "Up-to-date Oracle text")
      with HasTextFilter(_.normalizedOracle)
      with CantCompare[Seq[String]] {
    override def apply(e: CardListEntry) = e.card.faces.map(_.oracleText)
  }

  /** Flavor text for each face. */
  case object FlavorText extends CardAttribute[Seq[String], TextFilter]("Flavor Text", "Flavor text")
      with HasTextFilter(_.normalizedFlavor)
      with CantCompare[Seq[String]] {
    override def apply(e: CardListEntry) = e.card.faces.map(_.flavorText)
  }

  /** Printed text (what's actually on the card) for each face. */
  case object PrintedText extends CardAttribute[Seq[String], TextFilter]("Printed Text", "Rules text as printed on the card")
      with HasTextFilter(_.normalizedPrinted)
      with CantCompare[Seq[String]] {
    override def apply(e: CardListEntry) = e.card.faces.map(_.printedText)
  }

  /** Mana cost for each face. */
  case object ManaCost extends CardAttribute[Seq[ManaCost], ManaCostFilter]("Mana Cost", "Mana cost, including symbols") with ComparesOrdered[Seq[ManaCost]] {
    override def apply(e: CardListEntry) = e.card.faces.map(_.manaCost)
    override def filter = ManaCostFilter()
  }

  /** "Real" mana value.  Corresponds to the mana value of the entire card as defined by the rules. */
  case object RealManaValue extends CardAttribute[Double, NumberFilter]("Real Mana Value", "Card mana value as defined by the rules")
      with ComparesOrdered[Double]
      with HasNumberFilter(true, _.manaValue, None) {
    override def apply(e: CardListEntry) = e.card.manaValue
  }

  /** "Effective" mana value.  Corresponds to the mana value of each face of the card. */
  case object EffManaValue extends CardAttribute[Seq[Double], NumberFilter]("Eff. Mana Value", "Spell or permament mana value on the stack or battlefield of each face")
      with ComparesOrdered[Seq[Double]]
      with HasNumberFilter(false, _.manaValue, None) {
    override def apply(e: CardListEntry) = e.card.faces.map(_.manaValue)
  }

  /** Colors of the card across all faces. */
  case object Colors extends CardAttribute[Set[ManaType], ColorFilter]("Colors", "Card colors derived from mana cost or color indicator")
      with ComparesColors
      with HasColorFilter(_.colors) {
    override def apply(e: CardListEntry) = e.card.colors
  }

  /** Color identity of the card. */
  case object ColorIdentity extends CardAttribute[Set[ManaType], ColorFilter]("Color Identity", "Card colors plus the colors of any mana symbols that appear in its Oracle text")
      with ComparesColors
      with HasColorFilter(_.colorIdentity) {
    override def apply(e: CardListEntry) = e.card.colorIdentity
  }

  /** Type line for each face. */
  case object TypeLine extends CardAttribute[Seq[TypeLine], TypeLineFilter]("Type Line", "Full type line, including supertypes, card types, and subtypes") with ComparesOrdered[Seq[TypeLine]] {
    override def apply(e: CardListEntry) = e.card.faces.map(_.typeLine)
    override def filter = TypeLineFilter()
  }

  /** Printed type line (what's actually on the card) for each face. */
  case object PrintedTypes extends CardAttribute[Seq[String], TextFilter]("Printed Type Line", "Type line as printed on the card")
      with HasTextFilter(_.faces.map(_.printedTypes))
      with CantCompare[Seq[String]] {
    override def apply(e: CardListEntry) = e.card.faces.map(_.printedTypes)
  }

  /** All card types across all faces. */
  case object CardType extends CardAttribute[Set[String], MultiOptionsFilter[String]]("Card Type", "Card types only")
      with CantCompare[Set[String]]
      with HasMultiOptionsFilter[String]
      with HasAssignableOptions[String, MultiOptionsFilter[String]] {
    override def apply(e: CardListEntry) = e.card.types
  }

  /** All subtypes across all faces. */
  case object Subtype extends CardAttribute[Set[String], MultiOptionsFilter[String]]("Subtype", "Subtypes only")
      with CantCompare[Set[String]]
      with HasMultiOptionsFilter[String]
      with HasAssignableOptions[String, MultiOptionsFilter[String]] {
    override def apply(e: CardListEntry) = e.card.subtypes
  }

  /** All supertypes across all faces. */
  case object Supertype extends CardAttribute[Set[String], MultiOptionsFilter[String]]("Supertype", "Supertypes only")
      with CantCompare[Set[String]]
      with HasMultiOptionsFilter[String]
      with HasAssignableOptions[String, MultiOptionsFilter[String]] {
    override def apply(e: CardListEntry) = e.card.supertypes
  }

  /** Power of each face, if it's a creature. */
  case object Power extends CardAttribute[Seq[Option[CombatStat]], NumberFilter]("Power", "Creature power")
      with ComparesOrdered[Seq[Option[CombatStat]]]
      with HasNumberFilter(false, _.power.map(_.value).getOrElse(Double.NaN), Some(_.powerVariable)) {
    override def apply(e: CardListEntry) = e.card.faces.map(_.power)
  }

  /** Toughness of each face, if it's a creature. */
  case object Toughness extends CardAttribute[Seq[Option[CombatStat]], NumberFilter]("Toughness", "Creature toughness")
      with ComparesOrdered[Seq[Option[CombatStat]]]
      with HasNumberFilter(false, _.toughness.map(_.value).getOrElse(Double.NaN), Some(_.powerVariable)) {
    override def apply(e: CardListEntry) = e.card.faces.map(_.toughness)
  }

  /** Loyalty of each face, if it's a planeswalker. */
  case object Loyalty extends CardAttribute[Seq[Option[Loyalty]], NumberFilter]("Loyalty", "Planeswalker starting loyalty")
      with ComparesOrdered[Seq[Option[Loyalty]]]
      with HasNumberFilter(false, _.loyalty.map(_.value).getOrElse(Double.NaN), Some(_.loyaltyVariable)) {
    override def apply(e: CardListEntry) = e.card.faces.map(_.loyalty)
  }

  /** Overall layout of the card, or how faces are arranged if there are multiple faces. */
  case object Layout extends CardAttribute[CardLayout, SingletonOptionsFilter[CardLayout]]("Layout", "Layout of card faces")
      with ComparesOrdered[CardLayout]
      with HasSingletonOptionsFilter[CardLayout] {
    override def apply(e: CardListEntry) = e.card.layout
    override def options = CardLayout.values
  }

  /** Which expansion the card is printed in. */
  case object Expansion extends CardAttribute[editor.database.attributes.Expansion, SingletonOptionsFilter[editor.database.attributes.Expansion]]("Expansion", "Expansion a card belongs to")
      with ComparesCollator[editor.database.attributes.Expansion](_.name)
      with HasSingletonOptionsFilter[editor.database.attributes.Expansion] {
    override def apply(e: CardListEntry) = e.card.expansion
    override def options = editor.database.attributes.Expansion.expansions
  }

  /** Which block of expansions the card is printed in. */
  case object Block extends CardAttribute[String, SingletonOptionsFilter[String]]("Block", "Block of expansions, if any, a card's expansion belongs to")
      with ComparesCollator[String](identity)
      with HasSingletonOptionsFilter[String] {
    override def apply(e: CardListEntry) = e.card.expansion.block
    override def options = editor.database.attributes.Expansion.blocks
  }

  /** Printed rarity of the card. */
  case object Rarity extends CardAttribute[editor.database.attributes.Rarity, SingletonOptionsFilter[editor.database.attributes.Rarity]]("Rarity", "Printed rarity")
      with ComparesOrdered[Rarity]
      with HasSingletonOptionsFilter[editor.database.attributes.Rarity] {
    override def apply(e: CardListEntry) = e.card.rarity
    override def options = editor.database.attributes.Rarity.values
  }

  /** Printed artist of the card. */
  case object Artist extends CardAttribute[Seq[String], TextFilter]("Artist", "Credited artist")
      with ComparesCollator[Seq[String]](_(0)) // assume same artist for all faces
      with HasTextFilter(_.faces.map(_.artist)) {
    override def apply(e: CardListEntry) = e.card.faces.map(_.artist)
  }

  /** Collector number of the card printing. */
  case object CardNumber extends CardAttribute[Seq[String], NumberFilter]("Card Number", "Collector number in expansion")
      with ComparesCollator[Seq[String]](_.mkString)
      with HasNumberFilter(false, (f) => {
        try {
          f.number.replace("--", "0").replaceAll(raw"[\D]", "").toDouble
        } catch case e: NumberFormatException => 0.0
      }, None) {
    override def apply(e: CardListEntry) = e.card.faces.map(_.number)
  }

  /** Formats the card is legal in. */
  case object LegalIn extends CardAttribute[Set[String], LegalityFilter]("Format Legality", "Formats a card can be legally be played in and if it is restricted")
      with ComparesCollator[Set[String]](_.toSeq.sorted.mkString)
      with HasOptions[String, LegalityFilter] {
    override def apply(e: CardListEntry) = e.card.legalIn
    override def options = FormatConstraints.FormatNames
    override def filter = LegalityFilter()
  }

  /** User-assigned tags. */
  case object Tags extends CardAttribute[Set[String], MultiOptionsFilter[String]]("Tags", "Tags you have created and assigned")
      with ComparesCollator[Set[String]](_.toSeq.sorted.mkString)
      with HasMultiOptionsFilter[String] {

    /**
     * User-defined, per-card tags. Every card effectively starts with an empty tag set, and the user can add and remove them as
     * they like, and apply filters based on them for categories or inventory searches.
     */
    val tags = new collection.mutable.AbstractMap[Card, collection.mutable.Set[String]] {
      val tags = collection.mutable.Map[Card, collection.mutable.Set[String]]()

      override def iterator = tags.iterator
      override def get(c: Card) = Some(tags.getOrElseUpdate(c, collection.mutable.Set[String]()))
      override def addOne(e: (Card, collection.mutable.Set[String])) = { e match { case (c, s) => this.apply(c) ++= s }; this }
      override def subtractOne(c: Card) = { tags.subtractOne(c); this }
    }

    /**
     * Reset the user-defined tags.
     * 
     * @param elems new set of tags to replace the old ones
     * @return the tags after resetting to their new values
     */
    def tags_=(elems: IterableOnce[(Card, collection.mutable.Set[String])]) = {
      tags.clear()
      tags ++= elems
    }

    override def apply(e: CardListEntry) = tags.get(e.card).map(_.toSet).getOrElse(Set.empty)
    override def options = tags.flatMap{ case (_, s) => s }.toSeq.sorted
  }

  /** Categories a card belongs to in a deck. */
  case object Categories extends CardAttribute[Set[Categorization], Nothing]("Categories", "") with CantBeFiltered {
    override def apply(e: CardListEntry) = e.categories
    override def compare(x: Set[Categorization], y: Set[Categorization]) = {
      (x.toSeq.sortBy(_.name) zip y.toSeq.sortBy(_.name)).filter(_.name != _.name).map{ case (a, b) => a.name.compare(b.name) }.headOption.getOrElse(x.size - y.size)
    }
  }

  /** Number of copies of a card in a deck. */
  case object Count extends CardAttribute[Int, Nothing]("Count", "")
      with ComparesOrdered[Int]
      with CantBeFiltered {
    override def apply(e: CardListEntry) = e.count
  }

  /** Date a card was added to a deck. */
  case object DateAdded extends CardAttribute[LocalDate, Nothing]("Date Added", "")
      with ComparesOrdered[LocalDate]
      with CantBeFiltered {
    private val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")

    /** Format a date in "month day, year" format. */
    def format(date: LocalDate) = formatter.format(date)

    override def apply(e: CardListEntry) = e.dateAdded
  }

  /**
   * Not a real attribute, but produces a filter that matches any card.
   * Useful for creating categories that are only defined by blacklist.
   */
  case object AnyCard extends CardAttribute[Unit, BinaryFilter]("<Any Card>", "Match any card") with IsVirtualAttribute with CantCompare[Unit] {
    override def filter = BinaryFilter(true)
  }

  /**
   * Not a real attribute, but produces a filter that matches no card.
   * Useful for creating categories that are only defined by whitelist.
   */
  case object NoCard extends CardAttribute[Unit, BinaryFilter]("<No Card>", "Match no card") with IsVirtualAttribute with CantCompare[Unit] {
    override def filter = BinaryFilter(false)
  }

  /** Not a real attribute, but represents a group of combined filters. */
  case object Group extends CardAttribute[Unit, Nothing]("Group", "")
      with IsVirtualAttribute
      with CantBeFiltered
      with CantCompare[Unit]

  /** Array of all card attributes. */
  val values: IndexedSeq[CardAttribute[?, ?]] = IndexedSeq(Name, RulesText, FlavorText, PrintedText, ManaCost, RealManaValue, EffManaValue, Colors, ColorIdentity, TypeLine, PrintedTypes, CardType, Subtype, Supertype, Power, Toughness, Loyalty, Layout, Expansion, Block, Rarity, Artist, CardNumber, LegalIn, Tags, Categories, Count, DateAdded, AnyCard, NoCard, Group)

  /** Array of all card attributes that can be displayed in a GUI. */
  lazy val displayableValues = values.filter(!_.isInstanceOf[CantCompare[?]])

  /** Array of all card attributes that are applicable to the global inventory. */
  lazy val inventoryValues = displayableValues.filter(!Seq(Categories, Count).contains(_))

  /**
   * Parse a card attribute from a string, by name. Additionally, due to legacy reasons, "cmc" or "mana value" map to
   * [[RealManaValue]].
   * 
   * @param s string to parse
   * @return the card attribute parsed from the string, or None if there isn't one
   */
  def parse(s: String) = {
    if (s.equalsIgnoreCase("cmc") || s.equalsIgnoreCase("mana value"))
      Some(RealManaValue)
    else
      values.find(_.toString.equalsIgnoreCase(s)).headOption
  }
}