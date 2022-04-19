package editor.filter.leaf

import editor.database.attributes.CardAttribute
import editor.database.attributes.ManaType
import editor.database.card.Card
import editor.util.Containment

import java.util.Objects
import scala.jdk.CollectionConverters._

/**
 * Filter that groups cards by a color attribute.
 * 
 * @constructor create a new filter for a color attribute
 * @param t attribute to filter by
 * @param value function retrieving the value of the attribute from a card
 * 
 * @author Alec Roelke
 */
class ColorFilter(t: CardAttribute, value: (Card) => Seq[ManaType]) extends FilterLeaf(t, false) {
  /** Function to use to compare colors. */
  var contain = Containment.CONTAINS_ANY_OF
  /** Colors to compare cards with. */
  var colors = Set[ManaType]() // Using an immutable var here guarantees that copies don't reflect changes in each others' color sets
  /** Whether or not to only match multicolored cards. */
  var multicolored = false

  override protected def testFace(c: Card) = contain.test(value(c).asJava, colors.asJava) && (!multicolored || value(c).size > 1)

  override protected def copyLeaf = {
    val filter = CardAttribute.createFilter(attribute).asInstanceOf[ColorFilter]
    filter.contain = contain
    filter.colors = colors
    filter.multicolored = multicolored
    filter
  }

  override def leafEquals(other: Any) = other match {
    case o: ColorFilter if o.attribute == attribute => o.colors == colors && o.contain == contain && o.multicolored == multicolored
    case _ => false
  }

  override def hashCode = Objects.hash(attribute, colors, contain, multicolored)
}