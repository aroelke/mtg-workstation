package editor.database.card

import editor.database.attributes.ManaType

import javax.swing.text.StyledDocument
import scala.collection.immutable.TreeMap
import scala.jdk.CollectionConverters._

/**
 * A card with multiple faces, or multiple mini-cards printed on the same side. Attributes are
 * implemented by concatenating or otherwise combining values from among the faces where possible,
 * such as with card name, but where not possible, like with power, an exception is thrown and
 * access should be made through individual faces.
 * 
 * @constructor create a new multi-faced cad
 * @param layout way the faces are arranged or accessed on the card
 * @param faces single-faced cards making up the faces
 * @author Alec Roelke
 */
abstract class MultiCard(layout: CardLayout, override val faces: IndexedSeq[Card]) extends Card(faces(0).expansion, layout) {
  if (faces.exists(_.isInstanceOf[MultiCard]))
    throw IllegalArgumentException("only normal, single-faced cards can be joined into a multi-faced card")

  override lazy val name = faces.map(_.name).mkString(Card.FaceSeparator)
  override def manaCost = throw UnsupportedOperationException("only individual faces have mana costs")
  override def minManaValue = faces.map(_.manaValue).min
  override def maxManaValue = faces.map(_.manaValue).max
  override def avgManaValue = faces.map(_.manaValue).sum/faces.size
  override lazy val colors = faces.flatMap(_.colors).toSet
  override lazy val colorIdentity = faces.flatMap(_.colorIdentity).toSet
  override lazy val typeLine = faces.map(_.typeLine).reduce(_ ++ _)
  override lazy val printedTypes = faces.map(_.printedTypes).mkString(Card.FaceSeparator)
  override lazy val oracleText = faces.map(_.oracleText).mkString(s"\n${Card.TextSeparator}\n")
  override lazy val flavorText = faces.map(_.flavorText).mkString(s"\n${Card.TextSeparator}\n")
  override lazy val printedText = faces.map(_.printedText).mkString(s"\n${Card.TextSeparator}\n")
  override def power = throw UnsupportedOperationException("only individual faces can have power values")
  override def toughness = throw UnsupportedOperationException("only individual faces can have toughness values")
  override def loyalty = throw UnsupportedOperationException("only individual faces can have loyalty values")
  override def number = throw UnsupportedOperationException("only individual faces can have card numbers")
  override def artist = throw UnsupportedOperationException("only individual faces can have artists")
  override lazy val rulings = TreeMap.from(faces.flatMap(_.rulings))
  override def multiverseid = throw UnsupportedOperationException("only individual faces have multiverse IDs")
  override def scryfallid = throw UnsupportedOperationException("only individual faces have scryfall IDs")
  override lazy val commandFormats = faces.flatMap(_.commandFormats).distinct.sorted
  override def rarity = faces(0).rarity
  override def legality = faces(0).legality
  override def isLand = throw UnsupportedOperationException(s"look at individual faces to determine if $name is a land")

  override def formatDocument(document: StyledDocument, printed: Boolean) = {
    val textStyle = document.getStyle("text")
    try {
      for (i <- 0 until faces.size) {
        formatDocument(document, printed, i)
        if (i < faces.size - 1)
          document.insertString(document.getLength, s"\n${Card.TextSeparator}\n", textStyle)
      }
    } catch case e: Exception => e.printStackTrace()
  }

  override def formatDocument(document: StyledDocument, printed: Boolean, face: Int) = faces(face).formatDocument(document, printed)
}