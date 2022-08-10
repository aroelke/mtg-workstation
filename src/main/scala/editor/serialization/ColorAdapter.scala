package editor.serialization

import com.google.gson.JsonSerializer
import java.awt.Color
import com.google.gson.JsonDeserializer
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonPrimitive
import java.lang.reflect.Type
import com.google.gson.JsonElement
import com.google.gson.JsonDeserializationContext

/**
 * Serializer for [[Color]]s that converts them between ARGB hex strings and [[Color]] instances for use with JSON.
 * @constructor create a new [[Color]] serializer/deserializer
 * @author Alec Roelke
 */
class ColorAdapter extends JsonSerializer[Color] with JsonDeserializer[Color] {
  override def serialize(src: Color, typeOfSrc: Type, context: JsonSerializationContext) = JsonPrimitive(src.getRGB.toHexString)

  override def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext) = Color(BigInt(json.getAsString, 16).toInt, true)
}
