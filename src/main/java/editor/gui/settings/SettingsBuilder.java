package editor.gui.settings;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import scala.jdk.javaapi.CollectionConverters;

import editor.collection.deck.Category;
import editor.database.attributes.CardAttribute;
import editor.database.card.CardLayout;
import editor.database.version.DatabaseVersion;
import editor.database.version.UpdateFrequency;

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
    private DatabaseVersion inventoryVersion;
    private String inventoryLocation;
    private String inventoryScans;
    private String imageSource;
    private boolean enableImageLimit;
    private int imageLimit;
    private String inventoryTags;
    private UpdateFrequency inventoryUpdate;
    private boolean inventoryWarn;
    private List<CardAttribute> inventoryColumns;
    private Color inventoryBackground;
    private Color inventoryStripe;
    private int recentsCount;
    private List<String> recentsFiles;
    private int explicits;
    private List<Category> presetCategories;
    private int categoryRows;
    private List<CardAttribute> editorColumns;
    private Color editorStripe;
    private int handSize;
    private String handRounding;
    private Color handBackground;
    private boolean searchForCommander;
    private boolean main;
    private boolean all;
    private String list;
    private String sideboard;
    private String manaValue;
    private Set<CardLayout> backFaceLands;
    private String cwd;
    private Color none;
    private Color colorless, white, blue, black, red, green, multi;
    private Color creature, artifact, enchantment, planeswalker, instant, sorcery;
    private Color line;

    /**
     * Create a new SettingsBuilder with nothing set.
     */
    public SettingsBuilder() {}

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
            imageSource,
            enableImageLimit,
            imageLimit,
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
            searchForCommander,
            main,
            all,
            list,
            sideboard,
            manaValue,
            backFaceLands,
            cwd,
            none,
            colorless, white, blue, black, red, green, multi,
            creature, artifact, enchantment, planeswalker, instant, sorcery,
            line
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
        inventorySource = original.inventory().source();
        inventoryFile = original.inventory().file();
        inventoryVersionFile = original.inventory().versionFile();
        inventoryVersion = original.inventory().version();
        inventoryLocation = original.inventory().location();
        inventoryScans = original.inventory().scans();
        imageSource = original.inventory().imageSource();
        enableImageLimit = original.inventory().imageLimitEnable();
        imageLimit = original.inventory().imageLimit();
        inventoryTags = original.inventory().tags();
        inventoryUpdate = original.inventory().update();
        inventoryWarn = original.inventory().warn();
        inventoryColumns = CollectionConverters.asJava(original.inventory().columns());
        inventoryBackground = original.inventory().background();
        inventoryStripe = original.inventory().stripe();
        recentsCount = original.editor().recents().count();
        recentsFiles = CollectionConverters.asJava(original.editor().recents().files());
        presetCategories = new ArrayList<>(CollectionConverters.asJava(original.editor().categories().presets()).stream().map(Category::new).collect(Collectors.toList()));
        categoryRows = original.editor().categories().rows();
        explicits = original.editor().categories().explicits();
        editorColumns = CollectionConverters.asJava(original.editor().columns());
        editorStripe = original.editor().stripe();
        handSize = original.editor().hand().size();
        handRounding = original.editor().hand().rounding();
        handBackground = original.editor().hand().background();
        searchForCommander = original.editor().legality().searchForCommander();
        main = original.editor().legality().main();
        all = original.editor().legality().all();
        list = original.editor().legality().list();
        sideboard = original.editor().legality().sideboard();
        manaValue = original.editor().manaValue();
        backFaceLands = CollectionConverters.asJava(original.editor().backFaceLands());
        cwd = original.cwd();
        none = original.editor().manaAnalysis().none();
        colorless = original.editor().manaAnalysis().colorless();
        white = original.editor().manaAnalysis().white();
        blue = original.editor().manaAnalysis().blue();
        black = original.editor().manaAnalysis().black();
        red = original.editor().manaAnalysis().red();
        green = original.editor().manaAnalysis().green();
        multi = original.editor().manaAnalysis().multi();
        creature = original.editor().manaAnalysis().creature();
        artifact = original.editor().manaAnalysis().artifact();
        enchantment = original.editor().manaAnalysis().enchantment();
        planeswalker = original.editor().manaAnalysis().planeswalker();
        instant = original.editor().manaAnalysis().instant();
        sorcery = original.editor().manaAnalysis().sorcery();
        line = original.editor().manaAnalysis().line();

        return this;
    }

    /**
     * Set all settings to their defaults:
     * <ul>
     * <li>{@link Settings.InventorySettings#source}: "<a href="https://mtgjson.com/json/">"https://mtgjson.com/json/"</a>"
     * <li>{@link Settings.InventorySettings#file}: "AllSets.json"
     * <li>{@link Settings.InventorySettings#versionFile}: "version.json"
     * <li>{@link Settings.InventorySettings#version}: ""
     * <li>{@link Settings.InventorySettings#location}: <code>$HOME/.editor</code>
     * <li>{@link Settings.InventorySettings#scans}: <code>$HOME/.editor/scans</code>
     * <li>{@link Settings.InventorySettings#imageSource}: Scryfall
     * <li>{@link Settings.InventorySettings#imageLimitEnable}: <code>false</code>
     * <li>{@link Settings.InventorySettings#imageLimit}: 20
     * <li>{@link Settings.InventorySettings#tags}: <code>$HOME/.mtgworkstation/tags.json</code>
     * <li>{@link Settings.InventorySettings#update}: Daily
     * <li>{@link Settings.InventorySettings#warn}: <code>true</code>
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
     * <li>{@link Settings.EditorSettings.LegalitySettings#searchForCommander}:
     *     <code>true</code>
     * <li>{@link Settings.EditorSettings.LegalitySettings#main}: <code>true</code>
     * <li>{@link Settings.EditorSettings.LegalitySettings#all}: <code>false</code>
     * <li>{@link Settings.EditorSettings.LegalitySettings#list}: <code>""</code>
     * <li>{@link Settings.EditorSettings.LegalitySettings#sideboard}: <code>""</code>
     * <li>{@link Settings.EditorSettings#manaValue}: <code>"Minimum"</code>
     * <li>{@link Settings.EditorSettings#backFaceLands}: [{@link CardLayout#MODAL_DFC}]
     * <li>{@link Settings#cwd}: <code>$HOME</code>
     * <li>{@link Settings.EditorSettings.ManaAnalysisSettings#none()}: <code>#8080ff</code> (light blue)
     * <li>{@link Settings.EditorSettings.ManaAnalysisSettings#colorless()}: <code>#cbc6c1</code> (gray)
     * <li>{@link Settings.EditorSettings.ManaAnalysisSettings#white()}: <code>#f8f6d8</code>
     * <li>{@link Settings.EditorSettings.ManaAnalysisSettings#blue()}: <code>#c1d7e9</code>
     * <li>{@link Settings.EditorSettings.ManaAnalysisSettings#black()}: <code>#bab1ab</code>
     * <li>{@link Settings.EditorSettings.ManaAnalysisSettings#red()}: <code>#e49977</code>
     * <li>{@link Settings.EditorSettings.ManaAnalysisSettings#green()}: <code>#a3bf95</code>
     * <li>{@link Settings.EditorSettings.ManaAnalysisSettings#multi()}: <code>#cca552</code> (gold)
     * <li>{@link Settings.EditorSettings.ManaAnalysisSettings#creature()}: <code>#a3bf95</code> (same as green)
     * <li>{@link Settings.EditorSettings.ManaAnalysisSettings#artifact()}: <code>#cbc6c1</code> (same as colorless)
     * <li>{@link Settings.EditorSettings.ManaAnalysisSettings#enchantment()}: <code>#f8f6d8</code> (same as white)
     * <li>{@link Settings.EditorSettings.ManaAnalysisSettings#planeswalker()}: <code>#d6b4d6</code> (purple)
     * <li>{@link Settings.EditorSettings.ManaAnalysisSettings#instant()}: <code>#c1d7e9</code> (same as blue)
     * <li>{@link Settings.EditorSettings.ManaAnalysisSettings#sorcery()}: <code>#e49977</code> (same as red)
     * <li>{@link Settings.EditorSettings.ManaAnalysisSettings#line()}: black
     * </ul>
     * 
     * @return this SettingsBuilder
     */
    public SettingsBuilder defaults()
    {
        return copy(new Settings());
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
    public SettingsBuilder inventoryVersion(DatabaseVersion version)
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
     * Change where to download card images from.
     * 
     * @param source new site to download card images
     * @return this SettingsBuilder
     * @see Settings.InventorySettings#imageSource
     */
    public SettingsBuilder imageSource(String source)
    {
        imageSource = source;
        return this;
    }

    /**
     * Change whether or not the number of images cached should be limited.
     * 
     * @param enable enable or disable cache limit
     * @return this SettingsBuilder
     * @see Settings.InventorySettings#imageLimitEnable
     */
    public SettingsBuilder imageLimitEnable(boolean enable)
    {
        enableImageLimit = enable;
        return this;
    }

    /**
     * Change the number of images allowed to be cached if there is a limit.
     * 
     * @param limit new limit for image cache
     * @return this SettingsBuilder
     * @see Settings.InventorySettings#imageLimit
     */
    public SettingsBuilder imageLimit(int limit)
    {
        imageLimit = limit;
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
     * Change how often new updates should be dowloaded.
     * 
     * @param update how often to download new updates
     * @return this SettingsBuilder
     * @see Settings.InventorySettings#update
     */
    public SettingsBuilder inventoryUpdate(UpdateFrequency update)
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
    public SettingsBuilder presetCategories(List<Category> categories)
    {
        presetCategories = categories.stream().map(Category::new).collect(Collectors.toList());
        return this;
    }

    /**
     * Change the preset categories.
     * 
     * @param categories new list of preset categories
     * @return this SettingsBuilder.
     * @see Settings.EditorSettings.CategoriesSettings#presets
     */
    public SettingsBuilder presetCategories(Category... categories)
    {
        presetCategories = Arrays.stream(categories).map(Category::new).collect(Collectors.toList());
        return this;
    }

    /**
     * Add a preset category.
     * 
     * @param category category to add
     * @return this SettingsBuilder
     * @see Settings.EditorSettings.CategoriesSettings#presets
     */
    public SettingsBuilder addPresetCategory(Category category)
    {
        presetCategories.add(new Category(category));
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
     * Whether or not to search for a commander.
     * 
     * @param search search for a commander
     * @return this SettingsBuilder.
     * @see Settings.EditorSettings.LegalitySettings#searchForCommander
     */
    public SettingsBuilder searchForCommander(boolean search)
    {
        searchForCommander = search;
        return this;
    }

    /**
     * Whether or not to search the main deck for a commander.
     * 
     * @param main search the main deck for a commander
     * @return this SettingsBuilder.
     * @see Settings.EditorSettings.LegalitySettings#main
     */
    public SettingsBuilder commanderInMain(boolean main)
    {
        this.main = main;
        return this;
    }

    /**
     * Whether or not to search all lists for a commander.
     * 
     * @param all search all lists for a commander
     * @return this SettingsBuilder.
     * @see Settings.EditorSettings.LegalitySettings#all
     */
    public SettingsBuilder commanderInAll(boolean all)
    {
        this.all = all;
        return this;
    }

    /**
     * Name of the list to search for a commander.
     * 
     * @param list list name to search
     * @return this SettingsBuilder
     * @see Settings.EditorSettings.LegalitySettings#list
     */
    public SettingsBuilder commanderInList(String list)
    {
        this.list = list;
        return this;
    }

    /**
     * Name of the list to use as sideboard.
     * 
     * @param sideboard sideboard list name
     * @return this SettingsBuilder
     * @see Settings.EditorSettings.LegalitySettings#sideboard
     */
    public SettingsBuilder sideboardName(String sideboard)
    {
        this.sideboard = sideboard;
        return this;
    }

    /**
     * Set which mana value is used for analysis when a card has multiple.
     * Options are "Minimum," "Maximum," "Average," and "Real."
     * 
     * @param choice which mana value to use
     * @return this SettingsBuilder
     * @see Settings.EditorSettings#manaValue
     */
    public SettingsBuilder manaValue(String choice)
    {
        this.manaValue = choice;
        return this;
    }

    /**
     * Set which layouts (which must be multi-faced) to count as lands if their back faces
     * are lands
     * 
     * @param choices set of card layouts to count as lands
     * @return this SettingsBuilder
     * @see Settings.EditorSettings#backFaceLands
     */
    public SettingsBuilder backFaceLands(Set<CardLayout> choices)
    {
        this.backFaceLands = choices;
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

    /**
     * Change the color to display on mana curve bars with no divisions.
     * 
     * @param c new color
     * @return this SettingsBuilder
     * @see Settings.EditorSettings.ManaAnalysisSettings#none
     */
    public SettingsBuilder none(Color c)
    {
        none = c;
        return this;
    }

    /**
     * Change the color to display on the colorless section of mana curve bars.
     * 
     * @param c new color
     * @return this SettingsBuilder
     * @see Settings.EditorSettings.ManaAnalysisSettings#colorless
     */
    public SettingsBuilder colorless(Color c)
    {
        colorless = c;
        return this;
    }

    /**
     * Change the color to display on the white section of mana curve bars.
     * 
     * @param c new color
     * @return this SettingsBuilder
     * @see Settings.EditorSettings.ManaAnalysisSettings#white
     */
    public SettingsBuilder white(Color c)
    {
        white = c;
        return this;
    }

    /**
     * Change the color to display on the blue section of mana curve bars.
     * 
     * @param c new color
     * @return this SettingsBuilder
     * @see Settings.EditorSettings.ManaAnalysisSettings#blue
     */
    public SettingsBuilder blue(Color c)
    {
        blue = c;
        return this;
    }

    /**
     * Change the color to display on the black secton of mana curve bars.
     * 
     * @param c new color
     * @return this SettingsBuilder
     * @see Settings.EditorSettings.ManaAnalysisSettings#black
     */
    public SettingsBuilder black(Color c)
    {
        black = c;
        return this;
    }

    /**
     * Change the color to display on the red section of mana curve bars.
     * 
     * @param c new color
     * @return this SettingsBuilder
     * @see Settings.EditorSettings.ManaAnalysisSettings#red
     */
    public SettingsBuilder red(Color c)
    {
        red = c;
        return this;
    }

    /**
     * Change the color to display on the green section of mana curve bars.
     * 
     * @param c new color
     * @return this SettingsBuilder
     * @see Settings.EditorSettings.ManaAnalysisSettings#green
     */
    public SettingsBuilder green(Color c)
    {
        green = c;
        return this;
    }

    /**
     * Change the color to display on the multicolored section of mana curve bars.
     * 
     * @param c new color
     * @return this SettingsBuilder
     * @see Settings.EditorSettings.ManaAnalysisSettings#multi
     */
    public SettingsBuilder multi(Color c)
    {
        multi = c;
        return this;
    }

    /**
     * Change the color to display on the creature section of mana curve bars.
     * 
     * @param c new color
     * @return this SettingsBuilder
     * @see Settings.EditorSettings.ManaAnalysisSettings#creature
     */
    public SettingsBuilder creature(Color c)
    {
        creature = c;
        return this;
    }

    /**
     * Change the color to display on the artifact section of mana curve bars.
     * 
     * @param c new color
     * @return this SettingsBuilder
     * @see Settings.EditorSettings.ManaAnalysisSettings#artifact
     */
    public SettingsBuilder artifact(Color c)
    {
        artifact = c;
        return this;
    }

    /**
     * Change the color to display on the enchantment section of mana curve bars.
     * 
     * @param c new color
     * @return this SettingsBuilder
     * @see Settings.EditorSettings.ManaAnalysisSettings#enchantment
     */
    public SettingsBuilder enchantment(Color c)
    {
        enchantment = c;
        return this;
    }

    /**
     * Change the color to display on the planeswalker section of mana curve bars.
     * 
     * @param c new color
     * @return this SettingsBuilder
     * @see Settings.EditorSettings.ManaAnalysisSettings#planeswalker
     */
    public SettingsBuilder planeswalker(Color c)
    {
        planeswalker = c;
        return this;
    }

    /**
     * Change the color to display on the instant section of mana curve bars.
     * 
     * @param c new color
     * @return this SettingsBuilder
     * @see Settings.EditorSettings.ManaAnalysisSettings#instant
     */
    public SettingsBuilder instant(Color c)
    {
        instant = c;
        return this;
    }

    /**
     * Change the color to display on the sorcery section of mana curve bars.
     * 
     * @param c new color
     * @return this SettingsBuilder
     * @see Settings.EditorSettings.ManaAnalysisSettings#sorcery
     */
    public SettingsBuilder sorcery(Color c)
    {
        sorcery = c;
        return this;
    }

    /**
     * Change the color of the land analysis line.
     * 
     * @param c new color
     * @return this SettingsBuilder
     * @see Settings.EditorSettings.ManaAnalysisSettings#line
     */
    public SettingsBuilder line(Color c)
    {
        line = c;
        return this;
    }

    /**
     * Change the color of multiple bar sections at once.
     * 
     * @param c mapping of sections onto new colors
     * @return this SettingsBuilder
     * @see Settings.EditorSettings.ManaAnalysisSettings
     */
    public SettingsBuilder sections(Map<String, Color> c)
    {
        for (var e : c.entrySet())
        {
            switch (e.getKey().toLowerCase())
            {
            case "none", "nothing":
                none(e.getValue());
                break;
            case "colorless", "c":
                colorless(e.getValue());
                break;
            case "white", "w":
                white(e.getValue());
                break;
            case "blue", "u":
                blue(e.getValue());
                break;
            case "black", "b":
                black(e.getValue());
                break;
            case "red", "r":
                red(e.getValue());
                break;
            case "green", "g":
                green(e.getValue());
                break;
            case "multicolored", "multi", "m":
                multi(e.getValue());
                break;
            case "creature":
                creature(e.getValue());
                break;
            case "artifact":
                artifact(e.getValue());
                break;
            case "enchantment":
                enchantment(e.getValue());
                break;
            case "planeswalker":
                planeswalker(e.getValue());
                break;
            case "instant":
                instant(e.getValue());
                break;
            case "sorcery":
                sorcery(e.getValue());
                break;
            }
        }
        return this;
    }
}