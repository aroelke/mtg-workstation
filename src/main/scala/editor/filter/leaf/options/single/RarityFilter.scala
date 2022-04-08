package editor.filter.leaf.options.single

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import editor.database.attributes.CardAttribute
import editor.database.attributes.Rarity

/**
 * A filter that groups cards by rarity.
 * @author Alec Roelke
 */
class RarityFilter extends SingletonOptionsFilter[Rarity](CardAttribute.RARITY, _.rarity) {
  override protected def copyLeaf = {
    val filter = CardAttribute.createFilter(CardAttribute.RARITY).asInstanceOf[RarityFilter]
    filter.contain = contain
    filter.selected = java.util.HashSet(selected)
    filter
  }

  override protected def convertFromString(str: String) = Rarity.parseRarity(str)

  override protected def convertToJson(item: Rarity) = JsonPrimitive(item.toString)

  override protected def convertFromJson(item: JsonElement) = Rarity.parseRarity(item.getAsString)
}