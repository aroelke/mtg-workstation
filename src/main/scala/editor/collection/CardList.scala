package editor.collection

import editor.collection.Categorization
import editor.database.attributes.CardAttribute
import editor.database.attributes.ManaType
import editor.database.card.Card

import java.time.LocalDate
import scala.jdk.CollectionConverters._
import scala.collection.immutable.ListSet

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

  /** If applicable, the [[Categorization]]s that match the card in the list. */
  def categories: Set[Categorization]

  /** @return the value of the given attribute for the card */
  def apply(data: CardAttribute[?, ?]) = data match {
    case Name => card.faces.map(_.name)
    case Layout => card.layout
    case ManaCost => card.faces.map(_.manaCost)
    case RealManaValue => card.manaValue
    case EffManaValue => card.faces.map(_.manaValue)
    case Colors => card.colors
    case ColorIdentity => card.colorIdentity
    case TypeLine => card.faces.map(_.typeLine)
    case Expansion => card.expansion
    case Block => card.expansion.block
    case Rarity => card.rarity
    case Power => card.faces.map(_.power)
    case Toughness => card.faces.map(_.toughness)
    case Loyalty => card.faces.map(_.loyalty)
    case Artist => card.faces.map(_.artist)
    case CardNumber => card.faces.map(_.number)
    case LegalIn => card.legalIn.toSeq.sorted
    case Count => count
    case Categories => categories
    case DateAdded => dateAdded
    case Tags => Card.tags(card).toSet
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

type CardList = immutable.CardList