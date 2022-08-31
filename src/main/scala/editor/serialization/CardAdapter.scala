package editor.serialization

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import editor.collection.immutable.Inventory
import editor.database.card.Card
import editor.gui.MainFrame
import org.json4s._
import org.json4s.native._

import java.lang.reflect.Type

/**
 * JSON serializer/deserializer for [[Card]]s. Primarily its [[Card.scryfallid]] is used, but if that is not present
 *  in the JSON to deserialize (as in older versions of this program), then its [[Card.multiverseid]] is used instead.
 * The name and expansion are also stored when serializing for future implementation of inventory searches by
 * identifying characteristics other than scryfallid.
 * 
 * @author Alec Roelke
 */
class CardAdapter extends CustomSerializer[Card](format => (
  {
    case v if v \ "scryfallid" != JNothing => Inventory((v \ "scryfallid").extract[String]).card
    case v if v \ "multiverseid" != JNothing => val id = (v \ "multiverseid").extract[Int]; Inventory.find(_.card.faces.exists(_.multiverseid == id)).getOrElse(throw IndexOutOfBoundsException(id)).card
  },
  { case card: Card => JObject(
    JField("scryfallid", JString(card(0).scryfallid)),
    JField("name", JString(card.name)),
    JField("expansion", JString(card.expansion.name))
  ) }
)) with JsonSerializer[Card] {
  override def serialize(src: Card, typeOfSrc: Type, context: JsonSerializationContext) = {
    val card = JsonObject()
    card.addProperty("scryfallid", src(0).scryfallid)
    card.addProperty("name", src.name)
    card.addProperty("expansion", src.expansion.name)
    card
  }
}
