package editor.filter.leaf.options.single

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import editor.database.attributes.CardAttribute
import editor.database.attributes.Expansion

/**
 * Filter that groups cards by expansion.
 * @author Alec Roelke
 */
class ExpansionFilter extends SingletonOptionsFilter[Expansion](CardAttribute.EXPANSION, _.expansion) {
  override protected def copyLeaf = {
    val filter = CardAttribute.createFilter(CardAttribute.EXPANSION).asInstanceOf[ExpansionFilter]
    filter.contain = contain
    filter.selected = java.util.HashSet(selected)
    filter
  }

  override protected def convertFromString(str: String) = Expansion.expansions.find(_.name.equalsIgnoreCase(str)).getOrElse(throw IllegalArgumentException(s"unknown expansion \"$str\""))

  override protected def convertToJson(item: Expansion) = JsonPrimitive(item.name)
  override protected def convertFromJson(item: JsonElement) = Expansion.expansions.find(_.name.equalsIgnoreCase(item.getAsString)).getOrElse(throw IllegalArgumentException(s"unknown expansion \"${item.getAsString}\""))
}
