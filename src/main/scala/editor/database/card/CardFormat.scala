package editor.database.card

import editor.collection.CardListEntry
import editor.collection.Categorization
import editor.collection.mutable.Deck
import editor.database.attributes.CardAttribute

import java.time.LocalDate
import scala.jdk.CollectionConverters._

/**
 * A string formatter for [[Card]].  Format strings are arbitrary text with card attribute names in
 * lower case surrounded by braces.
 * 
 * @constructor create a new formatter for a string
 * @param pattern string pattern to use for formatting a card
 * 
 * @author Alec Roelke
 */
class CardFormat(pattern: String) {
  /**
   * Generate a string based on the format's pattern from the values of the attributes of a deck entry.
   * 
   * @param card deck entry to format
   * @return the formatted string
   */
  def format(card: CardListEntry): String = {
    import CardAttribute._

    CardAttribute.displayableValues.foldLeft(pattern)((p, a) => p.replace(s"{$a}".toLowerCase, a match {
      case f @ (ManaCost | EffManaValue | TypeLine | Power | Toughness | Loyalty) => f(card).mkString(Card.FaceSeparator)
      case RealManaValue => RealManaValue(card) match {
        case v if v == v.intValue => v.intValue.toString
        case v if v != v.intValue => v.toString
      }
      case c @ (Colors | ColorIdentity) => c(card).mkString(",")
      case Categories => Categories(card).map(_.name).toSeq.sorted.mkString(",")
      case DateAdded => CardAttribute.DateAdded.format(DateAdded(card))
      case _ => a(card).toString
    }))
  }

  /**
   * Generate a string based on the format's pattern from the values of the attributes of a card. Deck-specific
   * attributes are replaced with their default values.
   * 
   * @param card card to format
   * @return the formatted string
   */
  def format(c: Card): String = format(new CardListEntry {
    override val card = c
    override val categories = Set.empty[Categorization]
    override val count = 1
    override def dateAdded = card.expansion.released
  })
}
