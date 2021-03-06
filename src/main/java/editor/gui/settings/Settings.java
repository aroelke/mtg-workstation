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

import editor.collection.deck.CategorySpec;
import editor.database.attributes.CardAttribute;
import editor.database.card.CardLayout;
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
 */
public record Settings(InventorySettings inventory, EditorSettings editor, String cwd)
{
    /**
     * Sub-structure containing global inventory and card settings.
     * 
     * @author Alec Roelke
     */
    public record InventorySettings(
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
     * @param recents {@link RecentsSettings}
     * @param categories {@link CategoriesSettings}
     * @param columns card attributes to show in editor tables
     * @param stripe stripe color of editor tables
     * @param hand {@link HandSettings}
     * @param legality {@link LegalitySettings}
     * @param manaValue which mana value of cards with multiple values to use
     * @param backFaceLands which multi-faced layouts count as lands when back faces are lands
     * 
     * @author Alec Roelke
     */
    public record EditorSettings(
        RecentsSettings recents,
        CategoriesSettings categories,
        List<CardAttribute> columns,
        Color stripe,
        HandSettings hand,
        LegalitySettings legality,
        String manaValue,
        Set<CardLayout> backFaceLands)
    {
        /**
         * Sub-structure containing information about recently-edited files.
         * 
         * @author Alec Roelke
         */
        public record RecentsSettings(int count, List<String> files)
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
        public record CategoriesSettings(List<CategorySpec> presets, int rows, int explicits)
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
        public record HandSettings(int size, String rounding, Color background)
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
        public record LegalitySettings(boolean searchForCommander, boolean main, boolean all, String list, String sideboard)
        {
            private LegalitySettings()
            {
                this(true, true, false, "", "");
            }
        }

        private EditorSettings(int recentsCount, List<String> recentsFiles,
                               int explicits,
                               List<CategorySpec> presetCategories, int categoryRows,
                               List<CardAttribute> columns, Color stripe,
                               int handSize, String handRounding, Color handBackground,
                               boolean searchForCommander, boolean main, boolean all, String list, String sideboard,
                               String manaValue,
                               Set<CardLayout> backFaceLands)
        {
            this(
                new RecentsSettings(recentsCount, recentsFiles),
                new CategoriesSettings(presetCategories, categoryRows, explicits),
                Collections.unmodifiableList(new ArrayList<>(columns)),
                stripe,
                new HandSettings(handSize, handRounding, handBackground),
                new LegalitySettings(searchForCommander, main, all, list, sideboard),
                manaValue,
                backFaceLands
            );
        }

        private EditorSettings()
        {
            this(
                new RecentsSettings(),
                new CategoriesSettings(),
                List.of(NAME, MANA_COST, TYPE_LINE, EXPANSION, CATEGORIES, COUNT, DATE_ADDED),
                new Color(0xCC, 0xCC, 0xCC, 0xFF),
                new HandSettings(),
                new LegalitySettings(),
                "Minimum",
                Set.of(CardLayout.MODAL_DFC)
            );
        }
    }

    protected Settings(String inventorySource, String inventoryFile, String inventoryVersionFile, DatabaseVersion inventoryVersion, String inventoryLocation, String inventoryScans, String imageSource, boolean imageLimitEnable, int imageLimit, String inventoryTags, UpdateFrequency inventoryUpdate, boolean inventoryWarn, List<CardAttribute> inventoryColumns, Color inventoryBackground, Color inventoryStripe, int recentsCount, List<String> recentsFiles, int explicits, List<CategorySpec> presetCategories, int categoryRows, List<CardAttribute> editorColumns, Color editorStripe, int handSize, String handRounding, Color handBackground, boolean searchForCommander, boolean main, boolean all, String list, String sideboard, String manaValue, Set<CardLayout> backFaceLands, String cwd)
    {
        this(
            new InventorySettings(inventorySource, inventoryFile, inventoryVersionFile, inventoryVersion, inventoryLocation, inventoryScans, imageSource, imageLimitEnable, imageLimit, inventoryTags, inventoryUpdate, inventoryWarn, inventoryColumns, inventoryBackground, inventoryStripe),
            new EditorSettings(recentsCount, recentsFiles, explicits, presetCategories, categoryRows, editorColumns, editorStripe, handSize, handRounding, handBackground, searchForCommander, main, all, list, sideboard, manaValue, backFaceLands),
            cwd
        );
    }

    protected Settings()
    {
        this(new InventorySettings(), new EditorSettings(), System.getProperty("user.home"));
    }
}