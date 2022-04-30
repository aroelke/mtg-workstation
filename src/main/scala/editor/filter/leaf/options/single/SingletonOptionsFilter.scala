package editor.filter.leaf.options.single

import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.filter.leaf.options.OptionsFilter

/**
 * A type of filter that groups cards by an attribute that has only one of a set of possible values.
 * 
 * @constructor create a new singleton options filter
 * @param t attribute to filter by
 * @param value function for getting the value of the attribute from a card
 * @param unified whether or not the value of an attribute is the same across all card faces
 * @tparam T type of data that is being filtered
 * 
 * @author Alec Roelke
 */
abstract class SingletonOptionsFilter[T](t: CardAttribute, unified: Boolean, value: (Card) => T) extends OptionsFilter[T](t, unified) {
  override protected def testFace(c: Card) = contain(selected, Seq(value(c)))
}