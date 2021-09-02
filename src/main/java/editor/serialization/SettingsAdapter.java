package editor.serialization;

import java.awt.Color;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import scala.jdk.javaapi.CollectionConverters;

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
import editor.gui.settings.InventorySettings;
import editor.gui.settings.EditorSettings;
import editor.gui.settings.RecentsSettings;
import editor.gui.settings.CategoriesSettings;
import editor.gui.settings.HandSettings;
import editor.gui.settings.LegalitySettings;
import editor.gui.settings.ManaAnalysisSettings;

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
        Settings defaults = new Settings();
        JsonObject obj = json.getAsJsonObject();

        InventorySettings inventorySettings = defaults.inventory();
        if (obj.has("inventory"))
        {
            JsonObject inventory = obj.get("inventory").getAsJsonObject();

            String source = inventory.has("source") ? inventory.get("source").getAsString() : defaults.inventory().source();
            String file = inventory.has("file") ? inventory.get("file").getAsString() : defaults.inventory().file();
            String versionFile = inventory.has("versionFile") ? inventory.get("versionFile").getAsString() : defaults.inventory().versionFile();
            DatabaseVersion version = inventory.has("version") ? context.deserialize(inventory.get("version"), DatabaseVersion.class) : defaults.inventory().version();
            String location = inventory.has("location") ? inventory.get("location").getAsString() : defaults.inventory().location();
            String scans = inventory.has("scans") ? inventory.get("scans").getAsString() : defaults.inventory().scans();
            String imageSource = inventory.has("imageSource") ? inventory.get("imageSource").getAsString() : defaults.inventory().imageSource();
            boolean imageLimitEnable = inventory.has("imageLimitEnable") ? inventory.get("imageLimitEnable").getAsBoolean() : defaults.inventory().imageLimitEnable();
            int imageLimit = inventory.has("imageLimit") ? inventory.get("imageLimit").getAsInt() : defaults.inventory().imageLimit();
            String tags = inventory.has("tags") ? inventory.get("tags").getAsString() : defaults.inventory().tags();
            UpdateFrequency update = inventory.has("update") ? context.deserialize(inventory.get("update"), UpdateFrequency.class) : defaults.inventory().update();
            ArrayList<CardAttribute> columns = new ArrayList<>();
            if (inventory.has("columns"))
            {
                JsonArray inventoryColumnsJson = inventory.get("columns").getAsJsonArray();
                for (var column : inventoryColumnsJson)
                    columns.add(context.deserialize(column, CardAttribute.class));
            }
            else
                columns.addAll(CollectionConverters.asJava(defaults.inventory().columns()));
            Color background = inventory.has("background") ? context.deserialize(inventory.get("background"), Color.class) : defaults.inventory().background();
            Color stripe = inventory.has("stripe") ? context.deserialize(inventory.get("stripe"), Color.class) : defaults.inventory().stripe();
            boolean warn = inventory.has("warn") ? inventory.get("warn").getAsBoolean() : defaults.inventory().warn();

            inventorySettings = new InventorySettings(
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
                (UpdateFrequency)update,
                warn,
                CollectionConverters.asScala(columns).toSeq(),
                background,
                stripe
            );
        }

        EditorSettings editorSettings = defaults.editor();
        if (obj.has("editor"))
        {
            JsonObject editor = obj.get("editor").getAsJsonObject();

            RecentsSettings recentsSettings = defaults.editor().recents();
            if (editor.has("recents"))
            {
                JsonObject recents = editor.get("recents").getAsJsonObject();

                int count = recents.has("count") ? recents.get("count").getAsInt() : defaults.editor().recents().count();
                ArrayList<String> recentsFiles = new ArrayList<>();
                if (recents.has("files"))
                {
                    JsonArray recentsJson = recents.get("files").getAsJsonArray();
                    for (var file : recentsJson)
                        recentsFiles.add(file.getAsString());
                }
                else
                    recentsFiles.addAll(CollectionConverters.asJava(defaults.editor().recents().files()));

                recentsSettings = new RecentsSettings(count, CollectionConverters.asScala(recentsFiles).toSeq());
            }

            CategoriesSettings categoriesSettings = defaults.editor().categories();
            if (editor.has("categories"))
            {
                JsonObject categories = editor.get("categories").getAsJsonObject();

                ArrayList<Category> presets = new ArrayList<Category>();
                if (categories.has("presets"))
                {
                    JsonArray presetsJson = categories.get("presets").getAsJsonArray();
                    for (var preset : presetsJson)
                        presets.add(context.deserialize(preset, Category.class));
                }
                else
                    presets.addAll(CollectionConverters.asJava(defaults.editor().categories().presets()));
                int rows = categories.has("rows") ? categories.get("rows").getAsInt() : defaults.editor().categories().rows();
                int explicits = categories.has("explicits") ? categories.get("explicits").getAsInt() : defaults.editor().categories().explicits();

                categoriesSettings = new CategoriesSettings(CollectionConverters.asScala(presets).toSeq(), rows, explicits);
            }

            HandSettings handSettings = defaults.editor().hand();
            if (editor.has("hand"))
            {
                JsonObject hand = editor.get("hand").getAsJsonObject();

                int size = hand.has("size") ? hand.get("size").getAsInt() : defaults.editor().hand().size();
                String rounding = hand.has("rounding") ? hand.get("rounding").getAsString() : defaults.editor().hand().rounding();
                Color bg = hand.has("background") ? context.deserialize(hand.get("background"), Color.class) : defaults.editor().hand().background();

                handSettings = new HandSettings(size, rounding, bg);
            }

            LegalitySettings legalitySettings = defaults.editor().legality();
            if (editor.has("legality"))
            {
                JsonObject legality = editor.get("legality").getAsJsonObject();

                boolean search = legality.has("searchForCommander") ? legality.get("searchForCommander").getAsBoolean() : defaults.editor().legality().searchForCommander();
                boolean main = legality.has("main") ? legality.get("main").getAsBoolean() : defaults.editor().legality().main();
                boolean all = legality.has("all") ? legality.get("all").getAsBoolean() : defaults.editor().legality().all();
                String list = legality.has("list") ? legality.get("list").getAsString() : defaults.editor().legality().list();
                String sideboard = legality.has("sideboard") ? legality.get("sideboard").getAsString() : defaults.editor().legality().sideboard();

                legalitySettings = new LegalitySettings(search, main, all, list, sideboard);
            }

            ArrayList<CardAttribute> columns = new ArrayList<>();
            if (editor.has("columns"))
            {
                JsonArray editorColumnsJson = editor.get("columns").getAsJsonArray();
                for (var column : editorColumnsJson)
                    columns.add(context.deserialize(column, CardAttribute.class));
            }
            else
                columns.addAll(CollectionConverters.asJava(defaults.editor().columns()));
            Color stripe = editor.has("stripe") ? context.deserialize(editor.get("stripe"), Color.class) : defaults.editor().stripe();
            String mv = editor.has("manaValue") ? editor.get("manaValue").getAsString() : defaults.editor().manaValue();
            HashSet<CardLayout> backFaceLands = new HashSet<>();
            if (editor.has("backFaceLands"))
            {
                JsonArray backFaceLandsJson = editor.get("backFaceLands").getAsJsonArray();
                for (var layout : backFaceLandsJson)
                    backFaceLands.add(Arrays.stream(CardLayout.values()).filter((l) -> l.toString().equals(layout.getAsString())).findAny().get());
            }
            else
                backFaceLands.addAll(CollectionConverters.asJava(defaults.editor().backFaceLands()));

            ManaAnalysisSettings manaAnalysisSettings = defaults.editor().manaAnalysis();
            if (editor.has("manaAnalysis"))
            {
                JsonObject manaAnalysis = editor.get("manaAnalysis").getAsJsonObject();
                
                Color none = manaAnalysis.has("none") ? context.deserialize(manaAnalysis.get("none"), Color.class) : defaults.editor().manaAnalysis().none();
                Color colorless = manaAnalysis.has("colorless") ? context.deserialize(manaAnalysis.get("colorless"), Color.class) : defaults.editor().manaAnalysis().colorless();
                Color white = manaAnalysis.has("white") ? context.deserialize(manaAnalysis.get("white"), Color.class) : defaults.editor().manaAnalysis().white();
                Color blue = manaAnalysis.has("blue") ? context.deserialize(manaAnalysis.get("blue"), Color.class) : defaults.editor().manaAnalysis().blue();
                Color black = manaAnalysis.has("black") ? context.deserialize(manaAnalysis.get("black"), Color.class) : defaults.editor().manaAnalysis().black();
                Color red = manaAnalysis.has("red") ? context.deserialize(manaAnalysis.get("red"), Color.class) : defaults.editor().manaAnalysis().red();
                Color green = manaAnalysis.has("green") ? context.deserialize(manaAnalysis.get("green"), Color.class) : defaults.editor().manaAnalysis().green();
                Color multi = manaAnalysis.has("multi") ? context.deserialize(manaAnalysis.get("multi"), Color.class) : defaults.editor().manaAnalysis().multi();
                Color creature = manaAnalysis.has("creature") ? context.deserialize(manaAnalysis.get("creature"), Color.class) : defaults.editor().manaAnalysis().creature();
                Color artifact = manaAnalysis.has("artifact") ? context.deserialize(manaAnalysis.get("artifact"), Color.class) : defaults.editor().manaAnalysis().artifact();
                Color enchantment = manaAnalysis.has("enchantment") ? context.deserialize(manaAnalysis.get("enchantment"), Color.class) : defaults.editor().manaAnalysis().enchantment();
                Color planeswalker = manaAnalysis.has("planeswalker") ? context.deserialize(manaAnalysis.get("planeswalker"), Color.class) : defaults.editor().manaAnalysis().planeswalker();
                Color instant = manaAnalysis.has("instant") ? context.deserialize(manaAnalysis.get("instant"), Color.class) : defaults.editor().manaAnalysis().instant();
                Color sorcery = manaAnalysis.has("sorcery") ? context.deserialize(manaAnalysis.get("sorcery"), Color.class) : defaults.editor().manaAnalysis().sorcery();
                Color line = manaAnalysis.has("line") ? context.deserialize(manaAnalysis.get("line"), Color.class) : defaults.editor().manaAnalysis().line();

                manaAnalysisSettings = new ManaAnalysisSettings(
                    none,
                    colorless, white, blue, black, red, green, multi,
                    creature, artifact, enchantment, planeswalker, instant, sorcery,
                    line
                );
            }

            editorSettings = new EditorSettings(
                recentsSettings,
                categoriesSettings,
                CollectionConverters.asScala(columns).toSeq(),
                stripe,
                handSettings,
                legalitySettings,
                mv,
                CollectionConverters.asScala(backFaceLands).toSet(),
                manaAnalysisSettings
            );
        }

        String cwd = obj.has("cwd") ? obj.get("cwd").getAsString() : defaults.cwd();

        return new Settings(inventorySettings, editorSettings, cwd);
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
        for (CardAttribute column : CollectionConverters.asJava(src.inventory().columns()))
            inventoryColumns.add(context.serialize(column));
        inventory.add("columns", inventoryColumns);
        inventory.add("background", context.serialize(src.inventory().background()));
        inventory.add("stripe", context.serialize(src.inventory().stripe()));
        inventory.addProperty("warn", src.inventory().warn());

        JsonObject editor = new JsonObject();
        JsonObject recents = new JsonObject();
        recents.addProperty("count", src.editor().recents().count());
        JsonArray recentFiles = new JsonArray();
        for (String file : CollectionConverters.asJava(src.editor().recents().files()))
            recentFiles.add(file);
        recents.add("files", recentFiles);
        editor.add("recents", recents);
        JsonObject categories = new JsonObject();
        JsonArray presetCategories = new JsonArray();
        for (Category category : CollectionConverters.asJava(src.editor().categories().presets()))
            presetCategories.add(context.serialize(category));
        categories.add("presets", presetCategories);
        categories.addProperty("rows", src.editor().categories().rows());
        categories.addProperty("explicits", src.editor().categories().explicits());
        editor.add("categories", categories);
        JsonArray editorColumns = new JsonArray();
        for (CardAttribute column : CollectionConverters.asJava(src.editor().columns()))
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
        for (CardLayout layout : CollectionConverters.asJava(src.editor().backFaceLands()))
            backFaceLands.add(layout.toString());
        editor.add("backFaceLands", backFaceLands);
        JsonObject manaAnalysis = new JsonObject();
        manaAnalysis.add("none", context.serialize(src.editor().manaAnalysis().none()));
        manaAnalysis.add("colorless", context.serialize(src.editor().manaAnalysis().colorless()));
        manaAnalysis.add("white", context.serialize(src.editor().manaAnalysis().white()));
        manaAnalysis.add("blue", context.serialize(src.editor().manaAnalysis().blue()));
        manaAnalysis.add("black", context.serialize(src.editor().manaAnalysis().black()));
        manaAnalysis.add("red", context.serialize(src.editor().manaAnalysis().red()));
        manaAnalysis.add("green", context.serialize(src.editor().manaAnalysis().green()));
        manaAnalysis.add("multi", context.serialize(src.editor().manaAnalysis().multi()));
        manaAnalysis.add("creature", context.serialize(src.editor().manaAnalysis().creature()));
        manaAnalysis.add("artifact", context.serialize(src.editor().manaAnalysis().artifact()));
        manaAnalysis.add("enchantment", context.serialize(src.editor().manaAnalysis().enchantment()));
        manaAnalysis.add("planeswalker", context.serialize(src.editor().manaAnalysis().planeswalker()));
        manaAnalysis.add("instant", context.serialize(src.editor().manaAnalysis().instant()));
        manaAnalysis.add("sorcery", context.serialize(src.editor().manaAnalysis().sorcery()));
        manaAnalysis.add("line", context.serialize(src.editor().manaAnalysis().line()));
        editor.add("manaAnalysis", manaAnalysis);

        settings.add("inventory", inventory);
        settings.add("editor", editor);
        settings.addProperty("cwd", src.cwd());

        return settings;
    }
}
