package editor.gui.settings;

import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import editor.collection.deck.CategorySpec;
import editor.database.card.Card;
import editor.database.characteristics.CardAttribute;

public final class Settings
{
    public static final class InventorySettings
    {
        public final URL source;
        public final String file;
        public final String versionFile;
        public final String version;
        public final String location;
        public final String scans;
        public final Map<Card, Set<String>> tags;
        public final boolean update;
        public final boolean warn;
        public final List<CardAttribute> columns;
        public final Color background;
        public final Color stripe;

        protected InventorySettings(String source,
                                    String file,
                                    String versionFile,
                                    String version,
                                    String location,
                                    String scans,
                                    Map<Card, Set<String>> tags,
                                    boolean update,
                                    boolean warn,
                                    List<CardAttribute> columns,
                                    Color background,
                                    Color stripe) throws MalformedURLException
        {
            this.source = new URL(source);
            this.file = file;
            this.versionFile = versionFile;
            this.version = version;
            this.location = location;
            this.scans = scans;
            this.tags = Collections.unmodifiableMap(tags.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (e) -> Collections.unmodifiableSet(new HashSet<>(e.getValue())))));
            this.update = update;
            this.warn = warn;
            this.columns = Collections.unmodifiableList(new ArrayList<>(columns));
            this.background = background;
            this.stripe = stripe;
        }
    }

    public static final class EditorSettings
    {
        public static final class RecentsSettings
        {
            public final int count;
            public final List<String> files;

            protected RecentsSettings(int count, List<String> files)
            {
                this.count = count;
                this.files = Collections.unmodifiableList(new ArrayList<>(files));
            }
        }

        public static final class CategoriesSettings
        {
            public final List<CategorySpec> presets;
            public final int rows;

            protected CategoriesSettings(List<CategorySpec> presets, int rows)
            {
                this.presets = Collections.unmodifiableList(new ArrayList<>(presets));
                this.rows = rows;
            }
        }

        public static final class HandSettings
        {
            public final int size;
            public final String rounding;
            public final Color background;

            protected HandSettings(int size, String rounding, Color background)
            {
                this.size = size;
                this.rounding = rounding;
                this.background = background;
            }
        }

        public final RecentsSettings recents;
        public final int explicits;
        public final CategoriesSettings categories;
        public final List<CardAttribute> columns;
        public final Color stripe;
        public final HandSettings hand;

        protected EditorSettings(int recentsCount, List<String> recentsFiles,
                                 int explicits,
                                 List<CategorySpec> presetCategories, int categoryRows,
                                 List<CardAttribute> columns, Color stripe,
                                 int handSize, String handRounding, Color handBackground)
        {
            this.recents = new RecentsSettings(recentsCount, recentsFiles);
            this.explicits = explicits;
            this.categories = new CategoriesSettings(presetCategories, categoryRows);
            this.columns = Collections.unmodifiableList(new ArrayList<>(columns));
            this.stripe = stripe;
            this.hand = new HandSettings(handSize, handRounding, handBackground);
        }
    }

    public final InventorySettings inventory;
    public final EditorSettings editor;

    protected Settings(String inventorySource, String inventoryFile, String inventoryVersionFile, String inventoryVersion, String inventoryLocation, String inventoryScans, Map<Card, Set<String>> inventoryTags, boolean inventoryUpdate, boolean inventoryWarn, List<CardAttribute> inventoryColumns, Color inventoryBackground, Color inventoryStripe, int recentsCount, List<String> recentsFiles, int explicits, List<CategorySpec> presetCategories, int categoryRows, List<CardAttribute> editorColumns, Color editorStripe, int handSize, String handRounding, Color handBackground) throws MalformedURLException
    {
        inventory = new InventorySettings(inventorySource, inventoryFile, inventoryVersionFile, inventoryVersion, inventoryLocation, inventoryScans, inventoryTags, inventoryUpdate, inventoryWarn, inventoryColumns, inventoryBackground, inventoryStripe);
        editor = new EditorSettings(recentsCount, recentsFiles, explicits, presetCategories, categoryRows, editorColumns, editorStripe, handSize, handRounding, handBackground);
    }
}