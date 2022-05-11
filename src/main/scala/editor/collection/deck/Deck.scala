package editor.collection.deck

import editor.collection.CardList
import editor.collection.CardListEntry
import editor.database.card.Card
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Objects
import java.util.NoSuchElementException

case class DeckEntry(card: Card, private var amount: Int, override val dateAdded: LocalDate, categorizations: collection.mutable.LinkedHashSet[Category] = collection.mutable.LinkedHashSet[Category]()) extends CardListEntry {
  private[deck] def add(amt: Int) = if (amt < 1) false else {
    amount += amt
    true
  }

  private[deck] def remove(amt: Int) = if (amt < 1) 0 else {
    val old = count
    amount -= math.min(amt, count)
    old - count
  }

  def count_=(n: Int) = amount = count

  override def count = amount

  override def categories = categorizations.toSet
}

object Deck {
  val DateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")

  def apply() = new Deck()
  def apply(original: Deck) = {
    val d = new Deck()
    original.masterList.foreach((e) => d.add(e.card, e.count, e.dateAdded))
    original.caches.values.foreach((c) => d.addCategory(c.spec))
    d
  }
}

class Deck extends CardList {
  private class CategoryCache(var spec: Category) extends CardList {
    var rank: Int = caches.size
    var filtrate = collection.mutable.ArrayBuffer[Card]()
    update(spec)

    def update(s: Category) = {
      spec = s
      filtrate = collection.mutable.ArrayBuffer.from(masterList.collect{ case e if spec.includes(e.card) => e.card })
      masterList.foreach((e) => {
        if (spec.includes(e.card))
          e.categorizations += spec
        else
          e.categorizations -= spec
      })
    }

    override def add(card: Card) = add(card, 1)
    override def add(card: Card, amount: Int) = spec.includes(card) && Deck.this.add(card, amount)
    override def addAll(cards: CardList) = Deck.this.addAll(cards.collect{ case c if spec.includes(c) => c -> cards.getEntry(c).count }.toMap)
    override def addAll(cards: Map[? <: Card, Int]) = Deck.this.addAll(cards.filter{ case (c, _) => spec.includes(c) })
    override def addAll(cards: Set[? <: Card]) = Deck.this.addAll(cards.filter(spec.includes))
    override def clear() = Deck.this.removeAll(this)
    override def contains(card: Card) = filtrate.contains(card)
    override def containsAll(cards: Iterable[? <: Card]) = cards.forall(filtrate.contains)
    override def get(index: Int) = filtrate(index)
    override def getEntry(card: Card) = if (spec.includes(card)) Deck.this.getEntry(card) else DeckEntry(card, 0, null)
    override def getEntry(index: Int) = getEntry(get(index))
    override def indexOf(card: Card) = if (spec.includes(card)) filtrate.indexOf(card) else -1
    override def isEmpty = size == 0
    override def iterator = filtrate.iterator
    override def remove(card: Card) = remove(card, 1) > 0
    override def remove(card: Card, amount: Int) = if (contains(card)) Deck.this.remove(card, amount) else 0
    override def removeAll(cards: CardList) = Deck.this.removeAll(cards.collect{ case c if spec.includes(c) => c -> cards.getEntry(c).count }.toMap)
    override def removeAll(cards: Map[? <: Card, Int]) = Deck.this.removeAll(cards.filter{ case (c, _) => spec.includes(c) })
    override def removeAll(cards: Set[? <: Card]) = Deck.this.removeAll(cards.filter(spec.includes))
    override def set(card: Card, amount: Int) = spec.includes(card) && Deck.this.set(card, amount)
    override def set(index: Int, amount: Int) = set(get(index), amount)
    override def size = filtrate.size
    override def total = filtrate.map(Deck.this.getEntry(_).count).sum

    override def toString = spec.toString

    override def sort(comp: Ordering[? >: CardListEntry]) = throw UnsupportedOperationException("only the main deck can be sorted")
  }

  private val masterList = collection.mutable.ArrayBuffer[DeckEntry]()
  private val caches = collection.mutable.LinkedHashMap[String, CategoryCache]()
  private var ttl = 0

  private def createCategory(spec: Category) = {
    if (!caches.contains(spec.getName)) {
      val c = CategoryCache(spec)
      caches(spec.getName) = c
      true
    } else false
  }

  def add(card: Card, amount: Int, date: LocalDate) = if (amount < 1) false else {
    var entry = getEntry(card).asInstanceOf[DeckEntry]
    if (entry.count == 0) {
      entry = DeckEntry(card, 0, date)
      masterList += entry
      caches.values.foreach((cache) => if (cache.spec.includes(card)) {
        cache.filtrate += card
        entry.categorizations += cache.spec
      })
    }
    entry.add(amount)
    ttl += amount
    true
  }

  def addCategory(spec: Category): CardList = {
    createCategory(spec)
    caches(spec.getName)
  }

  def addCategory(spec: Category, rank: Int): CardList = {
    if (createCategory(spec)) {
      val c = caches(spec.getName)
      c.rank = rank
      c
    } else if (caches(spec.getName).rank == rank) {
      caches(spec.getName)
    } else if (swapCategoryRanks(spec.getName, rank)) {
      caches(spec.getName)
    } else throw IllegalArgumentException(s"could not add category ${spec.getName} at rank $rank")
  }

  def categories = caches.values.map(_.spec)

  def containsCategory(name: String) = caches.contains(name)

  def exclude(name: String, card: Card) = contains(card) && caches(name).spec.exclude(card)

  def getCategoryList(name: String): CardList = caches(name)

  def getCategoryRank(name: String) = if (containsCategory(name)) caches(name).rank else -1

  def getCategorySpec(name: String) = Category(caches(name).spec)

  def numCategories = caches.size

  def removeCategory(spec: Category): Boolean = if (!caches.contains(spec.getName)) false else {
    val c = caches(spec.getName)
    masterList.foreach(_.categorizations -= c.spec)
    val oldRanks = collection.mutable.HashMap[String, Int]()
    caches.values.foreach((category) => if (category.rank > c.rank) {
      oldRanks(category.spec.getName) = category.rank
      category.rank -= 1
    })
    caches -= spec.getName
    if (!oldRanks.isEmpty)
      oldRanks(c.spec.getName) = c.rank
    true
  }

  def removeCategory(name: String): Boolean = if (caches.contains(name)) removeCategory(getCategorySpec(name)) else false

  def swapCategoryRanks(name: String, target: Int) = if (!caches.contains(name) || caches(name).rank == target || target >= caches.size || target < 0) false else {
    caches.values.find(_.rank == target).map((second) => {
      val oldRanks = collection.mutable.HashMap[String, Int]()
      oldRanks(name) = caches(name).rank
      oldRanks(second.spec.getName) = second.rank
      second.rank = caches(name).rank
      caches(name).rank = target
      true
    }).getOrElse(false)
  }

  def updateCategory(name: String, spec: Category) = if (caches.contains(name)) {
    val c = caches(name)
    val old = Category(c.spec)
    caches -= name
    c.update(spec)
    caches(spec.getName) = c
    old
  } else throw NoSuchElementException(name)

  override def add(card: Card) = add(card, 1)
  override def add(card: Card, amount: Int) = add(card, amount, LocalDate.now)
  override def addAll(list: CardList) = {
    val added = collection.mutable.HashMap[Card, Int]()
    list.foreach((card) => {
      if (add(card, list.getEntry(card).count, list.getEntry(card).dateAdded))
        added(card) = list.getEntry(card).count
    })
    !added.isEmpty
  }
  override def addAll(list: Map[? <: Card, Int]) = {
    val added = collection.mutable.HashMap[Card, Int]()
    list.foreach{ case (card, amount) =>
      if (add(card, amount, LocalDate.now))
        added(card) = amount
    }
    !added.isEmpty
  }
  override def addAll(list: Set[? <: Card]) = addAll(list.map(_ -> 1).toMap)
  override def clear() = {
    masterList.clear()
    caches.clear()
    ttl = 0
  }
  override def contains(card: Card) = getEntry(card).count > 0
  override def containsAll(cards: Iterable[? <: Card]) = cards.forall(getEntry(_).count > 0)
  override def get(index: Int) = masterList(index).card
  override def getEntry(card: Card) = masterList.find(_.card == card).getOrElse(DeckEntry(card, 0, LocalDate.now))
  override def getEntry(index: Int) = masterList(index)
  override def indexOf(card: Card) = masterList.indexOf(getEntry(card))
  override def isEmpty = size == 0
  override def iterator = masterList.map(_.card).iterator
  override def remove(card: Card) = remove(card, Int.MaxValue) > 0
  override def remove(card: Card, amount: Int) = if (amount < 1) 0 else {
    val entry = getEntry(card).asInstanceOf[DeckEntry]
    if (entry.count == 0) 0 else {
      val removed = entry.remove(amount)
      if (removed > 0) {
        if (entry.count == 0) {
          caches.values.foreach((category) => {
            if (category.spec.getWhitelist.contains(card))
              category.spec.exclude(card)
            if (category.spec.getBlacklist.contains(card))
              category.spec.include(card)
            category.filtrate -= card
          })
          masterList -= entry
        }
        ttl -= removed
      }
      removed
    }
  }
  override def removeAll(cards: CardList) = removeAll(cards.map((c) => c -> cards.getEntry(c).count).toMap)
  override def removeAll(cards: Map[? <: Card, Int]) = {
    val removed = collection.mutable.HashMap[Card, Int]()
    cards.foreach{ case (card, amount) =>
      val r = remove(card, amount)
      if (r > 0)
        removed(card) = r
    }
    removed.toMap
  }
  override def removeAll(cards: Set[? <: Card]) = removeAll(cards.map(_ -> 1).toMap).keySet
  override def set(card: Card, amount: Int) = {
    val amt = math.max(amount, 0)
    val e = getEntry(card).asInstanceOf[DeckEntry]
    if (e.count == 0)
      add(card, amount)
    else if (e.count == amt)
      false
    else {
      ttl += amt - e.count
      val change = collection.mutable.HashMap[Card, Int]()
      change(card) = amt - e.count
      e.count = amt
      if (e.count == 0) {
        masterList -= e
        caches.values.foreach((category) => {
          category.filtrate -= e.card
          category.spec.getWhitelist.remove(e.card)
          category.spec.getBlacklist.remove(e.card)
        })
      }
      true
    }
  }
  override def set(index: Int, amount: Int) = set(masterList(index).card, amount)
  override def size = masterList.size
  override def total = ttl
  override def sort(comp: Ordering[? >: CardListEntry]) = {
    masterList.sortInPlace()(comp)
    caches.values.foreach((category) => category.filtrate.sortInPlace()((a, b) => comp.compare(getEntry(a), getEntry(b))))
  }

  override def equals(other: Any) = other match {
    case o: Deck => masterList == o.masterList && caches == o.caches
    case _ => false
  }
  override def hashCode = Objects.hash(masterList, caches)
}