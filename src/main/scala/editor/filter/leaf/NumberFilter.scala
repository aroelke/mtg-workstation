package editor.filter.leaf

import com.google.gson.JsonObject
import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.util.Comparison

import java.util.Objects
import scala.annotation.targetName

/**
 * Object containing convenience constructors for [[NumberFilter]].
 */
object NumberFilter {
  /**
   * Create a new number filter that can have different values for its attribute on different card faces.
   * 
   * @param t attribute to filter by
   * @param value function to get the value of the attribute on each face
   */
  def apply(t: CardAttribute, value: (Card) => Iterable[Double]): NumberFilter = new NumberFilter(t, value)

  /**
   * Create a new number filter that has the same value for its attribute on each card face.
   * 
   * @param t attribute to filter by
   * @param value function to get the value of the attribute
   */
  @targetName("singleton_apply")
  def apply(t: CardAttribute, value: (Card) => Double): NumberFilter = apply(t, (c) => Seq(value(c)))
}

/**
 * Filter that groups cards by the value of a numerical attribute.
 * 
 * @constructor create a new number filter
 * @param t attribute to filter by
 * @param value function to get the value of the attribute from a card
 * 
 * @author Alec Roelke
 */
class NumberFilter(t: CardAttribute, value: (Card) => Iterable[Double]) extends FilterLeaf(t) {
  var operation = Comparison.EQ
  var operand = 0.0

  override protected def testFace(c: Card) = value(c).exists((v) => !v.isNaN && operation.test(v, operand))

  override protected def copyLeaf = {
    val filter = CardAttribute.createFilter(`type`).asInstanceOf[NumberFilter]
    filter.operation = operation
    filter.operand = operand
    filter
  }

  override protected def serializeLeaf(fields: JsonObject) = {
    fields.addProperty("operation", operation.toString)
    fields.addProperty("operand", operand)
  }

  override protected def deserializeLeaf(fields: JsonObject) = {
    operation = Comparison.valueOf(fields.get("operation").getAsString.apply(0))
    operand = fields.get("operand").getAsDouble
  }

  override def leafEquals(other: Any) = other match {
    case o: NumberFilter => o.`type` == `type` && o.operation == operation && o.operand == operand
    case _ => false
  }

  override def hashCode = Objects.hash(`type`, operation, operand)
}
