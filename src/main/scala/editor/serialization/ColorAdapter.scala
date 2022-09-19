package editor.serialization

import org.json4s.CustomSerializer
import org.json4s.JString

import java.awt.Color

/**
 * Serializer for [[Color]]s that converts them between ARGB hex strings and [[Color]] instances for use with JSON.
 * @constructor create a new [[Color]] serializer/deserializer
 * @author Alec Roelke
 */
class ColorAdapter extends CustomSerializer[Color](formats => (
  { case JString(hex) => Color(BigInt(hex, 16).toInt, true) },
  { case color: Color => JString(color.getRGB.toHexString) }
))