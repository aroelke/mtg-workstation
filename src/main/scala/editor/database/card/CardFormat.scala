package editor.database.card

import editor.collection.CardList
import editor.collection.deck.Category
import editor.collection.deck.Deck
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
  def format(card: CardList.Entry): String = {
    import CardAttribute._

    CardAttribute.displayableValues.foldLeft(pattern)((p, a) => p.replace(s"{$a}".toLowerCase, a match {
      case MANA_COST | TYPE_LINE | POWER | TOUGHNESS | LOYALTY => card.get(a) match { case l: java.util.List[?] => l.asScala.mkString(Card.FaceSeparator) }
      case MANA_VALUE | MIN_VALUE | MAX_VALUE => card.get(a) match {
        case v: Double if v == v.intValue => v.intValue.toString
        case v: Double if v != v.intValue => v.toString
      }
      case COLORS | COLOR_IDENTITY => card.get(a) match { case l: java.util.List[?] => l.asScala.mkString(",") }
      case CATEGORIES => card.get(a) match { case s: java.util.Set[?] => s.asScala.collect{ case c: Category => c }.map(_.getName).toSeq.sorted.mkString(",") }
      case DATE_ADDED => card.get(a) match { case d: LocalDate => Deck.DATE_FORMATTER.format(d) }
      case _ => card.get(a).toString
    }))
  }

  /**
   * Generate a string based on the format's pattern from the values of the attributes of a card. Deck-specific
   * attributes are replaced with their default values.
   * 
   * @param card card to format
   * @return the formatted string
   */
  def format(c: Card): String = format(new CardList.Entry {
    override val card = c
    override val categories = Set.empty[Category].asJava
    override val count = 1
    override def dateAdded = card.expansion.released
  })
}
