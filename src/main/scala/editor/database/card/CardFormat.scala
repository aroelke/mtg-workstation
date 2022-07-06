package editor.database.card

import editor.collection.CardListEntry
import editor.collection.Categorization
import editor.collection.mutable.Deck
import editor.database.attributes.CardAttribute
import editor.util.CollectionUtils

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
      case ManaCost | EffManaValue | TypeLine | Power | Toughness | Loyalty => card(a) match { case l: java.util.List[?] => l.asScala.mkString(Card.FaceSeparator) }
      case RealManaValue => card(a) match {
        case v: Double if v == v.intValue => v.intValue.toString
        case v: Double if v != v.intValue => v.toString
      }
      case Colors | ColorIdentity => card(a) match { case l: java.util.List[?] => l.asScala.mkString(",") }
      case Categories => card(a) match { case s: java.util.Set[?] => s.asScala.collect{ case c: Categorization => c }.map(_.name).toSeq.sorted.mkString(",") }
      case DateAdded => card(a) match { case d: LocalDate => Deck.DateFormatter.format(d) }
      case _ => card(a).toString
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
