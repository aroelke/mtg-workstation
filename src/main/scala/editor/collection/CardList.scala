package editor.collection

import editor.collection.deck.Category
import editor.database.attributes.CardAttribute
import editor.database.card.Card

import java.time.LocalDate
import scala.jdk.CollectionConverters._
import scala.collection.IndexedSeqOps

/**
 * Global data and functions for [[CardListEntry]]s.
 * @author Alec Roelke
 */
object CardListEntry {
  private class StandaloneEntry(override val card: Card, override val count: Int, override val dateAdded: LocalDate) extends CardListEntry {
    override val categories = Set.empty
  }

  /** Convenience constructor for [[CardListEntry]]s to easily convert them from [[Card]]s. */
  def apply(card: Card, count: Int = 1, added: LocalDate = LocalDate.now): CardListEntry = StandaloneEntry(card, count, added)

  /** Extractor for [[CardListEntry]]s to easily match against their values. */
  def unapply(entry: CardListEntry): Option[(Card, Int, LocalDate)] = Some((entry.card, entry.count, entry.dateAdded))
}

/**
 * Entry in a [[CardList]], keeping track of the number of copies of a card, the date it was added to the list, and, if
 * applicable, the categories to which it belongs.
 * 
 * @author Alec Roelke
 */
trait CardListEntry extends Equals {
  import CardAttribute._

  /** The card in the entry. */
  def card: Card

  /** If applicable, the number of copies of the card in the list. */
  def count: Int

  /** The date the card was added to the list. */
  def dateAdded: LocalDate

  /** If applicable, the [[Category]]s that match the card in the list. */
  def categories: Set[Category]

  /** @return the value of the given attribute for the card */
  def apply(data: CardAttribute) = data match {
    case NAME => card.name
    case LAYOUT => card.layout
    case MANA_COST => card.faces.map(_.manaCost).asJava
    case REAL_MANA_VALUE => card.manaValue
    case EFF_MANA_VALUE => card.faces.map((f) => java.lang.Double(f.manaValue)).asJava
    case COLORS => card.colors.asJava
    case COLOR_IDENTITY => card.colorIdentity.asJava
    case TYPE_LINE => card.faces.map(_.typeLine).asJava
    case EXPANSION => card.expansion.toString
    case BLOCK => card.expansion.block
    case RARITY => card.rarity
    case POWER => card.faces.map(_.power).asJava
    case TOUGHNESS => card.faces.map(_.toughness).asJava
    case LOYALTY => card.faces.map(_.loyalty).asJava
    case ARTIST => card.faces(0).artist
    case CARD_NUMBER => card.faces.map(_.number).mkString(Card.FaceSeparator)
    case LEGAL_IN => card.legalIn.toSeq.sorted.asJava
    case COUNT => count
    case CATEGORIES => categories
    case DATE_ADDED => dateAdded
    case TAGS => java.util.LinkedHashSet(Card.tags(card).toSeq.sorted.asJava)
  }

  override def canEqual(that: Any) = that.isInstanceOf[CardListEntry]

  /** @return a new [[CardListEntry]] that is a copy of this one except it is not associated with any list and may have a new count or date. */
  def copy(count: Int = this.count, dateAdded: LocalDate = this.dateAdded) = CardListEntry(card, count, dateAdded)
  override def equals(that: Any) = that match {
    case e: CardListEntry => e.canEqual(this) && card == e.card && count == e.count && dateAdded == e.dateAdded
    case _ => false
  }
  override def hashCode = Seq(card, count, dateAdded).map(_.##).fold(0)(31*_ + _)
  override def toString = s"${card.name} (${card.expansion.name}) x$count @$dateAdded"
}

/**
 * Trait containing common operations that all card lists should have.
 * @author Alec Roelke
 */
trait CardListOps[+A <: CardListEntry, +CC[_], +C] extends IndexedSeqOps[A, CC, C] {
  /**
   * Determine if the list contains an entry for a card.
   * 
   * @param card card to search for
   * @return true if there is an entry for the card, and false otherwise
   */
  def contains(card: Card) = exists(_.card == card)

  /**
   * Get the index in the list of the entry for a card.
   * 
   * @param card card to search for
   * @return the index of the entry for the card, or -1 if there isn't one
   */
  def indexOf(card: Card) = indexWhere(_.card == card)

  /** @return the total number of copies of cards in the list, or the sum of the count in each entry */
  def total: Int
}

/**
 * Immutable list of [[CardListEntry]]s. There is only up to one entry for any card.
 * @author Alec Roelke
 */
trait CardList extends collection.IndexedSeq[CardListEntry] with CardListOps[CardListEntry, collection.IndexedSeq, collection.IndexedSeq[CardListEntry]]

/**
 * Mutable list of [[CardListEntry]]s. As with [[CardList]], there is only one entry for any card; attempting to add another should either fail or increase the
 * number of copies in the existing entry.
 * 
 * @author Alec Roelke
 * @note this class is not a subclass of [[Growable]] or [[Shrinkable]] because those classes most likely expect the list's [[size]] to change when adding and
 * removing elements, which may not happen with this class.
 */
trait MutableCardList extends collection.mutable.IndexedSeq[CardListEntry]
    with collection.mutable.Clearable
    with CardListOps[CardListEntry, collection.mutable.IndexedSeq, collection.mutable.IndexedSeq[CardListEntry]] {
  /** Add a [[CardListEntry]] to the list, or modify the existing one if it has the same card. */
  def addOne(card: CardListEntry): this.type
  /** Alias for [[addOne]]. */
  final def +=(card: CardListEntry) = addOne(card)

  /** Add all entries to the list, or modify existing ones with matching cards. */
  def addAll(cards: IterableOnce[CardListEntry]): this.type
  /** Alias for [[addAll]]. */
  final def ++=(cards: IterableOnce[CardListEntry]) = addAll(cards)

  /** Modify an existing entry of the list to remove copies of the matching card, and/or the entry entirely if applicable. */
  def subtractOne(card: CardListEntry): this.type
  /** Alias for [[subtractOne]]. */
  final def -=(card: CardListEntry) = subtractOne(card)

  /** Modify several entries to remove copies of matching cards, and/or the corresponding entries entirely if applicable. */
  def subtractAll(cards: IterableOnce[CardListEntry]): this.type
  /** Alias for [[subtractAll]]. */
  final def --=(cards: IterableOnce[CardListEntry]) = subtractAll(cards)
}