package editor.collection.`export`

import com.mdimension.jchronic.Chronic
import editor.collection.CardList
import editor.collection.deck.Deck
import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.database.card.CardFormat
import editor.gui.MainFrame
import editor.gui.editor.DeckSerializer
import editor.util.UnicodeSymbols

import java.io.InputStream
import java.text.ParseException
import java.time.LocalDate
import java.time.ZoneId
import java.util.StringJoiner
import scala.collection.immutable.ListMap
import scala.io.Source
import scala.jdk.CollectionConverters._

/**
 * Object containing useful information and functions for formatting and parsing delimited files.
 * @author Alec Roelke
 */
object DelimitedCardListFormat {
  /** Default delimiter to use for separating table cells (a comma) */
  val DefaultDelimiter = ","

  /** Default list of attributes to use for columns (name, expansion, and count) */
  val DefaultData = Seq(CardAttribute.NAME, CardAttribute.EXPANSION, CardAttribute.COUNT)

  /** Common delimiters used for separating cells on a line (comma, semicolon, colon, tab, and space) */
  val Delimiters = Seq(",", ";", ":", "{tab}", "{space}")

  /** String to use to escape delimiters (double quote) */
  val Escape = "\""

  /**
   * Split a string by a delimiter, ignoring delimiters contained inside [[Escape]] pairs.
   * 
   * @param delimiter delimiter to split by
   * @param line string to split
   * @return an array of strings containing the split line, with delimiters between tokens removed
   */
  def split(delimiter: String, line: String) = line.split(s"$delimiter(?=(?:[^$Escape]*$Escape[^$Escape]*$Escape)*[^$Escape]*$$)").collect{
    case cell if cell.substring(0, Escape.size) == Escape || cell.substring(cell.size - Escape.size) == Escape => cell.substring(1, cell.size - 1)
    case cell => cell
  }
}

/**
 * A card list formatter that formats cards into a table whose columns are [[CardAttribute]]s and whose rows
 * are the values of those attributes for individual [[Card]]s. It can also parse such a table into a card list,
 * making guesses about specific printings if the information provided doesn't narrow it down enough.
 * 
 * @constructor create a new card list formatter for a specific delimiter and list of attributes
 * @param delim delimiter to use to separate cells; to use a space or a tab, "{space}" and "{tab}" can be used
 * @param attributes list of attributes to include in the table in the order they should appear; use an empty list to elide headers from
 * the output or infer them from input when parsing
 * 
 * @author Alec Roelke
 */
class DelimitedCardListFormat(delim: String, attributes: Seq[CardAttribute]) extends CardListFormat {
  import DelimitedCardListFormat._

  private val delimiter = delim match {
    case "{space}" => " "
    case "{tab}" => "\t"
    case _ => delim
  }

  override def format(list: CardList) = {
    val columnFormats = attributes.map((a) => CardFormat(s"{$a}".toLowerCase))
    list.asScala.map((card) => {
      columnFormats.map((format) => {
        val value = format.format(list.getEntry(card)).replace(Escape, UnicodeSymbols.SUBSTITUTE.toString)
        (if (value.contains(delimiter)) s"$Escape${value.replace(Escape, Escape*2)}$Escape" else value).replace(UnicodeSymbols.SUBSTITUTE.toString, Escape*2)
      }).mkString(delimiter)
    }).mkString(System.lineSeparator)
  }

  override lazy val header = attributes.map(_.toString.replace(Escape, Escape*2)).mkString(delimiter)

  override def parse(source: InputStream) = {
    val deck = Deck()
    var extra: Option[String] = None
    var extras = ListMap[String, Deck]()
    var pos = 0

    val (attrs, lines) = {
      val lines = Source.fromInputStream(source).getLines.toSeq
      if (attributes.isEmpty) {
        val headers = lines.head.split(delimiter)
        val attrs = headers.map((h) => CardAttribute.displayableValues.find(_.toString.compareToIgnoreCase(h) == 0).getOrElse(throw ParseException(s"unknown data type $header", pos))).toSeq
        pos = lines.head.size
        (attrs, lines.tail)
      } else {
        pos = 0
        (attributes, lines)
      }
    }
    val name = attrs.indexOf(CardAttribute.NAME)
    val expansion = attrs.indexOf(CardAttribute.EXPANSION)
    val number = attrs.indexOf(CardAttribute.CARD_NUMBER)
    val count = attrs.indexOf(CardAttribute.COUNT)
    val date = attrs.indexOf(CardAttribute.DATE_ADDED)
    if (name < 0)
      throw IllegalStateException("can't parse cards without names")
    if (count < 0)
      System.err.println("warning: missing card count in parse; assuming one copy of each card")

    lines.foreach((line) => {
      try {
        val cells = split(delimiter, line.replace(Escape*2, Escape))
        val possibilities = MainFrame.inventory.asScala
            .filter(_.name.equalsIgnoreCase(cells(name)))
            .filter(expansion < 0 || _.expansion.name.equalsIgnoreCase(cells(expansion)))
            .filter(number < 0 || _.faces.map(_.number).mkString(Card.FaceSeparator) == cells(number))
        
        if (possibilities.size > 1)
          System.err.println(s"warning: cannot determine printing of \"${line.trim}\"")
        if (possibilities.isEmpty)
          throw ParseException(s"can't find card named ${cells(name)}", pos)
        
        extra.map(extras).getOrElse(deck).add(
          possibilities.head,
          if (count < 0) 1 else cells(count).toInt,
          if (date < 0) LocalDate.now else try {
            Option(Chronic.parse(cells(date)))
                .map(_.getBeginCalendar.getTime.toInstant.atZone(ZoneId.systemDefault).toLocalDate)
                .getOrElse(LocalDate.now)
          } catch case _: IllegalStateException => LocalDate.now
        )
      } catch case e: ParseException => {
        extra = Some(line)
        extras += line -> Deck()
      }
      pos += line.size
    })
    DeckSerializer(deck, extras, "", "")
  }
}
