package editor.serialization

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import editor.collection.deck.Category
import editor.collection.deck.Deck2
import editor.database.card.Card

import java.lang.reflect.Type
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.jdk.CollectionConverters._

/**
 * JSON serializer/deserializer for [[Deck]]s.  Cards and categories are mapped to "cards" and "categories" keys,
 * respectively.  Does not include the extra lists in an [[EditorFrame]]; those are considered entirely separate
 * "decks" that are serialized (with this class) on their own.
 * 
 * @author Alec Roelke
 */
class DeckAdapter extends JsonSerializer[Deck2] with JsonDeserializer[Deck2] {
  private val Formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  override def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext) = {
    val d = Deck2()
    val obj = json.getAsJsonObject
    obj.get("cards").getAsJsonArray.asScala.foreach((e) => {
      val entry = e.getAsJsonObject
      d.add(
        context.deserialize(entry.get("card"), classOf[Card]),
        entry.get("count").getAsInt,
        LocalDate.parse(entry.get("date").getAsString, Formatter)
      )
    })
    obj.get("categories").getAsJsonArray.asScala.map((e) => context.deserialize[Category](e, classOf[Category]) -> e.getAsJsonObject.get("rank").getAsInt).toSeq.sortBy{ case(_, r) => r }.foreach{ case (c, _) => d.categories += c }
    d
  }

  override def serialize(src: Deck2, typeOfSrc: Type, context: JsonSerializationContext) = {
    val deck = JsonObject()

    val cards = JsonArray()
    src.foreach((e) => {
      val entry = JsonObject()
      entry.add("card", context.serialize(e.card))
      entry.addProperty("count", e.count)
      entry.addProperty("date", e.dateAdded.format(Formatter))
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