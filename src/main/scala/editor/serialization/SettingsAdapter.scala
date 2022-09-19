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
      JField("categories", JObject(List(
        JField("presets", JArray(settings.editor.categories.presets.map(Extraction.decompose).toList)),
        JField("rows", JInt(settings.editor.categories.rows)),
        JField("explicits", JInt(settings.editor.categories.explicits))
      ))),
      JField("columns", JArray(settings.editor.columns.map(Extraction.decompose).toList)),
      JField("stripe", Extraction.decompose(settings.editor.stripe)),
      JField("hand", JObject(List(
        JField("size", JInt(settings.editor.hand.size)),
        JField("rounding", JString(settings.editor.hand.rounding)),
        JField("background", Extraction.decompose(settings.editor.hand.background))
      ))),
      JField("legality", JObject(List(
        JField("searchForCommander", JBool(settings.editor.legality.searchForCommander)),
        JField("main", JBool(settings.editor.legality.main)),
        JField("all", JBool(settings.editor.legality.all)),
        JField("list", JString(settings.editor.legality.list)),
        JField("sideboard", JString(settings.editor.legality.sideboard))
      ))),
      JField("manaValue", JString(settings.editor.manaValue)),
      JField("backFaceLands", JArray(settings.editor.backFaceLands.map((l) => JString(l.toString)).toList)),
      JField("manaAnalysis", JObject(List(
        JField("none", Extraction.decompose(settings.editor.manaAnalysis.none)),
        JField("white", Extraction.decompose(settings.editor.manaAnalysis.white)),
        JField("blue", Extraction.decompose(settings.editor.manaAnalysis.blue)),
        JField("black", Extraction.decompose(settings.editor.manaAnalysis.black)),
        JField("red", Extraction.decompose(settings.editor.manaAnalysis.red)),
        JField("green", Extraction.decompose(settings.editor.manaAnalysis.green)),
        JField("multi", Extraction.decompose(settings.editor.manaAnalysis.multi)),
        JField("creature", Extraction.decompose(settings.editor.manaAnalysis.creature)),
        JField("artifact", Extraction.decompose(settings.editor.manaAnalysis.artifact)),
        JField("enchantment", Extraction.decompose(settings.editor.manaAnalysis.enchantment)),
        JField("planeswalker", Extraction.decompose(settings.editor.manaAnalysis.planeswalker)),
        JField("instant", Extraction.decompose(settings.editor.manaAnalysis.instant)),
        JField("sorcery", Extraction.decompose(settings.editor.manaAnalysis.sorcery)),
        JField("line", Extraction.decompose(settings.editor.manaAnalysis.line))
      )))
    ))),
    JField("cwd", JString(settings.cwd))
  )) }
))