package editor.database.card

import javax.swing.text.StyledDocument;

import editor.database.attributes.CombatStat;
import editor.database.attributes.Expansion;
import editor.database.attributes.Legality;
import editor.database.attributes.Loyalty;
import editor.database.attributes.ManaCost;
import editor.database.attributes.ManaType;
import editor.database.attributes.Rarity;
import editor.util.Lazy;
import editor.util.UnicodeSymbols;
import java.text.Collator
import java.util.Locale
import java.util.Objects

import scala.jdk.CollectionConverters._
import java.util.Date
import java.util.regex.Pattern

object Card {
  val FaceSeparator = " // "
  val TextSeparator = "-----"
  val This = "~"

  val tagMap = java.util.HashMap[Card, java.util.Set[String]]
  def tags = tagMap.asScala.flatMap{ case (_, t) => t.asScala }.toSet.asJava

  @deprecated def FACE_SEPARATOR = FaceSeparator
  @deprecated def TEXT_SEPARATOR = TextSeparator
  @deprecated def THIS = This
}

abstract class Card(val expansion: Expansion, val layout: CardLayout) {
  import Card._

  lazy val normalizedName = name.asScala.map(UnicodeSymbols.normalize).asJava
  lazy val legendName = {
    val legendNames = collection.mutable.Buffer[String]()
    for (fullName <- normalizedName.asScala) {
      if (!supertypes.contains("Legendary"))
        legendNames += fullName
      else {
        val comma = fullName.indexOf(',')
        if (comma > 0)
          legendNames += fullName.substring(0, comma).trim
        else {
          val the = fullName.indexOf("the ")
          if (the == 0)
            legendNames += fullName
          else if (the > 0)
            legendNames += fullName.substring(0, the).trim
          else {
            val of = fullName.indexOf("of ")
            if (of > 0)
              legendNames += fullName.substring(0, of).trim
            else
              legendNames += fullName
          }
        }
      }
    }
    legendNames.asJava
  }
  lazy val normalizedOracle = {
    val texts = new collection.mutable.ArrayBuffer[String](oracleText.size)
    for (i <- 0 until oracleText.size) {
      var normal = UnicodeSymbols.normalize(oracleText.get(i).toLowerCase)
      normal = normal.replace(legendName.get(i), This).replace(normalizedName.get(i), This)
      texts += normal
    }
    texts.asJava
  }
  lazy val normalizedPrinted = printedText.asScala.map(UnicodeSymbols.normalize).asJava
  lazy val normalizedFlavor = flavorText.asScala.map(UnicodeSymbols.normalize).asJava
  lazy val ignoreCountRestriction = supertypeContains("basic") || oracleText.asScala.exists(_.toLowerCase.contains("a deck can have any number"))
  lazy val legalIn = legality.keySet.asScala.filter(legalityIn(_).isLegal).toSeq.asJava

  def name: java.util.List[String]
  def manaCost: java.util.List[ManaCost]
  def manaValue: Double
  def minManaValue: Double
  def maxManaValue: Double
  def avgManaValue: Double
  def colors: java.util.List[ManaType]
  def colors(face: Int): java.util.List[ManaType]
  def colorIdentity: java.util.List[ManaType]
  def supertypes: java.util.Set[String]
  def types: java.util.Set[String]
  def subtypes: java.util.Set[String]
  def typeLine: java.util.List[String]
  def allTypes: java.util.List[java.util.Set[String]]
  def printedTypes: java.util.List[String]
  def rarity: Rarity
  def oracleText: java.util.List[String]
  def printedText: java.util.List[String]
  def flavorText: java.util.List[String]
  def power: java.util.List[CombatStat]
  def toughness: java.util.List[CombatStat]
  def loyalty: java.util.List[Loyalty]
  def artist: java.util.List[String]
  def number: java.util.List[String]
  def legality: java.util.Map[String, Legality]
  def multiverseid: java.util.List[Integer]
  def scryfallid: java.util.List[String]
  def commandFormats: java.util.List[String]
  def rulings: java.util.Map[Date, java.util.List[String]]
  def isLand: Boolean
  def imageNames: java.util.List[String]

  def formatDocument(document: StyledDocument, printed: Boolean): Unit
  def formatDocument(document: StyledDocument, printed: Boolean, face: Int): Unit

  def compareName(other: Card) = Collator.getInstance(Locale.US).compare(unifiedName, other.unifiedName)
  def unifiedName = name.asScala.mkString(FaceSeparator)
  def unifiedTypeLine = typeLine.asScala.mkString(FaceSeparator)
  def typeContains(s: String) = {
    if (s.matches(raw"\s"))
      throw IllegalArgumentException("types don't contain white space")
    types.asScala.exists(_.equalsIgnoreCase(s))
  }
  def supertypeContains(s: String) = {
    if (s.matches(raw"\s"))
      throw IllegalArgumentException("supertypes don't contain white space")
    supertypes.asScala.exists(_.equalsIgnoreCase(s))
  }
  def powerVariable = power.asScala.exists(_.variable)
  def toughnessVariable = toughness.asScala.exists(_.variable)
  def loyaltyVariable = loyalty.asScala.exists(_.variable)
  def legalityIn(format: String) = legality.getOrDefault(format, Legality.ILLEGAL)

  override def equals(other: Any) = other match {
    case c: Card => scryfallid == c.scryfallid
    case _ => false
  }
  override def hashCode = Objects.hash(name, scryfallid)
  override def toString = unifiedName
}