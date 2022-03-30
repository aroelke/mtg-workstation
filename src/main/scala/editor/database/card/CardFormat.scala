package editor.database.card

import editor.collection.CardList
import editor.collection.deck.Category
import editor.collection.deck.Deck
import editor.database.attributes.CardAttribute
import editor.util.CollectionUtils

import scala.jdk.CollectionConverters._
import java.time.LocalDate

object CardFormat {
  private case class FakeEntry(override val card: Card) extends CardList.Entry {
    override val categories = Set.empty[Category].asJava
    override val count = 1
    override def dateAdded = card.expansion.released
  }
}

class CardFormat(pattern: String) {
  def format(card: CardList.Entry): String = {
    import CardAttribute._

    var p = pattern
    for (attribute <- CardAttribute.displayableValues) {
      val replacement = s"{$attribute}".toLowerCase
      p = p.replace(replacement, attribute match {
        case MANA_COST | POWER | TOUGHNESS | LOYALTY => card.get(attribute) match { case l: java.util.List[?] => l.asScala.mkString(Card.FaceSeparator) }
        case MANA_VALUE | MIN_VALUE | MAX_VALUE => card.get(attribute) match {
          case v: Double if v == v.intValue => v.intValue.toString
          case v: Double if v != v.intValue => v.toString
        }
        case COLORS | COLOR_IDENTITY => card.get(attribute) match { case l: java.util.List[?] => l.asScala.mkString(",") }
        case CATEGORIES => card.get(attribute) match { case s: java.util.Set[?] => s.asScala.collect{ case c: Category => c }.map(_.getName).toSeq.sorted.mkString(",") }
        case DATE_ADDED => card.get(attribute) match { case d: LocalDate => Deck.DATE_FORMATTER.format(d) }
        case _ => card.get(attribute).toString
      })
    }
    p
  }

  def format(card: Card): String = format(CardFormat.FakeEntry(card))
}
