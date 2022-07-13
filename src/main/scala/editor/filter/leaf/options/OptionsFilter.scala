package editor.filter.leaf.options

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

  @deprecated def copy(faces: FaceSearchOptions, contain: Containment, selected: Set[T]): F
}