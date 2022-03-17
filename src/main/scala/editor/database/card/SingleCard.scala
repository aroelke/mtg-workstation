package editor.database.card

import editor.database.attributes.CombatStat
import editor.database.attributes.Expansion
import editor.database.attributes.Legality
import editor.database.attributes.Loyalty
import editor.database.attributes.ManaCost
import editor.database.attributes.ManaType
import editor.database.attributes.Rarity
import editor.database.symbol.FunctionalSymbol
import editor.database.symbol.Symbol
import editor.gui.generic.ComponentUtils
import editor.util.UnicodeSymbols

import java.util.Date
import javax.swing.text.BadLocationException
import javax.swing.text.Style
import javax.swing.text.StyleConstants
import javax.swing.text.StyledDocument
import scala.collection.immutable.TreeMap
import scala.jdk.CollectionConverters._

/**
 * A single-faced [[Card]].  Each attribute that contains a list of items per face will only have one value.
 * 
 * @constructor create a new single-faced card.
 * @param layout layout of the card does not have to be a single-faced layout, but multi-faced ones should later
 * be joined together using the corresponding class
 * @param _name name of the card
 * @param manaCost manaCost cost of the card
 * @param colors colors of the card does not have to correspond with the colors of its manaCost cost (but usually does)
 * @param colorIdentity color identity of the card
 * @param supertypes supertype set of the card (preferably sorted in order of appearance)
 * @param types type set of the card (preferably sorted in order of appearance)
 * @param subtypes subtype set of the card (preferably sorted in order of appearance)
 * @param rarity rarity of the card
 * @param set expansion to which the card belongs
 * @param oracle Oracle text of the card
 * @param flavor flavor text of the card
 * @param printed printed text of the card using its original wording
 * @param art artist who illustrated the card
 * @param multiverse multiverseid of the card (unique ID used in Gatherer to identify cards)
 * @param scryfall Scryfall ID of the card
 * @param n collector number of the card
 * @param pow power of the card, if it's a creature
 * @param tough toughness of the card, if it's a creature
 * @param loyal loyalty of the card, if it's a planeswalker
 * @param rulings clarifications on how the card works and when they were made
 * @param legality which formats the card is legal (or restricted) in
 * @param commandFormats formats in which the card can be commander
 */
class SingleCard(
  layout: CardLayout,
  override val name: String,
  override val manaCost: ManaCost,
  override val colors: Seq[ManaType],
  override val colorIdentity: Seq[ManaType],
  override val supertypes: Set[String],
  override val types: Set[String],
  override val subtypes: Set[String],
  override val printedTypes: String,
  override val rarity: Rarity,
  set: Expansion,
  override val oracleText: String,
  override val flavorText: String,
  override val printedText: String,
  override val artist: String,
  override val multiverseid: Int,
  override val scryfallid: String,
  override val number: String,
  override val power: CombatStat,
  override val toughness: CombatStat,
  override val loyalty: Loyalty,
  override val rulings: TreeMap[Date, Seq[String]],
  override val legality: Map[String, Legality],
  override val commandFormats: Seq[String]
) extends Card(set, layout) {
  override def faces = Seq(this)

  override def manaValue = manaCost.manaValue
  override def minManaValue = manaCost.manaValue
  override def maxManaValue = manaCost.manaValue
  override def avgManaValue = manaCost.manaValue
  override lazy val typeLine = s"""${if (supertypes.isEmpty) "" else s"${supertypes.mkString(" ")} "}${types.mkString(" ")}${if (subtypes.isEmpty) "" else s" ${UnicodeSymbols.EM_DASH} ${subtypes.mkString(" ")}"}"""
  override lazy val allTypes = supertypes ++ types ++ subtypes
  override lazy val isLand = types.exists(_.equalsIgnoreCase("land"))
  override lazy val imageNames = Seq(name.toLowerCase)

  override def formatDocument(document: StyledDocument, printed: Boolean) = {
    val textStyle = document.getStyle("text")
    val reminderStyle = document.getStyle("reminder")
    val chaosStyle = document.addStyle("CHAOS", null)
    StyleConstants.setIcon(chaosStyle, FunctionalSymbol.Chaos.scaled(ComponentUtils.TextSize))
    try {
      document.insertString(document.getLength(), s"${name} ", textStyle)
      if (!manaCost.isEmpty) {
        for (symbol <- manaCost.asScala) {
          val style = document.addStyle(symbol.toString, null)
          StyleConstants.setIcon(style, symbol.scaled(ComponentUtils.TextSize))
          document.insertString(document.getLength, symbol.toString, style)
        }
        document.insertString(document.getLength, " ", textStyle)
      }
      if (manaCost.manaValue == manaCost.manaValue.toInt)
        document.insertString(document.getLength(), s"(${manaCost.manaValue.toInt})\n", textStyle)
      else
        document.insertString(document.getLength(), s"(${manaCost.manaValue})\n", textStyle)
      if (manaCost.colors != colors) {
        for (color <- colors) {
          val indicatorStyle = document.addStyle("indicator", document.getStyle("text"))
          StyleConstants.setForeground(indicatorStyle, color.color)
          document.insertString(document.getLength, UnicodeSymbols.BULLET.toString, indicatorStyle)
        }
        if (!colors.isEmpty)
          document.insertString(document.getLength, " ", textStyle)
      }
      if (printed)
        document.insertString(document.getLength, s"$printedTypes\n", textStyle)
      else
        document.insertString(document.getLength, s"${typeLine}\n", textStyle)
      document.insertString(document.getLength, s"${expansion.name} $rarity\n", textStyle)

      val abilities = if (printed) printedText else oracleText
      if (!abilities.isEmpty) {
        var i = 0
        var start = 0
        var style = textStyle
        while (i < abilities.size) {
          if (i == 0 || abilities(i) == '\n') {
            val nextLine = abilities.substring(i + 1).indexOf('\n')
            var dash = (if (nextLine > -1) abilities.substring(i, nextLine + i) else abilities.substring(i)).indexOf(UnicodeSymbols.EM_DASH)
            if (dash > -1) {
              dash += i
              if (i > 0)
                document.insertString(document.getLength, abilities.substring(start, i), style)
              start = i
              // Assume that the dash used for ability words is surrounded by spaces, but not
              // for keywords with cost parameters when the cost is nonmana cost
              if (abilities(dash - 1) == ' ' && abilities(dash + 1) == ' ')
                style = reminderStyle
            }
          }
          abilities(i) match {
            case '{' =>
              document.insertString(document.getLength, abilities.substring(start, i), style)
              start = i + 1
            case '}' =>
              val symbol = Symbol.parse(abilities.substring(start, i))
              if (symbol.isEmpty) {
                System.err.println(s"Unexpected symbol {${abilities.substring(start, i)}} in oracle text for $unifiedName.")
                document.insertString(document.getLength, abilities.substring(start, i), textStyle)
              } else {
                val symbolStyle = document.addStyle(symbol.get.toString, null)
                StyleConstants.setIcon(symbolStyle, symbol.get.scaled(ComponentUtils.TextSize))
                document.insertString(document.getLength, symbol.get.toString, symbolStyle)
              }
              start = i + 1
            case '(' =>
              document.insertString(document.getLength, abilities.substring(start, i), style)
              style = reminderStyle
              start = i
            case ')' =>
              document.insertString(document.getLength, abilities.substring(start, i + 1), style)
              style = textStyle
              start = i + 1
            case 'C' =>
              if (i < abilities.size - 5 && abilities.substring(i, i + 5) == "CHAOS") {
                document.insertString(document.getLength, abilities.substring(start, i), style)
                document.insertString(document.getLength, "CHAOS", chaosStyle)
                i += 5
                start = i
              }
              if (abilities(i) == '}')
                start += 1
            case UnicodeSymbols.EM_DASH =>
              document.insertString(document.getLength, abilities.substring(start, i), style)
              style = textStyle
              start = i
            case _ =>
          }
          if (i == abilities.size - 1 && abilities(i) != '}' && abilities(i) != ')')
            document.insertString(document.getLength, abilities.substring(start, i + 1), style)
          i += 1
        }
        document.insertString(document.getLength, "\n", textStyle)
      }
      if (!flavorText.isEmpty) {
        var i = 0
        var start = 0
        while (i < flavorText.size) {
          flavorText(i) match {
            case '{' =>
              document.insertString(document.getLength, flavorText.substring(start, i), reminderStyle)
              start = i + 1
            case '}' =>
              val symbol = Symbol.parse(flavorText.substring(start, i))
              if (symbol.isEmpty) {
                System.err.println(s"Unexpected symbol {${flavorText.substring(start, i)}} in flavor text for $unifiedName.")
                document.insertString(document.getLength, flavorText.substring(start, i), reminderStyle)
              } else {
                val symbolStyle = document.addStyle(symbol.get.toString, null)
                StyleConstants.setIcon(symbolStyle, symbol.get.scaled(ComponentUtils.TextSize))
                document.insertString(document.getLength, " ", symbolStyle)
              }
              start = i + 1
            case _ =>
          }
          if (i == flavorText.length - 1 && flavorText(i) != '}')
            document.insertString(document.getLength, flavorText.substring(start, i + 1), reminderStyle)
          i += 1
        }
        document.insertString(document.getLength, "\n", reminderStyle)
      }

      if (power.exists && toughness.exists)
        document.insertString(document.getLength, s"$power/$toughness\n", textStyle)
      else if (loyalty.exists)
          document.insertString(document.getLength, s"$loyalty\n", textStyle)

      document.insertString(document.getLength, s"$artist $number/${expansion.count}", textStyle)
    } catch case e: BadLocationException => e.printStackTrace()
  }

  override def formatDocument(document: StyledDocument, printed: Boolean, face: Int) = formatDocument(document, printed)
}