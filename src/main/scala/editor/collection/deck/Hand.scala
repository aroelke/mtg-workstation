package editor.collection.deck

import editor.collection.CardList
import editor.database.card.Card

import scala.util.Random

/**
 * A "flat" view of a [[CardList]], where multiple copies of a card are expanded into separate entries. The list can be shuffled,
 * and a customizable initial subset can be viewed.  Iterating or accessing the list uses this subset.
 * 
 * @constructor create a new hand from a [[CardList]]
 * @param deck list to use to create the hand
 * 
 * @author Alec Roelke
 */
class Hand(deck: CardList) extends IndexedSeq[Card] {
  private var hand = 0
  private def shuffled = Random.shuffle(deck.flatMap((e) => Seq.fill(e.count)(e.card)).toIndexedSeq)
  private var cards = shuffled

  /** Shuffle the cards. */
  def refresh() = cards = shuffled

  /**
   * Shuffle the cards and set the view to a new size.
   * @param n new hand size
   */
  def newHand(n: Int) = {
    refresh()
    hand = math.min(n, cards.size)
  }

  /** Increase the hand by one card, without shuffling. */
  def draw() = if (hand < cards.size) hand += 1

  /** Shuffle the list and reduce the hand size by one, to a minimum of 0. */
  def mulligan() = {
    refresh()
    if (hand > 0)
      hand -= 1
  }

  override def apply(index: Int) = if (index < hand) cards(index) else throw ArrayIndexOutOfBoundsException(index)
  override def length = hand
}
