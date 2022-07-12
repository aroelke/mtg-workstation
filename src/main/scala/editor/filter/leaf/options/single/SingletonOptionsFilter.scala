package editor.filter.leaf.options.single

import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.filter.leaf.options.OptionsFilter

import scala.reflect.ClassTag

/**
 * A type of filter that groups cards by an attribute that has only one of a set of possible values.
 * @tparam T type of data that is being filtered
 * @author Alec Roelke
 */
trait SingletonOptionsFilter[T, F <: SingletonOptionsFilter[T, ?] : ClassTag] extends OptionsFilter[T, F] {
  /** Function to use to get the value of the attribute from the card */
  def value: (Card) => T
  override protected def testFace(c: Card) = contain(selected, Seq(value(c)))
}