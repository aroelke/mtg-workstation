package editor.collection.`export`

import editor.database.card.CardFormat
import editor.collection.CardList
import scala.jdk.CollectionConverters._
import editor.gui.MainFrame
import java.text.ParseException
import editor.collection.deck.Deck
import com.joestelmach.natty.Parser
import java.util.Date
import java.time.ZoneId
import java.util.regex.Pattern
import java.io.InputStream
import scala.collection.immutable.ListMap
import editor.gui.editor.DeckSerializer
import editor.util.IterableReader

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

        val countMatcher = CountPattern.matcher(trimmed)
        // com.joelstelmach.natty.Parser().parse() can throw a NullPointerException on some inputs; probably a bug that won't be fixed (no updates since 2017)
        val date = (try {
          Parser().parse(trimmed).asScala.flatMap(_.getDates.asScala).headOption.getOrElse(Date())
        } catch case e: NullPointerException => {
          e.printStackTrace
          Date()
        }).toInstant.atZone(ZoneId.systemDefault).toLocalDate

        extra.map(extras).getOrElse(deck).add(possibilities.head, if (countMatcher.find) countMatcher.group.replace("x", "").toInt else 1, date)
      } catch case e: ParseException => {
        extra = Some(line.trim)
        extras += extra.get -> Deck()
      }
    })
    DeckSerializer(deck, extras, "", "")
  }
}