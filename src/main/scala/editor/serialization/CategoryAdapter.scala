package editor.serialization

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import editor.collection.Categorization
import editor.database.card.Card
import editor.filter.Filter
import org.json4s._
import org.json4s.native._

import java.awt.Color
import java.lang.reflect.Type
import scala.jdk.CollectionConverters._

/**
 * JSON serializer/deserializer for [[Categorization]]s.
 * @author Alec Roelke
 */
class CategoryAdapter extends CustomSerializer[Categorization](implicit format => (
  { case v => Categorization(
    (v \ "name").extract[String],
    (v \ "filter").extract[Filter],
    (v \ "whitelist").extract[Set[Card]],
    (v \ "blacklist").extract[Set[Card]],
    (v \ "color").extract[Color]
  ) },
  { case Categorization(name, filter, whitelist, blacklist, color) => JObject(List(
    JField("name", JString(name)),
    JField("filter", Extraction.decompose(filter)),
    JField("whitelist", JArray(whitelist.toList.map(Extraction.decompose))),
    JField("blacklist", JArray(blacklist.toList.map(Extraction.decompose))),
    JField("color", Extraction.decompose(color))
  )) }
)) with JsonSerializer[Categorization] {
  override def serialize(src: Categorization, typeOfSrc: Type, context: JsonSerializationContext) = {
    val category = JsonObject()
    category.addProperty("name", src.name)
    category.add("filter", context.serialize(src.filter))
    category.add("whitelist", context.serialize(src.whitelist.asJava))
    category.add("blacklist", context.serialize(src.blacklist.asJava))
    category.add("color", context.serialize(src.color))
    category
  }
}
