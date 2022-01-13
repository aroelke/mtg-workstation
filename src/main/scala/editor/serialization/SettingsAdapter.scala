package editor.serialization

import com.google.gson.JsonSerializer
import editor.gui.settings.Settings
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import com.google.gson.JsonDeserializationContext
import editor.database.attributes.CardAttribute
import scala.jdk.CollectionConverters._
import editor.database.version.DatabaseVersion
import editor.database.version.UpdateFrequency
import java.awt.Color
import editor.gui.settings.InventorySettings
import editor.gui.settings.RecentsSettings
import editor.gui.settings.CategoriesSettings
import editor.gui.settings.HandSettings
import editor.gui.settings.LegalitySettings
import editor.gui.settings.ManaAnalysisSettings
import editor.gui.settings.EditorSettings
import editor.collection.deck.Category
import editor.database.card.CardLayout
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonObject
import com.google.gson.JsonArray

class SettingsAdapter extends JsonSerializer[Settings] with JsonDeserializer[Settings] {
  override def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext) = {
    val defaults = Settings()
    val obj = json.getAsJsonObject

    val inventorySettings = if (obj.has("inventory")) {
      val inventory = obj.get("inventory").getAsJsonObject

      val source = if (inventory.has("source")) inventory.get("source").getAsString else  defaults.inventory.source
      val file = if (inventory.has("file")) inventory.get("file").getAsString else defaults.inventory.file
      val versionFile = if (inventory.has("versionFile")) inventory.get("versionFile").getAsString else defaults.inventory.versionFile
      val version = if (inventory.has("version")) context.deserialize(inventory.get("version"), classOf[DatabaseVersion]) else defaults.inventory.version
      val location = if (inventory.has("location")) inventory.get("location").getAsString else defaults.inventory.location
      val scans = if (inventory.has("scans")) inventory.get("scans").getAsString else defaults.inventory.scans
      val imageSource = if (inventory.has("imageSource")) inventory.get("imageSource").getAsString else defaults.inventory.imageSource
      val imageLimitEnable = if (inventory.has("imageLimitEnable")) inventory.get("imageLimitEnable").getAsBoolean else defaults.inventory.imageLimitEnable
      val imageLimit = if (inventory.has("imageLimit")) inventory.get("imageLimit").getAsInt else defaults.inventory.imageLimit
      val tags = if (inventory.has("tags")) inventory.get("tags").getAsString else defaults.inventory.tags
      val update = if (inventory.has("update")) context.deserialize(inventory.get("update"), classOf[UpdateFrequency]) else defaults.inventory.update
      val columns = collection.mutable.Buffer[CardAttribute]()
      if (inventory.has("columns"))
        inventory.get("columns").getAsJsonArray.asScala.foreach((c) => columns += context.deserialize(c, classOf[CardAttribute]))
      else
        columns ++= defaults.inventory.columns
      val background = if (inventory.has("background")) context.deserialize(inventory.get("background"), classOf[Color]) else defaults.inventory.background
      val stripe = if (inventory.has("stripe")) context.deserialize(inventory.get("stripe"), classOf[Color]) else defaults.inventory.stripe
      val warn = if (inventory.has("warn")) inventory.get("warn").getAsBoolean else defaults.inventory.warn

      InventorySettings(
        source,
        file,
        versionFile,
        version,
        location,
        scans,
        imageSource,
        imageLimitEnable,
        imageLimit,
        tags,
        update,
        warn,
        columns.toSeq,
        background,
        stripe
      )
    } else defaults.inventory

    val editorSettings = if (obj.has("editor")) {
      val editor = obj.get("editor").getAsJsonObject

      val recentsSettings = if (editor.has("recents")) {
        val recents = editor.get("recents").getAsJsonObject

        val count = if (recents.has("count")) recents.get("count").getAsInt else defaults.editor.recents.count
        val recentsFiles = collection.mutable.Buffer[String]()
        if (recents.has("files"))
          recents.get("files").getAsJsonArray.asScala.foreach(recentsFiles += _.getAsString)
        else
          recentsFiles ++= defaults.editor.recents.files
        
        RecentsSettings(count, recentsFiles.toSeq)
      } else defaults.editor.recents

      val categoriesSettings = if (editor.has("categories")) {
        val categories = editor.get("categories").getAsJsonObject

        val presets = collection.mutable.Buffer[Category]()
        if (categories.has("presets"))
          categories.get("presets").getAsJsonArray.asScala.foreach((p) => presets += context.deserialize(p, classOf[Category]))
        else
          presets ++= defaults.editor.categories.presets
        val rows = if (categories.has("rows")) categories.get("rows").getAsInt else defaults.editor.categories.rows
        val explicits = if (categories.has("explicits")) categories.get("explicits").getAsInt else defaults.editor.categories.explicits
        
        CategoriesSettings(presets.toSeq, rows, explicits)
      } else defaults.editor.categories

      val handSettings = if (editor.has("hand")) {
        val hand = editor.get("hand").getAsJsonObject

        val size = if (hand.has("size")) hand.get("size").getAsInt else defaults.editor.hand.size
        val rounding = if (hand.has("rounding")) hand.get("rounding").getAsString else defaults.editor.hand.rounding
        val bg = if (hand.has("background")) context.deserialize(hand.get("background"), classOf[Color]) else defaults.editor.hand.background

        HandSettings(size, rounding, bg)
      } else defaults.editor.hand

      val legalitySettings = if (editor.has("legality")) {
        val legality = editor.get("legality").getAsJsonObject

        val search = if (legality.has("searchForCommander")) legality.get("searchForCommander").getAsBoolean else defaults.editor.legality.searchForCommander
        val main = if (legality.has("main")) legality.get("main").getAsBoolean else defaults.editor.legality.main
        val all = if (legality.has("all")) legality.get("all").getAsBoolean else defaults.editor.legality.all
        val list = if (legality.has("list")) legality.get("list").getAsString else defaults.editor.legality.list
        val sideboard = if (legality.has("sideboard")) legality.get("sideboard").getAsString else defaults.editor.legality.sideboard

        LegalitySettings(search, main, all, list, sideboard)
      } else defaults.editor.legality

      val columns = collection.mutable.Buffer[CardAttribute]()
      if (editor.has("columns"))
        editor.get("columns").getAsJsonArray.asScala.foreach((c) => columns += context.deserialize(c, classOf[CardAttribute]))
      else
        columns ++= defaults.editor.columns
      val stripe = if (editor.has("stripe")) context.deserialize(editor.get("stripe"), classOf[Color]) else defaults.editor.stripe
      val mv = if (editor.has("manaValue")) editor.get("manaValue").getAsString else defaults.editor.manaValue
      val backFaceLands = collection.mutable.Set[CardLayout]()
      if (editor.has("backFaceLands"))
        editor.get("backFaceLands").getAsJsonArray.asScala.foreach((l) => backFaceLands += CardLayout.values.filter(_.toString == l.getAsString)(0))
      else
        backFaceLands ++= defaults.editor.backFaceLands

      val manaAnalysisSettings = if (editor.has("manaAnalysis")) {
        val manaAnalysis = editor.get("manaAnalysis").getAsJsonObject

        val none = if (manaAnalysis.has("none")) context.deserialize(manaAnalysis.get("none"), classOf[Color]) else defaults.editor.manaAnalysis.none
        val colorless = if (manaAnalysis.has("colorless")) context.deserialize(manaAnalysis.get("colorless"), classOf[Color]) else defaults.editor.manaAnalysis.colorless
        val white = if (manaAnalysis.has("white")) context.deserialize(manaAnalysis.get("white"), classOf[Color]) else defaults.editor.manaAnalysis.white
        val blue = if (manaAnalysis.has("blue")) context.deserialize(manaAnalysis.get("blue"), classOf[Color]) else defaults.editor.manaAnalysis.blue
        val black = if (manaAnalysis.has("black")) context.deserialize(manaAnalysis.get("black"), classOf[Color]) else defaults.editor.manaAnalysis.black
        val red = if (manaAnalysis.has("red")) context.deserialize(manaAnalysis.get("red"), classOf[Color]) else defaults.editor.manaAnalysis.red
        val green = if (manaAnalysis.has("green")) context.deserialize(manaAnalysis.get("green"), classOf[Color]) else defaults.editor.manaAnalysis.green
        val multi = if (manaAnalysis.has("multi")) context.deserialize(manaAnalysis.get("multi"), classOf[Color]) else defaults.editor.manaAnalysis.multi
        val creature = if (manaAnalysis.has("creature")) context.deserialize(manaAnalysis.get("creature"), classOf[Color]) else defaults.editor.manaAnalysis.creature
        val artifact = if (manaAnalysis.has("artifact")) context.deserialize(manaAnalysis.get("artifact"), classOf[Color]) else defaults.editor.manaAnalysis.artifact
        val enchantment = if (manaAnalysis.has("enchantment")) context.deserialize(manaAnalysis.get("enchantment"), classOf[Color]) else defaults.editor.manaAnalysis.enchantment
        val planeswalker = if (manaAnalysis.has("planeswalker")) context.deserialize(manaAnalysis.get("planeswalker"), classOf[Color]) else defaults.editor.manaAnalysis.planeswalker
        val instant = if (manaAnalysis.has("instant")) context.deserialize(manaAnalysis.get("instant"), classOf[Color]) else defaults.editor.manaAnalysis.instant
        val sorcery = if (manaAnalysis.has("sorcery")) context.deserialize(manaAnalysis.get("sorcery"), classOf[Color]) else defaults.editor.manaAnalysis.sorcery
        val line = if (manaAnalysis.has("line")) context.deserialize(manaAnalysis.get("line"), classOf[Color]) else defaults.editor.manaAnalysis.line

        ManaAnalysisSettings(
          none,
          colorless, white, blue, black, red, green, multi,
          creature, artifact, enchantment, planeswalker, instant, sorcery,
          line
        )
      } else defaults.editor.manaAnalysis

      EditorSettings(
        recentsSettings,
        categoriesSettings,
        columns.toSeq,
        stripe,
        handSettings,
        legalitySettings,
        mv,
        backFaceLands.toSet,
        manaAnalysisSettings
      )
    } else defaults.editor

    val cwd = if (obj.has("cwd")) obj.get("cwd").getAsString else defaults.cwd

    Settings(inventorySettings, editorSettings, cwd)
  }

  override def serialize(src: Settings, typeOfSrc: Type, context: JsonSerializationContext) = {
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
    src.inventory.columns.foreach((c) => inventoryColumns.add(context.serialize(c)))
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
    src.editor.columns.foreach((c) => editorColumns.add(context.serialize(c)))
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
