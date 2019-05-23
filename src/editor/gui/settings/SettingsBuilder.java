package editor.gui.settings;

import static editor.database.characteristics.CardAttribute.CATEGORIES;
import static editor.database.characteristics.CardAttribute.COUNT;
import static editor.database.characteristics.CardAttribute.DATE_ADDED;
import static editor.database.characteristics.CardAttribute.EXPANSION_NAME;
import static editor.database.characteristics.CardAttribute.MANA_COST;
import static editor.database.characteristics.CardAttribute.NAME;
import static editor.database.characteristics.CardAttribute.TYPE_LINE;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import editor.collection.deck.CategorySpec;
import editor.database.characteristics.CardAttribute;
import editor.filter.FilterAttribute;
import editor.filter.leaf.options.multi.CardTypeFilter;

public class SettingsBuilder
{
    private String inventorySource;
    private String inventoryFile;
    private String inventoryVersionFile;
    private String inventoryVersion;
    private String inventoryLocation;
    private String inventoryScans;
    private Map<Long, Set<String>> inventoryTags;
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

    public SettingsBuilder()
    {}

    public SettingsBuilder(Settings copy)
    {
        copy(copy);
    }

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
        explicits = original.editor.explicits;
        editorColumns = original.editor.columns;
        editorStripe = original.editor.stripe;
        handSize = original.editor.hand.size;
        handRounding = original.editor.hand.rounding;
        handBackground = original.editor.hand.background;
        cwd = original.cwd;

        return this;
    }

    public SettingsBuilder defaults()
    {
        inventorySource = "https://mtgjson.com/json/";
        inventoryFile = "AllSets.json";
        inventoryVersionFile = "version.json";
        inventoryVersion = "";
        inventoryLocation = ".";
        inventoryScans = "images" + File.separatorChar +  "cards";
        inventoryTags = Collections.emptyMap();
        inventoryUpdate = true;
        inventoryWarn = true;
        inventoryColumns = List.of(NAME, MANA_COST, TYPE_LINE, EXPANSION_NAME);
        inventoryBackground = Color.WHITE;
        inventoryStripe = new Color(0xCC, 0xCC, 0xCC, 0xFF);
        recentsCount = 4;
        recentsFiles = Collections.emptyList();
        categoryRows = 6;
        explicits = 3;
        editorColumns = List.of(NAME, COUNT, MANA_COST, TYPE_LINE, EXPANSION_NAME, CATEGORIES, DATE_ADDED);
        editorStripe = new Color(0xCC, 0xCC, 0xCC, 0xFF);
        handSize = 7;
        handRounding = "No rounding";
        handBackground = Color.WHITE;
        cwd = ".";

        presetCategories = new ArrayList<CategorySpec>();
        CardTypeFilter artifacts = (CardTypeFilter)FilterAttribute.createFilter(FilterAttribute.CARD_TYPE);
        artifacts.selected.add("Artifact");
        presetCategories.add(new CategorySpec("Artifacts", Collections.emptySet(), Collections.emptySet(), Color.WHITE, artifacts));
        CardTypeFilter creatures = (CardTypeFilter)FilterAttribute.createFilter(FilterAttribute.CARD_TYPE);
        creatures.selected.add("Creature");
        presetCategories.add(new CategorySpec("Creatures", Collections.emptySet(), Collections.emptySet(), Color.WHITE, creatures));
        CardTypeFilter lands = (CardTypeFilter)FilterAttribute.createFilter(FilterAttribute.CARD_TYPE);
        lands.selected.add("Land");
        presetCategories.add(new CategorySpec("Lands", Collections.emptySet(), Collections.emptySet(), Color.WHITE, lands));
        CardTypeFilter spells = (CardTypeFilter)FilterAttribute.createFilter(FilterAttribute.CARD_TYPE);
        spells.selected.addAll(List.of("Instant", "Sorcery"));
        presetCategories.add(new CategorySpec("Instants/Sorceries", Collections.emptySet(), Collections.emptySet(), Color.WHITE, spells));

        return this;
    }

    public SettingsBuilder inventorySource(String source)
    {
        inventorySource = source;
        return this;
    }

    public SettingsBuilder inventoryFile(String file)
    {
        inventoryFile = file;
        return this;
    }

    public SettingsBuilder inventoryVersionFile(String file)
    {
        inventoryVersionFile = file;
        return this;
    }

    public SettingsBuilder inventoryVersion(String version)
    {
        inventoryVersion = version;
        return this;
    }

    public SettingsBuilder inventoryLocation(String location)
    {
        inventoryLocation = location;
        return this;
    }

    public SettingsBuilder inventoryScans(String scans)
    {
        inventoryScans = scans;
        return this;
    }

    public SettingsBuilder inventoryTags(Map<Long, ? extends Set<String>> tags)
    {
        inventoryTags = tags.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (e) -> new HashSet<>(e.getValue())));
        return this;
    }

    public SettingsBuilder inventoryUpdate(boolean update)
    {
        inventoryUpdate = update;
        return this;
    }

    public SettingsBuilder inventoryWarn(boolean warn)
    {
        inventoryWarn = warn;
        return this;
    }

    public SettingsBuilder inventoryColumns(List<CardAttribute> columns)
    {
        inventoryColumns = new ArrayList<>(columns);
        return this;
    }

    public SettingsBuilder inventoryColumns(CardAttribute... columns)
    {
        inventoryColumns = Arrays.asList(columns);
        return this;
    }

    public SettingsBuilder inventoryBackground(Color background)
    {
        inventoryBackground = background;
        return this;
    }

    public SettingsBuilder inventoryStripe(Color stripe)
    {
        inventoryStripe = stripe;
        return this;
    }

    public SettingsBuilder recentsCount(int count)
    {
        recentsCount = count;
        return this;
    }

    public SettingsBuilder recentsFiles(List<String> files)
    {
        recentsFiles = new ArrayList<>(files);
        return this;
    }

    public SettingsBuilder recentsFiles(String... files)
    {
        recentsFiles = Arrays.asList(files);
        return this;
    }

    public SettingsBuilder explicits(int count)
    {
        explicits = count;
        return this;
    }

    public SettingsBuilder presetCategories(List<CategorySpec> categories)
    {
        presetCategories = categories.stream().map(CategorySpec::new).collect(Collectors.toList());
        return this;
    }

    public SettingsBuilder presetCategories(CategorySpec... categories)
    {
        presetCategories = Arrays.stream(categories).map(CategorySpec::new).collect(Collectors.toList());
        return this;
    }

    public SettingsBuilder addPresetCategory(CategorySpec category)
    {
        presetCategories.add(new CategorySpec(category));
        return this;
    }

    public SettingsBuilder categoryRows(int rows)
    {
        categoryRows = rows;
        return this;
    }

    public SettingsBuilder editorColumns(List<CardAttribute> columns)
    {
        editorColumns = new ArrayList<>(columns);
        return this;
    }

    public SettingsBuilder editorColumns(CardAttribute... columns)
    {
        editorColumns = Arrays.asList(columns);
        return this;
    }

    public SettingsBuilder editorStripe(Color stripe)
    {
        editorStripe = stripe;
        return this;
    }

    public SettingsBuilder handSize(int size)
    {
        handSize = size;
        return this;
    }

    public SettingsBuilder handRounding(String rounding)
    {
        handRounding = rounding;
        return this;
    }

    public SettingsBuilder handBackground(Color background)
    {
        handBackground = background;
        return this;
    }

    public SettingsBuilder cwd(String dir)
    {
        cwd = dir;
        return this;
    }
}