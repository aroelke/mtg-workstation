package editor.serialization

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
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
import java.lang.reflect.Type
import scala.jdk.CollectionConverters._

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
    JField("inventory", JObject(List(
      JField("source", JString(settings.inventory.source)),
      JField("file", JString(settings.inventory.file)),
      JField("versionFile", JString(settings.inventory.versionFile)),
      JField("version", Extraction.decompose(settings.inventory.version)),
      JField("location", JString(settings.inventory.location)),
      JField("scans", JString(settings.inventory.scans)),
      JField("imageSource", JString(settings.inventory.imageSource)),
      JField("imageLimitEnable", JBool(settings.inventory.imageLimitEnable)),
      JField("imageLimit", JInt(settings.inventory.imageLimit)),
      JField("tags", JString(settings.inventory.tags)),
      JField("update", Extraction.decompose(settings.inventory.update)),
      JField("columns", JArray(settings.inventory.columns.map(Extraction.decompose).toList)),
      JField("background", Extraction.decompose(settings.inventory.background)),
      JField("stripe", Extraction.decompose(settings.inventory.stripe)),
      JField("warn", JBool(settings.inventory.warn))
    ))),
    JField("editor", JObject(List(
      JField("recents", JObject(List(
        JField("count", JInt(settings.editor.recents.count)),
        JField("files", JArray(settings.editor.recents.files.map(JString(_)).toList))
      ))),
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
)) with JsonSerializer[Settings] {
  override def serialize(src: Settings, typeOfSrc: Type, context: JsonSerializationContext) = {
    val attributeType = (new TypeToken[CardAttribute[?, ?]] {}).getType

    val settings = JsonObject()

    val inventory = JsonObject()
    inventory.addProperty("source", src.inventory.source)
    inventory.addProperty("file", src.inventory.file)
    inventory.addProperty("versionFile", src.inventory.versionFile)
    inventory.add("version", context.serialize(src.inventory.version))
    inventory.addProperty("location", src.inventory.location)
    inventory.addProperty("scans", src.inventory.scans)
    inventory.addProperty("imageSource", src.inventory.imageSource)
    inventory.addProperty("imageLimitEnable", src.inventory.imageLimitEnable)
    inventory.addProperty("imageLimit", src.inventory.imageLimit)
    inventory.addProperty("tags", src.inventory.tags)
    inventory.add("update", context.serialize(src.inventory.update))
    val inventoryColumns = JsonArray()
    src.inventory.columns.foreach((c) => inventoryColumns.add(context.serialize(c, attributeType)))
    inventory.add("columns", inventoryColumns)
    inventory.add("background", context.serialize(src.inventory.background))
    inventory.add("stripe", context.serialize(src.inventory.stripe))
    inventory.addProperty("warn", src.inventory.warn)

    val editor = JsonObject()
    val recents = JsonObject()
    recents.addProperty("count", src.editor.recents.count)
    val recentFiles = JsonArray()
    src.editor.recents.files.foreach(recentFiles.add)
    recents.add("files", recentFiles)
    editor.add("recents", recents)
    val categories = JsonObject()
    val presetCategories = JsonArray()
    src.editor.categories.presets.foreach((c) => presetCategories.add(context.serialize(c)))
    categories.add("presets", presetCategories)
    categories.addProperty("rows", src.editor.categories.rows)
    categories.addProperty("explicits", src.editor.categories.explicits)
    editor.add("categories", categories)
    val editorColumns = JsonArray()
    src.editor.columns.foreach((c) => editorColumns.add(context.serialize(c, attributeType)))
    editor.add("columns", editorColumns)
    editor.add("stripe", context.serialize(src.editor.stripe))
    val hand = JsonObject()
    hand.addProperty("size", src.editor.hand.size)
    hand.addProperty("rounding", src.editor.hand.rounding)
    hand.add("background", context.serialize(src.editor.hand.background))
    editor.add("hand", hand)
    val legality = JsonObject()
    legality.addProperty("searchForCommander", src.editor.legality.searchForCommander)
    legality.addProperty("main", src.editor.legality.main)
    legality.addProperty("all", src.editor.legality.all)
    legality.addProperty("list", src.editor.legality.list)
    legality.addProperty("sideboard", src.editor.legality.sideboard)
    editor.add("legality", legality)
    editor.addProperty("manaValue", src.editor.manaValue)
    val backFaceLands = JsonArray()
    src.editor.backFaceLands.foreach((l) => backFaceLands.add(l.toString))
    editor.add("backFaceLands", backFaceLands)
    val manaAnalysis = JsonObject()
    manaAnalysis.add("none", context.serialize(src.editor.manaAnalysis.none))
    manaAnalysis.add("colorless", context.serialize(src.editor.manaAnalysis.colorless))
    manaAnalysis.add("white", context.serialize(src.editor.manaAnalysis.white))
    manaAnalysis.add("blue", context.serialize(src.editor.manaAnalysis.blue))
    manaAnalysis.add("black", context.serialize(src.editor.manaAnalysis.black))
    manaAnalysis.add("red", context.serialize(src.editor.manaAnalysis.red))
    manaAnalysis.add("green", context.serialize(src.editor.manaAnalysis.green))
    manaAnalysis.add("multi", context.serialize(src.editor.manaAnalysis.multi))
    manaAnalysis.add("creature", context.serialize(src.editor.manaAnalysis.creature))
    manaAnalysis.add("artifact", context.serialize(src.editor.manaAnalysis.artifact))
    manaAnalysis.add("enchantment", context.serialize(src.editor.manaAnalysis.enchantment))
    manaAnalysis.add("planeswalker", context.serialize(src.editor.manaAnalysis.planeswalker))
    manaAnalysis.add("instant", context.serialize(src.editor.manaAnalysis.instant))
    manaAnalysis.add("sorcery", context.serialize(src.editor.manaAnalysis.sorcery))
    manaAnalysis.add("line", context.serialize(src.editor.manaAnalysis.line))
    editor.add("manaAnalysis", manaAnalysis)

    settings.add("inventory", inventory)
    settings.add("editor", editor)
    settings.addProperty("cwd", src.cwd)

    settings
  }
}
