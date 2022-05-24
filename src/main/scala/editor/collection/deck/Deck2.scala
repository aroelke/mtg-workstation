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
import editor.collection.MutableCardList

class Deck2 extends CardList2 with MutableCardList {
  private val entries = collection.mutable.ArrayBuffer[Entry]()

  case class Entry private[Deck2](override val card: Card, private var amount: Int = 0, override val dateAdded: LocalDate = LocalDate.now) extends CardListEntry {
    private[Deck2] val _categories = collection.mutable.Map[String, Category]()
    if (amount > 0) {
      Deck2.this.categories.caches.foreach{ case (_, cache) => if (cache.categorization.includes(card)) {
        cache.filtrate += this
        _categories += cache.categorization.getName -> cache.categorization
      }}
    }

    override def count = amount
    def count_=(n: Int) = {
      if (amount > 0) {
        val old = amount
        amount = math.max(n, 0)
        if (amount == 0) {
          entries -= this
          Deck2.this.categories.caches.foreach{ case (_, cache) =>
            cache.filtrate -= this
            _categories -= cache.categorization.getName
          }
        }
      } else if (n > 0) {
        amount = n
        entries += this
        Deck2.this.categories.caches.foreach{ case (_, cache) => if (cache.categorization.includes(card)) {
          cache.filtrate += this
          _categories += cache.categorization.getName -> cache.categorization
        }}
      }
    }

    def +=(n: Int) = count += n
    def -=(n: Int) = count -= math.min(n, count)

    override def categories = _categories.values.toSet
  }

  def add(card: Card, n: Int = 1, date: LocalDate = LocalDate.now) = entries.find(_.card == card).map(_ += n).getOrElse(entries += Entry(card, n, date))

  def remove(card: Card, n: Int = 1) = entries.find(_.card == card).map(_ -= n).getOrElse(throw NoSuchElementException(card.toString))

  def apply(card: Card) = entries.find(_.card == card).getOrElse(Entry(card))

  override def addOne(card: CardListEntry) = {
    add(card.card, card.count, card.dateAdded)
    this
  }

  override def addAll(cards: IterableOnce[CardListEntry]) = {
    cards.foreach((e) => add(e.card, e.count, e.dateAdded))
    this
  }

  override def subtractOne(card: CardListEntry) = {
    if (contains(card.card)) remove(card.card, card.count)
    this
  }

  def subtractAll(cards: IterableOnce[CardListEntry]) = {
    cards.filter((e) => contains(e.card)).foreach((e) => remove(e.card, e.count))
    this
  }

  override def update(index: Int, card: CardListEntry) = entries(index) = Entry(card.card, card.count, card.dateAdded)

  override def apply(index: Int): Entry = entries(index)
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
      filtrate.foreach((e) => if (spec.includes(e.card)) e._categories += spec.getName -> spec else e._categories -= spec.getName)
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
        caches.foreach{ case (_, cache) => if (cache.rank > removed.rank) cache.rank -= 1 }
        entries.foreach(_._categories -= removed.categorization.getName)
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

    def apply(name: String) = CategoryData(caches(name))

    override def clear() = {
      caches.clear()
      entries.foreach(_._categories.clear())
    }

    override def knownSize = caches.size

    override def iterator = caches.map{ case (_, cache) => CategoryData(cache) }.iterator
  }
}