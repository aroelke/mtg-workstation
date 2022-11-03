package editor.filter.leaf

import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.filter.FaceSearchOptions
import editor.filter.leaf.FilterLeaf
import editor.util.Containment

import scala.reflect.ClassTag

/**
 * A type of filter that groups cards by attributes that have values taken from a set of discreet possibiliies.
 *
 * @tparam T type of data used for filtering
 * @tparam F type of filter this is
 * 
 * @author Alec Roelke
 */
trait OptionsFilter[T, F <: OptionsFilter[T, F] : ClassTag] extends FilterLeaf {
  override def attribute: CardAttribute[?, F]
  /** Function to use to compare card attributes. */
  def contain: Containment
  /** Set of items to look for in cards. */
  def selected: Set[T] // Using an immutable var guarantees that changing this in a copy doesn't change this filter's version
}

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
}

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
}