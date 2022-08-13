package editor.collection.`export`

import com.mdimension.jchronic.Chronic
import editor.collection.CardList
import editor.collection.immutable.Inventory
import editor.collection.mutable.Deck
import editor.database.card.CardFormat
import editor.gui.MainFrame
import editor.gui.deck.DeckSerializer
import editor.util.IterableReader

import java.io.InputStream
import java.text.ParseException
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import scala.collection.immutable.ListMap
import scala.util.matching._

/**
 * Object containing constants used for formatting cards.
 * @author Alec Roelke
 */
object TextCardListFormat {
  /** Default format string. */
  val DefaultFormat = "{count}x {name} ({expansion})"

  /** Regex pattern for recognizing card counts in text. */
  val CountPattern = raw"(?:^(?:\d+x|x\d+|\d+)|(?:\d+x|x\d+|\d+)$$)".r
}

/**
 * Formatter that formats a [[CardList]] using an arbitrary text format. Attributes to include in the list
 * are formatted according to [[CardFormat]].  Parsing from text is not performed according to the format; it
 * tries to guess card identites, counts, and insertion dates based on the contents of each line.
 * 
 * @constructor create a new card list formatter with a particular format
 * @param pattern format for converting cards in the list into text
 * 
 * @author Alec Roelke
 */
class TextCardListFormat(pattern: String) extends CardListFormat {
  import TextCardListFormat._

  private val formatter = CardFormat(pattern)

  override val header = None

  override def format(list: CardList) = list.map(formatter.format).mkString(System.lineSeparator)

  override def parse(source: InputStream) = {
    val deck = Deck()
    var extra: Option[String] = None
    var extras = ListMap[String, Deck]()

    IterableReader(source).foreach((line) => {
      try {
        val trimmed = line.trim.toLowerCase
        var possibilities = Inventory.filter((e) => trimmed.contains(e.card.name.toLowerCase) || e.card.faces.exists((f) => trimmed.contains(f.name.toLowerCase)))
        if (possibilities.isEmpty)
          throw ParseException(s"Can't parse card name from \"${line.trim}\"", 0)

        var filtered = possibilities.filter((e) => trimmed.contains(e.card.expansion.name.toLowerCase))
        if (!filtered.isEmpty)
          possibilities = filtered
        
        if (possibilities.size > 1)
          System.err.println(s"multiple matches for \"${line.trim}\"")
        val choice = possibilities.head

        val date = try {
          Option(Chronic.parse(trimmed.replace(choice.card.name.toLowerCase, "").replace(choice.card.expansion.name.toLowerCase, "")))
              .map(_.getBeginCalendar.getTime.toInstant.atZone(ZoneId.systemDefault).toLocalDate)
              .getOrElse(LocalDate.now)
        } catch case _: IllegalStateException => LocalDate.now

        extra.map(extras).getOrElse(deck).add(choice.card, CountPattern.findFirstIn(trimmed).map(_.replace("x", "").toInt).getOrElse(1), date)
      } catch case e: ParseException => {
        extra = Some(line.trim)
        extras += extra.get -> Deck()
      }
    })
    DeckSerializer(deck, extras, "", "")
  }
}