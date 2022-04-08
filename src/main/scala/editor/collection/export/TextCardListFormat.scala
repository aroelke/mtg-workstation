package editor.collection.`export`

import editor.database.card.CardFormat
import editor.collection.CardList
import scala.jdk.CollectionConverters._
import editor.gui.MainFrame
import java.text.ParseException
import editor.collection.deck.Deck
import java.util.Date
import java.time.ZoneId
import java.util.regex.Pattern
import java.io.InputStream
import scala.collection.immutable.ListMap
import editor.gui.editor.DeckSerializer
import editor.util.IterableReader
import com.mdimension.jchronic.Chronic
import java.time.LocalDate

object TextCardListFormat {
  val DefaultFormat = "{count}x {name} ({expansion})"

  val CountPattern = Pattern.compile(raw"(?:^(?:\d+x|x\d+|\d+)|(?:\d+x|x\d+|\d+)$$)")
}

class TextCardListFormat(pattern: String) extends CardListFormat {
  import TextCardListFormat._

  private val formatter = CardFormat(pattern)

  override val header = ""

  override def format(list: CardList) = list.asScala.map((c) => formatter.format(list.getEntry(c))).mkString(System.lineSeparator)

  override def parse(source: InputStream) = {
    val deck = Deck()
    var extra: Option[String] = None
    var extras = ListMap[String, Deck]()

    IterableReader(source).foreach((line) => {
      try {
        val trimmed = line.trim.toLowerCase
        var possibilities = MainFrame.inventory.asScala.filter((c) => trimmed.contains(c.name.toLowerCase) || c.faces.exists((f) => trimmed.contains(f.name.toLowerCase)))
        if (possibilities.isEmpty)
          throw ParseException(s"Can't parse card name from \"${line.trim}\"", 0)

        var filtered = possibilities.filter((c) => trimmed.contains(c.expansion.name.toLowerCase))
        if (!filtered.isEmpty)
          possibilities = filtered
        
        if (possibilities.size > 1)
          System.err.println(s"multiple matches for \"${line.trim}\"")
        val choice = possibilities.head

        val countMatcher = CountPattern.matcher(trimmed)
        val date = Option(Chronic.parse(trimmed.replace(choice.name.toLowerCase, "").replace(choice.expansion.name.toLowerCase, "")))
            .map(_.getBeginCalendar.getTime.toInstant.atZone(ZoneId.systemDefault).toLocalDate)
            .getOrElse(LocalDate.now)

        extra.map(extras).getOrElse(deck).add(choice, if (countMatcher.find) countMatcher.group.replace("x", "").toInt else 1, date)
      } catch case e: ParseException => {
        extra = Some(line.trim)
        extras += extra.get -> Deck()
      }
    })
    DeckSerializer(deck, extras, "", "")
  }
}