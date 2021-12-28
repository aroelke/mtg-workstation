package editor.serialization

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import editor.database.version.DatabaseVersion

import java.lang.reflect.Type
import java.text.ParseException

/**
 * JSON serializer/deserializer for [[DatabaseVersion]]s using the format specified by [[DatabaseVersion.toString]].
 * @author Alec Roelke
 */
class VersionAdapter extends JsonSerializer[DatabaseVersion] with JsonDeserializer[DatabaseVersion] {
  override def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext) = try {
    DatabaseVersion.parseVersion(json.getAsString)
  } catch case e: ParseException => throw JsonParseException(e)

  override def serialize(src: DatabaseVersion, typeOfSrc: Type, context: JsonSerializationContext) = JsonPrimitive(src.toString)
}
