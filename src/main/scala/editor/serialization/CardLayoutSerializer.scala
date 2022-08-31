package editor.serialization

import editor.database.card.CardLayout
import org.json4s.CustomSerializer
import org.json4s.JString

/**
 * JSON serializer/deserializer for [[CardLayout]]s.
 * @author Alec Roelke
 */
class CardLayoutSerializer extends CustomSerializer[CardLayout](formats => (
{ case JString(layout) => CardLayout.values.find(_.toString.equalsIgnoreCase(layout)).get },
{ case layout: CardLayout => JString(layout.toString) }
))