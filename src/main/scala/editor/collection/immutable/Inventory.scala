package editor.collection.immutable

import editor.collection.CardListEntry
import editor.database.card.Card
import editor.filter.Filter
import editor.filter.leaf.BinaryFilter

import java.time.LocalDate

/**
 * An entry in the [[Inventory]]. Since the inventory is just a source for cards, it doesn't track card counts and attempting
 * to get a card count results in an error. The date added corresponds with the date the card was released.
 * 
 * @constructor create a new inventory entry
 * @param card card for the new entry
 * 
 * @author Alec Roelke
 */
class InventoryEntry(override val card: Card) extends CardListEntry {
  override def count = throw UnsupportedOperationException("inventory doesn't count cards")
  override def dateAdded = card.expansion.released
  override def categories = throw UnsupportedOperationException("inventory doesn't have categories")
}

/**
 * Inventory of cards that can be added to decks.  Can't be changed, but provides a view of the cards in it that is filtered
 * using a user-defined filter. Indexing into or iterating over the inventory uses this view.
 * 
 * @param cards cards to use to create the inventory
 * 
 * @author Alec Roelke
 */
class Inventory(val cards: Iterable[Card] = IndexedSeq.empty) extends CardList {
  private val list = cards.toIndexedSeq
  private lazy val ids = list.flatMap((c) => c.faces.map(_.scryfallid -> c)).toMap
  private var _filter: Filter = BinaryFilter(true)
  private var filtrate = list

  /** @return the card whose Scryfall ID matches the given string, even if it's filtered out, or None if there isn't one. */
  def get(id: String) = ids.get(id).map(InventoryEntry(_))

  /** @return the card whose Scryfall ID matches the given string, even if it's filtered out. */
  def apply(id: String) = InventoryEntry(ids(id))

  /** @return true if there is a card with the given Scryfall ID, even if it's filtered out, and false otherwise. */
  def contains(id: String) = ids.contains(id)

  /** @return the filter used for filtering the inventory. */
  def filter = _filter.copy

  /**
   * Update the filtered view of cards. Applies to the whole inventory, not to the current view (i.e. new filters aren't
   * cumulative).
   * @param f new filter to use for cards
   */
  def filter_=(f: Filter) = {
    _filter = f
    filtrate = list.filter(f)
  }

  override def apply(index: Int) = InventoryEntry(filtrate(index))
  override def length = filtrate.size
  override def total = list.size
}