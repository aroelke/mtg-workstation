package editor.serialization;

import java.awt.Color;
import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import editor.collection.deck.CategorySpec;
import editor.database.attributes.CardAttribute;
import editor.database.version.DatabaseVersion;
import editor.database.version.UpdateFrequency;
import editor.gui.settings.Settings;
import editor.gui.settings.SettingsBuilder;

/**
 * Type adapter for serializing and deserializing the {@link Settings} structure to and from JSON format, since Gson isn't compatible with
 * Java 16 records yet.
 * 
 * @author Alec Roelke
 * 
 * @see Settings
 */
public class SettingsAdapter implements JsonSerializer<Settings>, JsonDeserializer<Settings>
{
    @Override
    public Settings deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject obj = json.getAsJsonObject();
        JsonObject inventory = obj.get("inventory").getAsJsonObject();
        JsonObject editor = obj.get("editor").getAsJsonObject();
        JsonObject recents = editor.get("recents").getAsJsonObject();
        JsonObject categories = editor.get("categories").getAsJsonObject();
        JsonObject hand = editor.get("hand").getAsJsonObject();
        JsonObject legality = editor.get("legality").getAsJsonObject();

        JsonArray inventoryColumnsJson = inventory.get("columns").getAsJsonArray();
        var inventoryColumns = new ArrayList<CardAttribute>(inventoryColumnsJson.size());
        for (var column : inventoryColumnsJson)
            inventoryColumns.add(context.deserialize(column, CardAttribute.class));

        JsonArray recentsJson = recents.get("files").getAsJsonArray();
        var recentsFiles = new ArrayList<String>(recentsJson.size());
        for (var file : recentsJson)
            recentsFiles.add(file.getAsString());

        JsonArray presetsJson = categories.get("presets").getAsJsonArray();
        var presets = new ArrayList<CategorySpec>(presetsJson.size());
        for (var preset : presetsJson)
            presets.add(context.deserialize(preset, CategorySpec.class));
        
        JsonArray editorColumnsJson = editor.get("columns").getAsJsonArray();
        var editorColumns = new ArrayList<CardAttribute>(editorColumnsJson.size());
        for (var column : editorColumnsJson)
            editorColumns.add(context.deserialize(column, CardAttribute.class));

        return new SettingsBuilder()
            .inventorySource(inventory.get("source").getAsString())
            .inventoryFile(inventory.get("file").getAsString())
            .inventoryVersionFile(inventory.get("versionFile").getAsString())
            .inventoryVersion(context.deserialize(inventory.get("version"), DatabaseVersion.class))
            .inventoryLocation(inventory.get("location").getAsString())
            .inventoryScans(inventory.get("scans").getAsString())
            .imageSource(inventory.get("imageSource").getAsString())
            .imageLimitEnable(inventory.get("imageLimitEnable").getAsBoolean())
            .imageLimit(inventory.get("imageLimit").getAsInt())
            .inventoryTags(inventory.get("tags").getAsString())
            .inventoryUpdate(context.deserialize(inventory.get("update"), UpdateFrequency.class))
            .inventoryColumns(inventoryColumns)
            .inventoryBackground(context.deserialize(inventory.get("background"), Color.class))
            .inventoryStripe(context.deserialize(inventory.get("stripe"), Color.class))
            .inventoryWarn(inventory.get("warn").getAsBoolean())
            .recentsCount(recents.get("count").getAsInt())
            .recentsFiles(recentsFiles)
            .presetCategories(presets)
            .categoryRows(categories.get("rows").getAsInt())
            .explicits(categories.get("explicits").getAsInt())
            .editorColumns(editorColumns)
            .editorStripe(context.deserialize(editor.get("stripe"), Color.class))
            .handSize(hand.get("size").getAsInt())
            .handRounding(hand.get("rounding").getAsString())
            .handBackground(context.deserialize(hand.get("background"), Color.class))
            .searchForCommander(legality.get("searchForCommander").getAsBoolean())
            .commanderInMain(legality.get("main").getAsBoolean())
            .commanderInAll(legality.get("all").getAsBoolean())
            .commanderInList(legality.get("list").getAsString())
            .sideboardName(legality.get("sideboard").getAsString())
            .manaValue(editor.get("manaValue").getAsString())
            .cwd(obj.get("cwd").getAsString())
            .build();
    }

    @Override
    public JsonElement serialize(Settings src, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonObject settings = new JsonObject();

        JsonObject inventory = new JsonObject();
        inventory.addProperty("source", src.inventory.source());
        inventory.addProperty("file", src.inventory.file());
        inventory.addProperty("versionFile", src.inventory.versionFile());
        inventory.add("version", context.serialize(src.inventory.version()));
        inventory.addProperty("location", src.inventory.location());
        inventory.addProperty("scans", src.inventory.scans());
        inventory.addProperty("imageSource", src.inventory.imageSource());
        inventory.addProperty("imageLimitEnable", src.inventory.imageLimitEnable());
        inventory.addProperty("imageLimit", src.inventory.imageLimit());
        inventory.addProperty("tags", src.inventory.tags());
        inventory.add("update", context.serialize(src.inventory.update()));
        JsonArray inventoryColumns = new JsonArray();
        for (CardAttribute column : src.inventory.columns())
            inventoryColumns.add(context.serialize(column));
        inventory.add("columns", inventoryColumns);
        inventory.add("background", context.serialize(src.inventory.background()));
        inventory.add("stripe", context.serialize(src.inventory.stripe()));
        inventory.addProperty("warn", src.inventory.warn());

        JsonObject editor = new JsonObject();
        JsonObject recents = new JsonObject();
        recents.addProperty("count", src.editor.recents().count());
        JsonArray recentFiles = new JsonArray();
        for (String file : src.editor.recents().files())
            recentFiles.add(file);
        recents.add("files", recentFiles);
        editor.add("recents", recents);
        JsonObject categories = new JsonObject();
        JsonArray presetCategories = new JsonArray();
        for (CategorySpec category : src.editor.categories().presets())
            presetCategories.add(context.serialize(category));
        categories.add("presets", presetCategories);
        categories.addProperty("rows", src.editor.categories().rows());
        categories.addProperty("explicits", src.editor.categories().explicits());
        editor.add("categories", categories);
        JsonArray editorColumns = new JsonArray();
        for (CardAttribute column : src.editor.columns())
            editorColumns.add(context.serialize(column));
        editor.add("columns", editorColumns);
        editor.add("stripe", context.serialize(src.editor.stripe()));
        JsonObject hand = new JsonObject();
        hand.addProperty("size", src.editor.hand().size());
        hand.addProperty("rounding", src.editor.hand().rounding());
        hand.add("background", context.serialize(src.editor.hand().background()));
        editor.add("hand", hand);
        JsonObject legality = new JsonObject();
        legality.addProperty("searchForCommander", src.editor.legality().searchForCommander());
        legality.addProperty("main", src.editor.legality().main());
        legality.addProperty("all", src.editor.legality().all());
        legality.addProperty("list", src.editor.legality().list());
        legality.addProperty("sideboard", src.editor.legality().sideboard());
        editor.add("legality", legality);
        editor.addProperty("manaValue", src.editor.manaValue());

        settings.add("inventory", inventory);
        settings.add("editor", editor);
        settings.addProperty("cwd", src.cwd);

        return settings;
    }
}
