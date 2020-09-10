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
import java.util.Objects;
import java.util.stream.Collectors;

import editor.collection.deck.CategorySpec;
import editor.database.attributes.CardAttribute;
import editor.database.version.DatabaseVersion;
import editor.database.version.UpdateFrequency;
import editor.filter.leaf.options.multi.CardTypeFilter;

/**
 * Structure containing global settings.  This structure consists of immutable
 * members and sub-structures and should be created using
 * {@link SettingsBuilder}.
 * 
 * @author Alec Roelke
 */
public final class Settings
{
    /**
     * Sub-structure containing global inventory and card settings.
     * 
     * @author Alec Roelke
     */
    public static final class InventorySettings
    {
        /** Site containing the inventory to download. */
        public final String source;
        /** Actual inventory file to download (without .zip). */
        public final String file;
        /** File name containing latest inventory version. */
        public final String versionFile;
        /** Version of the stored inventory. */
        public final DatabaseVersion version;
        /** Directory to store inventory file in. */
        public final String location;
        /** Directory to store card images in. */
        public final String scans;
        /** File to store tags in. */
        public final String tags;
        /** Check for inventory update on startup or don't. */
        public final UpdateFrequency update;
        /** Show warnings from loading inventory. */
        public final boolean warn;
        /** Card attributes to show in inventory table. */
        public final List<CardAttribute> columns;
        /** Background color of card image panel. */
        public final Color background;
        /** Stripe color of inventory table. */
        public final Color stripe;

        private InventorySettings(String source,
                                    String file,
                                    String versionFile,
                                    DatabaseVersion version,
                                    String location,
                                    String scans,
                                    String tags,
                                    UpdateFrequency update,
                                    boolean warn,
                                    List<CardAttribute> columns,
                                    Color background,
                                    Color stripe)
        {
            this.source = source;
            this.file = file;
            this.versionFile = versionFile;
            this.version = version;
            this.location = location;
            this.scans = scans;
            this.tags = tags;
            this.update = update;
            this.warn = warn;
            this.columns = Collections.unmodifiableList(new ArrayList<>(columns));
            this.background = background;
            this.stripe = stripe;
        }

        private InventorySettings()
        {
            this(
                "https://mtgjson.com/api/v5/",
                "AllSets.json",
                "version.json",
                new DatabaseVersion(0, 0, 0),
                SettingsDialog.EDITOR_HOME.toString(),
                SettingsDialog.EDITOR_HOME.resolve("scans").toString(),
                SettingsDialog.EDITOR_HOME.resolve("tags.json").toString(),
                UpdateFrequency.DAILY,
                true,
                List.of(NAME, MANA_COST, TYPE_LINE, EXPANSION),
                Color.WHITE,
                new Color(0xCC, 0xCC, 0xCC, 0xFF)
            );
        }

        /**
         * @return the full path name of the inventory file, i.e.
         * [@{link #location}]/[{@link #file}].
         */
        public String path()
        {
            return location + File.separator + file;
        }

        /**
         * @return the full URL of the latest inventory file without .zip, i.e.
         * [{@link #source}]/[{@link file}].
         */
        public String url()
        {
            return source + file;
        }

        /**
         * @return the full URL of the latest inventory version, i.e.
         * [{@link #source}]/[{@link #versionFile}].
         */
        public String versionSite()
        {
            return source + versionFile;
        }

        @Override
        public boolean equals(Object other)
        {
            if (other == null)
                return false;
            if (other == this)
                return true;
            if (!(other instanceof InventorySettings))
                return false;

            InventorySettings o = (InventorySettings)other;
            return source.equals(o.source) &&
                   file.equals(o.file) &&
                   versionFile.equals(o.versionFile) &&
                   version.equals(o.version) &&
                   location.equals(o.location) &&
                   scans.equals(o.scans) &&
                   tags.equals(o.tags) &&
                   update == o.update &&
                   warn == o.warn &&
                   columns.equals(o.columns) &&
                   background.equals(o.background) &&
                   stripe.equals(o.stripe);
        }
    }

    /**
     * Sub-structure containing settings for the editor frames.  It has some
     * of its own sub-structures that are also immutable.
     * 
     * @author Alec Roelke
     */
    public static final class EditorSettings
    {
        /**
         * Sub-structure containing information about recently-edited files.
         * 
         * @author Alec Roelke
         */
        public static final class RecentsSettings
        {
            /** Number of recent files to store. */
            public final int count;
            /** List of recently-edited files. */
            public final List<String> files;

            private RecentsSettings(int count, List<String> files)
            {
                this.count = count;
                this.files = Collections.unmodifiableList(new ArrayList<>(files));
            }

            private RecentsSettings()
            {
                this(4, Collections.emptyList());
            }

            @Override
            public boolean equals(Object other)
            {
                if (other == null)
                    return false;
                if (other == this)
                    return true;
                if (!(other instanceof RecentsSettings))
                    return false;

                RecentsSettings o = (RecentsSettings)other;
                return count == o.count && files.equals(o.files);
            }

            @Override
            public int hashCode()
            {
                return Objects.hash(count, files);
            }
        }

        /**
         * Sub-structure containing settings for category editing.
         * 
         * @author Alec Roelke
         */
        public static final class CategoriesSettings
        {
            /** Preset categories for quickly adding to decks. */
            public final List<CategorySpec> presets;
            /** Max number of rows to display cards in category tables. */
            public final int rows;
            /** Number of rows to show in white- or blacklists. */
            public final int explicits;

            private CategoriesSettings(List<CategorySpec> presets, int rows, int explicits)
            {
                this.presets = Collections.unmodifiableList(new ArrayList<>(presets));
                this.rows = rows;
                this.explicits = explicits;
            }

            private CategoriesSettings()
            {
                this(
                    Map.of(
                        "Artifacts", List.of("Artifact"),
                        "Creatures", List.of("Creature"),
                        "Lands", List.of("Land"),
                        "Instants/Sorceries", List.of("Instant", "Sorcery")
                    ).entrySet().stream().map((e) -> {
                        CardTypeFilter filter = (CardTypeFilter)CardAttribute.createFilter(CardAttribute.CARD_TYPE);
                        filter.selected.addAll(e.getValue());
                        return new CategorySpec(e.getKey(), Collections.emptySet(), Collections.emptySet(), Color.WHITE, filter);
                    }).collect(Collectors.toList()),
                    6, 3
                );
            }

            @Override
            public boolean equals(Object other)
            {
                if (other == null)
                    return false;
                if (other == this)
                    return true;
                if (!(other instanceof CategoriesSettings))
                    return false;

                CategoriesSettings o = (CategoriesSettings)other;
                return presets.equals(o.presets) && rows == o.rows && explicits == o.explicits;
            }

            @Override
            public int hashCode()
            {
                return Objects.hash(presets, rows, explicits);
            }
        }

        /**
         * Sub-structure containing settings for displaying information about
         * opening hands.
         * 
         * @author Alec Roelke
         */
        public static final class HandSettings
        {
            /** Initial size of opening hands before mulligans. */
            public final int size;
            /** How to round statistics (nearest, truncate, or don't). */
            public final String rounding;
            /** Background color for sample hand images. */
            public final Color background;

            private HandSettings(int size, String rounding, Color background)
            {
                this.size = size;
                this.rounding = rounding;
                this.background = background;
            }

            private HandSettings()
            {
                this(7, "No rounding", Color.WHITE);
            }

            @Override
            public boolean equals(Object other)
            {
                if (other == null)
                    return false;
                if (other == this)
                    return true;
                if (!(other instanceof HandSettings))
                    return false;
                
                HandSettings o = (HandSettings)other;
                return size == o.size && rounding.equals(o.rounding) && background.equals(o.background);
            }

            @Override
            public int hashCode()
            {
                return Objects.hash(size, rounding, background);
            }
        }

        /**
         * Sub-structure containing settings for determining the legality of a deck.
         * 
         * @author Alec Roelke
         */
        public static final class LegalitySettings
        {
            /** Whether or not to search for a commander if determine legality in applicable formats. */
            public final boolean searchForCommander;
            /** If searching for a commander, whether or not to search just the main deck. */
            public final boolean main;
            /** If searching for a commander, whether or not to check all lists. */
            public final boolean all;
            /** If searching for a commander, default name of the list to search that isn't the main deck. */
            public final String list;
            /** Include sideboard size in legality determination. */
            public final String sideboard;

            private LegalitySettings(boolean searchForCommander, boolean main, boolean all, String list, String sideboard)
            {
                this.searchForCommander = searchForCommander;
                this.main = main;
                this.all = all;
                this.list = list;
                this.sideboard = sideboard;
            }

            private LegalitySettings()
            {
                this(true, true, false, "", "");
            }

            @Override
            public boolean equals(Object other)
            {
                if (other == null)
                    return false;
                if (other == this)
                    return true;
                if (!(other instanceof LegalitySettings))
                    return false;
                LegalitySettings o = (LegalitySettings)other;
                return searchForCommander == o.searchForCommander &&
                       main == o.main &&
                       all == o.all &&
                       list.equals(o.list) &&
                       sideboard.equals(o.sideboard);
            }

            @Override
            public int hashCode()
            {
                return Objects.hash(searchForCommander, main, all, list, sideboard);
            }
        }

        /** @see RecentsSettings */
        public final RecentsSettings recents;
        /** @see CategoriesSettings */
        public final CategoriesSettings categories;
        /** Card attributes to show in editor tables. */
        public final List<CardAttribute> columns;
        /** Stripe color of editor tables. */
        public final Color stripe;
        /** @see HandSettings */
        public final HandSettings hand;
        /** @see LegalitySettings */
        public final LegalitySettings legality;

        private EditorSettings(int recentsCount, List<String> recentsFiles,
                                 int explicits,
                                 List<CategorySpec> presetCategories, int categoryRows,
                                 List<CardAttribute> columns, Color stripe,
                                 int handSize, String handRounding, Color handBackground,
                                 boolean searchForCommander, boolean main, boolean all, String list, String sideboard)
        {
            this.recents = new RecentsSettings(recentsCount, recentsFiles);
            this.categories = new CategoriesSettings(presetCategories, categoryRows, explicits);
            this.columns = Collections.unmodifiableList(new ArrayList<>(columns));
            this.stripe = stripe;
            this.hand = new HandSettings(handSize, handRounding, handBackground);
            this.legality = new LegalitySettings(searchForCommander, main, all, list, sideboard);
        }

        private EditorSettings()
        {
            recents = new RecentsSettings();
            categories = new CategoriesSettings();
            columns = List.of(NAME, MANA_COST, TYPE_LINE, EXPANSION, CATEGORIES, COUNT, DATE_ADDED);
            stripe = new Color(0xCC, 0xCC, 0xCC, 0xFF);
            hand = new HandSettings();
            legality = new LegalitySettings();
        }

        @Override
        public boolean equals(Object other)
        {
            if (other == null)
                return false;
            if (other == this)
                return true;
            if (!(other instanceof EditorSettings))
                return false;
            
            EditorSettings o = (EditorSettings)other;
            return recents.equals(o.recents) &&
                   categories.equals(o.categories) &&
                   columns.equals(o.columns) &&
                   stripe.equals(o.stripe) &&
                   hand.equals(o.hand) &&
                   legality.equals(o.legality);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(recents, categories, columns, stripe, hand, legality);
        }
    }

    /** @see InventorySettings */
    public final InventorySettings inventory;
    /** @see EditorSettings */
    public final EditorSettings editor;
    /** Initial directory of file choosers. */
    public final String cwd;

    protected Settings(String inventorySource, String inventoryFile, String inventoryVersionFile, DatabaseVersion inventoryVersion, String inventoryLocation, String inventoryScans, String inventoryTags, UpdateFrequency inventoryUpdate, boolean inventoryWarn, List<CardAttribute> inventoryColumns, Color inventoryBackground, Color inventoryStripe, int recentsCount, List<String> recentsFiles, int explicits, List<CategorySpec> presetCategories, int categoryRows, List<CardAttribute> editorColumns, Color editorStripe, int handSize, String handRounding, Color handBackground, boolean searchForCommander, boolean main, boolean all, String list, String sideboard, String cwd)
    {
        this.inventory = new InventorySettings(inventorySource, inventoryFile, inventoryVersionFile, inventoryVersion, inventoryLocation, inventoryScans, inventoryTags, inventoryUpdate, inventoryWarn, inventoryColumns, inventoryBackground, inventoryStripe);
        this.editor = new EditorSettings(recentsCount, recentsFiles, explicits, presetCategories, categoryRows, editorColumns, editorStripe, handSize, handRounding, handBackground, searchForCommander, main, all, list, sideboard);
        this.cwd = cwd;
    }

    protected Settings()
    {
        inventory = new InventorySettings();
        editor = new EditorSettings();
        cwd = System.getProperty("user.home");
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null)
            return false;
        if (this == other)
            return true;
        if (!(other instanceof Settings))
            return false;

        Settings o = (Settings)other;
        return inventory.equals(o.inventory) && editor.equals(o.editor) && cwd == o.cwd;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(inventory, editor, cwd);
    }
}