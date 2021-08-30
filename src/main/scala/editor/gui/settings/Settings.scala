package editor.gui.settings

import editor.collection.deck.Category
import editor.database.attributes.CardAttribute
import editor.database.card.CardLayout
import editor.database.version.DatabaseVersion
import editor.database.version.UpdateFrequency

import java.awt.Color

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

  @deprecated def this(inventorySource: String, inventoryFile: String, inventoryVersionFile: String, inventoryVersion: DatabaseVersion, inventoryLocation: String, inventoryScans: String, imageSource: String, imageLimitEnable: Boolean, imageLimit: Int, inventoryTags: String, inventoryUpdate: UpdateFrequency, inventoryWarn: Boolean, inventoryColumns: Seq[CardAttribute], inventoryBackground: Color, inventoryStripe: Color, recentsCount: Int, recentsFiles: Seq[String], explicits: Int, presetCategories: Seq[Category], categoryRows: Int, editorColumns: Seq[CardAttribute], editorStripe: Color, handSize: Int, handRounding: String, handBackground: Color, searchForCommander: Boolean, main: Boolean, all: Boolean, list: String, sideboard: String, manaValue: String, backFaceLands: Set[CardLayout], cwd: String, none: Color, colorless: Color, white: Color, blue: Color, black: Color, red: Color, green: Color, multi: Color, creature: Color, artifact: Color, enchantment: Color, planeswalker: Color, instant: Color, sorcery: Color, line: Color)
    = this(
      InventorySettings(inventorySource, inventoryFile, inventoryVersionFile, inventoryVersion, inventoryLocation, inventoryScans, imageSource, imageLimitEnable, imageLimit, inventoryTags, inventoryUpdate, inventoryWarn, inventoryColumns, inventoryBackground, inventoryStripe),
      new EditorSettings(recentsCount, recentsFiles, explicits, presetCategories, categoryRows, editorColumns, editorStripe, handSize, handRounding, handBackground, searchForCommander, main, all, list, sideboard, manaValue, backFaceLands, none, colorless, white, blue, black, red, green, multi, creature, artifact, enchantment, planeswalker, instant, sorcery, line),
      cwd
    )
}