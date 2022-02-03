package editor.serialization

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import editor.database.version.UpdateFrequency

import java.lang.reflect.Type

/**
 * JSON serialize/deserializer for [[UpdateFrequency]]s using its name.
 * @author Alec Roelke
 */
class UpdateAdapter extends JsonSerializer[UpdateFrequency] with JsonDeserializer[UpdateFrequency] {
  import editor.database.version.UpdateFrequency._

  override def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext) = UpdateFrequency.values.find(_.name.equalsIgnoreCase(json.getAsString)).getOrElse(Never)

  override def serialize(src: UpdateFrequency, typeOfSrc: Type, context: JsonSerializationContext) = JsonPrimitive(src.name.toLowerCase)
}