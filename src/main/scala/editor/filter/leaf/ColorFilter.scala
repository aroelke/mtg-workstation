package editor.filter.leaf

import editor.database.attributes.CardAttribute
import editor.database.attributes.ManaType
import editor.database.card.Card
import editor.filter.FaceSearchOptions
import editor.util.Containment

/**
 * Filter that groups cards by a color attribute.
 * 
 * @constructor create a new filter for a color attribute
 * @param value function retrieving the value of the color attribute from a card
 * @param contain function to use to compare card colors
 * @param colors colors to test for
 * @param multicolored whether or not to only count multicolored cards
 * 
 * @author Alec Roelke
 */
final case class ColorFilter(attribute: CardAttribute[?, ColorFilter], value: (Card) => Set[ManaType], faces: FaceSearchOptions = FaceSearchOptions.ANY, contain: Containment = Containment.AnyOf, colors: Set[ManaType] = Set.empty, multicolored: Boolean = false) extends FilterLeaf {
  override val unified = false
  override protected def testFace(c: Card) = contain(value(c), colors) && (!multicolored || value(c).size > 1)
}