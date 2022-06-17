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
 * Immutable list of [[CardListEntry]]s. There is only up to one entry for any card.
 * @author Alec Roelke
 */
trait CardList extends collection.IndexedSeq[CardListEntry] {
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
 * Mutable list of [[CardListEntry]]s. Adding and removing entries should only create new entries (and increase [[size]]) if there is not
 * already an entry with the same card. Otherwise, the existing entry should be modified instead, changing [[total]] but not [[size]] unless
 * the modification causes the entry to be removed (for example, if its count reaches 0).
 * 
 * @author Alec Roelke
 */
trait MutableCardList extends CardList
    with collection.mutable.IndexedSeq[CardListEntry]
    with collection.mutable.Growable[CardListEntry]
    with collection.mutable.Shrinkable[CardListEntry]
    with collection.mutable.Clearable {
  override def knownSize: Int = size
}