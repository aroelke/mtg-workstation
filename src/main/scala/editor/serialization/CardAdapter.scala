package editor.serialization

import editor.collection.immutable.Inventory
import editor.database.card.Card
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.native._

/**
 * JSON serializer/deserializer for [[Card]]s. Primarily its [[Card.scryfallid]] is used, but if that is not present
 * in the JSON to deserialize (as in older versions of this program), then its [[Card.multiverseid]] is used instead.
 * The name and expansion are also stored when serializing for future implementation of inventory searches by
 * identifying characteristics other than scryfallid.
 * 
 * @author Alec Roelke
 */
object CardAdapter extends CustomSerializer[Card](format => (
  {
    case v if v \ "scryfallid" != JNothing => Inventory((v \ "scryfallid").extract[String]).card
    case v if v \ "multiverseid" != JNothing => val id = (v \ "multiverseid").extract[Int]; Inventory.find(_.card.faces.exists(_.multiverseid == id)).getOrElse(throw IndexOutOfBoundsException(id)).card
  },
  { case card: Card => ("scryfallid" -> card(0).scryfallid) ~ ("name" -> card.name) ~ ("expansion" -> card.expansion.name) }
))