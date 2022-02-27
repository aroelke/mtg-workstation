package editor.database.card

import editor.database.attributes.ManaType

import javax.swing.text.StyledDocument
import scala.jdk.CollectionConverters._

/**
 * A card with multiple faces, or multiple mini-cards printed on the same side.
 * 
 * @constructor create a new multi-faced cad
 * @param layout way the faces are arranged or accessed on the card
 * @param faces single-faced cards making up the faces
 * @author Alec Roelke
 */
abstract class MultiCard(layout: CardLayout, val faces: Seq[Card]) extends Card(faces(0).expansion, layout) {
  if (faces.exists(_.isInstanceOf[MultiCard]))
    throw IllegalArgumentException("only normal, single-faced cards can be joined into a multi-faced card")

  override lazy val name = faces.map(_.name(0))
  override lazy val manaCost = faces.map(_.manaCost(0))
  override def minManaValue = faces.map(_.manaValue).min
  override def maxManaValue = faces.map(_.manaValue).max
  override def avgManaValue = faces.map(_.manaValue).sum/faces.size
  override lazy val colors = faces.flatMap(_.colors).distinct
  override def colors(face: Int) = faces(face).colors
  override lazy val colorIdentity = faces.flatMap(_.colorIdentity).distinct
  override lazy val supertypes = faces.flatMap(_.supertypes).toSet
  override lazy val types = faces.flatMap(_.types).toSet
  override lazy val subtypes = faces.flatMap(_.subtypes).toSet
  override lazy val allTypes = faces.map(_.allTypes(0))
  override lazy val typeLine = faces.map(_.typeLine(0))
  override lazy val printedTypes = faces.map(_.printedTypes(0))
  override lazy val oracleText = faces.map(_.oracleText(0))
  override lazy val flavorText = faces.map(_.flavorText.get(0)).asJava
  override lazy val printedText = faces.map(_.printedText.get(0)).asJava
  override lazy val artist = faces.map(_.artist.get(0)).asJava
  override lazy val number = faces.map(_.number.get(0)).asJava
  override lazy val power = faces.map(_.power.get(0)).asJava
  override lazy val toughness = faces.map(_.toughness.get(0)).asJava
  override lazy val loyalty = faces.map(_.loyalty.get(0)).asJava
  override lazy val rulings = collection.mutable.TreeMap.from(faces.flatMap(_.rulings.asScala)).asJava
  override lazy val imageNames = faces.map(_.imageNames.get(0)).asJava
  override lazy val multiverseid = faces.map(_.multiverseid.get(0)).asJava
  override lazy val scryfallid = faces.map(_.scryfallid.get(0)).asJava
  override lazy val commandFormats = faces.flatMap(_.commandFormats.asScala).distinct.sorted.asJava
  override def rarity = faces.head.rarity
  override def legality = faces(0).legality
  override def isLand = throw UnsupportedOperationException(s"look at individual faces to determine if $unifiedName is a land")

  override def formatDocument(document: StyledDocument, printed: Boolean) = {
    val textStyle = document.getStyle("text")
    try {
      for (i <- 0 until faces.size) {
        formatDocument(document, printed, i)
        if (i < faces.size - 1)
          document.insertString(document.getLength, s"\n${Card.TEXT_SEPARATOR}\n", textStyle)
      }
    } catch case e: Exception => e.printStackTrace()
  }

  override def formatDocument(document: StyledDocument, printed: Boolean, face: Int) = faces(face).formatDocument(document, printed)
}