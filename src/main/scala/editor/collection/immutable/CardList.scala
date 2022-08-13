package editor.collection.immutable

import editor.collection.CardListEntry
import editor.database.card.Card

/**
 * Immutable list of [[CardListEntry]]s. There is only up to one entry for any card.
 * @author Alec Roelke
 */
trait CardList extends collection.IndexedSeq[CardListEntry] {
  /**
   * Determine if the list contains an entry for a card.
   * 
   * @param card card to search for
   * @return true if there is an entry for the card, and false otherwise
   */
  def contains(card: Card) = exists(_.card == card)

  /**
   * Get the index in the list of the entry for a card.
   * 
   * @param card card to search for
   * @return the index of the entry for the card, or -1 if there isn't one
   */
  def indexOf(card: Card) = indexWhere(_.card == card)

  /** @return the total number of copies of cards in the list, or the sum of the count in each entry */
  def total: Int
}