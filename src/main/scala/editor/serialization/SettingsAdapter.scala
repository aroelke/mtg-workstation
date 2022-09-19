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
    JField("editor", JObject(List(
      JField("recents", Extraction.decompose(settings.editor.recents)),
      JField("categories", Extraction.decompose(settings.editor.categories)),
      JField("columns", JArray(settings.editor.columns.map(Extraction.decompose).toList)),
      JField("stripe", Extraction.decompose(settings.editor.stripe)),
      JField("hand", Extraction.decompose(settings.editor.hand)),
      JField("legality", Extraction.decompose(settings.editor.legality)),
      JField("manaValue", JString(settings.editor.manaValue)),
      JField("backFaceLands", JArray(settings.editor.backFaceLands.map((l) => JString(l.toString)).toList)),
      JField("manaAnalysis", Extraction.decompose(settings.editor.manaAnalysis))
    ))),
    JField("cwd", JString(settings.cwd))
  )) }
))