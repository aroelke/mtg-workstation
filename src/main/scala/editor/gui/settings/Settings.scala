package editor.gui.settings

import editor.collection.Categorization
import editor.database.attributes.CardAttribute
import editor.database.attributes.CardAttribute._
import editor.database.card.Card
import editor.database.card.CardLayout
import editor.database.card.MultiCard
import editor.database.version.DatabaseVersion
import editor.database.version.UpdateFrequency
import editor.filter.leaf.options.multi.CardTypeFilter

import java.awt.Color
import java.io.File
import java.net.MalformedURLException
import java.net.URL
import scala.jdk.CollectionConverters._

/**
 * Global settings structure, modifiable only using the [[editor.gui.settings.SettingsDialog]].
 * 
 * @constructor create a new settings structure
 * @param inventory global inventory and card settings
 * @param editor editor window settings
 * @param cwd last directory from which a file was opened
 * 
 * @author Alec Roelke
 */
case class Settings(inventory: InventorySettings = InventorySettings(), editor: EditorSettings = EditorSettings(), cwd: String = System.getProperty("user.home")) {
  @deprecated def this() = this(InventorySettings(), EditorSettings(), System.getProperty("user.home"))
}

/**
 * Settings structure containing global inventory and card settings.
 * 
 * @constructor create a new inventory settings structure
 * @param source link to site containing the card database
 * @param file name of the file containing the card database
 * @param versionFile name of the file containing the database version
 * @param version current version of the database on disk
 * @param location diretory to store the database and other information (defaults to [[editor.gui.settings.SettingsDialog.EDITOR_HOME]])
 * @param scans directory inside [[location]] to store card images
 * @param imageSource web site to get card images from ("Scryfall" or "Gatherer")
 * @param imageLimitEnable enable a limit on the number of images to be stored on disk
 * @param imageLimit if enabled, limit on the number of images to store on disk (oldest are deleted first)
 * @param tags file in [[location]] to store user-defined card tags
 * @param update which database version change triggers a download of an update
 * @param warn present warnings from loading inventory
 * @param columns columns to display in inventory table
 * @param background background color of selected card image panel
 * @param stripe stripe color of the inventory table
 * @note The URL of the file downloaded can be determined by concatenating [[source]] with [[file]]
 * @note The URL used to determine the version of the database can be found by concatenating [[source]] with [[versionFile]]
 * 
 * @author Alec Roelke
 */
@throws[MalformedURLException]("if the URL formed by the JSON source link and JSON file name or version file name is invalid")
case class InventorySettings(
  source: String = "https://mtgjson.com/api/v5/",
  file: String = "AllSets.json",
  versionFile: String = "version.json",
  version: DatabaseVersion = DatabaseVersion(0, 0, 0),
  location: String = SettingsDialog.EditorHome.toString,
  scans: String = SettingsDialog.EditorHome.resolve("scans").toString,
  imageSource: String = "Scryfall",
  imageLimitEnable: Boolean = false,
  imageLimit: Int = 20,
  tags: String = SettingsDialog.EditorHome.resolve("tags.json").toString(),
  update: UpdateFrequency = UpdateFrequency.Daily,
  warn: Boolean = true,
  columns: IndexedSeq[CardAttribute[?]] = IndexedSeq(Name, ManaCost, TypeLine, Expansion),
  background: Color = Color.WHITE,
  stripe: Color = Color(0xCC, 0xCC, 0xCC, 0xFF)
) {
  /** Path to the downloaded database. */
  lazy val path = location + File.separator + file
  lazy val inventoryFile = File(path)

  /** URL to the database online. */
  val url = URL(s"$source$file.zip")

  /** URL to the version online. */
  val versionSite = URL(source + versionFile)
}

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
  recents: RecentsSettings = RecentsSettings(),
  categories: CategoriesSettings = CategoriesSettings(),
  columns: IndexedSeq[CardAttribute[?]] = IndexedSeq(Name, ManaCost, TypeLine, Expansion, Categories, Count, DateAdded),
  stripe: Color = Color(0xCC, 0xCC, 0xCC, 0xFF),
  hand: HandSettings = HandSettings(),
  legality: LegalitySettings = LegalitySettings(),
  manaValue: String = "Minimum",
  backFaceLands: Set[CardLayout] = Set(CardLayout.MODAL_DFC),
  manaAnalysis: ManaAnalysisSettings = ManaAnalysisSettings()
) {
  /**
   * Determine if a card counts as a land based on its faces and the [[backFaceLands]] setting.
   * 
   * @param c card to examine
   * @return true if the card is a land, and false otherwise
   */ 
  def isLand(c: Card) = c match {
    case m: MultiCard =>
      if (SettingsDialog.settings.editor.backFaceLands.contains(m.layout))
        m.faces.exists(_.isLand)
      else
        m.faces(0).isLand
    case _ => c.isLand
  }

  /**
   * Determine the mana value of a card based the [[manaValue]] setting.
   * 
   * @param c card to examine
   * @return the single mana value of the card to be used for analysis
   * @see [[editor.database.card.Card.minManaValue]]
   * @see [[editor.database.card.Card.maxManaValue]]
   * @see [[editor.database.card.Card.avgManaValue]]
   * @see [[editor.database.card.Card.manaValue]]
   */
  def getManaValue(c: Card) = manaValue match {
    case "Minimum" => c.minManaValue
    case "Maximum" => c.maxManaValue
    case "Average" => c.avgManaValue
    case "Real"    => c.manaValue
  }
}

/**
 * Settings structure containing information about recently-opened files.
 * 
 * @constructor create a new recent-files structure
 * @param count number of recently-opened files to track
 * @param files list of recently-opened files
 * 
 * @author Alec Roelke
 */
case class RecentsSettings(count: Int = 4, files: Seq[String] = Nil)

/**
 * Settings structure containing informationa about default categories and category appearance.
 * 
 * @param presets list of preset categories
 * @param rows number of rows of cards to show in categories
 * @param explicits number of rows of cards in the category editor dialog to show for whitelist/
 * blacklist tables
 * 
 * @author Alec Roelke
 */
case class CategoriesSettings(
  presets: Seq[Categorization] = Map(
    "Artifacts" -> Seq("Artifact"),
    "Creatures" -> Seq("Creature"),
    "Lands" -> Seq("Land"),
    "Instants/Sorceries" -> Seq("Instant", "Sorcery")
  ).map{ case (name, types) => {
    val filter = CardAttribute.CardType.filter.asInstanceOf[CardTypeFilter]
    filter.selected ++= types
    Categorization(name, filter, Set.empty, Set.empty, Color.WHITE)
  }}.toSeq,
  rows: Int = 6,
  explicits: Int = 3
)

/**
 * Settings structure containing information about analyzing opening hands.
 * 
 * @constructor create a new hand analysis structure
 * @param size starting hand size
 * @param rounding how to round probabilities
 * @param background background color for visual hand display
 * 
 * @author Alec Roelke
 */
case class HandSettings(size: Int = 7, rounding: String = "No Rounding", background: Color = Color.WHITE)

/**
 * Settings structure containing information about how to determine deck legality.
 * 
 * @constructor create a new deck legality structure
 * @param searchForCommander whether or not to determine if a valid commander exists
 * @param main search in the main deck for a valid commander
 * @param all search all lists in a file for a valid commander
 * @param list search in list with a specific name (if it exists) for a valid commander
 * @param sideboard include the list with the given name as a sideboard
 * 
 * @author Alec Roelke
 */
case class LegalitySettings(searchForCommander: Boolean = true, main: Boolean = true, all: Boolean = false, list: String = "", sideboard: String = "")

/**
 * Settings structure containing mana analysis settings.
 * 
 * @constructor create a new mana analysis structure
 * @param none color of uncategorized histogram bars
 * @param colorless color of histogram bar sections for colorless cards
 * @param white color of histogram bar sections for white cards
 * @param blue color of histogram bar sections for blue cards
 * @param black color of histogram bar sections for black cards
 * @param red color of histogram bar sections for red cards
 * @param green color of histogram bar sections for green cards
 * @param multi color of histogram bar sections for multicolored cards
 * @param creature color of histogram bar sections for creature cards
 * @param artifact color of histogram bar sections for artifact cards
 * @param enchantment color of histogram bar sections for enchantment cards
 * @param planeswalker color of histogram bar sections for planeswalker cards
 * @param instant color of histogram bar sections for instant cards
 * @param sorcery color of histogram bar sections for sorcery cards
 * 
 * @author Alec Roelke
 */
case class ManaAnalysisSettings(
  none: Color = Color(128, 128, 255),
  
  colorless: Color = Color(203, 198, 193),
  white: Color = Color(248, 246, 216),
  blue: Color = Color(193, 215, 233),
  black: Color = Color(186, 177, 171),
  red: Color = Color(228, 153, 119),
  green: Color = Color(163, 192, 149),
  multi: Color = Color(204, 166, 82),

  creature: Color = Color(163, 192, 149),
  artifact: Color = Color(203, 198, 193),
  enchantment: Color = Color(248, 246, 216),
  planeswalker: Color = Color(215, 181, 215),
  instant: Color = Color(193, 215, 233),
  sorcery: Color = Color(228, 153, 119),

  line: Color = Color.BLACK
) {
  /**
   * Get the color associated with a histogram bar section by name.
   * 
   * @param key name of the section
   * @return the color of the section
   */
  def apply(key: String) = key.toLowerCase match {
    case "none" | "nothing" => none

    case "colorless" | "c" => colorless
    case "white"     | "w" => white
    case "blue"      | "u" => blue
    case "black"     | "b" => black
    case "red"       | "r" => red
    case "green"     | "g" => green
    case "multicolored" | "multi" | "m" => multi

    case "creature"     => creature
    case "artifact"     => artifact
    case "enchantment"  => enchantment
    case "planeswalker" => planeswalker
    case "instant"      => instant
    case "sorcery"      => sorcery
  }

  /** List of bar colors when dividing by color */
  val colorColors = Seq(colorless, white, blue, black, red, green, multi)
  
  /** List of bar colors when dividing by card type */
  val typeColors = Seq(creature, artifact, enchantment, planeswalker, instant, sorcery)
}

/**
 * Convenience object for creating a [[ManaAnalysisSettings]] from a mapping of graph section name
 * onto color.
 * 
 * @author Alec Roelke
 */
object ManaAnalysisSettings {
  /**
   * Create a new [[ManaAnalysisSettings]] from a mapping of section name onto color.  Section names should
   * match the names of the fields of [[ManaAnalysisSettings]].  Any missing sections will be set to their defaults.
   * 
   * @param colors mapping of section names onto their colors
   * @return a new [[ManaAnalysisSettings]] with the specified section colors
   */
  def apply(colors: Map[String, Color]): ManaAnalysisSettings = {
    val colorsLC = colors.map{ case (s, c) => s.toLowerCase -> c }.toMap
    val defaults = ManaAnalysisSettings()
    def findColor(options: Seq[String], default: Color) = options.flatMap(colorsLC.get(_)).headOption.getOrElse(default)
    defaults.copy(
      none = findColor(Seq("nothing", "none"), defaults.none),

      colorless = findColor(Seq("colorless", "c"), defaults.colorless),
      white = findColor(Seq("white", "w"), defaults.white),
      blue = findColor(Seq("blue", "u"), defaults.blue),
      black = findColor(Seq("black", "b"), defaults.black),
      red = findColor(Seq("red", "r"), defaults.red),
      green = findColor(Seq("green", "g"), defaults.green),
      multi = findColor(Seq("multicolored", "multi", "m"), defaults.multi),

      creature = findColor(Seq("creature"), defaults.creature),
      artifact = findColor(Seq("artifact"), defaults.artifact),
      enchantment = findColor(Seq("enchantmen"), defaults.enchantment),
      planeswalker = findColor(Seq("planeswalker"), defaults.planeswalker),
      instant = findColor(Seq("instant"), defaults.instant),
      sorcery = findColor(Seq("sorcery"), defaults.sorcery)
    )
  }
}