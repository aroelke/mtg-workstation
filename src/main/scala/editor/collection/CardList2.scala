package editor.collection

import editor.database.card.Card
import java.time.LocalDate

case class StandaloneEntry(card: Card, count: Int, dateAdded: LocalDate) extends CardListEntry {
  override val categories = Set.empty
}

given card2Entry: Conversion[Card, CardListEntry] = StandaloneEntry(_, 1, LocalDate.now)
given Conversion[(Card, Int), CardListEntry] with { def apply(card: (Card, Int)) = StandaloneEntry(card._1, card._2, LocalDate.now) }

trait CardList2 extends collection.IndexedSeq[CardListEntry] {
  def contains(card: Card) = exists(_.card == card)
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