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
    public static record InventorySettings(
        String source,
        String file,
        String versionFile,
        DatabaseVersion version,
        String location,
        String scans,
        String imageSource,
        boolean imageLimitEnable,
        int imageLimit,
        String tags,
        UpdateFrequency update,
        boolean warn,
        List<CardAttribute> columns,
        Color background,
        Color stripe
    ) {
        private InventorySettings()
        {
            this(
                "https://mtgjson.com/api/v5/",
                "AllSets.json",
                "version.json",
                new DatabaseVersion(0, 0, 0),
                SettingsDialog.EDITOR_HOME.toString(),
                SettingsDialog.EDITOR_HOME.resolve("scans").toString(),
                "Scryfall",
                false,
                20,
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
        public static record RecentsSettings(int count, List<String> files)
        {
            private RecentsSettings()
            {
                this(4, Collections.emptyList());
            }
        }

        /**
         * Sub-structure containing settings for category editing.
         * 
         * @author Alec Roelke
         */
        public static record CategoriesSettings(List<CategorySpec> presets, int rows, int explicits)
        {
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
        }

        /**
         * Sub-structure containing settings for displaying information about
         * opening hands.
         * 
         * @author Alec Roelke
         */
        public static record HandSettings(int size, String rounding, Color background)
        {
            private HandSettings()
            {
                this(7, "No rounding", Color.WHITE);
            }
        }

        /**
         * Sub-structure containing settings for determining the legality of a deck.
         * 
         * @author Alec Roelke
         */
        public static record LegalitySettings(boolean searchForCommander, boolean main, boolean all, String list, String sideboard)
        {
            private LegalitySettings()
            {
                this(true, true, false, "", "");
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
        /** Which mana value of cards with multiple values to use. */
        public final String manaValue;

        private EditorSettings(int recentsCount, List<String> recentsFiles,
                                 int explicits,
                                 List<CategorySpec> presetCategories, int categoryRows,
                                 List<CardAttribute> columns, Color stripe,
                                 int handSize, String handRounding, Color handBackground,
                                 boolean searchForCommander, boolean main, boolean all, String list, String sideboard,
                                 String manaValue)
        {
            this.recents = new RecentsSettings(recentsCount, recentsFiles);
            this.categories = new CategoriesSettings(presetCategories, categoryRows, explicits);
            this.columns = Collections.unmodifiableList(new ArrayList<>(columns));
            this.stripe = stripe;
            this.hand = new HandSettings(handSize, handRounding, handBackground);
            this.legality = new LegalitySettings(searchForCommander, main, all, list, sideboard);
            this.manaValue = manaValue;
        }

        private EditorSettings()
        {
            recents = new RecentsSettings();
            categories = new CategoriesSettings();
            columns = List.of(NAME, MANA_COST, TYPE_LINE, EXPANSION, CATEGORIES, COUNT, DATE_ADDED);
            stripe = new Color(0xCC, 0xCC, 0xCC, 0xFF);
            hand = new HandSettings();
            legality = new LegalitySettings();
            manaValue = "Minimum";
        }

        @Override
        public boolean equals(Object other)
        {
            if (other == null)
                return false;
            if (other == this)
                return true;
            if (other instanceof EditorSettings o)
                return recents.equals(o.recents) &&
                       categories.equals(o.categories) &&
                       columns.equals(o.columns) &&
                       stripe.equals(o.stripe) &&
                       hand.equals(o.hand) &&
                       legality.equals(o.legality);
            return false;
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

    protected Settings(String inventorySource, String inventoryFile, String inventoryVersionFile, DatabaseVersion inventoryVersion, String inventoryLocation, String inventoryScans, String imageSource, boolean imageLimitEnable, int imageLimit, String inventoryTags, UpdateFrequency inventoryUpdate, boolean inventoryWarn, List<CardAttribute> inventoryColumns, Color inventoryBackground, Color inventoryStripe, int recentsCount, List<String> recentsFiles, int explicits, List<CategorySpec> presetCategories, int categoryRows, List<CardAttribute> editorColumns, Color editorStripe, int handSize, String handRounding, Color handBackground, boolean searchForCommander, boolean main, boolean all, String list, String sideboard, String manaValue, String cwd)
    {
        this.inventory = new InventorySettings(inventorySource, inventoryFile, inventoryVersionFile, inventoryVersion, inventoryLocation, inventoryScans, imageSource, imageLimitEnable, imageLimit, inventoryTags, inventoryUpdate, inventoryWarn, inventoryColumns, inventoryBackground, inventoryStripe);
        this.editor = new EditorSettings(recentsCount, recentsFiles, explicits, presetCategories, categoryRows, editorColumns, editorStripe, handSize, handRounding, handBackground, searchForCommander, main, all, list, sideboard, manaValue);
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
        if (other instanceof Settings o)
            return inventory.equals(o.inventory) && editor.equals(o.editor) && cwd.equals(o.cwd);
        return false;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(inventory, editor, cwd);
    }
}