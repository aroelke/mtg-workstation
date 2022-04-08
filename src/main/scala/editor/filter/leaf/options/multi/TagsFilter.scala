package editor.filter.leaf.options.multi

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import editor.database.attributes.CardAttribute
import editor.database.card.Card

import scala.jdk.CollectionConverters._

/**
 * Filter that groups cards by user-defined tags.
 * @author Alec Roelke
 */
class TagsFilter extends MultiOptionsFilter[String](CardAttribute.TAGS, (c) => (if (Card.tags.contains(c)) Card.tags(c) else Set.empty[String]).asJava) {
  override protected def copyLeaf = {
    val filter = CardAttribute.createFilter(CardAttribute.TAGS).asInstanceOf[TagsFilter]
    filter.contain = contain
    filter.selected = java.util.HashSet(selected)
    filter
  }

  override protected def convertFromString(str: String) = str

  override protected def convertToJson(item: String) = JsonPrimitive(item)

  override protected def convertFromJson(item: JsonElement) = item.getAsString
}
