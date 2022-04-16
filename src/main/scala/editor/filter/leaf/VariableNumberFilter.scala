package editor.filter.leaf

import com.google.gson.JsonObject
import editor.database.attributes.CardAttribute
import editor.database.card.Card

import java.util.Objects
import scala.jdk.CollectionConverters._

/**
 * Filter that groups cards by a numerical attribute that can vary during the course of a game (usually denoted by a *).
 * 
 * @constructor create a new variable filter
 * @param t attribute to filter by
 * @param value function to use to get the value of the attribute from a card
 * @param variable function to use to determine if the value should is variable
 * 
 * @author Alec Roelke
 */
class VariableNumberFilter(t: CardAttribute, value: (Card) => Double, variable: (Card) => Boolean) extends NumberFilter(t, false, value) {
  /** Whether or not the value of the attribute should be variable during a game. */
  var varies = false

  override protected def testFace(c: Card) = if (varies) variable(c) else super.testFace(c)

  override protected def copyLeaf = {
    val filter = super.copyLeaf.asInstanceOf[VariableNumberFilter]
    filter.varies = varies
    filter
  }

  override protected def serializeLeaf(fields: JsonObject) = {
    super.serializeLeaf(fields)
    fields.addProperty("varies", varies)
  }

  override protected def deserializeLeaf(fields: JsonObject) = {
    super.deserializeLeaf(fields)
    varies = fields.get("varies").getAsBoolean
  }

  override def leafEquals(other: Any) = other match {
    case o: VariableNumberFilter => super.leafEquals(o) && o.varies == varies
    case _ => false
  }

  override def hashCode = Objects.hash(attribute, varies, operation, operand)
}
