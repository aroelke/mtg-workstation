package editor.serialization

import editor.database.version.UpdateFrequency
import org.json4s.CustomSerializer
import org.json4s.JString

/**
 * JSON serialize/deserializer for [[UpdateFrequency]]s using its name.
 * @author Alec Roelke
 */
object UpdateSerializer extends CustomSerializer[UpdateFrequency](formats => (
  { case JString(freq) => UpdateFrequency.values.find(_.name.equalsIgnoreCase(freq)).getOrElse(UpdateFrequency.Never) },
  { case freq: UpdateFrequency => JString(freq.name.toLowerCase) }
))