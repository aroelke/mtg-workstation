package editor.filter.leaf.options.multi

import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.filter.leaf.options.OptionsFilter

/**
 * A type of filter that groups cards by an attribute that can have zero or more of a set of distinct vales.
 * 
 * @constructor create a new multi-item options filter
 * @param t attribute to be filtered by
 * @param values function to use to get the value of the attribute from a card
 * @param unified whether or not the value of an attribute is the same across all card faces
 * @tparam T type of the data that is being filtered
 */
abstract class MultiOptionsFilter[T](t: CardAttribute[?], unified: Boolean, values: (Card) => Set[T]) extends OptionsFilter[T](t, unified) {
  override protected def testFace(c: Card) = contain(values(c), selected)
}