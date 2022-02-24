package editor.database.card

import editor.database.attributes.CombatStat
import editor.database.attributes.Expansion
import editor.database.attributes.Legality
import editor.database.attributes.Loyalty
import editor.database.attributes.ManaCost
import editor.database.attributes.ManaType
import editor.database.attributes.Rarity
import editor.util.Lazy
import editor.util.UnicodeSymbols

import java.text.Collator
import java.util.Date
import java.util.Locale
import java.util.Objects
import java.util.regex.Pattern
import javax.swing.text.StyledDocument
import scala.jdk.CollectionConverters._

object Card {
  val FaceSeparator = " // "
  val TextSeparator = "-----"
  val This = "~"

  val tagMap = java.util.HashMap[Card, java.util.Set[String]]
  def tags = tagMap.asScala.flatMap{ case (_, t) => t.asScala }.toSet.asJava

  @deprecated def FACE_SEPARATOR = FaceSeparator
  @deprecated def TEXT_SEPARATOR = TextSeparator
  @deprecated def THIS = This
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

  /** Names of the card faces with special characters changed to ASCII equivalents for searchability. @see [[Card.name]] */
  lazy val normalizedName = name.asScala.map(UnicodeSymbols.normalize).asJava

  /** Name of the entity represented by a legendary card, or just the card name if it's not legendary. */
  lazy val legendName = {
    val legendNames = collection.mutable.Buffer[String]()
    for (fullName <- normalizedName.asScala) {
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
    legendNames.asJava
  }

  /** Oracle text with special characters converted to ASCII equivalents for searchability. @see [[Card.oracleText]] */
  lazy val normalizedOracle = {
    val texts = new collection.mutable.ArrayBuffer[String](oracleText.size)
    for (i <- 0 until oracleText.size) {
      var normal = UnicodeSymbols.normalize(oracleText.get(i).toLowerCase)
      normal = normal.replace(legendName.get(i), This).replace(normalizedName.get(i), This)
      texts += normal
    }
    texts.asJava
  }

  /** Printed text with special characters converted to ASCII equivalents for searchability. @see [[Card.printedText]] */
  lazy val normalizedPrinted = printedText.asScala.map(UnicodeSymbols.normalize).asJava

  /** Flavor text with special characters converted to ASCII equivalents for searchability. @see [[Card.flavorText]] */
  lazy val normalizedFlavor = flavorText.asScala.map(UnicodeSymbols.normalize).asJava

  /** Whether or not the card ignores the restriction on the number allowed in a deck. */
  lazy val ignoreCountRestriction = supertypeContains("basic") || oracleText.asScala.exists(_.toLowerCase.contains("a deck can have any number"))

  /** List of formats the card is legal in. */
  lazy val legalIn = legality.keySet.asScala.filter(legalityIn(_).isLegal).toSeq.asJava

  /** @return a list containing the name of each face of the card. */
  def name: java.util.List[String]

  /** @return a list containing the mana cost of each face of the card. */
  def manaCost: java.util.List[ManaCost]

  /** @return the mana value of the card. */
  def manaValue: Double

  /** @return the lowest mana value among faces of the card. */
  def minManaValue: Double

  /** @return the highest mana value among faces of the card. */
  def maxManaValue: Double

  /** @return the average mana value of faces of the card. */
  def avgManaValue: Double

  /** @return the colors of the card across all faces. */
  def colors: java.util.List[ManaType]

  /** @return the colors of a particular face of the card. */
  def colors(face: Int): java.util.List[ManaType]

  /** @return the color identity of the card, which is the set of colors across all of its faces plus those of any mana symbols in its text boxes. */
  def colorIdentity: java.util.List[ManaType]

  /** @return the set of supertypes across all faces of the card. */
  def supertypes: java.util.Set[String]

  /** @return the set of card types across all faces of the card. */
  def types: java.util.Set[String]

  /** @return the set of subtypes across all faces of the card. */
  def subtypes: java.util.Set[String]

  /** @return a list containing each face's full type line. */
  def typeLine: java.util.List[String]

  /** @return a list containing each face's complete set of types. */
  def allTypes: java.util.List[java.util.Set[String]]

  /** @return a list containing each face's printed type line. */
  def printedTypes: java.util.List[String]

  /** @return the card's rarity. */
  def rarity: Rarity

  /** @return a list containing each face's Oracle text. */
  def oracleText: java.util.List[String]

  /** @return a list containing each face's printed text. */
  def printedText: java.util.List[String]

  /** @return a list containing each face's flavor text. */
  def flavorText: java.util.List[String]

  /** @return a list containing each face's power, if it's a creature. */
  def power: java.util.List[CombatStat]

  /** @return a list containing each face's toughness, if it's a creature. */
  def toughness: java.util.List[CombatStat]

  /** @return a list containing each face's loyalty, if it's a planeswalker. */
  def loyalty: java.util.List[Loyalty]

  /** @return a list containing the artist of each face. */
  def artist: java.util.List[String]

  /** @return a list containing the collector number of each face. */
  def number: java.util.List[String]

  /** @return a mapping of each format onto this card's legality in that format as of the most recent inventory update. */
  def legality: java.util.Map[String, Legality]

  /** @return the card's multiverse ID, which is a unique number used in Gatherer to identify the card. */
  def multiverseid: java.util.List[Integer]

  /** @return the car'd Scryfall ID, which is used to identify the card on Scryfall. */
  def scryfallid: java.util.List[String]

  /** @return the list of formats this card can be commander in. */
  def commandFormats: java.util.List[String]

  /** @return the Gatherer rulings for the card that clarify how it works. */
  def rulings: java.util.Map[Date, java.util.List[String]]

  /** @return true if this card is a land, or false otherwise. */
  def isLand: Boolean

  /** @return the file names of the images to use to display the card. */
  def imageNames: java.util.List[String]

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
   * Compare this card's [[Card.unifiedName]] to that of another.
   * 
   * @param other card to compare with
   * @return a negative number if this card's name comes lexicographically before the other one's name,
   * a positive number if it comes after, or 0 if they're the same, ignoring case.
   */
  def compareName(other: Card) = Collator.getInstance(Locale.US).compare(unifiedName, other.unifiedName)

  /** @return the names of the card's faces joined together by [[Card.FaceSeparator]]. */
  def unifiedName = name.asScala.mkString(FaceSeparator)

  /** @return the type lines of the card's faces joined together by [[Card.FaceSeparator]]. */
  def unifiedTypeLine = typeLine.asScala.mkString(FaceSeparator)

  /**
   * Search the card's card types, ignoring case.
   * 
   * @param s card type to search for
   * @return true if the card has the given card type, and false otherwise.
   */
  def typeContains(s: String) = {
    if (s.matches(raw"\s"))
      throw IllegalArgumentException("types don't contain white space")
    types.asScala.exists(_.equalsIgnoreCase(s))
  }

  /**
   * Search the card's supertypes, ignoring case.
   * 
   * @param s supertype to search for
   * @return true if the card has the given supertype, and false otherwise.
   */
  def supertypeContains(s: String) = {
    if (s.matches(raw"\s"))
      throw IllegalArgumentException("supertypes don't contain white space")
    supertypes.asScala.exists(_.equalsIgnoreCase(s))
  }

  /** @return true if the card has a power value and it's variable (contains *), or false otherwise. */
  def powerVariable = power.asScala.exists(_.variable)

  /** @return true if the card has a toughness value and it's variable (contains *) or false otherwise. */
  def toughnessVariable = toughness.asScala.exists(_.variable)

  /** @return true if the card has a loyalty value and it's variable (contains * or X) or false otherwise. */
  def loyaltyVariable = loyalty.asScala.exists(_.variable)

  /**
   * Determine the card's legality in a particular format.
   * 
   * @param format format to check
   * @return the legality of the card in the given format.
   */
  def legalityIn(format: String) = legality.getOrDefault(format, Legality.ILLEGAL)

  override def equals(other: Any) = other match {
    case c: Card => scryfallid == c.scryfallid
    case _ => false
  }
  override def hashCode = Objects.hash(name, scryfallid)
  override def toString = unifiedName
}