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