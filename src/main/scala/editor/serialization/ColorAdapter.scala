package editor.serialization

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import org.json4s.CustomSerializer
import org.json4s.JString

import java.awt.Color
import java.lang.reflect.Type

/**
 * Serializer for [[Color]]s that converts them between ARGB hex strings and [[Color]] instances for use with JSON.
 * @constructor create a new [[Color]] serializer/deserializer
 * @author Alec Roelke
 */
class ColorAdapter extends CustomSerializer[Color](formats => (
  { case JString(hex) => Color(BigInt(hex, 16).toInt, true) },
  { case color: Color => JString(color.getRGB.toHexString) }
)) with JsonSerializer[Color] with JsonDeserializer[Color] {
  override def serialize(src: Color, typeOfSrc: Type, context: JsonSerializationContext) = JsonPrimitive(src.getRGB.toHexString)

  override def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext) = Color(BigInt(json.getAsString, 16).toInt, true)
}
