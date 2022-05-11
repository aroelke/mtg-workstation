package editor.collection.deck

import editor.collection.CardList
import editor.collection.CardListEntry
import editor.database.card.Card
import scala.util.Random

class Hand(deck: Deck, cards: Set[? <: Card] = Set.empty) extends CardList {
  private val hand = collection.mutable.ArrayBuffer[Card]()
  private val exclusion = collection.mutable.Set.from(cards)
  private var inHand = 0

  refresh()

  def refresh() = {
    clear()
    deck.foreach((c) => {
      if (!exclusion.contains(c))
        hand ++= Seq.fill(deck.getEntry(c).count)(c)
    })
  }

  def newHand(n: Int) = {
    refresh()
    Random.shuffle(hand)
    inHand = math.min(n, hand.size)
  }

  def clearExclusion() = exclusion.clear()

  def draw() = inHand += 1

  def exclude(card: Card) = exclusion.add(card)

  def excluded = exclusion.toSet

  def getHand = hand.slice(0, inHand).toSeq
  
  def mulligan() = if (inHand > 0) {
    Random.shuffle(hand)
    inHand -= 1
  }

  override def contains(card: Card) = getHand.contains(card)
  override def containsAll(cards: Iterable[? <: Card]) = { val slice = getHand; cards.forall(slice.contains) }
  override def clear() = {
    hand.clear()
    inHand = 0
  }
  override def get(index: Int) = if (index < inHand) hand(index) else throw IndexOutOfBoundsException(index)
  override def getEntry(card: Card) = deck.getEntry(card)
  override def getEntry(index: Int) = getEntry(get(index))
  override def indexOf(card: Card) = hand.indexOf(card)
  override def isEmpty = hand.isEmpty || inHand == 0
  override def iterator = getHand.iterator
  override def size = math.min(inHand, hand.size)
  override def total = size
  override def sort(comp: Ordering[? >: CardListEntry]) = hand.sortInPlace()((a, b) => comp.compare(deck.getEntry(a), deck.getEntry(b)))

  override def add(card: Card) = throw UnsupportedOperationException("hand is only an immutable view")
  override def add(card: Card, amount: Int) = throw UnsupportedOperationException("hand is only an immutable view")
  override def addAll(cards: CardList) = throw UnsupportedOperationException("hand is only an immutable view")
  override def addAll(cards: Map[? <: Card, Int]) = throw UnsupportedOperationException("hand is only an immutable view")
  override def addAll(cards: Set[? <: Card]) = throw UnsupportedOperationException("hand is only an immutable view")
  override def remove(card: Card) = throw UnsupportedOperationException("hand is only an immutable view")
  override def remove(card: Card, amount: Int) = throw UnsupportedOperationException("hand is only an immutable view")
  override def removeAll(cards: CardList) = throw UnsupportedOperationException("hand is only an immutable view")
  override def removeAll(cards: Map[? <: Card, Int]) = throw UnsupportedOperationException("hand is only an immutable view")
  override def removeAll(cards: Set[? <: Card]) = throw UnsupportedOperationException("hand is only an immutable view")
  override def set(card: Card, amount: Int) = throw UnsupportedOperationException("hand is only an immutable view")
  override def set(index: Int, amount: Int) = throw UnsupportedOperationException("hand is only an immutable view")
}