package editor.filter.leaf.options.multi

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import editor.database.attributes.CardAttribute

import scala.jdk.CollectionConverters._

/**
 * Object containing global information about supertypes.
 * @author Alec Roelke
 */
object SupertypeFilter {
  /** List of all supertypes. */
  var supertypeList = Seq[String]()
}

/**
 * Filter that groups cards by supertype.
 * @author Alec Roelke
 */
class SupertypeFilter extends MultiOptionsFilter[String](CardAttribute.SUPERTYPE, false, _.supertypes) {
  override protected def copyLeaf = {
    val filter = CardAttribute.createFilter(CardAttribute.SUPERTYPE).asInstanceOf[SupertypeFilter]
    filter.contain = contain
    filter.selected = selected
    filter
  }

  override protected def convertFromString(str: String) = str

  override protected def convertToJson(item: String) = JsonPrimitive(item)

  override protected def convertFromJson(item: JsonElement) = item.getAsString
}
