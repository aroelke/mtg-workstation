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

  private case class Indices(name: Int, expansion: Int, number: Int, count: Int, date: Int) {
    if (name < 0)
      throw IllegalStateException("can't parse cards without names")
    if (count < 0)
      System.err.println("warning: missing card count in parse; assuming one copy of each card")
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

    val (indices, lines) = {
      val lines = Source.fromInputStream(source).getLines.toSeq
      if (include) {
        pos = 0
        (Indices(
          attributes.indexOf(CardAttribute.NAME),
          attributes.indexOf(CardAttribute.EXPANSION),
          attributes.indexOf(CardAttribute.CARD_NUMBER),
          attributes.indexOf(CardAttribute.COUNT),
          attributes.indexOf(CardAttribute.DATE_ADDED)
        ), lines)
      } else {
        val headers = lines.head.split(delimiter)
        val attrs = collection.mutable.Buffer[CardAttribute]()
        for (header <- headers) {
          var success = false
          for (attribute <- CardAttribute.displayableValues) {
            if (header.compareToIgnoreCase(attribute.toString) == 0) {
              attrs += attribute
              success = true
            }
          }
          if (!success)
            throw ParseException(s"unknown data type $header", pos)
        }
        pos = lines.head.size
        (Indices(
          attrs.indexOf(CardAttribute.NAME),
          attrs.indexOf(CardAttribute.EXPANSION),
          attrs.indexOf(CardAttribute.CARD_NUMBER),
          attrs.indexOf(CardAttribute.COUNT),
          attrs.indexOf(CardAttribute.DATE_ADDED)
        ), lines.tail)
      }
    }

    lines.foreach((line) => {
      try {
        val cells = split(delimiter, line.replace(Escape*2, Escape))
        val possibilities = MainFrame.inventory.asScala
            .filter(_.name.equalsIgnoreCase(cells(indices.name)))
            .filter(_.expansion.name.equalsIgnoreCase(cells(indices.expansion)))
            .filter(_.faces.map(_.number).mkString(Card.FaceSeparator) == cells(indices.number))
            .toSeq
        
        if (possibilities.size > 1)
          System.err.println(s"warning: cannot determine printing of ${possibilities(0).name}")
        if (possibilities.isEmpty)
          throw ParseException(s"can't find card named ${cells(indices.name)}", pos)
        
        extra.map(extras).getOrElse(deck).add(
          possibilities(0),
          if (indices.count < 0) 1 else cells(indices.count).toInt,
          if (indices.date < 0) LocalDate.now else LocalDate.parse(cells(indices.date), Deck.DATE_FORMATTER)
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
