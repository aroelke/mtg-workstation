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
class TagsFilter extends MultiOptionsFilter[String](CardAttribute.TAGS, (c) => Card.tags.get(c).map(_.toSet).getOrElse(Set.empty)) {
  override protected def copyLeaf = {
    val filter = CardAttribute.createFilter(CardAttribute.TAGS).asInstanceOf[TagsFilter]
    filter.contain = contain
    filter.selected = selected
    filter
  }

  override protected def convertFromString(str: String) = str

  override protected def convertToJson(item: String) = JsonPrimitive(item)

  override protected def convertFromJson(item: JsonElement) = item.getAsString
}
