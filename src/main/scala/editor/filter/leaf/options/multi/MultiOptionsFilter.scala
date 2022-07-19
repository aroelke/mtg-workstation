package editor.filter.leaf.options.multi

import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.filter.leaf.options.OptionsFilter

import scala.reflect.ClassTag
import editor.filter.FaceSearchOptions
import editor.util.Containment

/**
 * A type of filter that groups cards by an attribute that can have zero or more of a set of distinct values.
 *
 * @param value function to use to get the values of the attribute from a card
 * @tparam T type of the data that is being filtered
 * @tparam F type of filter this is
 *
 * @author Alec Roelke
 */
final case class MultiOptionsFilter[T](attribute: CardAttribute[Set[T], MultiOptionsFilter[T]], values: (Card) => Set[T], contain: Containment = Containment.AnyOf, selected: Set[T] = Set.empty[T]) extends OptionsFilter[T, MultiOptionsFilter[T]] {
  override def faces = FaceSearchOptions.ANY
  override val unified = true
  override protected def testFace(c: Card) = contain(values(c), selected)
  override def copy(faces: FaceSearchOptions, contain: Containment, selected: Set[T]) = copy(contain = contain, selected = selected)
}