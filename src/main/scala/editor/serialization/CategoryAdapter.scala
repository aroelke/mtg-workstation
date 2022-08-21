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
import org.json4s.CustomSerializer
import org.json4s.Extraction
import org.json4s.JArray
import org.json4s.JField
import org.json4s.JObject
import org.json4s.JString

import java.awt.Color
import java.lang.reflect.Type
import scala.jdk.CollectionConverters._

/**
 * JSON serializer/deserializer for [[Categorization]]s.
 * @author Alec Roelke
 */
class CategoryAdapter extends CustomSerializer[Categorization](implicit format => (
  { case JObject(List(
    JField("name", JString(name)),
    JField("filter", filter: JObject),
    JField("whitelist", JArray(whitelist)),
    JField("blacklist", JArray(blacklist)),
    JField("color", color: JObject)
  )) => Categorization(
    name,
    Extraction.extract[Filter](filter),
    whitelist.map(Extraction.extract[Card]).toSet,
    blacklist.map(Extraction.extract[Card]).toSet,
    Extraction.extract[Color](color)
  ) },
  { case Categorization(name, filter, whitelist, blacklist, color) => JObject(List(
    JField("name", JString(name)),
    JField("filter", Extraction.decompose(filter)),
    JField("whitelist", JArray(whitelist.toList.map(Extraction.decompose))),
    JField("blacklist", JArray(blacklist.toList.map(Extraction.decompose))),
    JField("color", Extraction.decompose(color))
  )) }
)) with JsonSerializer[Categorization] with JsonDeserializer[Categorization] {
  override def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext) = {
    val obj = json.getAsJsonObject
    Categorization(
      obj.get("name").getAsString,
      context.deserialize(obj.get("filter"), classOf[Filter]),
      obj.get("whitelist").getAsJsonArray.asScala.map((e) => context.deserialize[Card](e, classOf[Card])).toSet,
      obj.get("blacklist").getAsJsonArray.asScala.map((e) => context.deserialize[Card](e, classOf[Card])).toSet,
      context.deserialize(obj.get("color"), classOf[Color])
    )
  }

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
