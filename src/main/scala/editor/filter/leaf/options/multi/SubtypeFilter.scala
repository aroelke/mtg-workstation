package editor.filter.leaf.options.multi

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import editor.database.attributes.CardAttribute

import scala.jdk.CollectionConverters._

/**
 * Object containing global information about subtypes.
 */
object SubtypeFilter {
  /** List of all of the subtypes across all card types. */
  var subtypeList = Seq[String]()
}

/**
 * Filter for grouping cards by subtype.
 * @author Alec Roelke
 */
class SubtypeFilter extends MultiOptionsFilter[String](CardAttribute.SUBTYPE, _.subtypes) {
  override protected def copyLeaf = {
    val filter = CardAttribute.createFilter(CardAttribute.SUBTYPE).asInstanceOf[SubtypeFilter]
    filter.contain = contain
    filter.selected = java.util.HashSet(selected)
    filter
  }

  override protected def convertFromString(str: String) = str

  override protected def convertToJson(item: String) = JsonPrimitive(item)

  override protected def convertFromJson(item: JsonElement) = item.getAsString
}
