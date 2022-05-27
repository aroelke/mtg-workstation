package editor.collection.deck

import editor.collection.CardList
import editor.collection.CardListEntry
import editor.collection.MutableCardList
import editor.collection.deck.Category
import editor.database.card.Card

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.NoSuchElementException
import scala.collection.mutable.Clearable
import scala.collection.mutable.Growable
import scala.collection.mutable.Shrinkable

/**
 * Global information about [[Deck]]s.
 * @author Alec Roelke
 */
object Deck {
  /** Formatter used to format the date a card was added to a deck. */
  val DateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")
}

/**
 * A mutable list of [[CardListEntry]]s and collection of categories that can be used to filter them.  Adding duplicates of
 * individual cards or removing them causes the 'count' field of the existing entry to increase or decrease rather than adding or
 * removing whole entries. Entries are only removed when their 'count' fields are reduced to 0. Categories are added, removed, and updated using
 * their [[Category]] specifications and their names, respectively.
 * 
 * @author Alec Roelke
 */
class Deck extends CardList with MutableCardList {
  private val entries = collection.mutable.ArrayBuffer[Entry]()

  /**
   * An entry in a [[Deck]], with a mutable count value.
   * 
   * @constructor create a new deck entry
   * @param card card for the entry
   * @param amount initial number of copies of the card in the deck
   * @param dateAdded date the card was added to the deck
   * 
   * @author Alec Roelke
   */
  class Entry private[Deck](override val card: Card, private var amount: Int = 0, override val dateAdded: LocalDate = LocalDate.now) extends CardListEntry {
    private[Deck] val _categories = collection.mutable.Map[String, Category]()
    if (amount > 0) {
      Deck.this.categories.caches.foreach{ case (_, cache) => if (cache.categorization.includes(card)) {
        cache.filtrate += this
        _categories += cache.categorization.getName -> cache.categorization
      }}
    }

    override def count = amount

    /**
     * Update the number of copies of the card that are in the deck. If that number is 0, then this entry is removed from
     * the deck.
     * 
     * @param n new number of copies of the card in the deck
     */
    def count_=(n: Int) = {
      if (amount > 0) {
        val old = amount
        amount = math.max(n, 0)
        if (amount == 0) {
          entries -= this
          Deck.this.categories.caches.foreach{ case (_, cache) =>
            cache.filtrate -= this
            _categories -= cache.categorization.getName
          }
        }
      } else if (n > 0) {
        amount = n
        entries += this
        Deck.this.categories.caches.foreach{ case (_, cache) => if (cache.categorization.includes(card)) {
          cache.filtrate += this
          _categories += cache.categorization.getName -> cache.categorization
        }}
      }
    }

    /** Increase the number of copies of the card in the deck. */
    def +=(n: Int) = count += n

    /** Decrease the number of copies of the card in the deck, to a minimum of 0. */
    def -=(n: Int) = count -= math.min(n, count)

    override def categories = _categories.values.toSet

    override def canEqual(that: Any) = that match {
      case e: Deck#Entry => e.isInstanceOf[this.type]
      case e: CardListEntry => true
      case _ => false
    }
  }

  /**
   * Add a number of copies of a card to the deck at a certain date.
   * 
   * @param card card to add
   * @param n number of copies to add
   * @param date date which the card was added
   */
  def add(card: Card, n: Int = 1, date: LocalDate = LocalDate.now): Unit = entries.find(_.card == card).map(_ += n).getOrElse(entries += Entry(card, n, date))

  /**
   * Remove a number of copies of a card from the deck.
   * 
   * @param card card to remove
   * @param n number of copies to remove
   */
  def remove(card: Card, n: Int = 1) = entries.find(_.card == card).map(_ -= n).getOrElse(throw NoSuchElementException(card.toString))

  /**
   * Get the entry for a card in the deck, or create one if there isn't one already.  It won't actually be added to the deck
   * until its count is set to a positive number.
   * 
   * @param card card to look up
   * @return the entry corresponding to the card, or a new, empty entry for the card that can be used to add it to the deck
   */
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

  private class Cache(private var spec: Category) extends CardList {
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

  /**
   * Information about a category, including the cards in it, its categorization, and its rank.
   * 
   * @constructor create a new set of category data
   * @param cache cache of cards in the category
   * 
   * @author Alec Roelke
   */
  class CategoryData private[Deck](cache: Cache) {
    /** @return the list of cards in the category */
    def list: CardList = cache

    /** @return the [[Category]] of the category. */
    def categorization = cache.categorization

    /**
     * Update the [[Category]] of the category.  The name is allowed to change this way.
     * @param next new [[Category]]
     */
    def categorization_=(next: Category) = categories(categorization.getName) = next

    /** @return the category's rank. */
    def rank = cache.rank

    /**
     * Swap ranks with another category.  Attempting to set this category's rank to one that doesn't exist is an error.
     * @param r new rank for this category; the category at that rank will get this category's rank
     */
    def rank_=(r: Int) = if (r != rank) categories.caches.find{ case (_, c) => c.rank == r }.map{ case (_, c) =>
      val temp = c.rank
      c.rank = r
      cache.rank = temp
    }.getOrElse(throw ArrayIndexOutOfBoundsException(r))
  }

  /**
   * Collection of categories in the deck. Categories are added using their [[Category]]s, which contain their names, and
   * removed using their names only. Categories with duplicate names are not allowed.
   *
   * @author Alec Roelke
   */
  object categories extends Iterable[CategoryData] with Growable[Category] with Shrinkable[String] with Clearable {
    private[Deck] val caches = collection.mutable.Map[String, Cache]()

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

    /**
     * Update a category with a new [[Category]]. The new [[Category]] may have a different name than the old one.
     * 
     * @param name name of the category to update
     * @param next new [[Category]] to use
     */
    def update(name: String, next: Category): Unit = if (next.getName == name || !caches.contains(next.getName)) {
      val cache = caches(name)
      caches -= name
      cache.categorization = next
      caches += next.getName -> cache
    } else throw IllegalArgumentException(s"there is already a category named ${next.getName}")

    /** @return true if there is a category with the given name and false otherwise. */
    def contains(name: String) = caches.contains(name)

    /** @return the [[CategoryData]] for the category with the given name */
    def apply(name: String) = CategoryData(caches(name))

    override def clear() = {
      caches.clear()
      entries.foreach(_._categories.clear())
    }

    override def knownSize = caches.size

    override def iterator = caches.map{ case (_, cache) => CategoryData(cache) }.iterator
  }
}