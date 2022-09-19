package editor.serialization

import editor.collection.Categorization
import editor.database.card.Card
import editor.filter.Filter
import org.json4s._
import org.json4s.native._

import java.awt.Color

/**
 * JSON serializer/deserializer for [[Categorization]]s.
 * @author Alec Roelke
 */
class CategoryAdapter extends CustomSerializer[Categorization](implicit format => (
  { case v => Categorization(
    (v \ "name").extract[String],
    (v \ "filter").extract[Filter],
    (v \ "whitelist").extract[Set[Card]],
    (v \ "blacklist").extract[Set[Card]],
    (v \ "color").extract[Color]
  ) },
  { case Categorization(name, filter, whitelist, blacklist, color) => JObject(List(
    JField("name", JString(name)),
    JField("filter", Extraction.decompose(filter)),
    JField("whitelist", JArray(whitelist.toList.map(Extraction.decompose))),
    JField("blacklist", JArray(blacklist.toList.map(Extraction.decompose))),
    JField("color", Extraction.decompose(color))
  )) }
))