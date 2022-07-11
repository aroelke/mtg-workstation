package editor.filter.leaf

import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.util.Comparison

import java.util.Objects
import scala.annotation.targetName

/**
 * Filter that groups cards by the value of a numerical attribute, which could vary during the game.
 * 
 * @constructor create a new number filter
 * @param t attribute to filter by
 * @param unified whether or not the value of the attribute is the same across all card faces
 * @param value function to get the value of the attribute from a card
 * @param variable if the attribute could vary during the game for some cards, whether or not to filter cards for which it does
 * 
 * @author Alec Roelke
 */
class NumberFilter(t: CardAttribute[?, ?], unified: Boolean, value: (Card) => Double, val variable: Option[(Card) => Boolean] = None) extends FilterLeaf(t, unified) {
  private var _varies = false

  /** Comparison to use for the desired value and card's value */
  var operation = Comparison.EQ
  /** Desired value of the attribute to filter by */
  var operand = 0.0

  def varies = variable.isDefined && _varies
  def varies_=(v: Boolean) = if (variable.isDefined) _varies = v else throw UnsupportedOperationException(s"attribute ${t.toString} does not vary")

  override protected def testFace(c: Card) = if (_varies) (variable.get)(c) else { val v = value(c); !v.isNaN && operation(v, operand) }

  override protected def copyLeaf = {
    val filter = attribute.filter.asInstanceOf[NumberFilter]
    filter.operation = operation
    filter.operand = operand
    filter._varies = _varies
    filter
  }

  override def leafEquals(other: Any) = other match {
    case o: NumberFilter => o.attribute == attribute && o.operation == operation && o.operand == operand && o._varies == _varies
    case _ => false
  }

  override def hashCode = Objects.hash(attribute, operation, operand, _varies)
}
