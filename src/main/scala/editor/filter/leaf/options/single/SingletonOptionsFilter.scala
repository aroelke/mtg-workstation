package editor.filter.leaf.options.single

import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.filter.leaf.options.OptionsFilter

import scala.reflect.ClassTag

/**
 * A type of filter that groups cards by an attribute that has only one of a set of possible values.
 * 
 * @constructor create a new singleton options filter
 * @param attribute attribute to filter by
 * @param value function for getting the value of the attribute from a card
 * @param unified whether or not the value of an attribute is the same across all card faces
 * @tparam T type of data that is being filtered
 * 
 * @author Alec Roelke
 */
abstract class SingletonOptionsFilter[T, F <: SingletonOptionsFilter[T, ?] : ClassTag](override val attribute: CardAttribute[?, F], unified: Boolean, value: (Card) => T) extends OptionsFilter[T, F](attribute, unified) {
  override protected def testFace(c: Card) = contain(selected, Seq(value(c)))
}