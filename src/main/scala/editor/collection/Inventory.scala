package editor.collection

import editor.collection.deck.Category
import editor.database.card.Card
import editor.filter.Filter
import editor.filter.leaf.BinaryFilter

case class InventoryEntry(override val card: Card) extends CardListEntry {
  override def categories = throw UnsupportedOperationException("inventory doesn't have categories")
  override def count = throw UnsupportedOperationException("inventory doesn't count cards")
  override def dateAdded = card.expansion.released
}

class Inventory(private var cards: Seq[Card] = Seq.empty) extends CardList {
  private val ids = cards.flatMap((c) => c.faces.map((f) => f.scryfallid -> c)).toMap
  private var filter: Filter = BinaryFilter(true)
  private var filtrate = cards

  def contains(id: String) = ids.contains(id)
  def contains(id: Int) = cards.exists(_.faces.exists(_.multiverseid == id))
  def find(id: String) = ids(id)
  def find(id: Int) = cards.find(_.faces.exists(_.multiverseid == id)).get
  def getFilter = filter.copy
  def updateFilter(f: Filter) = {
    filter = f
    filtrate = cards.filter(f)
  }

  override def contains(card: Card) = ids.values.toSet.contains(card)
  override def containsAll(list: Iterable[Card]) = { val s = ids.values.toSet; list.forall(s.contains) }
  override def get(index: Int) = filtrate(index)
  override def getEntry(card: Card) = InventoryEntry(card)
  override def getEntry(index: Int) = InventoryEntry(get(index))
  override def indexOf(card: Card) = filtrate.indexOf(card)
  override def isEmpty = filtrate.isEmpty
  override def iterator = cards.iterator
  override def size = filtrate.size
  override def sort(comp: Ordering[? >: CardListEntry]) = {
    cards = cards.sorted((a, b) => comp.compare(InventoryEntry(a), InventoryEntry(b)))
    filtrate = cards.filter(filter)
  }
  override def total = cards.size

  override def add(card: Card) = throw UnsupportedOperationException("can't change inventory")
  override def add(card: Card, amount: Int) = throw UnsupportedOperationException("can't change inventory")
  override def addAll(cards: CardList) = throw UnsupportedOperationException("can't change inventory")
  override def addAll(cards: Map[? <: Card, Int]) = throw UnsupportedOperationException("can't change inventory")
  override def addAll(cards: Set[? <: Card]) = throw UnsupportedOperationException("can't change inventory")
  override def clear() = throw UnsupportedOperationException("can't change inventory")
  override def remove(card: Card) = throw UnsupportedOperationException("can't change inventory")
  override def remove(card: Card, amount: Int) = throw UnsupportedOperationException("can't change inventory")
  override def removeAll(list: CardList) = throw UnsupportedOperationException("can't change inventory")
  override def removeAll(list: Map[? <: Card, Int]) = throw UnsupportedOperationException("can't change inventory")
  override def removeAll(list: Set[? <: Card]) = throw UnsupportedOperationException("can't change inventory")
  override def set(card: Card, amount: Int) = throw UnsupportedOperationException("can't change inventory")
  override def set(index: Int, amount: Int) = throw UnsupportedOperationException("can't change inventory")
}