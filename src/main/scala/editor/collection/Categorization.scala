package editor.collection

import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.filter.Filter

import java.awt.Color

case class Categorization(
  name: String = "All Cards",
  filter: Filter = CardAttribute.createFilter(CardAttribute.ANY),
  whitelist: Set[Card] = Set.empty,
  blacklist: Set[Card] = Set.empty,
  color: Color = Color.BLACK
) extends ((Card) => Boolean) {
  def including(card: Card) = if (filter(card)) copy(blacklist = blacklist - card) else copy(whitelist = whitelist + card)
  def +(card: Card) = including(card)

  def includingAll(cards: IterableOnce[Card]) = cards.foldLeft(this)(_ + _)
  def ++(cards: IterableOnce[Card]) = includingAll(cards)

  def excluding(card: Card) = if (filter(card)) copy(blacklist = blacklist + card) else copy(whitelist = whitelist - card)
  def -(card: Card) = excluding(card)

  def excludingAll(cards: IterableOnce[Card]) = cards.foldLeft(this)(_ - _)
  def --(cards: IterableOnce[Card]) = excludingAll(cards)

  override def apply(card: Card) = !blacklist.contains(card) && (filter(card) || whitelist.contains(card))
}