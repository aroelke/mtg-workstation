package editor.filter.leaf

import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.filter.FaceSearchOptions
import editor.util.Comparison

/**
 * Filter that groups cards by the value of a numerical attribute, which could vary during the game.
 * 
 * @constructor create a new number filter
 * @param value function to get the value of the attribute from a card
 * @param variable function determining if the card's attribute value can vary
 * @param operation function to use to compare with the value of the numerical attribute
 * @param operand value to compare with the card's attribute
 * @param varies whether or not to filter by values that could vary during the game
 * 
 * @author Alec Roelke
 */
final case class NumberFilter(attribute: CardAttribute[?, NumberFilter], unified: Boolean, value: (Card) => Double, variable: Option[(Card) => Boolean] = None, faces: FaceSearchOptions = FaceSearchOptions.ANY, operation: Comparison = Comparison.EQ, operand: Double = 0.0, varies: Boolean = false) extends FilterLeaf {
  if (varies && !variable.isDefined)
    throw IllegalArgumentException(s"attribute ${attribute.toString} cannot vary")

  override protected def testFace(c: Card) = if (varies) (variable.get)(c) else { val v = value(c); !v.isNaN && operation(v, operand) }
}