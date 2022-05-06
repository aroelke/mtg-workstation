package editor.collection

import editor.collection.deck.Category
import editor.database.attributes.CardAttribute
import editor.database.card.Card
import java.time.LocalDate
import scala.jdk.CollectionConverters._

trait CardListEntry {
  import CardAttribute._

  def card: Card
  def categories: Set[Category]
  def count: Int
  def dateAdded: LocalDate
  def apply(data: CardAttribute) = data match {
    case NAME => card.name
    case LAYOUT => card.layout
    case MANA_COST => card.faces.map(_.manaCost).asJava
    case REAL_MANA_VALUE => card.manaValue
    case EFF_MANA_VALUE => card.faces.map((f) => java.lang.Double(f.manaValue)).asJava
    case COLORS => card.colors.asJava
    case COLOR_IDENTITY => card.colorIdentity.asJava
    case TYPE_LINE => card.faces.map(_.typeLine).asJava
    case EXPANSION => card.expansion.toString
    case BLOCK => card.expansion.block
    case RARITY => card.rarity
    case POWER => card.faces.map(_.power).asJava
    case TOUGHNESS => card.faces.map(_.toughness).asJava
    case LOYALTY => card.faces.map(_.loyalty).asJava
    case ARTIST => card.faces(0).artist
    case CARD_NUMBER => card.faces.map(_.number).mkString(Card.FaceSeparator)
    case LEGAL_IN => card.legalIn.toSeq.sorted.asJava
    case COUNT => count
    case CATEGORIES => categories
    case DATE_ADDED => dateAdded
    case TAGS => java.util.LinkedHashSet(Card.tags(card).toSeq.sorted.asJava)
  }
}

trait CardList extends Iterable[Card] {
  def add(card: Card): Boolean
  def add(card: Card, amount: Int): Boolean
  def addAll(cards: CardList): Boolean
  def addAll(amounts: Map[? <: Card, Int]): Boolean
  def addAll(cards: Set[? <: Card]): Boolean
  def clear(): Unit
  def contains(card: Card): Boolean
  def containsAll(cards: Iterable[? <: Card]): Boolean
  def get(index: Int): Card
  def getEntry(card: Card): CardListEntry
  def getEntry(index: Int): CardListEntry
  def indexOf(card: Card): Int
  def isEmpty: Boolean
  def remove(card: Card): Boolean
  def remove(card: Card, amount: Int): Int
  def removeAll(cards: CardList): Map[Card, Int]
  def removeAll(cards: Map[? <: Card, Int]): Map[Card, Int]
  def removeAll(cards: Set[? <: Card]): Set[Card]
  def set(card: Card, amount: Int): Boolean
  def set(index: Int, amount: Int): Boolean
  def size: Int
  def total: Int
  def sort(comp: Ordering[? >: CardListEntry]): Unit
}