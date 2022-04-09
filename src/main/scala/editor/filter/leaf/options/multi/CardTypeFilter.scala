package editor.filter.leaf.options.multi

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import editor.database.attributes.CardAttribute

import scala.jdk.CollectionConverters._

/**
 * Object containing global information about card types.
 * @author Alec Roelke
 */
object CardTypeFilter {
  /** List of all card types. */
  var typeList = Seq[String]()
}

/**
 * Filter that groups cards based on card types.
 * @author Alec Roelke
 */
class CardTypeFilter extends MultiOptionsFilter[String](CardAttribute.CARD_TYPE, _.types) {
  override protected def copyLeaf = {
    val filter = CardAttribute.createFilter(CardAttribute.CARD_TYPE).asInstanceOf[CardTypeFilter]
    filter.contain = contain
    filter.selected = selected
    filter
  }

  override protected def convertFromString(str: String) = str

  override protected def convertToJson(item: String) = JsonPrimitive(item)

  override protected def convertFromJson(item: JsonElement) = item.getAsString
}
