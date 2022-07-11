package editor.filter.leaf.options

import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.filter.leaf.FilterLeaf
import editor.util.Containment

import java.util.Objects
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
abstract class OptionsFilter[T, F <: OptionsFilter[T, ?] : ClassTag](override val attribute: CardAttribute[?, F], unified: Boolean) extends FilterLeaf[F](attribute, unified) {
  /** Function to use to compare card attributes. */
  var contain = Containment.AnyOf
  /** Set of items to look for in cards. */
  var selected = Set[T]() // Using an immutable var guarantees that changing this in a copy doesn't change this filter's version

  override protected def copyLeaf = {
    val filter = attribute.filter
    filter.contain = contain
    filter.selected = selected
    filter
  }

  override def leafEquals(other: F) = attribute == other.attribute && contain == other.contain && selected == other.selected

  override def hashCode = Objects.hash(attribute, contain, selected)
}