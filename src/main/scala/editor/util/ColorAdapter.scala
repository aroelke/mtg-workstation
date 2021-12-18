package editor.util

import com.google.gson.JsonSerializer
import java.awt.Color
import com.google.gson.JsonDeserializer
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonPrimitive
import java.lang.reflect.Type
import com.google.gson.JsonElement
import com.google.gson.JsonDeserializationContext

class ColorAdapter extends JsonSerializer[Color] with JsonDeserializer[Color] {
  override def serialize(src: Color, typeOfSrc: Type, context: JsonSerializationContext) = JsonPrimitive(src.getRGB.toHexString)

  override def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext) = Color(BigInt(json.getAsString, 16).toInt, true)
}
