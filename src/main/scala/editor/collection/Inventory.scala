package editor.collection

import editor.database.card.Card
import editor.filter.Filter
import editor.filter.leaf.BinaryFilter
import scala.collection.immutable.MapOps
import scala.annotation.targetName

case class InventoryEntry(override val card: Card) extends CardListEntry {
  override def categories = throw UnsupportedOperationException("inventory doesn't have categories")
  override def count = throw UnsupportedOperationException("inventory doesn't count cards")
  override def dateAdded = card.expansion.released
}

class Inventory(val cards: Iterable[Card] = IndexedSeq.empty) extends CardList {
  private val list = cards.toIndexedSeq
  private lazy val ids = list.flatMap((c) => c.faces.map(_.scryfallid -> c)).toMap
  private var _filter: Filter = BinaryFilter(true)
  private var filtrate = list

  def get(id: String) = ids.get(id).map(InventoryEntry(_))
  def apply(id: String) = InventoryEntry(ids(id))
  def contains(id: String) = ids.contains(id)

  def filter = _filter.copy
  def filter_=(f: Filter) = {
    _filter = f
    filtrate = list.filter(f)
  }

  override def apply(index: Int) = InventoryEntry(filtrate(index))
  override def length = filtrate.size
  override def total = list.size
}