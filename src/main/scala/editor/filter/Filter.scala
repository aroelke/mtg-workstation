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
trait Filter extends ((Card) => Boolean) {
  def attribute: CardAttribute[?, ?]
}