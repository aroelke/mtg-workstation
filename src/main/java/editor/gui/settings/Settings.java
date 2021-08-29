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
    /**
     * Sub-structure containing global inventory and card settings.
     * 
     * @author Alec Roelke
     */
    public static class InventorySettings/*(
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
    )*/ {
        private final String source;
        private final String file;
        private final String versionFile;
        private final DatabaseVersion version;
        private final String location;
        private final String scans;
        private final String imageSource;
        private final boolean imageLimitEnable;
        private final int imageLimit;
        private final String tags;
        private final UpdateFrequency update;
        private final boolean warn;
        private final List<CardAttribute> columns;
        private final Color background;
        private final Color stripe;

        public InventorySettings(
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
            this.source = source;
            this.file = file;
            this.versionFile = versionFile;
            this.version = version;
            this.location = location;
            this.scans = scans;
            this.imageSource = imageSource;
            this.imageLimitEnable = imageLimitEnable;
            this.imageLimit = imageLimit;
            this.tags = tags;
            this.update = update;
            this.warn = warn;
            this.columns = columns;
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

        public String source()
        {
            return source;
        }

        public String file()
        {
            return file;
        }

        public String versionFile()
        {
            return versionFile;
        }

        public DatabaseVersion version()
        {
            return version;
        }

        public String location()
        {
            return location;
        }

        public String scans()
        {
            return scans;
        }

        public String imageSource()
        {
            return imageSource;
        }

        public boolean imageLimitEnable()
        {
            return imageLimitEnable;
        }

        public int imageLimit()
        {
            return imageLimit;
        }

        public String tags()
        {
            return tags;
        }

        public UpdateFrequency update()
        {
            return update;
        }

        public boolean warn()
        {
            return warn;
        }

        public List<CardAttribute> columns()
        {
            return columns;
        }

        public Color background()
        {
            return background;
        }

        public Color stripe()
        {
            return stripe;
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
    public static class EditorSettings/*(
        RecentsSettings recents,
        CategoriesSettings categories,
        List<CardAttribute> columns,
        Color stripe,
        HandSettings hand,
        LegalitySettings legality,
        String manaValue,
        Set<CardLayout> backFaceLands,
        ManaAnalysisSettings manaAnalysis)*/
    {
        private EditorSettings(int recentsCount, List<String> recentsFiles,
                               int explicits,
                               List<Category> presetCategories, int categoryRows,
                               List<CardAttribute> columns, Color stripe,
                               int handSize, String handRounding, Color handBackground,
                               boolean searchForCommander, boolean main, boolean all, String list, String sideboard,
                               String manaValue,
                               Set<CardLayout> backFaceLands,
                               Color none,
                               Color colorless, Color white, Color blue, Color black, Color red, Color green, Color multi,
                               Color creature, Color artifact, Color enchantment, Color planeswalker, Color instant, Color sorcery,
                               Color line)
        {
            this(
                new RecentsSettings(recentsCount, CollectionConverters.asScala(recentsFiles).toSeq()),
                new CategoriesSettings(CollectionConverters.asScala(presetCategories).toSeq(), categoryRows, explicits),
                Collections.unmodifiableList(new ArrayList<>(columns)),
                stripe,
                new HandSettings(handSize, handRounding, handBackground),
                new LegalitySettings(searchForCommander, main, all, list, sideboard),
                manaValue,
                backFaceLands,
                new ManaAnalysisSettings(
                    none,
                    colorless, white, blue, black, red, green, multi,
                    creature, artifact, enchantment, planeswalker, instant, sorcery,
                    line
                )
            );
        }

        public final RecentsSettings recents;
        public final CategoriesSettings categories;
        public final List<CardAttribute> columns;
        public final Color stripe;
        public final HandSettings hand;
        public final LegalitySettings legality;
        public final String manaValue;
        public final Set<CardLayout> backFaceLands;
        public final ManaAnalysisSettings manaAnalysis;

        public EditorSettings(RecentsSettings recents,
                              CategoriesSettings categories,
                              List<CardAttribute> columns,
                              Color stripe,
                              HandSettings hand,
                              LegalitySettings legality,
                              String manaValue,
                              Set<CardLayout> backFaceLands,
                              ManaAnalysisSettings manaAnalysis)
        {
            this.recents = recents;
            this.categories = categories;
            this.columns = columns;
            this.stripe = stripe;
            this.hand = hand;
            this.legality = legality;
            this.manaValue = manaValue;
            this.backFaceLands = backFaceLands;
            this.manaAnalysis = manaAnalysis;
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
                Set.of(CardLayout.MODAL_DFC),
                new ManaAnalysisSettings()
            );
        }

        public RecentsSettings recents()
        {
            return recents;
        }

        public CategoriesSettings categories()
        {
            return categories;
        }

        public List<CardAttribute> columns()
        {
            return columns;
        }

        public Color stripe()
        {
            return stripe;
        }

        public HandSettings hand()
        {
            return hand;
        }

        public LegalitySettings legality()
        {
            return legality;
        }

        public String manaValue()
        {
            return manaValue;
        }

        public Set<CardLayout> backFaceLands()
        {
            return backFaceLands;
        }

        public ManaAnalysisSettings manaAnalysis()
        {
            return manaAnalysis;
        }

        /**
         * Determine if a card counts as a land based on its faces and the #backFaceLands setting.
         * 
         * @param c card to examine
         * @returns <code>true</code> if the card counts as a land, and <code>false</code>
         * otherwise
         * @see backFaceLands
         */
        public boolean isLand(Card c)
        {
            if (c instanceof MultiCard m)
            {
                if (SettingsDialog.settings().editor().backFaceLands().contains(m.layout()))
                    return m.faces().stream().anyMatch(Card::isLand);
                else
                    return m.faces().get(0).isLand();
            }
            else
                return c.isLand();
        }

        /**
         * @param c card to examine
         * @return The mana value of the card based on the selection of #manaValue.
         * @see manaValue
         * @see Card#minManaValue
         * @see Card#maxManaValue
         * @see Card#avgManaValue
         * @see Card#manaValue
         */
        public double getManaValue(Card c)
        {
            return switch (manaValue) {
                case "Minimum" -> c.minManaValue();
                case "Maximum" -> c.maxManaValue();
                case "Average" -> c.avgManaValue();
                case "Real"    -> c.manaValue();
                default -> Double.NaN;
            };
        }
    }

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
            new InventorySettings(inventorySource, inventoryFile, inventoryVersionFile, inventoryVersion, inventoryLocation, inventoryScans, imageSource, imageLimitEnable, imageLimit, inventoryTags, inventoryUpdate, inventoryWarn, inventoryColumns, inventoryBackground, inventoryStripe),
            new EditorSettings(recentsCount, recentsFiles, explicits, presetCategories, categoryRows, editorColumns, editorStripe, handSize, handRounding, handBackground, searchForCommander, main, all, list, sideboard, manaValue, backFaceLands, none, colorless, white, blue, black, red, green, multi, creature, artifact, enchantment, planeswalker, instant, sorcery, line),
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