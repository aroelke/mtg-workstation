package editor.gui.settings;

import static editor.database.attributes.CardAttribute.CATEGORIES;
import static editor.database.attributes.CardAttribute.COUNT;
import static editor.database.attributes.CardAttribute.DATE_ADDED;
import static editor.database.attributes.CardAttribute.EXPANSION;
import static editor.database.attributes.CardAttribute.MANA_COST;
import static editor.database.attributes.CardAttribute.NAME;
import static editor.database.attributes.CardAttribute.TYPE_LINE;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import scala.jdk.javaapi.CollectionConverters;

import editor.collection.deck.Category;
import editor.database.attributes.CardAttribute;
import editor.database.card.Card;
import editor.database.card.CardLayout;
import editor.database.card.MultiCard;
import editor.database.version.DatabaseVersion;
import editor.database.version.UpdateFrequency;
import editor.filter.leaf.options.multi.CardTypeFilter;

/**
 * Structure containing global settings.  This structure consists of immutable
 * members and sub-structures and should be created using
 * {@link SettingsBuilder}.
 * 
 * @param inventory {@link InventorySettings}
 * @param editor {@link EditorSettings}
 * @param cwd initial directory of file choosers
 * 
 * @author Alec Roelke
 * 
 * ~~RECORD~~
 */
public class Settings//(InventorySettings inventory, EditorSettings editor, String cwd)
{
    public final InventorySettings inventory;
    public final EditorSettings editor;
    public final String cwd;

    public Settings(InventorySettings i, EditorSettings e, String c)
    {
        inventory = i;
        editor = e;
        cwd = c;
    }

    protected Settings(String inventorySource, String inventoryFile, String inventoryVersionFile, DatabaseVersion inventoryVersion, String inventoryLocation, String inventoryScans, String imageSource, boolean imageLimitEnable, int imageLimit, String inventoryTags, UpdateFrequency inventoryUpdate, boolean inventoryWarn, List<CardAttribute> inventoryColumns, Color inventoryBackground, Color inventoryStripe, int recentsCount, List<String> recentsFiles, int explicits, List<Category> presetCategories, int categoryRows, List<CardAttribute> editorColumns, Color editorStripe, int handSize, String handRounding, Color handBackground, boolean searchForCommander, boolean main, boolean all, String list, String sideboard, String manaValue, Set<CardLayout> backFaceLands, String cwd, Color none, Color colorless, Color white, Color blue, Color black, Color red, Color green, Color multi, Color creature, Color artifact, Color enchantment, Color planeswalker, Color instant, Color sorcery, Color line)
    {
        this(
            new InventorySettings(inventorySource, inventoryFile, inventoryVersionFile, inventoryVersion, inventoryLocation, inventoryScans, imageSource, imageLimitEnable, imageLimit, inventoryTags, inventoryUpdate, inventoryWarn, CollectionConverters.asScala(inventoryColumns).toSeq(), inventoryBackground, inventoryStripe),
            new EditorSettings(recentsCount, CollectionConverters.asScala(recentsFiles).toSeq(), explicits, CollectionConverters.asScala(presetCategories).toSeq(), categoryRows, CollectionConverters.asScala(editorColumns).toSeq(), editorStripe, handSize, handRounding, handBackground, searchForCommander, main, all, list, sideboard, manaValue, CollectionConverters.asScala(backFaceLands).toSet(), none, colorless, white, blue, black, red, green, multi, creature, artifact, enchantment, planeswalker, instant, sorcery, line),
            cwd
        );
    }

    protected Settings()
    {
        this(new InventorySettings(), new EditorSettings(), System.getProperty("user.home"));
    }

    public InventorySettings inventory()
    {
        return inventory;
    }

    public EditorSettings editor()
    {
        return editor;
    }

    public String cwd()
    {
        return cwd;
    }
}