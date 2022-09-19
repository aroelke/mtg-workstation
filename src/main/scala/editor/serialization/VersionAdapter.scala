package editor.serialization

import editor.database.version.DatabaseVersion
import org.json4s.CustomSerializer
import org.json4s.JString

/**
 * JSON serializer/deserializer for [[DatabaseVersion]]s using the format specified by [[DatabaseVersion.toString]].
 * @author Alec Roelke
 */
class VersionAdapter extends CustomSerializer[DatabaseVersion](formats => (
  { case JString(version) => DatabaseVersion.parseVersion(version) },
  { case version: DatabaseVersion => JString(version.toString) }
))