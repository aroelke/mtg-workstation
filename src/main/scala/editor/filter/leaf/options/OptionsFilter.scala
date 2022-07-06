package editor.filter.leaf.options

import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.filter.leaf.FilterLeaf
import editor.util.Containment

import java.util.Objects

/**
 * A type of filter that groups cards by attributes that have values taken from a set of discreet possibiliies.
 * 
 * @constructor create a new options filter
 * @param t attribute to filter by
 * @param unified whether or not the value of an attribute is the same across all card faces
 * @tparam T type of data used for filtering
 * 
 * @author Alec Roelke
 */
abstract class OptionsFilter[T](t: CardAttribute[?], unified: Boolean) extends FilterLeaf(t, unified) {
  /** Function to use to compare card attributes. */
  var contain = Containment.AnyOf
  /** Set of items to look for in cards. */
  var selected = Set[T]() // Using an immutable var guarantees that changing this in a copy doesn't change this filter's version

  override protected def copyLeaf = {
    val filter = attribute.filter.get.asInstanceOf[OptionsFilter[T]]
    filter.contain = contain
    filter.selected = selected
    filter
  }

  override def leafEquals(other: Any) = other match {
    case o: OptionsFilter[?] if o.getClass == getClass => o.attribute == attribute && o.contain == contain && o.selected == selected
    case _ => false
  }

  override def hashCode = Objects.hash(attribute, contain, selected)
}