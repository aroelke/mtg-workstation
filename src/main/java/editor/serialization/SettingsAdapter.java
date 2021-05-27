package editor.serialization;

import java.awt.Color;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import editor.collection.deck.Category;
import editor.database.attributes.CardAttribute;
import editor.database.card.CardLayout;
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
        SettingsBuilder builder = new SettingsBuilder().defaults();
        JsonObject obj = json.getAsJsonObject();

        if (obj.has("inventory"))
        {
            JsonObject inventory = obj.get("inventory").getAsJsonObject();

            if (inventory.has("source"))
                builder = builder.inventorySource(inventory.get("source").getAsString());
            if (inventory.has("file"))
                builder = builder.inventoryFile(inventory.get("file").getAsString());
            if (inventory.has("versionFile"))
                builder = builder.inventoryVersionFile(inventory.get("versionFile").getAsString());
            if (inventory.has("version"))
                builder = builder.inventoryVersion(context.deserialize(inventory.get("version"), DatabaseVersion.class));
            if (inventory.has("location"))
                builder = builder.inventoryLocation(inventory.get("location").getAsString());
            if (inventory.has("scans"))
                builder = builder.inventoryScans(inventory.get("scans").getAsString());
            if (inventory.has("imageSource"))
                builder = builder.imageSource(inventory.get("imageSource").getAsString());
            if (inventory.has("imageLimitEnable"))
                builder = builder.imageLimitEnable(inventory.get("imageLimitEnable").getAsBoolean());
            if (inventory.has("imageLimit"))
                builder = builder.imageLimit(inventory.get("imageLimit").getAsInt());
            if (inventory.has("tags"))
                builder = builder.inventoryTags(inventory.get("tags").getAsString());
            if (inventory.has("update"))
                builder = builder.inventoryUpdate(context.deserialize(inventory.get("update"), UpdateFrequency.class));
            if (inventory.has("columns"))
            {
                JsonArray inventoryColumnsJson = inventory.get("columns").getAsJsonArray();
                var inventoryColumns = new ArrayList<CardAttribute>(inventoryColumnsJson.size());
                for (var column : inventoryColumnsJson)
                    inventoryColumns.add(context.deserialize(column, CardAttribute.class));
                builder = builder.inventoryColumns(inventoryColumns);
            }
            if (inventory.has("background"))
                builder = builder.inventoryBackground(context.deserialize(inventory.get("background"), Color.class));
            if (inventory.has("stripe"))
                builder = builder.inventoryStripe(context.deserialize(inventory.get("stripe"), Color.class));
            if (inventory.has("warn"))
                builder = builder.inventoryWarn(inventory.get("warn").getAsBoolean());
        }

        if (obj.has("editor"))
        {
            JsonObject editor = obj.get("editor").getAsJsonObject();

            if (editor.has("recents"))
            {
                JsonObject recents = editor.get("recents").getAsJsonObject();

                if (recents.has("count"))
                    builder = builder.recentsCount(recents.get("count").getAsInt());
                if (recents.has("files"))
                {
                    JsonArray recentsJson = recents.get("files").getAsJsonArray();
                    var recentsFiles = new ArrayList<String>(recentsJson.size());
                    for (var file : recentsJson)
                        recentsFiles.add(file.getAsString());
                    builder = builder.recentsFiles(recentsFiles);
                }
            }

            if (editor.has("categories"))
            {
                JsonObject categories = editor.get("categories").getAsJsonObject();

                if (categories.has("presets"))
                {
                    JsonArray presetsJson = categories.get("presets").getAsJsonArray();
                    var presets = new ArrayList<Category>(presetsJson.size());
                    for (var preset : presetsJson)
                        presets.add(context.deserialize(preset, Category.class));
                    builder = builder.presetCategories(presets);
                }
                if (categories.has("rows"))
                    builder = builder.categoryRows(categories.get("rows").getAsInt());
                if (categories.has("explicits"))
                    builder = builder.explicits(categories.get("explicits").getAsInt());
            }

            if (editor.has("hand"))
            {
                JsonObject hand = editor.get("hand").getAsJsonObject();

                if (hand.has("size"))
                    builder = builder.handSize(hand.get("size").getAsInt());
                if (hand.has("rounding"))
                    builder = builder.handRounding(hand.get("rounding").getAsString());
                if (hand.has("background"))
                    builder = builder.handBackground(context.deserialize(hand.get("background"), Color.class));
            }

            if (editor.has("legality"))
            {
                JsonObject legality = editor.get("legality").getAsJsonObject();

                if (legality.has("searchForCommander"))
                    builder = builder.searchForCommander(legality.get("searchForCommander").getAsBoolean());
                if (legality.has("main"))
                    builder = builder.commanderInMain(legality.get("main").getAsBoolean());
                if (legality.has("all"))
                    builder = builder.commanderInAll(legality.get("all").getAsBoolean());
                if (legality.has("list"))
                    builder = builder.commanderInList(legality.get("list").getAsString());
                if (legality.has("sideboard"))
                    builder = builder.sideboardName(legality.get("sideboard").getAsString());
            }

            if (editor.has("columns"))
            {
                JsonArray editorColumnsJson = editor.get("columns").getAsJsonArray();
                var editorColumns = new ArrayList<CardAttribute>(editorColumnsJson.size());
                for (var column : editorColumnsJson)
                    editorColumns.add(context.deserialize(column, CardAttribute.class));
                builder = builder.editorColumns(editorColumns);
            }
            if (editor.has("stripe"))
                builder = builder.editorStripe(context.deserialize(editor.get("stripe"), Color.class));
            if (editor.has("manaValue"))
                builder = builder.manaValue(editor.get("manaValue").getAsString());
            if (editor.has("backFaceLands"))
            {
                JsonArray backFaceLandsJson = editor.get("backFaceLands").getAsJsonArray();
                var backFaceLands = new HashSet<CardLayout>(backFaceLandsJson.size());
                for (var layout : backFaceLandsJson)
                    backFaceLands.add(Arrays.stream(CardLayout.values()).filter((l) -> l.toString().equals(layout.getAsString())).findAny().get());
                builder = builder.backFaceLands(backFaceLands);
            }
        }

        if (obj.has("cwd"))
            builder = builder.cwd(obj.get("cwd").getAsString());

        return builder.build();
    }

    @Override
    public JsonElement serialize(Settings src, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonObject settings = new JsonObject();

        JsonObject inventory = new JsonObject();
        inventory.addProperty("source", src.inventory().source());
        inventory.addProperty("file", src.inventory().file());
        inventory.addProperty("versionFile", src.inventory().versionFile());
        inventory.add("version", context.serialize(src.inventory().version()));
        inventory.addProperty("location", src.inventory().location());
        inventory.addProperty("scans", src.inventory().scans());
        inventory.addProperty("imageSource", src.inventory().imageSource());
        inventory.addProperty("imageLimitEnable", src.inventory().imageLimitEnable());
        inventory.addProperty("imageLimit", src.inventory().imageLimit());
        inventory.addProperty("tags", src.inventory().tags());
        inventory.add("update", context.serialize(src.inventory().update()));
        JsonArray inventoryColumns = new JsonArray();
        for (CardAttribute column : src.inventory().columns())
            inventoryColumns.add(context.serialize(column));
        inventory.add("columns", inventoryColumns);
        inventory.add("background", context.serialize(src.inventory().background()));
        inventory.add("stripe", context.serialize(src.inventory().stripe()));
        inventory.addProperty("warn", src.inventory().warn());

        JsonObject editor = new JsonObject();
        JsonObject recents = new JsonObject();
        recents.addProperty("count", src.editor().recents().count());
        JsonArray recentFiles = new JsonArray();
        for (String file : src.editor().recents().files())
            recentFiles.add(file);
        recents.add("files", recentFiles);
        editor.add("recents", recents);
        JsonObject categories = new JsonObject();
        JsonArray presetCategories = new JsonArray();
        for (Category category : src.editor().categories().presets())
            presetCategories.add(context.serialize(category));
        categories.add("presets", presetCategories);
        categories.addProperty("rows", src.editor().categories().rows());
        categories.addProperty("explicits", src.editor().categories().explicits());
        editor.add("categories", categories);
        JsonArray editorColumns = new JsonArray();
        for (CardAttribute column : src.editor().columns())
            editorColumns.add(context.serialize(column));
        editor.add("columns", editorColumns);
        editor.add("stripe", context.serialize(src.editor().stripe()));
        JsonObject hand = new JsonObject();
        hand.addProperty("size", src.editor().hand().size());
        hand.addProperty("rounding", src.editor().hand().rounding());
        hand.add("background", context.serialize(src.editor().hand().background()));
        editor.add("hand", hand);
        JsonObject legality = new JsonObject();
        legality.addProperty("searchForCommander", src.editor().legality().searchForCommander());
        legality.addProperty("main", src.editor().legality().main());
        legality.addProperty("all", src.editor().legality().all());
        legality.addProperty("list", src.editor().legality().list());
        legality.addProperty("sideboard", src.editor().legality().sideboard());
        editor.add("legality", legality);
        editor.addProperty("manaValue", src.editor().manaValue());
        JsonArray backFaceLands = new JsonArray();
        for (CardLayout layout : src.editor().backFaceLands())
            backFaceLands.add(layout.toString());
        editor.add("backFaceLands", backFaceLands);

        settings.add("inventory", inventory);
        settings.add("editor", editor);
        settings.addProperty("cwd", src.cwd());

        return settings;
    }
}
