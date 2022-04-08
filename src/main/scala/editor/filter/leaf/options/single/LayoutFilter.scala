package editor.filter.leaf.options.single

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.database.card.CardLayout

/**
 * A filter that groups cards based on layout.
 * @author Alec Roelke
 */
class LayoutFilter extends SingletonOptionsFilter[CardLayout](CardAttribute.LAYOUT, _.layout) {
  override protected def copyLeaf = {
    val filter = CardAttribute.createFilter(CardAttribute.LAYOUT).asInstanceOf[LayoutFilter]
    filter.contain = contain
    filter.selected = java.util.HashSet(selected)
    filter
  }

  override protected def convertFromString(str: String) = CardLayout.valueOf(str.replace(' ', '_').toUpperCase)

  override protected def convertToJson(item: CardLayout) = JsonPrimitive(item.toString)

  override protected def convertFromJson(item: JsonElement) = CardLayout.valueOf(item.getAsString.replace(' ', '_').toUpperCase)
}
