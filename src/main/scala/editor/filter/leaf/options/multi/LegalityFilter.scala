package editor.filter.leaf.options.multi

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import editor.database.attributes.CardAttribute
import editor.database.attributes.Legality
import editor.database.card.Card

import java.util.Objects
import scala.jdk.CollectionConverters._

/**
 * Filter that groups cards by format legality and, optionally, by whether or not they are restricted in those formats (if applicable).
 * @author Alec Roelke
 */
class LegalityFilter extends MultiOptionsFilter[String](CardAttribute.LEGAL_IN, true, _.legalIn) {
  /** Whether or not matching cards should be restricted in the formats they're legal in. */
  var restricted = false

  override protected def testFace(c: Card) = {
    if (!super.testFace(c))
      false
    else if (restricted)
      c.legalIn.filter(selected.contains).forall((f) => c.legality(f) == Legality.RESTRICTED)
    else
      true
  }

  override protected def copyLeaf = {
    val filter = CardAttribute.createFilter(CardAttribute.LEGAL_IN).asInstanceOf[LegalityFilter]
    filter.contain = contain
    filter.selected = selected
    filter.restricted = restricted
    filter
  }

  override protected def convertFromString(str: String) = str

  override protected def convertToJson(item: String) = JsonPrimitive(item)

  override protected def convertFromJson(item: JsonElement) = item.getAsString

  override protected def serializeLeaf(fields: JsonObject) = {
    super.serializeLeaf(fields)
    fields.addProperty("restricted", restricted)
  }

  override protected def deserializeLeaf(fields: JsonObject) = {
    super.deserializeLeaf(fields)
    restricted = fields.get("restricted").getAsBoolean
  }

  override def leafEquals(other: Any) = other match {
    case o: LegalityFilter => o.contain == contain && o.selected == selected && o.restricted == restricted
    case _ => false
  }

  override def hashCode = Objects.hash(contain, selected, restricted)
}
