package editor.filter.leaf.options.single

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import editor.database.attributes.CardAttribute

/**
 * A filter that groups cards by the block they belong in.
 * @author Alec Roelke
 */
class BlockFilter extends SingletonOptionsFilter[String](CardAttribute.BLOCK, true, _.expansion.block) {
  override protected def copyLeaf = {
    val filter = CardAttribute.createFilter(CardAttribute.BLOCK).asInstanceOf[BlockFilter]
    filter.contain = contain
    filter.selected = selected
    filter
  }

  override protected def convertFromString(str: String) = str

  override protected def convertToJson(item: String) = JsonPrimitive(item)

  override protected def convertFromJson(item: JsonElement) = item.getAsString
}