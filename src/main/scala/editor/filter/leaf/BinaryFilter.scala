package editor.filter.leaf

import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.filter.FaceSearchOptions

/**
 * A filter that either statically passes every card or no card.
 * 
 * @constructor create a new static filter
 * @param all whether or not the filter should pass cards
 * 
 * @author Alec Roelke
 */
final case class BinaryFilter(all: Boolean) extends FilterLeaf {
  override def faces = FaceSearchOptions.ANY
  override def attribute = if (all) CardAttribute.AnyCard else CardAttribute.NoCard
  override val unified = true
  override protected def testFace(c: Card) = all
}