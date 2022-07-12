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
 * @constructor create a new options filter
 * @param attribute attribute to filter by
 * @param unified whether or not the value of an attribute is the same across all card faces
 * @tparam T type of data used for filtering
 * 
 * @author Alec Roelke
 */
trait OptionsFilter[T, F <: OptionsFilter[T, ?] : ClassTag] extends FilterLeaf[F] {
  override def attribute: CardAttribute[?, F]
  /** Function to use to compare card attributes. */
  def contain: Containment
  /** Set of items to look for in cards. */
  def selected: Set[T] // Using an immutable var guarantees that changing this in a copy doesn't change this filter's version

  @deprecated def copy(faces: FaceSearchOptions, contain: Containment, selected: Set[T]): F
}