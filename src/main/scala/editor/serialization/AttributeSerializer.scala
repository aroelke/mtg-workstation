package editor.serialization

import editor.database.attributes.CardAttribute
import org.json4s.CustomSerializer
import org.json4s.JString

/**
 * JSON serializer/deserializer for [[CardAdapter]], which uses its [[CardAdapter.toString]] method for conversion.
 * @author Alec Roelke
 */
object AttributeSerializer extends CustomSerializer[CardAttribute[?, ?]](format => (
  { case JString(attribute) => CardAttribute.parse(attribute).getOrElse(throw IllegalArgumentException(attribute)) },
  { case attribute: CardAttribute[?, ?] => JString(attribute.toString) }
))