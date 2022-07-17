package editor.filter.leaf.options.single

import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.filter.leaf.options.OptionsFilter

import scala.reflect.ClassTag
import editor.util.Containment
import editor.filter.FaceSearchOptions

/**
 * A type of filter that groups cards by an attribute that has only one of a set of possible values.
 *
 * @param value function for getting the value of the attribute from a card
 * @tparam T type of data that is being filtered
 * @tparam F type of filter this is
 *
 * @author Alec Roelke
 */
final case class SingletonOptionsFilter[T](attribute: CardAttribute[T, SingletonOptionsFilter[T]], value: (Card) => T, contain: Containment = Containment.AnyOf, selected: Set[T] = Set.empty[T]) extends OptionsFilter[T, SingletonOptionsFilter[T]] {
  override def faces = FaceSearchOptions.ANY
  override val unified = true
  override protected def testFace(c: Card) = contain(selected, Seq(value(c)))
  override def copy(faces: FaceSearchOptions, contain: Containment, selected: Set[T]) = copy(contain = contain, selected = selected)
}