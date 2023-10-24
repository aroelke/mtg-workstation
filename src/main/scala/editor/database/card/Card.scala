package editor.database.card

import editor.database.attributes.CombatStat
import editor.database.attributes.Expansion
import editor.database.attributes.Legality
import editor.database.attributes.CounterStat
import editor.database.attributes.ManaCost
import editor.database.attributes.ManaType
import editor.database.attributes.Rarity
import editor.database.attributes.TypeLine
import editor.unicode

import java.text.Collator
import java.util.Date
import java.util.Locale
import java.util.Objects
import javax.swing.text.StyledDocument
import scala.collection.immutable.AbstractSet
import scala.collection.immutable.TreeMap
import scala.collection.mutable.Growable
import scala.collection.mutable.Shrinkable

/**
 * Object containing global card data and data to aid with displaying card information.
 * @author Alec Roelke
 */
object Card {
  /** String used to separate information per card face when that data is to be displayed on a single line, like a card's name. */
  val FaceSeparator = " // "

  /** String used to separate information per card face when that data is to be displayed on multiple lines, like a card's text. */
  val TextSeparator = "-----"

  /** String used to represent a card's own name in its text box. */
  val This = "~"
}

/**
 * Data structure containing all relevant information about an individual Magic: The Gathering card. Some
 * cards may have multiple faces; each of those is represented as a child [[Card]] whose attributes are
 * singleton lists of values.
 * 
 * @constructor create a new card
 * @param expansion expansion to which the card belongs
 * @param layout layout of the card; determines how many faces the card has
 * 
 * @author Alec Roelke
 */
abstract class Card(val expansion: Expansion, val layout: CardLayout) {
  import Card._

  /** @return the individual faces of the card. */
  def faces: Seq[Card]

  /**
   * Alias for [[faces.apply]].
   * 
   * @param i index of the face to get
   * @return the face at the given index
   */
  def apply(i: Int) = faces(i)

  /** Card name with special characters changed to ASCII equivalents for searchability. @see [[Card.name]] */
  lazy val normalizedName = faces.map(f => unicode.normalize(f.name))

  /** Name of the entity represented by a legendary card, or just the card name if it's not legendary. */
  lazy val legendName = {
    val legendNames = collection.mutable.Buffer[String]()
    for (fullName <- normalizedName) {
      if (!supertypes.contains("Legendary"))
        legendNames += fullName
      else {
        val comma = fullName.indexOf(',')
        if (comma > 0)
          legendNames += fullName.substring(0, comma).trim
        else {
          val the = fullName.indexOf("the ")
          if (the == 0)
            legendNames += fullName
          else if (the > 0)
            legendNames += fullName.substring(0, the).trim
          else {
            val of = fullName.indexOf("of ")
            if (of > 0)
              legendNames += fullName.substring(0, of).trim
            else
              legendNames += fullName
          }
        }
      }
    }
    legendNames.toSeq
  }

  /** Oracle text with special characters converted to ASCII equivalents for searchability. @see [[Card.oracleText]] */
  lazy val normalizedOracle = {
    val texts = new collection.mutable.ArrayBuffer[String](oracleText.size)
    for (i <- 0 until faces.size) {
      var normal = unicode.normalize(faces(i).oracleText.toLowerCase)
      normal = normal.replace(legendName(i), This).replace(normalizedName(i), This)
      texts += normal
    }
    texts.toSeq
  }

  /** Printed text with special characters converted to ASCII equivalents for searchability. @see [[Card.printedText]] */
  lazy val normalizedPrinted = faces.map((f) => unicode.normalize(f.printedText))

  /** Flavor text with special characters converted to ASCII equivalents for searchability. @see [[Card.flavorText]] */
  lazy val normalizedFlavor = faces.map((f) => unicode.normalize(f.flavorText))

  /** Whether or not the card ignores the restriction on the number allowed in a deck. */
  lazy val ignoreCountRestriction = supertypes.exists(_.equalsIgnoreCase("basic")) || faces.exists(_.oracleText.toLowerCase.contains("a deck can have any number"))

  /** List of formats the card is legal in. */
  lazy val legalIn = legality.keys.filter(legalityIn(_).isLegal).toSet

  /** @return the name of the card. */
  def name: String

  /** @return the mana cost of the card. */
  def manaCost: ManaCost

  /** @return the mana value of the card. */
  def manaValue: Double

  /** @return the lowest mana value among faces of the card. */
  def minManaValue: Double

  /** @return the highest mana value among faces of the card. */
  def maxManaValue: Double

  /** @return the average mana value of faces of the card. */
  def avgManaValue: Double

  /** @return the colors of the card. */
  def colors: Set[ManaType]

  /** @return the color identity of the card, which is the set of colors across all of its faces plus those of any mana symbols in its text boxes. */
  def colorIdentity: Set[ManaType]

  /** @return the card's type line. */
  def typeLine: TypeLine

  /** @return all supertypes of the card. */
  final def supertypes = typeLine.supertypes

  /** @return all card types of the card. */
  final def types = typeLine.types

  /** @return all subtypes of the card. */
  final def subtypes = typeLine.subtypes

  /** @return the card's printed type line. */
  def printedTypes: String

  /** @return the card's rarity. */
  def rarity: Rarity

  /** @return the card's Oracle text. */
  def oracleText: String

  /** @return the card's printed text. */
  def printedText: String

  /** @return the card's flavor text. */
  def flavorText: String

  /** @return the card's power if it has one, or None otherwise. */
  def power: Option[CombatStat]

  /** @return the card's toughness if it has one, or None otherwise. */
  def toughness: Option[CombatStat]

  /** @return the card's loyalty if it's a planeswalker, or None otherwise. */
  def loyalty: Option[CounterStat]

  /** @return the card's defense if it's a battle, or None otherwise. */
  def defense: Option[CounterStat]

  /** @return the car'd artist. */
  def artist: String

  /** @return the card's collector number. */
  def number: String

  /** @return a mapping of each format onto this card's legality in that format as of the most recent inventory update. */
  def legality: Map[String, Legality]

  /** @return the card's multiverse ID, which is a unique number used in Gatherer to identify the card. */
  def multiverseid: Int

  /** @return the card's Scryfall ID, which is used to identify the card on Scryfall. */
  def scryfallid: String

  /** @return the list of formats this card can be commander in. */
  def commandFormats: Seq[String]

  /** @return the Gatherer rulings for the card that clarify how it works. */
  def rulings: TreeMap[Date, Seq[String]]

  /** @return true if this card is a land, or false otherwise. */
  def isLand: Boolean

  /** @return the set of [[ManaType]]s this card can produce, or an empty set if it can't produce mana. */
  def produces: Set[ManaType]

  /** @return the file names of the images to use to display the card. */
  def imageNames: Seq[String]

  /**
   * Add information about all of the faces of the card to a GUI element's document.
   * 
   * @param document document to add to
   * @param printed true to use the card's printed information, and false to use its Oracle data
   */
  def formatDocument(document: StyledDocument, printed: Boolean): Unit

  /**
   * Add information about a single face to a GUI element's document.
   * 
   * @param document document to add to
   * @param printed true to use the face's printed information, and false to use its Oracle data
   * @param face index of the face to use
   */
  def formatDocument(document: StyledDocument, printed: Boolean, face: Int): Unit

  /**
   * Compare this card's [[Card.name]] to that of another.
   * 
   * @param other card to compare with
   * @return a negative number if this card's name comes lexicographically before the other one's name,
   * a positive number if it comes after, or 0 if they're the same, ignoring case.
   */
  def compareName(other: Card) = Collator.getInstance(Locale.US).compare(name, other.name)

  /** @return true if the card has a power value and it's variable (contains *), or false otherwise. */
  def powerVariable = faces.exists(_.power.exists(_.variable))

  /** @return true if the card has a toughness value and it's variable (contains *) or false otherwise. */
  def toughnessVariable = faces.exists(_.toughness.exists(_.variable))

  /** @return true if the card has a loyalty value and it's variable (contains * or X) or false otherwise. */
  def loyaltyVariable = faces.exists(_.loyalty.exists(_.variable))

  /** @return true if the card has a defense value and it's variable (contains * or X) or false otherwise. */
  def defenseVariable = faces.exists(_.defense.exists(_.variable))

  /**
   * Determine the card's legality in a particular format.
   * 
   * @param format format to check
   * @return the legality of the card in the given format.
   */
  def legalityIn(format: String) = legality.getOrElse(format, Legality.Illegal)

  override def equals(other: Any) = other match {
    case c: Card => faces.map(_.scryfallid) == c.faces.map(_.scryfallid)
    case _ => false
  }
  override def hashCode = Objects.hash((faces.map(_.name) ++ faces.map(_.scryfallid)):_*)
  override def toString = name
}