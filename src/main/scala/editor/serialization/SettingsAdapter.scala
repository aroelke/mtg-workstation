package editor.serialization

import editor.collection.Categorization
import editor.database.attributes.CardAttribute
import editor.database.card.CardLayout
import editor.database.version.DatabaseVersion
import editor.database.version.UpdateFrequency
import editor.gui.settings.CategoriesSettings
import editor.gui.settings.EditorSettings
import editor.gui.settings.HandSettings
import editor.gui.settings.InventorySettings
import editor.gui.settings.LegalitySettings
import editor.gui.settings.ManaAnalysisSettings
import editor.gui.settings.RecentsSettings
import editor.gui.settings.Settings
import org.json4s._
import org.json4s.native._

import java.awt.Color

/**
 * JSON serializer/deserializer for [[Settings]].
 * @author Alec Roelke
 */
class SettingsAdapter extends CustomSerializer[Settings](implicit formats => (
  { case v =>
    val defaults = Settings()
    Settings(
      (v \ "inventory").extract[Option[InventorySettings]].getOrElse(defaults.inventory),
      (v \ "editor").extract[Option[EditorSettings]].getOrElse(defaults.editor),
      (v \ "cwd").extract[Option[String]].getOrElse(defaults.cwd)
    ) },
  { case settings: Settings => JObject(List(
    JField("inventory", Extraction.decompose(settings.inventory)),
    JField("editor", Extraction.decompose(settings.editor)),
    JField("cwd", JString(settings.cwd))
  )) }
))