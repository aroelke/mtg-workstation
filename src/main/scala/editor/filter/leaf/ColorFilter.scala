package editor.filter.leaf

import editor.database.attributes.CardAttribute
import editor.database.attributes.ManaType
import editor.database.card.Card
import editor.util.Containment

import java.util.Objects

/**
 * Filter that groups cards by a color attribute.
 * 
 * @constructor create a new filter for a color attribute
 * @param attribute attribute to filter by
 * @param value function retrieving the value of the attribute from a card
 * 
 * @author Alec Roelke
 */
final class ColorFilter(override val attribute: CardAttribute[?, ColorFilter], value: (Card) => Set[ManaType]) extends FilterLeaf[ColorFilter](attribute, false) {
  /** Function to use to compare colors. */
  var contain = Containment.AnyOf
  /** Colors to compare cards with. */
  var colors = Set[ManaType]() // Using an immutable var here guarantees that copies don't reflect changes in each others' color sets
  /** Whether or not to only match multicolored cards. */
  var multicolored = false

  override protected def testFace(c: Card) = contain(value(c), colors) && (!multicolored || value(c).size > 1)

  override protected def copyLeaf = {
    val filter = attribute.filter
    filter.contain = contain
    filter.colors = colors
    filter.multicolored = multicolored
    filter
  }

  override def leafEquals(other: ColorFilter) = attribute == other.attribute && colors == other.colors && contain == other.contain && multicolored == other.multicolored

  override def hashCode = Objects.hash(attribute, colors, contain, multicolored)
}