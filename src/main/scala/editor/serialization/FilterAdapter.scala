package editor.serialization

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import editor.database.attributes.CardAttribute
import editor.filter.Filter
import editor.filter.FilterGroup

import java.lang.reflect.Type

/**
 * JSON serializer/deserializer for [[Filter]]s using their methods for converting to/from JSON objects.
 * @author Alec Roelke
 */
class FilterAdapter extends JsonSerializer[Filter] with JsonDeserializer[Filter] {
  override def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext) = {
    val attribute = context.deserialize[CardAttribute](json.getAsJsonObject.get("type"), classOf[CardAttribute])
    val filter = if (attribute == CardAttribute.GROUP) FilterGroup() else CardAttribute.createFilter(attribute)
    filter.fromJsonObject(json.getAsJsonObject)
    filter
  }

  override def serialize(src: Filter, typeOfSrc: Type, context: JsonSerializationContext) = src.toJsonObject
}