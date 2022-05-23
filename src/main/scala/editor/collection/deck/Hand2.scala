package editor.collection.deck

import editor.database.card.Card
import scala.util.Random
import editor.collection.CardList2

class Hand2(deck: CardList2) extends IndexedSeq[Card] {
  private var hand = 0
  private def shuffled = Random.shuffle(deck.flatMap((e) => Seq.fill(e.count)(e.card)).toIndexedSeq)
  private var cards = shuffled

  def refresh() = cards = shuffled

  def newHand(n: Int) = {
    refresh()
    hand = math.min(n, cards.size)
  }

  def draw() = if (hand < cards.size) hand += 1

  def mulligan() = {
    refresh()
    if (hand > 0)
      hand -= 1
  }

  override def apply(index: Int) = if (index < hand) cards(index) else throw ArrayIndexOutOfBoundsException(index)
  override def length = hand
}
