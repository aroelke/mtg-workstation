package editor.filter.leaf

import editor.database.attributes.CardAttribute
import editor.database.card.Card

import java.util.Objects

/**
 * A filter that either statically passes every card or no card.
 * 
 * @constructor create a new static filter
 * @param all whether or not the filter should pass cards
 * 
 * @author Alec Roelke
 */
class BinaryFilter(all: Boolean) extends FilterLeaf(if (all) CardAttribute.ANY else CardAttribute.NONE, true) {
  override protected def testFace(c: Card) = all

  override protected def copyLeaf = CardAttribute.createFilter(attribute).asInstanceOf[BinaryFilter]

  override def leafEquals(other: Any) = other match {
    case o: BinaryFilter => o.attribute == attribute
    case _ => false
  }

  override def hashCode = Objects.hash(attribute)
}
