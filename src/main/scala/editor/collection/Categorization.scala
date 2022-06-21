package editor.collection

import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.filter.Filter

import java.awt.Color

/**
 * A named means of grouping cards together in a deck by like properties specified by a [[Filter]]. Additionally has a whitelist and
 * blacklist to explicitly include or exclude cards. A card should never be in both the blacklist and whitelist at the same time or
 * be in the whitelist if it passes the filter or the blacklist if it doesn't.
 * 
 * @constructor create a new categorization
 * @param name name of the categorization
 * @param filter grouping for cards
 * @param whitelist explicitly-included cards, even if they don't pass the filter
 * @param blacklist explicitly-excluded cards, even if they pass the filter
 * @param color color to use for showing the category instead of text
 * 
 * @author Alec Roelke
 */
case class Categorization(
  name: String = "All Cards",
  filter: Filter = CardAttribute.createFilter(CardAttribute.ANY),
  whitelist: Set[Card] = Set.empty,
  blacklist: Set[Card] = Set.empty,
  color: Color = Color.BLACK
) extends ((Card) => Boolean) {
  /**
   * Explicitly include a card in the categorization or remove it from the blacklist.
   * 
   * @param card card to include
   * @return a copy of this categorization with the card included in it
   */
  def including(card: Card) = if (filter(card)) copy(blacklist = blacklist - card) else copy(whitelist = whitelist + card)
  /** Alias for [[including]]. */
  def +(card: Card) = including(card)

  /**
   * Explicitly include cards in the categorization or remove t hem from the blacklist.
   * 
   * @param cards cards to include
   * @return a copy of this categorization with all the cards included in it
   */
  def includingAll(cards: IterableOnce[Card]) = cards.foldLeft(this)(_ + _)
  /** Alias for [[includingAll]]. */
  def ++(cards: IterableOnce[Card]) = includingAll(cards)

  /**
   * Explicitly exclude a card from the categorization or remove it from the whitelist.
   * 
   * @param card card to exclude
   * @return a copy of this categorization with the card excluded from it
   */
  def excluding(card: Card) = if (filter(card)) copy(blacklist = blacklist + card) else copy(whitelist = whitelist - card)
  /** Alias for [[excluding]]. */
  def -(card: Card) = excluding(card)

  /**
   * Explicitly exclude cards from the categorization or remove them from the whitelist.
   * 
   * @param cards cards to exclude
   * @return a copy of this categorization with all the cards excluded from it
   */
  def excludingAll(cards: IterableOnce[Card]) = cards.foldLeft(this)(_ - _)
  /** Alias for [[excludingAll]]. */
  def --(cards: IterableOnce[Card]) = excludingAll(cards)

  /**
   * Determine if a card belongs in the category.
   * 
   * @param card card to test
   * @return true if the card isn't in the blacklist and either passes the filter or is in the whitelist
   */
  override def apply(card: Card) = !blacklist.contains(card) && (filter(card) || whitelist.contains(card))
}