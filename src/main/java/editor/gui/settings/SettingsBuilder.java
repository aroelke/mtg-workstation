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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import editor.collection.deck.CategorySpec;
import editor.database.attributes.CardAttribute;
import editor.filter.leaf.options.multi.CardTypeFilter;

/**
 * Builder for {@link Settings}, used for getting defaults, copying, and
 * creating new settings structures.
 * 
 * @author Alec Roelke
 */
public class SettingsBuilder
{
    private String inventorySource;
    private String inventoryFile;
    private String inventoryVersionFile;
    private String inventoryVersion;
    private String inventoryLocation;
    private String inventoryScans;
    private String inventoryTags;
    private boolean inventoryUpdate;
    private boolean inventoryWarn;
    private List<CardAttribute> inventoryColumns;
    private Color inventoryBackground;
    private Color inventoryStripe;
    private int recentsCount;
    private List<String> recentsFiles;
    private int explicits;
    private List<CategorySpec> presetCategories;
    private int categoryRows;
    private List<CardAttribute> editorColumns;
    private Color editorStripe;
    private int handSize;
    private String handRounding;
    private Color handBackground;
    private String cwd;

    /**
     * Create a new SettingsBuilder with nothing set.
     */
    public SettingsBuilder()
    {}

    /**
     * Create a new SettingsBuilder that is prepared to copy a settings
     * structure.
     * 
     * @param copy Settings to copy
     */
    public SettingsBuilder(Settings copy)
    {
        copy(copy);
    }

    /**
     * Create a {@link Settings} instance.  If some of the fields have not
     * been set, behavior is undefined.
     * @return a new {@link Settings} according to the values that have been
     * given to this SettingsBuilder.
     */
    public Settings build()
    {
        return new Settings(
            inventorySource,
            inventoryFile,
            inventoryVersionFile,
            inventoryVersion,
            inventoryLocation,
            inventoryScans,
            inventoryTags,
            inventoryUpdate,
            inventoryWarn,
            inventoryColumns,
            inventoryBackground,
            inventoryStripe,
            recentsCount,
            recentsFiles,
            explicits,
            presetCategories,
            categoryRows,
            editorColumns,
            editorStripe,
            handSize,
            handRounding,
            handBackground,
            cwd
        );
    }

    /**
     * Copy a {@link Settings} structure.
     * 
     * @param original {@link Settings} to copy
     * @return this SettingsBuilder.
     */
    public SettingsBuilder copy(Settings original)
    {
        inventorySource = original.inventory.source;
        inventoryFile = original.inventory.file;
        inventoryVersionFile = original.inventory.versionFile;
        inventoryVersion = original.inventory.version;
        inventoryLocation = original.inventory.location.toString();
        inventoryScans = original.inventory.scans.toString();
        inventoryTags = original.inventory.tags;
        inventoryUpdate = original.inventory.update;
        inventoryWarn = original.inventory.warn;
        inventoryColumns = original.inventory.columns;
        inventoryBackground = original.inventory.background;
        inventoryStripe = original.inventory.stripe;
        recentsCount = original.editor.recents.count;
        recentsFiles = original.editor.recents.files;
        presetCategories = new ArrayList<>(original.editor.categories.presets.stream().map(CategorySpec::new).collect(Collectors.toList()));
        categoryRows = original.editor.categories.rows;
        explicits = original.editor.categories.explicits;
        editorColumns = original.editor.columns;
        editorStripe = original.editor.stripe;
        handSize = original.editor.hand.size;
        handRounding = original.editor.hand.rounding;
        handBackground = original.editor.hand.background;
        cwd = original.cwd;

        return this;
    }

    /**
     * Set all settings to their defaults:
     * <ul>
     * <li>{@link Settings.InventorySettings#source}: "<a href="https://mtgjson.com/json/">"https://mtgjson.com/json/"</a>"
     * <li>{@link Settings.InventorySettings#file}: "AllSets.json"
     * <li>{@link Settings.InventorySettings#versionFile}: "version.json"
     * <li>{@link Settings.InventorySettings#version}: ""
     * <li>{@link Settings.InventorySettings#location}: "."
     * <li>{@link Settings.InventorySettings#scans}: "images/cards"
     * <li>{@link Settings.InventorySettings#tags}: "tags.json"
     * <li>{@link Settings.InventorySettings#update}: true
     * <li>{@link Settings.InventorySettings#warn}: true
     * <li>{@link Settings.InventorySettings#columns}:
     *     {@link CardAttribute#NAME}, {@link CardAttribute#MANA_COST},
     *     {@link CardAttribute#TYPE_LINE}, {@link CardAttribute#EXPANSION}
     * <li>{@link Settings.InventorySettings#background}: {@link Color#white}
     * <li>{@link Settings.InventorySettings#stripe}: gray
     * <li>{@link Settings.EditorSettings.RecentsSettings#count}: 4
     * <li>{@link Settings.EditorSettings.RecentsSettings#files}: empty list
     * <li>{@link Settings.EditorSettings.CategoriesSettings#rows}: 6
     * <li>{@link Settings.EditorSettings.CategoriesSettings#presets}:
     *     type filters for Artifacts, Creatures, Lands, and Instants/Sorceries
     * <li>{@link Settings.EditorSettings.CategoriesSettings#explicits}: 3
     * <li>{@link Settings.EditorSettings#columns}:
     *     {@link CardAttribute#NAME}, {@link CardAttribute#COUNT},
     *     {@link CardAttribute#MANA_COST}, {@link CardAttribute#TYPE_LINE},
     *     {@link CardAttribute#CATEGORIES}, {@link CardAttribute#DATE_ADDED}
     * <li>{@link Settings.EditorSettings#stripe}: gray
     * <li>{@link Settings.EditorSettings.HandSettings#size}: 7
     * <li>{@link Settings.EditorSettings.HandSettings#rounding}: "No rounding"
     * <li>{@link Settings.EditorSettings.HandSettings#background}:
     *     {@link Color#white}
     * <li>{@link Settings#cwd}: "."
     * </ul>
     * 
     * @return this SettingsBuilder
     */
    public SettingsBuilder defaults()
    {
        inventorySource = "https://mtgjson.com/json/";
        inventoryFile = "AllSets.json";
        inventoryVersionFile = "version.json";
        inventoryVersion = "";
        inventoryLocation = ".";
        inventoryScans = "images" + File.separatorChar +  "cards";
        inventoryTags = "tags.json";
        inventoryUpdate = true;
        inventoryWarn = true;
        inventoryColumns = List.of(NAME, MANA_COST, TYPE_LINE, EXPANSION);
        inventoryBackground = Color.WHITE;
        inventoryStripe = new Color(0xCC, 0xCC, 0xCC, 0xFF);
        recentsCount = 4;
        recentsFiles = Collections.emptyList();
        categoryRows = 6;
        explicits = 3;
        editorColumns = List.of(NAME, COUNT, MANA_COST, TYPE_LINE, EXPANSION, CATEGORIES, DATE_ADDED);
        editorStripe = new Color(0xCC, 0xCC, 0xCC, 0xFF);
        handSize = 7;
        handRounding = "No rounding";
        handBackground = Color.WHITE;
        cwd = ".";

        presetCategories = new ArrayList<CategorySpec>();
        CardTypeFilter artifacts = (CardTypeFilter)CardAttribute.createFilter(CardAttribute.CARD_TYPE);
        artifacts.selected.add("Artifact");
        presetCategories.add(new CategorySpec("Artifacts", Collections.emptySet(), Collections.emptySet(), Color.WHITE, artifacts));
        CardTypeFilter creatures = (CardTypeFilter)CardAttribute.createFilter(CardAttribute.CARD_TYPE);
        creatures.selected.add("Creature");
        presetCategories.add(new CategorySpec("Creatures", Collections.emptySet(), Collections.emptySet(), Color.WHITE, creatures));
        CardTypeFilter lands = (CardTypeFilter)CardAttribute.createFilter(CardAttribute.CARD_TYPE);
        lands.selected.add("Land");
        presetCategories.add(new CategorySpec("Lands", Collections.emptySet(), Collections.emptySet(), Color.WHITE, lands));
        CardTypeFilter spells = (CardTypeFilter)CardAttribute.createFilter(CardAttribute.CARD_TYPE);
        spells.selected.addAll(List.of("Instant", "Sorcery"));
        presetCategories.add(new CategorySpec("Instants/Sorceries", Collections.emptySet(), Collections.emptySet(), Color.WHITE, spells));

        return this;
    }

    /**
     * Change the inventory source (dangerous).
     * 
     * @param source new inventory source
     * @return this SettingsBuilder.
     * @see Settings.InventorySettings#source
     */
    public SettingsBuilder inventorySource(String source)
    {
        inventorySource = source;
        return this;
    }

    /**
     * Change the inventory file (dangerous).
     * 
     * @param file new file name of the inventory
     * @return this SettingsBuilder.
     * @see Settings.InventorySettings#file
     */
    public SettingsBuilder inventoryFile(String file)
    {
        inventoryFile = file;
        return this;
    }

    /**
     * Change the file to look for the inventory version in (dangerous).
     * 
     * @param file new inventory version file
     * @return this SettingsBuilder.
     * @see Settings.InventorySettings#versionFile
     */
    public SettingsBuilder inventoryVersionFile(String file)
    {
        inventoryVersionFile = file;
        return this;
    }

    /**
     * Change the inventory version.
     * 
     * @param version new inventory version
     * @return this SettingsBuilder.
     * @see Settings.InventorySettings#version
     */
    public SettingsBuilder inventoryVersion(String version)
    {
        inventoryVersion = version;
        return this;
    }

    /**
     * Change where to store the inventory file.
     * 
     * @param location new location for the inventory
     * @return this SettingsBuilder
     * @see Settings.InventorySettings#location
     */
    public SettingsBuilder inventoryLocation(String location)
    {
        inventoryLocation = location;
        return this;
    }

    /**
     * Change where to store card images.
     * 
     * @param scans new location for card images
     * @return this SettingsBuilder
     * @see Settings.InventorySettings#scans
     */
    public SettingsBuilder inventoryScans(String scans)
    {
        inventoryScans = scans;
        return this;
    }

    /**
     * Change the file to store tags in.
     * 
     * @param tags new tags file
     * @return this SettingsBuilder
     * @see Settings.InventorySettings#tags
     */
    public SettingsBuilder inventoryTags(String tags)
    {
        inventoryTags = tags;
        return this;
    }

    /**
     * Change whether or not to automatically check for updates on startup.
     * 
     * @param update whether or not to check for updates
     * @return this SettingsBuilder
     * @see Settings.InventorySettings#update
     */
    public SettingsBuilder inventoryUpdate(boolean update)
    {
        inventoryUpdate = update;
        return this;
    }

    /**
     * Change whether or not to show warnings from loading cards.
     * 
     * @param warn whether or not to warn about loading errors
     * @return this SettingsBuilder
     * @see Settings.InventorySettings#warn
     */
    public SettingsBuilder inventoryWarn(boolean warn)
    {
        inventoryWarn = warn;
        return this;
    }

    /**
     * Change the information to show in the inventory table.
     * 
     * @param columns new inventory table columns
     * @return this SettingsBuilder
     * @see Settings.InventorySettings#columns
     */
    public SettingsBuilder inventoryColumns(List<CardAttribute> columns)
    {
        inventoryColumns = new ArrayList<>(columns);
        return this;
    }

    /**
     * Change the information to show in the inventory table.
     * 
     * @param columns new inventory table columns
     * @return this SettingsBuilder
     * @see Settings.InventorySettings#columns
     */
    public SettingsBuilder inventoryColumns(CardAttribute... columns)
    {
        inventoryColumns = Arrays.asList(columns);
        return this;
    }

    /**
     * Change the background color of the card image panel.
     * 
     * @param background new color behind cards
     * @return this SettingsBuilder
     * @see Settings.InventorySettings#background
     */
    public SettingsBuilder inventoryBackground(Color background)
    {
        inventoryBackground = background;
        return this;
    }

    /**
     * Change the inventory table stripe color.
     * 
     * @param color new stripe color
     * @return this SettingsBuilder
     * @see Settings.InventorySettings#stripe
     */
    public SettingsBuilder inventoryStripe(Color stripe)
    {
        inventoryStripe = stripe;
        return this;
    }

    /**
     * Change the number of recent files to remember.
     * 
     * @param count new number of recents
     * @return this SettingsBuilder
     * @see Settings.EditorSettings.RecentsSettings#count
     */
    public SettingsBuilder recentsCount(int count)
    {
        recentsCount = count;
        return this;
    }

    /**
     * Change the recently-edited files.
     * 
     * @param files files that were recently edited
     * @return this SettingsBuilder
     * @see Settings.EditorSettings.RecentsSettings#files
     */
    public SettingsBuilder recentsFiles(List<String> files)
    {
        recentsFiles = new ArrayList<>(files);
        return this;
    }

    /**
     * Change the recently-edited files.
     * 
     * @param files files that were recently edited
     * @return this SettingsBuilder
     * @see Settings.EditorSettings.RecentsSettings#files
     */
    public SettingsBuilder recentsFiles(String... files)
    {
        recentsFiles = Arrays.asList(files);
        return this;
    }

    /**
     * Change the number of cards to show in white- or blacklists in
     * category editors.
     * 
     * @param count new number of cards to show
     * @return this SettingsBuilder
     * @see Settings.EditorSettings.CategoriesSettings#explicits
     */
    public SettingsBuilder explicits(int count)
    {
        explicits = count;
        return this;
    }

    /**
     * Change the preset categories.
     * 
     * @param categories new list of preset categories
     * @return this SettingsBuilder.
     * @see Settings.EditorSettings.CategoriesSettings#presets
     */
    public SettingsBuilder presetCategories(List<CategorySpec> categories)
    {
        presetCategories = categories.stream().map(CategorySpec::new).collect(Collectors.toList());
        return this;
    }

    /**
     * Change the preset categories.
     * 
     * @param categories new list of preset categories
     * @return this SettingsBuilder.
     * @see Settings.EditorSettings.CategoriesSettings#presets
     */
    public SettingsBuilder presetCategories(CategorySpec... categories)
    {
        presetCategories = Arrays.stream(categories).map(CategorySpec::new).collect(Collectors.toList());
        return this;
    }

    /**
     * Add a preset category.
     * 
     * @param category category to add
     * @return this SettingsBuilder
     * @see Settings.EditorSettings.CategoriesSettings#presets
     */
    public SettingsBuilder addPresetCategory(CategorySpec category)
    {
        presetCategories.add(new CategorySpec(category));
        return this;
    }

    /**
     * Change the number of rows of cards to display in categories.
     * 
     * @param rows new number of rows
     * @return this SettingsBuilder
     * @see Settings.EditorSettings.CategoriesSettings#rows
     */
    public SettingsBuilder categoryRows(int rows)
    {
        categoryRows = rows;
        return this;
    }

    /**
     * Change the information about cards to show in editor tables.
     * 
     * @param columns new columns in editor tables
     * @return this SettingsBuilder
     * @see Settings.EditorSettings#columns
     */
    public SettingsBuilder editorColumns(List<CardAttribute> columns)
    {
        editorColumns = new ArrayList<>(columns);
        return this;
    }

    /**
     * Change the information about cards to show in editor tables.
     * 
     * @param columns new columns in editor tables
     * @return this SettingsBuilder
     * @see Settings.EditorSettings#columns
     */
    public SettingsBuilder editorColumns(CardAttribute... columns)
    {
        editorColumns = Arrays.asList(columns);
        return this;
    }

    /**
     * Change the stripe color for editor tables.
     * 
     * @param stripe new stripe color
     * @return this SettingsBuilder.
     * @see Settings.EditorSettings#stripe
     */
    public SettingsBuilder editorStripe(Color stripe)
    {
        editorStripe = stripe;
        return this;
    }

    /**
     * Change the starting hand size for hand analysis.
     * 
     * @param size new starting hand size
     * @return this SettingsBuilder
     * @see Settings.EditorSettings.HandSettings#size
     */
    public SettingsBuilder handSize(int size)
    {
        handSize = size;
        return this;
    }

    /**
     * Change the rounding mode for hand statistics. The choices are:
     * <ul>
     * <li>No rounding: Display numbers up to two decimal places
     * <li>Round to nearest: round to the nearest integer
     * <li>Truncate: Round down to the nearest integer
     * </ul>
     * 
     * @param rounding new rounding mode
     * @return this SettingsBuilder.
     * @see Settings.EditorSettings.HandSettings#rounding
     */
    public SettingsBuilder handRounding(String rounding)
    {
        handRounding = rounding;
        return this;
    }

    /**
     * Background color of sample hand.
     * 
     * @param background new background color
     * @return this SettingsBuilder.
     * @see Settings.EditorSettings.HandSettings#background
     */
    public SettingsBuilder handBackground(Color background)
    {
        handBackground = background;
        return this;
    }

    /**
     * Change the starting directory for file choosers.
     * 
     * @param dir new starting directory
     * @return this SettingsBuilder
     * @see Settings#cwd
     */
    public SettingsBuilder cwd(String dir)
    {
        cwd = dir;
        return this;
    }
}