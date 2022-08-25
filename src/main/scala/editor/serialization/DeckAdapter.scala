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
  private val Formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
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
    (v \ "cards") match {
      case JArray(cards) => cards.map{
        case JObject(JField("card", card) :: JField("count", JInt(count)) :: JField("date", JString(date)) :: Nil) =>
          CardListEntry(card.extract[Card], count.toInt, LocalDate.parse(date, DeckAdapter.Formatter))
        case x => throw MatchError(x.toString)
      }
      case _ => throw MatchError(v)
    },
    (v \ "categories").extract[Set[Categorization]]
  ) },
  { case deck: Deck => JObject(
    JField("cards", JArray(deck.map((e) => JObject(
      JField("card", Extraction.decompose(e.card)),
      JField("count", JInt(e.count)),
      JField("date", JString(e.dateAdded.format(DeckAdapter.Formatter)))
    )).toList)),
    JField("categories", JArray(deck.categories.map(Extraction.decompose).toList))
  ) }
)) with JsonSerializer[Deck] with JsonDeserializer[Deck] {
  override def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext) = {
    val d = Deck()
    val obj = json.getAsJsonObject
    obj.get("cards").getAsJsonArray.asScala.foreach((e) => {
      val entry = e.getAsJsonObject
      d.add(
        context.deserialize(entry.get("card"), classOf[Card]),
        entry.get("count").getAsInt,
        LocalDate.parse(entry.get("date").getAsString, DeckAdapter.Formatter)
      )
    })
    obj.get("categories").getAsJsonArray.asScala.map((e) => context.deserialize[Categorization](e, classOf[Categorization]) -> e.getAsJsonObject.get("rank").getAsInt).toSeq.sortBy{ case(_, r) => r }.foreach{ case (c, _) => d.categories += c }
    d
  }

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