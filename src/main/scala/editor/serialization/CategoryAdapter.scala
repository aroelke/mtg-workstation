package editor.serialization

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import editor.collection.deck.Category
import editor.database.card.Card
import editor.filter.Filter

import java.awt.Color
import java.lang.reflect.Type
import scala.jdk.CollectionConverters._

/**
 * JSON serializer/deserializer for [[Category]]s.
 * @author Alec Roelke
 */
class CategoryAdapter extends JsonSerializer[Category] with JsonDeserializer[Category] {
  override def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext) = {
    val obj = json.getAsJsonObject
    val category = Category()
    category.setName(obj.get("name").getAsString)
    category.setFilter(context.deserialize(obj.get("filter"), classOf[Filter]))
    obj.get("whitelist").getAsJsonArray.asScala.foreach((e) => category.include(context.deserialize(e, classOf[Card])))
    obj.get("blacklist").getAsJsonArray.asScala.foreach((e) => category.exclude(context.deserialize(e, classOf[Card])))
    category.setColor(context.deserialize(obj.get("color"), classOf[Color]))
    category
  }

  override def serialize(src: Category, typeOfSrc: Type, context: JsonSerializationContext) = {
    val category = JsonObject()
    category.addProperty("name", src.getName)
    category.add("filter", context.serialize(src.getFilter))
    category.add("whitelist", context.serialize(src.getWhitelist))
    category.add("blacklist", context.serialize(src.getBlacklist))
    category.add("color", context.serialize(src.getColor))
    category
  }
}
