package editor.serialization

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import editor.database.attributes.CardAttribute
import org.json4s.CustomSerializer
import org.json4s.JString

import java.lang.reflect.Type

/**
 * JSON serializer/deserializer for [[CardAdapter]], which uses its [[CardAdapter.toString]] method for conversion.
 * @author Alec Roelke
 */
class AttributeAdapter extends CustomSerializer[CardAttribute[?, ?]](format => (
  { case JString(attribute) => CardAttribute.parse(attribute).getOrElse(throw IllegalArgumentException(attribute)) },
  { case attribute: CardAttribute[?, ?] => JString(attribute.toString) }
)) with JsonSerializer[CardAttribute[?, ?]] with JsonDeserializer[CardAttribute[?, ?]] {
  override def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext) = CardAttribute.parse(json.getAsString).getOrElse(throw JsonParseException(json.getAsString))

  override def serialize(src: CardAttribute[?, ?], typeOfSrc: Type, context: JsonSerializationContext) = JsonPrimitive(src.toString)
}