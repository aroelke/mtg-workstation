package editor.serialization

import editor.collection.CardListEntry
import editor.collection.Categorization
import editor.collection.mutable.Deck
import editor.database.card.Card
import org.json4s._
import org.json4s.native._

import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
))