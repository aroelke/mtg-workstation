package editor.filter

import editor.database.attributes.CardAttribute
import editor.database.card.Card

/**
 * A filter that groups cards by one or more attributes.
 * 
 * @constructor create a new filter
 * @param attribute attribute to filter by
 * 
 * @author Alec Roelke
 */
trait Filter(val attribute: CardAttribute) extends ((Card) => Boolean) with java.util.function.Predicate[Card] {
  /** @return a copy of this filter. */
  def copy: Filter

  @deprecated final override def test(c: Card) = apply(c)
}