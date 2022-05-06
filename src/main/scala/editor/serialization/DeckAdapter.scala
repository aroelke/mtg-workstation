package editor.serialization

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import editor.collection.deck.Category
import editor.collection.deck.Deck
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
class DeckAdapter extends JsonSerializer[Deck] with JsonDeserializer[Deck] {
  private val Formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  override def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext) = {
    val d = Deck()
    val obj = json.getAsJsonObject
    obj.get("cards").getAsJsonArray.asScala.foreach((e) => {
      val entry = e.getAsJsonObject
      d.add(
        context.deserialize(entry.get("card"), classOf[Card]),
        entry.get("count").getAsInt,
        LocalDate.parse(entry.get("date").getAsString, Formatter)
      )
    })
    obj.get("categories").getAsJsonArray.asScala.foreach((e) => d.addCategory(context.deserialize(e, classOf[Category]), e.getAsJsonObject.get("rank").getAsInt))
    d
  }

  override def serialize(src: Deck, typeOfSrc: Type, context: JsonSerializationContext) = {
    val deck = JsonObject()

    val cards = JsonArray()
    src.foreach((card) => {
      val entry = JsonObject()
      entry.add("card", context.serialize(card))
      entry.addProperty("count", src.getEntry(card).count)
      entry.addProperty("date", src.getEntry(card).dateAdded.format(Formatter))
      cards.add(entry)
    })
    deck.add("cards", cards)

    val categories = JsonArray()
    src.categories.asScala.foreach((spec) => {
      val category = context.serialize(spec).getAsJsonObject
      category.addProperty("rank", src.getCategoryRank(spec.getName))
      categories.add(category)
    })
    deck.add("categories", categories)

    deck
  }
}