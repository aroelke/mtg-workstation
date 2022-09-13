package editor.serialization

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import editor.collection.Categorization
import editor.collection.mutable.Deck
import editor.database.card.Card

import java.lang.reflect.Type
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.jdk.CollectionConverters._
import org.json4s._
import org.json4s.native._
import editor.collection.CardListEntry

object DeckAdapter {
  val Formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
}

/**
 * JSON serializer/deserializer for [[Deck]]s.  Cards and categories are mapped to "cards" and "categories" keys,
 * respectively.  Does not include the extra lists in an [[EditorFrame]]; those are considered entirely separate
 * "decks" that are serialized (with this class) on their own.
 * 
 * @author Alec Roelke
 */
class DeckAdapter extends CustomSerializer[Deck](implicit format => (
  { case v => Deck(
    (v \ "cards").extract[Seq[CardListEntry]],
    (v \ "categories").extract[Set[Categorization]]
  ) },
  { case deck: Deck => JObject(
    JField("cards", JArray(deck.map((e) => JObject(
      JField("card", Extraction.decompose(e.card)),
      JField("count", JInt(e.count)),
      JField("date", JString(e.dateAdded.format(DeckAdapter.Formatter)))
    )).toList)),
    JField("categories", JArray(deck.categories.map((c) => Extraction.decompose(c.categorization)).toList))
  ) }
)) with JsonSerializer[Deck] {
  override def serialize(src: Deck, typeOfSrc: Type, context: JsonSerializationContext) = {
    val deck = JsonObject()

    val cards = JsonArray()
    src.foreach((e) => {
      val entry = JsonObject()
      entry.add("card", context.serialize(e.card))
      entry.addProperty("count", e.count)
      entry.addProperty("date", e.dateAdded.format(DeckAdapter.Formatter))
      cards.add(entry)
    })
    deck.add("cards", cards)

    val categories = JsonArray()
    src.categories.foreach((c) => {
      val category = context.serialize(c.categorization).getAsJsonObject
      category.addProperty("rank", c.rank)
      categories.add(category)
    })
    deck.add("categories", categories)

    deck
  }
}