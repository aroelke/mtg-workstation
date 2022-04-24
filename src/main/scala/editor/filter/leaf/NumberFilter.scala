package editor.filter.leaf

import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.util.Comparison

import java.util.Objects
import scala.annotation.targetName

/**
 * Filter that groups cards by the value of a numerical attribute.
 * 
 * @constructor create a new number filter
 * @param t attribute to filter by
 * @param unified whether or not the value of the attribute is the same across all card faces
 * @param value function to get the value of the attribute from a card
 * 
 * @author Alec Roelke
 */
class NumberFilter(t: CardAttribute, unified: Boolean, value: (Card) => Double) extends FilterLeaf(t, unified) {
  var operation = Comparison.EQ
  var operand = 0.0

  override protected def testFace(c: Card) = { val v = value(c); !v.isNaN && operation(v, operand) }

  override protected def copyLeaf = {
    val filter = CardAttribute.createFilter(attribute).asInstanceOf[NumberFilter]
    filter.operation = operation
    filter.operand = operand
    filter
  }

  override def leafEquals(other: Any) = other match {
    case o: NumberFilter => o.attribute == attribute && o.operation == operation && o.operand == operand
    case _ => false
  }

  override def hashCode = Objects.hash(attribute, operation, operand)
}
