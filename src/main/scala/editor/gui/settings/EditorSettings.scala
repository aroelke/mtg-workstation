package editor.gui.settings

import editor.collection.deck.Category
import editor.database.attributes.CardAttribute
import editor.database.attributes.CardAttribute._
import editor.database.card.Card
import editor.database.card.CardLayout
import editor.database.card.MultiCard

import java.awt.Color
import scala.jdk.CollectionConverters._

/**
 * Settings structure containing settings for deck editor windows.
 * 
 * @constructor create a new editor frame settings structure
 * @param recents recent files settings
 * @param categories category settings
 * @param columns columns to display in card tables
 * @param stripe color of stripes in card tables
 * @param hand hand analysis settings
 * @param legality legality analysis settings
 * @param manaValue how to determine the mana value of multi-faced cards
 * @param backFaceLands which multi-faced card layouts to search all faces when counting lands
 * @param manaAnalysis mana analysis settings
 * 
 * @author Alec Roelke
 */
case class EditorSettings(
  recents: RecentsSettings,
  categories: CategoriesSettings,
  columns: Seq[CardAttribute],
  stripe: Color,
  hand: HandSettings,
  legality: LegalitySettings,
  manaValue: String,
  backFaceLands: Set[CardLayout],
  manaAnalysis: ManaAnalysisSettings
) {
  def this() = this(
    RecentsSettings(),
    CategoriesSettings(),
    Seq(NAME, MANA_COST, TYPE_LINE, EXPANSION, CATEGORIES, COUNT, DATE_ADDED),
    Color(0xCC, 0xCC, 0xCC, 0xFF),
    HandSettings(),
    LegalitySettings(),
    "Minimum",
    Set(CardLayout.MODAL_DFC),
    ManaAnalysisSettings()
  );

  def this(recentsCount: Int, recentsFiles: Seq[String],
    explicits: Int,
    presetCategories: Seq[Category], categoryRows: Int,
    columns: Seq[CardAttribute], stripe: Color,
    handSize: Int, handRounding: String, handBackground: Color,
    searchForCommander: Boolean, main: Boolean, all: Boolean, list: String, sideboard: String,
    manaValue: String,
    backFaceLands: Set[CardLayout],
    none: Color,
    colorless: Color, white: Color, blue: Color, black:  Color, red:  Color, green:  Color, multi:  Color,
    creature:  Color, artifact:  Color, enchantment:  Color, planeswalker:  Color, instant:  Color, sorcery:  Color,
    line: Color) = this(
      RecentsSettings(recentsCount, recentsFiles),
      CategoriesSettings(presetCategories, categoryRows, explicits),
      columns,
      stripe,
      HandSettings(handSize, handRounding, handBackground),
      LegalitySettings(searchForCommander, main, all, list, sideboard),
      manaValue,
      backFaceLands,
      ManaAnalysisSettings(
          none,
          colorless, white, blue, black, red, green, multi,
          creature, artifact, enchantment, planeswalker, instant, sorcery,
          line
      )
    )
  
  def isLand(c: Card) = c match {
    case m: MultiCard =>
      if (SettingsDialog.settings.editor.backFaceLands contains m.layout)
        m.faces.asScala.exists(_.isLand)
      else
        m.faces.get(0).isLand
    case _ => c.isLand
  }

  def getManaValue(c: Card) = manaValue match {
    case "Minimum" => c.minManaValue
    case "Maximum" => c.maxManaValue
    case "Average" => c.avgManaValue
    case "Real"    => c.manaValue
  }
}