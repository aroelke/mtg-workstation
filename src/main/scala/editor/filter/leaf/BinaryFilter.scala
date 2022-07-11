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
final class BinaryFilter(private val all: Boolean) extends FilterLeaf(if (all) CardAttribute.AnyCard else CardAttribute.NoCard, true) {
  override protected def testFace(c: Card) = all

  override protected def copyLeaf = BinaryFilter(all)

  override def leafEquals(other: Any) = other match {
    case o: BinaryFilter => all == o.all
    case _ => false
  }

  override def hashCode = Objects.hash(all)
}