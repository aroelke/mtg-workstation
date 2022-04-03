package editor.collection.`export`

import editor.database.attributes.CardAttribute
import java.util.StringJoiner
import editor.collection.CardList
import editor.database.card.CardFormat
import scala.jdk.CollectionConverters._
import java.text.ParseException
import editor.collection.deck.Deck
import editor.gui.MainFrame
import editor.database.card.Card
import java.time.LocalDate
import java.io.InputStream
import editor.gui.editor.DeckSerializer
import scala.collection.immutable.ListMap
import editor.util.UnicodeSymbols
import scala.io.Source

object DelimitedCardListFormat {
  val DefaultDelimiter = ","

  val DefaultData = Seq(CardAttribute.NAME, CardAttribute.EXPANSION, CardAttribute.COUNT)

  val Delimiters = Seq(",", ";", ":", "{tab}", "{space}")

  val Escape = "\""

  def split(delimiter: String, line: String) = line.split(s"$delimiter(?=(?:[^$Escape]*$Escape[^$Escape]*$Escape)*[^$Escape]*$$)").collect{
    case cell if cell.substring(0, Escape.size) == Escape || cell.substring(cell.size - Escape.size) == Escape => cell.substring(1, cell.size - 1)
    case cell => cell
  }
}

class DelimitedCardListFormat(delim: String, attributes: Seq[CardAttribute], include: Boolean) extends CardListFormat {
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

  override lazy val header = if (include) attributes.map(_.toString.replace(Escape, Escape*2)).mkString(delimiter) else ""

  override def parse(source: InputStream) = {
    val deck = Deck()
    var extra: Option[String] = None
    var extras = ListMap[String, Deck]()
    var pos = 0

    val (attrs, lines) = {
      val lines = Source.fromInputStream(source).getLines.toSeq
      if (include) {
        pos = 0
        (attributes, lines)
      } else {
        val headers = lines.head.split(delimiter)
        val attrs = headers.map((h) => CardAttribute.displayableValues.find(_.toString.compareToIgnoreCase(h) == 0).getOrElse(throw ParseException(s"unknown data type $header", pos))).toSeq
        pos = lines.head.size
        (attrs, lines.tail)
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
            .filter(_.expansion.name.equalsIgnoreCase(cells(expansion)))
            .filter(_.faces.map(_.number).mkString(Card.FaceSeparator) == cells(number))
        
        if (possibilities.size > 1)
          System.err.println(s"warning: cannot determine printing of \"$line\"")
        if (possibilities.isEmpty)
          throw ParseException(s"can't find card named ${cells(name)}", pos)
        
        extra.map(extras).getOrElse(deck).add(
          possibilities.head,
          if (count < 0) 1 else cells(count).toInt,
          if (date < 0) LocalDate.now else LocalDate.parse(cells(date), Deck.DATE_FORMATTER)
        )
      } catch case e: ParseException => {
        extra = Some(line.toString)
        extras += extra.get -> Deck()
      }
      pos += line.size
    })
    DeckSerializer(deck, extras, "", "")
  }
}
