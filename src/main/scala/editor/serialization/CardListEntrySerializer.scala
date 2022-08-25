package editor.serialization

import editor.collection.CardListEntry
import editor.database.card.Card
import org.json4s._
import org.json4s.native._

import java.time.LocalDate

/**
 * JSON serializer/deserializer for card list entries. Fields are mapped as expected except "dateAdded" is mapped to "date."
 * It's not a case class, so this can't be done using json4s's case class support.
 * @author Alec Roelke
 */
class CardListEntrySerializer extends CustomSerializer[CardListEntry](format => (
  { case JObject(JField("card", card) :: JField("count", JInt(count)) :: JField("date", JString(date)) :: Nil) => CardListEntry(card.extract[Card], count.toInt, LocalDate.parse(date, DeckAdapter.Formatter)) },
  { case CardListEntry(card, count, date) => JObject(List(JField("card", Extraction.decompose(card)), JField("count", JInt(count)), JField("date", JString(date.format(DeckAdapter.Formatter))))) }
))