package editor.serialization

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import editor.database.card.Card
import editor.gui.MainFrame

import java.lang.reflect.Type

/**
 * JSON serializer/deserializer for [[Card]]s. Primarily its [[Card.scryfallid]] is used, but if that is not present
 *  in the JSON to deserialize (as in older versions of this program), then its [[Card.multiverseid]] is used instead.
 * The name and expansion are also stored when serializing for future implementation of inventory searches by
 * identifying characteristics other than scryfallid.
 * 
 * @author Alec Roelke
 */
class CardAdapter extends JsonSerializer[Card] with JsonDeserializer[Card] {
  override def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext) = {
    if (json.getAsJsonObject.has("scryfallid"))
      MainFrame.inventory(json.getAsJsonObject.get("scryfallid").getAsString).card
    else {
      val multiverseid = json.getAsJsonObject.get("multiverseid").getAsInt
      MainFrame.inventory.find(_.card.faces.exists(_.multiverseid == multiverseid)).getOrElse(throw JsonParseException(s"no card with multiverseid $multiverseid exists")).card
    }
  }

  override def serialize(src: Card, typeOfSrc: Type, context: JsonSerializationContext) = {
    val card = JsonObject()
    card.addProperty("scryfallid", src(0).scryfallid)
    card.addProperty("name", src.name)
    card.addProperty("expansion", src.expansion.name)
    card
  }
}
