package editor.serialization

import editor.database.card.CardLayout
import org.json4s.CustomSerializer
import org.json4s.JString

/**
 * JSON serializer/deserializer for [[CardLayout]]s.
 * @author Alec Roelke
 */
object CardLayoutSerializer extends CustomSerializer[CardLayout](formats => (
{ case JString(layout) =>
    try {
      CardLayout.valueOf(layout.toUpperCase.replaceAll("[^A-Z]", "_"))
    } catch case _ => CardLayout.values.find(_.toString.equalsIgnoreCase(layout)).getOrElse(throw IllegalArgumentException(layout)) },
{ case layout: CardLayout => JString(layout.toString) }
))