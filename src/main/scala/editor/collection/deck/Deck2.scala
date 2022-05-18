package editor.collection.deck

import editor.collection.CardList2
import editor.collection.CardListEntry
import editor.database.card.Card
import java.time.LocalDate
import editor.collection.deck.Category
import java.util.NoSuchElementException
import scala.collection.mutable.Clearable
import scala.collection.mutable.Growable
import scala.collection.mutable.Shrinkable

class Deck2 extends CardList2 with collection.mutable.IndexedSeq[CardListEntry] with collection.mutable.Clearable {
  private val entries = collection.mutable.ArrayBuffer[Entry]()

  class Entry private[Deck2](override val card: Card, private var amount: Int = 0, override val dateAdded: LocalDate = LocalDate.now) extends CardListEntry {
    private[Deck2] val _categories = collection.mutable.Set[Category]()
    if (amount > 0)
      Deck2.this.categories.caches.foreach{ case (_, cache) if cache.categorization.includes(card) => cache.filtrate += this }

    override def count = amount
    def count_=(n: Int) = {
      if (amount > 0) {
        val old = amount
        amount = math.max(n, 0)
        if (amount == 0) {
          entries -= this
          Deck2.this.categories.caches.foreach{ case (_, cache) => cache.filtrate -= this }
        }
      } else if (n > 0) {
        amount = n
        entries += this
        Deck2.this.categories.caches.foreach{ case (_, cache) if cache.categorization.includes(card) => cache.filtrate += this }
      }
    }

    def +=(n: Int) = count += n
    def -=(n: Int) = count -= math.min(n, count)

    override def categories = _categories.toSet
  }

  def contains(card: Card) = entries.exists(_.card == card)

  def add(card: Card, n: Int = 1, date: LocalDate = LocalDate.now) = entries.find(_.card == card).map(_ += n).getOrElse(entries += Entry(card, n, date))

  def remove(card: Card, n: Int = 1) = entries.find(_.card == card).map(_ -= n).getOrElse(throw NoSuchElementException(card.toString))

  def apply(card: Card) = entries.find(_.card == card).getOrElse(Entry(card))

  def addOne(card: CardListEntry) = {
    add(card.card, card.count, card.dateAdded)
    this
  }
  def +=(card: CardListEntry) = addOne(card)

  def addAll(cards: IterableOnce[CardListEntry]) = {
    cards.foreach((e) => add(e.card, e.count, e.dateAdded))
    this
  }
  def ++=(cards: IterableOnce[CardListEntry]) = addAll(cards)

  def subtractOne(card: CardListEntry) = {
    if (contains(card.card)) remove(card.card, card.count)
    this
  }
  def -=(card: CardListEntry) = subtractOne(card)

  def subtractAll(cards: IterableOnce[CardListEntry]) = {
    cards.filter((e) => contains(e.card)).foreach((e) => remove(e.card, e.count))
    this
  }
  def --=(cards: IterableOnce[CardListEntry]) = subtractAll(cards)

  override def apply(index: Int): Entry = entries(index)
  override def update(index: Int, card: CardListEntry) = entries(index) = Entry(card.card, card.count, card.dateAdded)
  override def length = entries.size
  override def total = entries.map(_.count).sum

  override def clear() = {
    entries.clear()
    categories.caches.foreach{ case (_, cache) => cache.filtrate.clear() }
  }

  private class Cache(private var spec: Category) extends CardList2 {
    var filtrate = collection.mutable.ArrayBuffer[Entry]()
    var rank = categories.size
    categorization = spec

    def categorization = spec
    def categorization_=(c: Category) = {
      spec = c
      filtrate = entries.filter((e) => spec.includes(e.card))
      filtrate.foreach((e) => if (spec.includes(e.card)) e._categories += spec else e._categories -= spec)
    }

    override def apply(index: Int) = filtrate(index)
    override def length = filtrate.length
    override def total = filtrate.map(_.count).sum
  }

  class CategoryData private[Deck2](cache: Cache) {
    def list: CardList2 = cache

    def categorization = cache.categorization
    def categorization_=(next: Category) = categories(categorization.getName) = next

    def rank = cache.rank
    def rank_=(r: Int) = if (r != rank) categories.caches.find{ case (_, c) => c.rank == r }.map{ case (_, c) =>
      val temp = c.rank
      c.rank = r
      cache.rank = temp
    }.getOrElse(throw ArrayIndexOutOfBoundsException(r))
  }

  object categories extends Iterable[CategoryData] with Growable[Category] with Shrinkable[String] with Clearable {
    private[Deck2] val caches = collection.mutable.Map[String, Cache]()

    override def addOne(categorization: Category) = if (!caches.contains(categorization.getName)) {
      caches += categorization.getName -> Cache(categorization)
      this
    } else throw IllegalArgumentException(s"there is already a category named ${categorization.getName}")

    override def subtractOne(name: String) = {
      if (caches.contains(name)) {
        val removed = caches(name)
        caches -= name
        caches.foreach{ case (_, cache) if cache.rank > removed.rank => cache.rank -= 1 }
        entries.foreach(_._categories -= removed.categorization)
      }
      this
    }

    def update(name: String, next: Category): Unit = if (next.getName == name || !caches.contains(next.getName)) {
      val cache = caches(name)
      caches -= name
      cache.categorization = next
      caches += next.getName -> cache
    } else throw IllegalArgumentException(s"there is already a category named ${next.getName}")

    def contains(name: String) = caches.contains(name)

    override def clear() = {
      caches.clear()
      entries.foreach(_._categories.clear())
    }

    override def knownSize = caches.size

    override def iterator = caches.map{ case (_, cache) => CategoryData(cache) }.iterator
  }
}