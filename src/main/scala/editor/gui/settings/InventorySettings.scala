package editor.gui.settings

import editor.database.attributes.CardAttribute
import editor.database.attributes.CardAttribute._
import editor.database.version.DatabaseVersion
import editor.database.version.UpdateFrequency

import java.awt.Color
import java.io.File

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
case class InventorySettings(
  source: String = "https://mtgjson.com/api/v5/",
  file: String = "AllSets.json",
  versionFile: String = "version.json",
  version: DatabaseVersion = DatabaseVersion(0, 0, 0),
  location: String = SettingsDialog.EDITOR_HOME.toString,
  scans: String = SettingsDialog.EDITOR_HOME.resolve("scans").toString,
  imageSource: String = "Scryfall",
  imageLimitEnable: Boolean = false,
  imageLimit: Int = 20,
  tags: String = SettingsDialog.EDITOR_HOME.resolve("tags.json").toString(),
  update: UpdateFrequency = UpdateFrequency.DAILY,
  warn: Boolean = true,
  columns: Seq[CardAttribute] = Seq(NAME, MANA_COST, TYPE_LINE, EXPANSION),
  background: Color = Color.WHITE,
  stripe: Color = Color(0xCC, 0xCC, 0xCC, 0xFF)
) {
  @deprecated def this() = this(
    "https://mtgjson.com/api/v5/",
    "AllSets.json",
    "version.json",
    DatabaseVersion(0, 0, 0),
    SettingsDialog.EDITOR_HOME.toString(),
    SettingsDialog.EDITOR_HOME.resolve("scans").toString(),
    "Scryfall",
    false,
    20,
    SettingsDialog.EDITOR_HOME.resolve("tags.json").toString(),
    UpdateFrequency.DAILY,
    true,
    Seq(NAME, MANA_COST, TYPE_LINE, EXPANSION),
    Color.WHITE,
    Color(0xCC, 0xCC, 0xCC, 0xFF)
  )

  /** Path to the downloaded database. */
  lazy val path = location + File.separator + file

  /** URL to the database online. */
  lazy val url = source + file

  /** URL to the version online. */
  lazy val versionSite = source + versionFile
}