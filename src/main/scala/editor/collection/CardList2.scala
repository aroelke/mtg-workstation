package editor.collection

import editor.database.card.Card
import java.time.LocalDate

case class StandaloneEntry(card: Card, count: Int, dateAdded: LocalDate) extends CardListEntry {
  override val categories = Set.empty
}

trait CardList2 extends collection.IndexedSeq[CardListEntry] {
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