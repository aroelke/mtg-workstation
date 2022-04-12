package editor.filter.leaf

import com.google.gson.JsonObject
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

  override protected def copyLeaf = CardAttribute.createFilter(`type`).asInstanceOf[BinaryFilter]

  override def serializeLeaf(fields: JsonObject) = fields.addProperty("all", all)

  override def deserializeLeaf(fields: JsonObject) = require(fields.get("all").getAsBoolean == all)

  override def leafEquals(other: Any) = other match {
    case o: BinaryFilter => o.`type` == `type`
    case _ => false
  }

  override def hashCode = Objects.hash(`type`)
}
