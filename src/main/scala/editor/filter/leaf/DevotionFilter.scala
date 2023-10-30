package editor.filter.leaf

import editor.filter.FaceSearchOptions
import editor.database.attributes.ManaType
import editor.util.Comparison
import editor.database.attributes.CardAttribute
import editor.database.card.Card

/**
 * A filter that groups cards by their contribution to devotion to a set of mana types. Devotion to a set of
 * mana types is defined as the number of mana symbols among mana costs in permanents a player controls that
 * are of at least one of those types. This filter does not filter out nonpermanent cards, as their devotion
 * contribution could still be useful for searching or category creation.
 * 
 * @constructor create a new devotion contribution filter
 * @param types set of mana types to compute devotion contribution for
 * @param operation function to use to compare the devotion to the operand
 * @param operand value to compare the devotion to
 * 
 * @author Alec Roelke
 */
final case class DevotionFilter(faces: FaceSearchOptions = FaceSearchOptions.ANY, types: Set[ManaType] = Set.empty, operation: Comparison = Comparison.EQ, operand: Int = 0) extends FilterLeaf {
  override def unified = false
  override def attribute = CardAttribute.Devotion
  override protected def testFace(c: Card) = operation(c.manaCost.devotionTo(types), operand)
}