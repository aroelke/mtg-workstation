package editor.collection

import editor.collection.deck.Category
import editor.database.attributes.CardAttribute
import editor.database.card.Card
import java.time.LocalDate
import scala.jdk.CollectionConverters._

object CardListEntry {
  private class StandaloneEntry(override val card: Card, override val count: Int, override val dateAdded: LocalDate) extends CardListEntry {
    override val categories = Set.empty
  }

  def apply(card: Card, count: Int = 1, added: LocalDate = LocalDate.now): CardListEntry = StandaloneEntry(card, count, added)
  def unapply(entry: CardListEntry): Option[(Card, Int, LocalDate)] = Some((entry.card, entry.count, entry.dateAdded))
}

trait CardListEntry extends Equals {
  import CardAttribute._

  def card: Card
  def count: Int
  def dateAdded: LocalDate

  def categories: Set[Category]

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

  def copy(count: Int = this.count, dateAdded: LocalDate = this.dateAdded) = CardListEntry(card, count, dateAdded)
  override def equals(that: Any) = that match {
    case e: CardListEntry => e.canEqual(this) && card == e.card && count == e.count && dateAdded == e.dateAdded
    case _ => false
  }
  override def hashCode = Seq(card, count, dateAdded).map(_.##).fold(0)(31*_ + _)
  override def toString = s"${card.name} (${card.expansion.name}) x$count @$dateAdded"
}

trait CardList extends collection.IndexedSeq[CardListEntry] {
  def contains(card: Card) = exists(_.card == card)
  def indexOf(card: Card) = indexWhere(_.card == card)
  def total: Int
}

trait MutableCardList extends collection.mutable.IndexedSeq[CardListEntry] with collection.mutable.Clearable {
  def addOne(card: CardListEntry): this.type
  final def +=(card: CardListEntry) = addOne(card)

  def addAll(cards: IterableOnce[CardListEntry]): this.type
  final def ++=(cards: IterableOnce[CardListEntry]) = addAll(cards)

  def subtractOne(card: CardListEntry): this.type
  final def -=(card: CardListEntry) = subtractOne(card)

  def subtractAll(cards: IterableOnce[CardListEntry]): this.type
  final def --=(cards: IterableOnce[CardListEntry]) = subtractAll(cards)
}