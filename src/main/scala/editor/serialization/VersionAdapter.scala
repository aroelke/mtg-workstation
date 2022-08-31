package editor.serialization

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import editor.database.version.DatabaseVersion
import org.json4s.CustomSerializer
import org.json4s.JString

import java.lang.reflect.Type
import java.text.ParseException

/**
 * JSON serializer/deserializer for [[DatabaseVersion]]s using the format specified by [[DatabaseVersion.toString]].
 * @author Alec Roelke
 */
class VersionAdapter extends CustomSerializer[DatabaseVersion](formats => (
  { case JString(version) => DatabaseVersion.parseVersion(version) },
  { case version: DatabaseVersion => JString(version.toString) }
)) with JsonSerializer[DatabaseVersion] {
  override def serialize(src: DatabaseVersion, typeOfSrc: Type, context: JsonSerializationContext) = JsonPrimitive(src.toString)
}