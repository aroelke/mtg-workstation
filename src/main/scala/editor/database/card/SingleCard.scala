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
import scala.jdk.CollectionConverters._

/**
 * A single-faced [[Card]].  Each attribute that contains a list of items per face will only have one value.
 * 
 * @constructor create a new single-faced card.
 * @param layout layout of the card does not have to be a single-faced layout, but multi-faced ones should later
 * be joined together using the corresponding class
 * @param _name name of the card
 * @param mana mana cost of the card
 * @param colors colors of the card does not have to correspond with the colors of its mana cost (but usually does)
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
  _name: String,
  mana: ManaCost,
  override val colors: Seq[ManaType],
  override val colorIdentity: Seq[ManaType],
  override val supertypes: Set[String],
  override val types: Set[String],
  override val subtypes: Set[String],
  pTypes: String,
  override val rarity: Rarity,
  set: Expansion,
  oracle: String,
  flavor: String,
  printed: String,
  art: String,
  multiverse: Int,
  scryfall: String,
  n: String,
  pow: CombatStat,
  tough: CombatStat,
  loyal: Loyalty,
  override val rulings: java.util.TreeMap[Date, java.util.List[String]],
  override val legality: java.util.Map[String, Legality],
  override val commandFormats: java.util.List[String]
) extends Card(set, layout) {
  override def name = Seq(_name)
  override def manaCost = Seq(mana)
  override def printedTypes = Seq(pTypes)
  override def oracleText = Seq(oracle)
  override def flavorText = Seq(flavor)
  override def printedText = Seq(printed)
  override def artist = Seq(art).asJava
  override def number = Seq(n).asJava
  override def multiverseid = Seq(Integer(multiverse)).asJava
  override def scryfallid = Seq(scryfall).asJava
  override def power = Seq(pow)
  override def toughness = Seq(tough)
  override def loyalty = Seq(loyal).asJava

  override def colors(face: Int) = colors
  override def manaValue = mana.manaValue
  override def minManaValue = mana.manaValue
  override def maxManaValue = mana.manaValue
  override def avgManaValue = mana.manaValue
  override lazy val typeLine = Seq(s"""${if (supertypes.isEmpty) "" else s"${supertypes.mkString(" ")} "}${types.mkString(" ")}${if (subtypes.isEmpty) "" else s" ${UnicodeSymbols.EM_DASH} ${subtypes.mkString(" ")}"}""")
  override lazy val allTypes = Seq(supertypes ++ types ++ subtypes)
  override lazy val isLand = types.exists(_.equalsIgnoreCase("land"))
  override lazy val imageNames = Seq(_name.toLowerCase).asJava

  override def formatDocument(document: StyledDocument, printed: Boolean) = {
    val textStyle = document.getStyle("text")
    val reminderStyle = document.getStyle("reminder")
    val chaosStyle = document.addStyle("CHAOS", null)
    StyleConstants.setIcon(chaosStyle, FunctionalSymbol.Chaos.scaled(ComponentUtils.TextSize))
    try {
      document.insertString(document.getLength(), s"${_name} ", textStyle)
      if (!mana.isEmpty) {
        for (symbol <- mana.asScala) {
          val style = document.addStyle(symbol.toString, null)
          StyleConstants.setIcon(style, symbol.scaled(ComponentUtils.TextSize))
          document.insertString(document.getLength, symbol.toString, style)
        }
        document.insertString(document.getLength, " ", textStyle)
      }
      if (mana.manaValue == mana.manaValue.toInt)
        document.insertString(document.getLength(), s"(${mana.manaValue.toInt})\n", textStyle)
      else
        document.insertString(document.getLength(), s"(${mana.manaValue})\n", textStyle)
      if (mana.colors != colors) {
        for (color <- colors) {
          val indicatorStyle = document.addStyle("indicator", document.getStyle("text"))
          StyleConstants.setForeground(indicatorStyle, color.color)
          document.insertString(document.getLength, UnicodeSymbols.BULLET.toString, indicatorStyle)
        }
        if (!colors.isEmpty)
          document.insertString(document.getLength, " ", textStyle)
      }
      if (printed)
        document.insertString(document.getLength, s"$pTypes\n", textStyle)
      else
        document.insertString(document.getLength, s"${typeLine(0)}\n", textStyle)
      document.insertString(document.getLength, s"${expansion.name} $rarity\n", textStyle)

      val abilities = if (printed) this.printed else oracle
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
      if (!flavor.isEmpty()) {
        var i = 0
        var start = 0
        while (i < flavor.size) {
          flavor(i) match {
            case '{' =>
              document.insertString(document.getLength, flavor.substring(start, i), reminderStyle)
              start = i + 1
            case '}' =>
              val symbol = Symbol.parse(flavor.substring(start, i))
              if (symbol.isEmpty) {
                System.err.println(s"Unexpected symbol {${flavor.substring(start, i)}} in flavor text for $unifiedName.")
                document.insertString(document.getLength, flavor.substring(start, i), reminderStyle)
              } else {
                val symbolStyle = document.addStyle(symbol.get.toString, null)
                StyleConstants.setIcon(symbolStyle, symbol.get.scaled(ComponentUtils.TextSize))
                document.insertString(document.getLength, " ", symbolStyle)
              }
              start = i + 1
            case _ =>
          }
          if (i == flavor.length - 1 && flavor(i) != '}')
            document.insertString(document.getLength, flavor.substring(start, i + 1), reminderStyle)
          i += 1
        }
        document.insertString(document.getLength, "\n", reminderStyle)
      }

      if (pow.exists && tough.exists)
        document.insertString(document.getLength, s"$pow/$tough\n", textStyle)
      else if (loyal.exists)
          document.insertString(document.getLength, s"$loyal\n", textStyle)

      document.insertString(document.getLength, s"$art $n/${expansion.count}", textStyle)
    } catch case e: BadLocationException => e.printStackTrace()
  }

  override def formatDocument(document: StyledDocument, printed: Boolean, face: Int) = formatDocument(document, printed)
}