package editor.gui;

import editor.collection.category.CategorySpec;
import editor.database.card.Card;
import editor.database.characteristics.CardAttribute;
import editor.filter.FilterAttribute;
import editor.filter.leaf.options.multi.CardTypeFilter;
import editor.gui.display.CardTable;
import editor.gui.display.CategoryList;
import editor.gui.editor.CalculateHandPanel;
import editor.gui.editor.CategoryEditorPanel;
import editor.gui.generic.ScrollablePanel;
import editor.gui.generic.VerticalButtonList;
import editor.util.UnicodeSymbols;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class is a dialog that allows the user to change various properties about
 * the program.
 * <p>
 * TODO: Make a panel for editing tags
 * TODO: Make a button for clearing card tags
 * TODO: Consider switching to listener paradigm for applying settings
 *
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class SettingsDialog extends JDialog
{
    /**
     * Location to find card scans.
     */
    public static final String CARD_SCANS = "scans";

    /**
     * Tags that have been applied to cards.
     */
    public static final String CARD_TAGS = "card_tags";
    /**
     * Maximum number of rows to show in category panels.
     */
    public static final String CATEGORY_ROWS = "editor.rows";
    /**
     * Pattern to match when parsing an ARGB color from a string to a @link{java.awt.Color}
     */
    public static final Pattern COLOR_PATTERN = Pattern.compile("^#([0-9a-fA-F]{2})?([0-9a-fA-F]{6})$");

    /**
     * Columns to display in editor tables.
     */
    public static final String EDITOR_COLUMNS = "editor.columns";
    /**
     * Preset categories that can be added to editors.
     */
    public static final String EDITOR_PRESETS = "editor.presets";
    /**
     * Stripe color for editor tables.
     */
    public static final String EDITOR_STRIPE = "editor.stripe";
    /**
     * Round mode for displaying expected category counts in the hand panel.
     */
    public static final String EXPECTED_ROUND_MODE = "hand.expectedround";
    /**
     * Number of rows to show in the blacklist and whitelist displays in the
     * category editor.
     */
    public static final String EXPLICITS_ROWS = "category.explicits_rows";
    /**
     * Background color for card scans in sample hands.
     */
    public static final String HAND_BGCOLOR = "hand.bgcolor";
    /**
     * Default initial size for a hand.
     */
    public static final String HAND_SIZE = "hand.size";
    /**
     * Background color for card scans in the left pane.
     */
    public static final String IMAGE_BGCOLOR = "inventory.scan_bgcolor";

    /**
     * Whether or not to check for the latest inventory version on startup.
     */
    public static final String INITIAL_CHECK = "inventory.initialcheck";
    /**
     * Directory to start the file chooser in.
     */
    public static final String INITIALDIR = "initialdir";
    /**
     * Columns to display in the inventory table.
     */
    public static final String INVENTORY_COLUMNS = "inventory.columns";

    /**
     * File to download containing the inventory.
     */
    public static final String INVENTORY_FILE = "inventory.file";
    /**
     * Directory to store the inventory file in.
     */
    public static final String INVENTORY_LOCATION = "inventory.location";
    /**
     * Website to connect to for downloading the inventory.
     */
    public static final String INVENTORY_SOURCE = "inventory.source";

    /**
     * Code for the color of the stripes of the inventory table.
     */
    public static final String INVENTORY_STRIPE = "inventory.stripe";

    /**
     * Name of the file to get settings from.
     */
    public static final String PROPERTIES_FILE = "settings.txt";
    /**
     * Number of recently-opened files to save.
     */
    public static final String RECENT_COUNT = "recents.count";
    /**
     * Recently-opened files paths.
     */
    public static final String RECENT_FILES = "recents.files";

    /**
     * List of preset categories.
     */
    private static final List<CategorySpec> PRESET_CATEGORIES = new ArrayList<>();
    /**
     * Global settings for the program.
     */
    private static final Properties SETTINGS = new Properties();
    /**
     * Whether or not to suppress warnings on load.
     */
    public static final String SUPPRESS_LOAD_WARNINGS = "inventory.suppressload";
    /**
     * Current inventory version.
     */
    public static final String VERSION = "inventory.version";
    /**
     * File to download to check the latest version of the inventory.
     */
    public static final String VERSION_FILE = "inventory.version_file";

    /**
     * Create the preview panel for a color chooser that customizes the stripe color
     * of a #CardTable.
     *
     * @param chooser #JColorChooser to create the new preview panel for
     */
    private static void createStripeChooserPreview(JColorChooser chooser)
    {
        JPanel preview = new JPanel();
        preview.setLayout(new BoxLayout(preview, BoxLayout.Y_AXIS));
        TableModel model = new AbstractTableModel()
        {
            @Override
            public int getColumnCount()
            {
                return 4;
            }

            @Override
            public int getRowCount()
            {
                return 4;
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex)
            {
                return "Sample Text";
            }
        };
        CardTable table = new CardTable(model);
        table.setStripeColor(chooser.getColor());
        preview.add(table);

        chooser.getSelectionModel().addChangeListener((e) -> table.setStripeColor(chooser.getColor()));

        chooser.setPreviewPanel(preview);
    }

    /**
     * If the given file is contained the current directory, make it a relative file to
     * the current working directory.  If it is the current working directory, make it
     * "."  Otherwise, keep it what it is.
     *
     * @param f #File to relativize
     * @return relativized version of the given file.
     */
    private static File relativize(File f)
    {
        Path p = new File(".").getAbsoluteFile().getParentFile().toPath();
        Path fp = f.getAbsoluteFile().toPath();
        if (fp.startsWith(p))
        {
            fp = p.relativize(fp);
            if (fp.toString().isEmpty())
                f = new File(".");
            else
                f = fp.toFile();
        }
        return f;
    }

    /**
     * Add a new preset category.
     *
     * @param category specification of the preset category to add
     */
    public static void addPresetCategory(CategorySpec category)
    {
        PRESET_CATEGORIES.add(new CategorySpec(category));
    }

    /**
     * Convert a #Color to a String in the format <code>#AARRGGBB</code>.
     *
     * @param col #Color to convert
     * @return String code of the color.
     */
    public static String colorToString(Color col)
    {
        return colorToString(col, 4);
    }

    /**
     * Convert a #Color to a String in the format <code>#AARRGGBB</code>.
     *
     * @param col   #Color to convert
     * @param width minimum width of the color string
     * @return String code of the color.
     */
    public static String colorToString(Color col, int width)
    {
        return String.format("#%0" + (width * 2) + "X", col.getRGB()&((1L << (width * 8)) - 1));
    }

    /**
     * Get the boolean value of the given global setting.
     *
     * @param name name of the setting to get
     * @return true if the String value of the given setting is "true" and false otherwise.
     */
    public static boolean getAsBoolean(String name)
    {
        return Boolean.valueOf(SETTINGS.getProperty(name));
    }

    /**
     * Get the list of #CardAttribute types represented by the given global setting.
     *
     * @param name name of the setting to get
     * @return list of #CardAttribute that is represented by the setting
     * @throws IllegalArgumentException if the given setting is not a list of #CardAttribute types
     */
    public static List<CardAttribute> getAsCharacteristics(String name) throws IllegalArgumentException
    {
        try
        {
            return Arrays.stream(SETTINGS.getProperty(name).split(",")).map(CardAttribute::parseCardData).collect(Collectors.toList());
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException(name + " is not a setting for a list of CardAttribute types");
        }
    }

    /**
     * Get the #Color value of the given global setting.
     *
     * @param name name of the setting to get
     * @return the Color value of the given setting.
     * @throws IllegalArgumentException If the given setting does not represent a #Color.
     */
    public static Color getAsColor(String name) throws IllegalArgumentException
    {
        try
        {
            return stringToColor(SETTINGS.getProperty(name));
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException(name + " is not a color setting");
        }
    }

    /**
     * Get the integer value of the given global setting.
     *
     * @param name name of the setting to get
     * @return the integer value of the given setting.
     * @throws IllegalArgumentException if the given setting is not a numerical setting.
     */
    public static int getAsInt(String name) throws NumberFormatException
    {
        try
        {
            return Integer.valueOf(SETTINGS.getProperty(name));
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException(name + " is not a numeric setting");
        }
    }

    /**
     * @param name name of the setting to get
     * @return The string value of the setting with the given name.
     */
    public static String getAsString(String name)
    {
        return SETTINGS.getProperty(name);
    }

    /**
     * Get the default categories.
     *
     * @return the list of preset CategorySpecs.
     */
    public static List<CategorySpec> getPresetCategories()
    {
        return Collections.unmodifiableList(PRESET_CATEGORIES);
    }

    /**
     * Load global settings from the settings file.  This does not affect card tags.
     *
     * @throws FileNotFoundException if the settings file can't be found
     * @throws IOException           if an error occurred during loading
     */
    public static void load() throws IOException
    {
        resetDefaultSettings();
        try (InputStreamReader in = new InputStreamReader(new FileInputStream(PROPERTIES_FILE)))
        {
            SETTINGS.load(in);
        }
        catch (FileNotFoundException e)
        {
        }
    }

    /**
     * Load preset categories.
     *
     * @throws FileNotFoundException  if the settings file can't be found
     * @throws IOException            if an error occurred during loading
     * @throws ClassNotFoundException if a class of a category specification can't be found or restored
     */
    public static void loadPresetCategories() throws IOException, ClassNotFoundException
    {
        List<CategorySpec> presets = new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getAsString(EDITOR_PRESETS))))
        {
            int n = ois.readInt();
            for (int i = 0; i < n; i++)
            {
                CategorySpec preset = new CategorySpec();
                preset.readExternal(ois);
                presets.add(preset);
            }
        }
        catch (FileNotFoundException e)
        {
        }
        if (!presets.isEmpty())
        {
            PRESET_CATEGORIES.clear();
            PRESET_CATEGORIES.addAll(presets);
        }
    }

    /**
     * Set program settings back to their default values.  This does not change card tags.
     */
    public static void resetDefaultSettings()
    {
        SETTINGS.clear();
        SETTINGS.put(VERSION_FILE, "version.json");
        SETTINGS.put(INVENTORY_SOURCE, "http://mtgjson.com/json/");
        SETTINGS.put(VERSION, "");
        SETTINGS.put(INVENTORY_FILE, "AllSets-x.json");
        SETTINGS.put(INITIAL_CHECK, "true");
        SETTINGS.put(SUPPRESS_LOAD_WARNINGS, "false");
        SETTINGS.put(INVENTORY_LOCATION, ".");
        SETTINGS.put(INVENTORY_COLUMNS, "Name,Mana Cost,Type,Expansion");
        SETTINGS.put(INVENTORY_STRIPE, "#FFCCCCCC");
        SETTINGS.put(INITIALDIR, ".");
        SETTINGS.put(RECENT_COUNT, "4");
        SETTINGS.put(RECENT_FILES, "");
        SETTINGS.put(EXPLICITS_ROWS, "3");
        SETTINGS.put(CATEGORY_ROWS, "6");
        SETTINGS.put(EDITOR_COLUMNS, "Name,Count,Mana Cost,Type,Expansion,Rarity,Categories,Date Added");
        SETTINGS.put(EDITOR_STRIPE, "#FFCCCCCC");
        SETTINGS.put(EDITOR_PRESETS, "presets");
        SETTINGS.put(HAND_SIZE, "7");
        SETTINGS.put(EXPECTED_ROUND_MODE, "No rounding");
        SETTINGS.put(CARD_SCANS, "images" + File.separatorChar + "cards");
        SETTINGS.put(IMAGE_BGCOLOR, "#FFFFFFFF");
        SETTINGS.put(HAND_BGCOLOR, "#FFFFFFFF");

        PRESET_CATEGORIES.clear();
        CardTypeFilter artifacts = (CardTypeFilter) FilterAttribute.createFilter(FilterAttribute.CARD_TYPE);
        artifacts.selected.add("Artifact");
        PRESET_CATEGORIES.add(new CategorySpec("Artifacts", Collections.emptySet(), Collections.emptySet(), Color.WHITE, artifacts));
        CardTypeFilter creatures = (CardTypeFilter)FilterAttribute.createFilter(FilterAttribute.CARD_TYPE);
        creatures.selected.add("Creature");
        PRESET_CATEGORIES.add(new CategorySpec("Creatures", Collections.emptySet(), Collections.emptySet(), Color.WHITE, creatures));
        CardTypeFilter lands = (CardTypeFilter)FilterAttribute.createFilter(FilterAttribute.CARD_TYPE);
        lands.selected.add("Land");
        PRESET_CATEGORIES.add(new CategorySpec("Lands", Collections.emptySet(), Collections.emptySet(), Color.WHITE, lands));
        CardTypeFilter spells = (CardTypeFilter)FilterAttribute.createFilter(FilterAttribute.CARD_TYPE);
        spells.selected.addAll(List.of("Instant", "Sorcery"));
        PRESET_CATEGORIES.add(new CategorySpec("Instants/Sorceries", Collections.emptySet(), Collections.emptySet(), Color.WHITE, spells));
    }

    /**
     * Save preferences to file whose name is specified by the value of {@link #PROPERTIES_FILE}.
     *
     * @throws IOException if an exception occurred during saving.
     */
    public static void save() throws IOException
    {
        try (FileOutputStream out = new FileOutputStream(PROPERTIES_FILE))
        {
            StringBuilder str = new StringBuilder();
            for (Card c : Card.tags.keySet())
            {
                str.append("(");
                str.append(c.id());
                str.append("::");
                str.append(Card.tags.get(c).toString());
                str.append(")");
            }
            SETTINGS.put(CARD_TAGS, str.toString());
            SETTINGS.store(out, "Settings for the deck editor.  Don't touch this file; edit settings using the settings dialog!");
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getAsString(EDITOR_PRESETS))))
        {
            oos.writeInt(PRESET_CATEGORIES.size());
            for (CategorySpec preset : PRESET_CATEGORIES)
                preset.writeExternal(oos);
        }
    }

    /**
     * Set the value Of the given setting.
     *
     * @param name  name of the setting to set
     * @param value value to set it to
     * @throws IllegalArgumentException if the name isn't the name of an existing setting
     */
    public static void set(String name, Object value) throws IllegalArgumentException
    {
        if (!SETTINGS.containsKey(name))
            throw new IllegalArgumentException("Unknown setting name " + name);
        SETTINGS.put(name, String.valueOf(value));
    }

    /**
     * Decode an ARGB #Color from a String of either the format
     * <code>#AARRGGBB</code> or <code>#RRGGBB</code>.
     *
     * @param s String to parse
     * @return the #Color corresponding to the String.
     * @throw IllegalArgumentException if the given String does not represent a #Color.
     */
    public static Color stringToColor(String s) throws IllegalArgumentException
    {
        Matcher m = COLOR_PATTERN.matcher(s);
        if (m.matches())
        {
            Color col = Color.decode("#" + m.group(2));
            if (m.group(1) != null)
                col = new Color(col.getRed(), col.getGreen(), col.getBlue(), Integer.parseInt(m.group(1), 16));
            return col;
        }
        else
            throw new IllegalArgumentException("Illegal color string \"" + s + "\"");
    }

    /**
     * List of preset categories.
     */
    private CategoryList categoriesList;
    /**
     * Check boxes indicating which columns to show in editor tables.
     */
    private List<JCheckBox> editorColumnCheckBoxes;
    /**
     * Color chooser for the color of editor tables' alternate stripes.
     */
    private JColorChooser editorStripeColor;
    /**
     * Spinner allowing setting the number of rows to display in whitelists/blacklists
     * in the category editor.
     */
    private JSpinner explicitsSpinner;
    /**
     * Color chooser for the background of card images in the hand tab.
     */
    private JColorChooser handBGColor;
    /**
     * Check boxes indicating which columns to show in the inventory table.
     */
    private List<JCheckBox> inventoryColumnCheckBoxes;
    /**
     * Text field controlling the directory to store the inventory in once it is downloaded.
     */
    private JTextField inventoryDirField;
    /**
     * Text field controlling the name of the file to be downloaded.
     */
    private JTextField inventoryFileField;
    /**
     * Text field controlling the web site that the inventory should be downloaded from.
     */
    private JTextField inventorySiteField;
    /**
     * Color chooser for the color of alternate inventory table stripes.
     */
    private JColorChooser inventoryStripeColor;
    /**
     * Button indicating the rounding mode for the expected counts tab in the editor.
     */
    private List<JRadioButton> modeButtons;
    /**
     * MainFrame showing the dialog.
     */
    private MainFrame parent;
    /**
     * Name of the file containing preset categories.
     */
    private JTextField presetsFileField;
    /**
     * Spinner for the number of recent files to save.
     */
    private JSpinner recentSpinner;
    /**
     * Spinner allowing setting the maximum number of rows for category panels.
     */
    private JSpinner rowsSpinner;
    /**
     * Color chooser for the background of the card scan tab.
     */
    private JColorChooser scanBGChooser;
    /**
     * Text field containing the directory to look for card scans in.
     */
    private JTextField scansDirField;
    /**
     * Number of cards to draw in the starting hand.
     */
    private JSpinner startingSizeSpinner;
    /**
     * Check box indicating whether or not warnings after loading cards should be suppressed.
     */
    private JCheckBox suppressCheckBox;
    /**
     * Check box indicating whether or not to perform a check for updates on program start.
     */
    private JCheckBox updateCheckBox;

    /**
     * Create a new SettingsDialog.
     *
     * @param owner parent of the dialog
     */
    public SettingsDialog(MainFrame owner)
    {
        super(owner, "Preferences", Dialog.ModalityType.APPLICATION_MODAL);
        setResizable(false);

        parent = owner;

        // Tree
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Preferences");
        DefaultMutableTreeNode inventoryNode = new DefaultMutableTreeNode("Inventory");
        root.add(inventoryNode);
        DefaultMutableTreeNode inventoryAppearanceNode = new DefaultMutableTreeNode("Appearance");
        inventoryNode.add(inventoryAppearanceNode);
        DefaultMutableTreeNode editorNode = new DefaultMutableTreeNode("Editor");
        DefaultMutableTreeNode editorCategoriesNode = new DefaultMutableTreeNode("Preset Categories");
        editorNode.add(editorCategoriesNode);
        DefaultMutableTreeNode editorAppearanceNode = new DefaultMutableTreeNode("Appearance");
        editorNode.add(editorAppearanceNode);
        DefaultMutableTreeNode handAppearanceNode = new DefaultMutableTreeNode("Sample Hand");
        editorNode.add(handAppearanceNode);
        root.add(editorNode);

        // Settings panels
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new CardLayout());
        add(settingsPanel, BorderLayout.CENTER);

        // Inventory paths
        JPanel inventoryPanel = new JPanel();
        inventoryPanel.setLayout(new BoxLayout(inventoryPanel, BoxLayout.Y_AXIS));
        inventoryPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        settingsPanel.add(inventoryPanel, new TreePath(inventoryNode.getPath()).toString());

        // Inventory site
        JPanel inventorySitePanel = new JPanel();
        inventorySitePanel.setLayout(new BoxLayout(inventorySitePanel, BoxLayout.X_AXIS));
        inventorySitePanel.add(new JLabel("Inventory Site:"));
        inventorySitePanel.add(Box.createHorizontalStrut(5));
        inventorySiteField = new JTextField(15);
        inventorySiteField.setText(getAsString(INVENTORY_SOURCE));
        inventorySitePanel.add(inventorySiteField);
        inventorySitePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, inventorySitePanel.getPreferredSize().height));
        inventoryPanel.add(inventorySitePanel);
        inventoryPanel.add(Box.createVerticalStrut(5));

        // Inventory file name
        JPanel inventoryFilePanel = new JPanel();
        inventoryFilePanel.setLayout(new BoxLayout(inventoryFilePanel, BoxLayout.X_AXIS));
        inventoryFilePanel.add(new JLabel("Inventory File:"));
        inventoryFilePanel.add(Box.createHorizontalStrut(5));
        inventoryFileField = new JTextField(10);
        inventoryFileField.setText(getAsString(INVENTORY_FILE));
        inventoryFilePanel.add(inventoryFileField);
        inventoryFilePanel.add(Box.createHorizontalStrut(5));
        JLabel currentVersionLabel = new JLabel("(Current version: " + getAsString(VERSION) + ")");
        currentVersionLabel.setFont(new Font(currentVersionLabel.getFont().getFontName(), Font.ITALIC, currentVersionLabel.getFont().getSize()));
        inventoryFilePanel.add(currentVersionLabel);
        inventoryFilePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, inventoryFilePanel.getPreferredSize().height));
        inventoryPanel.add(inventoryFilePanel);
        inventoryPanel.add(Box.createVerticalStrut(5));

        // Inventory file directory
        JPanel inventoryDirPanel = new JPanel();
        inventoryDirPanel.setLayout(new BoxLayout(inventoryDirPanel, BoxLayout.X_AXIS));
        inventoryDirPanel.add(new JLabel("Inventory File Location:"));
        inventoryDirPanel.add(Box.createHorizontalStrut(5));
        inventoryDirField = new JTextField(25);
        JFileChooser inventoryChooser = new JFileChooser();
        inventoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        inventoryChooser.setAcceptAllFileFilterUsed(false);
        inventoryDirField.setText(getAsString(INVENTORY_LOCATION));
        inventoryChooser.setCurrentDirectory(new File(inventoryDirField.getText()).getAbsoluteFile());
        inventoryDirPanel.add(inventoryDirField);
        inventoryDirPanel.add(Box.createHorizontalStrut(5));
        JButton inventoryDirButton = new JButton(String.valueOf(UnicodeSymbols.ELLIPSIS));
        inventoryDirButton.addActionListener((e) -> {
            if (inventoryChooser.showDialog(null, "Select Folder") == JFileChooser.APPROVE_OPTION)
            {
                File f = relativize(inventoryChooser.getSelectedFile());
                inventoryDirField.setText(f.getPath());
                inventoryChooser.setCurrentDirectory(f);
            }
        });
        inventoryDirPanel.add(inventoryDirButton);
        inventoryDirPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, inventoryDirPanel.getPreferredSize().height));
        inventoryPanel.add(inventoryDirPanel);
        inventoryPanel.add(Box.createVerticalStrut(5));

        // Card scans directory
        JPanel scansDirPanel = new JPanel();
        scansDirPanel.setLayout(new BoxLayout(scansDirPanel, BoxLayout.X_AXIS));
        scansDirPanel.add(new JLabel("Card Images Location:"));
        scansDirPanel.add(Box.createHorizontalStrut(5));
        scansDirField = new JTextField(25);
        JFileChooser scansChooser = new JFileChooser();
        scansChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        scansChooser.setAcceptAllFileFilterUsed(false);
        scansDirField.setText(getAsString(CARD_SCANS));
        scansChooser.setCurrentDirectory(new File(scansDirField.getText()).getAbsoluteFile());
        scansDirPanel.add(scansDirField);
        scansDirPanel.add(Box.createHorizontalStrut(5));
        JButton scansDirButton = new JButton(String.valueOf(UnicodeSymbols.ELLIPSIS));
        scansDirButton.addActionListener((e) -> {
            if (scansChooser.showDialog(null, "Select Folder") == JFileChooser.APPROVE_OPTION)
            {
                File f = relativize(scansChooser.getSelectedFile());
                scansDirField.setText(f.getPath());
                scansChooser.setCurrentDirectory(f);
            }
        });
        scansDirPanel.add(scansDirButton);
        scansDirPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, scansDirPanel.getPreferredSize().height));
        inventoryPanel.add(scansDirPanel);
        inventoryPanel.add(Box.createVerticalStrut(5));

        // Check for update on startup
        JPanel updatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        updateCheckBox = new JCheckBox("Check for update on program start", getAsBoolean(INITIAL_CHECK));
        updatePanel.add(updateCheckBox);
        updatePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, updatePanel.getPreferredSize().height));
        inventoryPanel.add(updatePanel);

        // Suppress warnings after loading cards
        JPanel suppressPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        suppressCheckBox = new JCheckBox("Suppress warnings while loading cards", getAsBoolean(SUPPRESS_LOAD_WARNINGS));
        suppressPanel.add(suppressCheckBox);
        suppressPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, suppressPanel.getPreferredSize().height));
        inventoryPanel.add(suppressPanel);

        inventoryPanel.add(Box.createVerticalGlue());

        // Warning panel
        JPanel pathWarningPanel = new JPanel(new BorderLayout());
        JLabel pathWarningLabel = new JLabel("Warning:  Changing these settings may break functionality");
        pathWarningLabel.setFont(new Font(pathWarningLabel.getFont().getFontName(), Font.ITALIC, pathWarningLabel.getFont().getSize()));
        pathWarningLabel.setForeground(Color.RED);
        pathWarningPanel.add(pathWarningLabel);
        pathWarningPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, pathWarningLabel.getPreferredSize().height));
        inventoryPanel.add(pathWarningPanel);

        // Inventory appearance
        ScrollablePanel inventoryAppearancePanel = new ScrollablePanel(ScrollablePanel.TRACK_WIDTH);
        inventoryAppearancePanel.setLayout(new BoxLayout(inventoryAppearancePanel, BoxLayout.Y_AXIS));
        JScrollPane inventoryAppearanceScroll = new JScrollPane(inventoryAppearancePanel);
        inventoryAppearanceScroll.setBorder(BorderFactory.createEmptyBorder());
        settingsPanel.add(inventoryAppearanceScroll, new TreePath(inventoryAppearanceNode.getPath()).toString());

        // Columns
        JPanel inventoryColumnsPanel = new JPanel(new GridLayout(0, 5));
        inventoryColumnsPanel.setBorder(BorderFactory.createTitledBorder("Columns"));
        inventoryColumnCheckBoxes = new ArrayList<>();
        for (CardAttribute characteristic : CardAttribute.inventoryValues())
        {
            JCheckBox checkBox = new JCheckBox(characteristic.toString());
            inventoryColumnCheckBoxes.add(checkBox);
            inventoryColumnsPanel.add(checkBox);
            checkBox.setSelected(getAsString(INVENTORY_COLUMNS).contains(characteristic.toString()));
        }
        inventoryAppearancePanel.add(inventoryColumnsPanel);

        // Stripe color
        JPanel inventoryColorPanel = new JPanel(new BorderLayout());
        inventoryColorPanel.setBorder(BorderFactory.createTitledBorder("Stripe Color"));
        inventoryStripeColor = new JColorChooser(getAsColor(INVENTORY_STRIPE));
        createStripeChooserPreview(inventoryStripeColor);
        inventoryColorPanel.add(inventoryStripeColor);
        inventoryAppearancePanel.add(inventoryColorPanel);

        // Card image background color
        JPanel scanBGPanel = new JPanel(new BorderLayout());
        scanBGPanel.setBorder(BorderFactory.createTitledBorder("Image Background Color"));
        scanBGChooser = new JColorChooser(getAsColor(IMAGE_BGCOLOR));
        scanBGChooser.getSelectionModel().addChangeListener((e) -> parent.setImageBackground(scanBGChooser.getColor()));
        scanBGPanel.add(scanBGChooser);
        inventoryAppearancePanel.add(scanBGPanel);

        // Editor
        JPanel editorPanel = new JPanel();
        editorPanel.setLayout(new BoxLayout(editorPanel, BoxLayout.Y_AXIS));
        editorPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        settingsPanel.add(editorPanel, new TreePath(editorNode.getPath()).toString());

        // Recent count
        JPanel recentPanel = new JPanel();
        recentPanel.setLayout(new BoxLayout(recentPanel, BoxLayout.X_AXIS));
        recentPanel.add(new JLabel("Recent file count:"));
        recentPanel.add(Box.createHorizontalStrut(5));
        recentSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        recentSpinner.getModel().setValue(Integer.valueOf(getAsString(RECENT_COUNT)));
        recentPanel.add(recentSpinner);
        recentPanel.add(Box.createHorizontalStrut(5));
        JLabel recentInfoLabel = new JLabel("(Changes will not be visible until program restart)");
        recentInfoLabel.setFont(new Font(recentInfoLabel.getFont().getFontName(), Font.ITALIC, recentInfoLabel.getFont().getSize()));
        recentPanel.add(recentInfoLabel);
        recentPanel.setMaximumSize(recentPanel.getPreferredSize());
        recentPanel.setAlignmentX(LEFT_ALIGNMENT);
        editorPanel.add(recentPanel);
        editorPanel.add(Box.createVerticalStrut(5));

        // Whitelist and blacklist rows to show
        JPanel explicitsPanel = new JPanel();
        explicitsPanel.setLayout(new BoxLayout(explicitsPanel, BoxLayout.X_AXIS));
        explicitsPanel.add(new JLabel("Blacklist/Whitelist rows to display:"));
        explicitsPanel.add(Box.createHorizontalStrut(5));
        explicitsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        explicitsSpinner.getModel().setValue(Integer.valueOf(getAsString(EXPLICITS_ROWS)));
        explicitsPanel.add(explicitsSpinner);
        explicitsPanel.setMaximumSize(explicitsPanel.getPreferredSize());
        explicitsPanel.setAlignmentX(LEFT_ALIGNMENT);
        editorPanel.add(explicitsPanel);
        editorPanel.add(Box.createVerticalStrut(5));

        // Presets file name
        JPanel presetsFilePanel = new JPanel();
        presetsFilePanel.setLayout(new BoxLayout(presetsFilePanel, BoxLayout.X_AXIS));
        presetsFilePanel.add(new JLabel("Preset Categories File:"));
        presetsFilePanel.add(Box.createHorizontalStrut(5));
        presetsFileField = new JTextField(10);
        presetsFileField.setText(getAsString(EDITOR_PRESETS));
        presetsFilePanel.add(presetsFileField);
        presetsFilePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, presetsFilePanel.getPreferredSize().height));
        presetsFilePanel.setAlignmentX(LEFT_ALIGNMENT);
        editorPanel.add(presetsFilePanel);

        editorPanel.add(Box.createVerticalGlue());

        // Editor categories
        JPanel categoriesPanel = new JPanel();
        categoriesPanel.setLayout(new BorderLayout(5, 0));
        categoriesPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        settingsPanel.add(categoriesPanel, new TreePath(editorCategoriesNode.getPath()).toString());
        categoriesList = new CategoryList("<html><i>&lt;Double-click to add or edit&gt;</i></html>");
        if (!PRESET_CATEGORIES.isEmpty())
            for (CategorySpec preset : PRESET_CATEGORIES)
                categoriesList.addCategory(new CategorySpec(preset));
        categoriesPanel.add(new JScrollPane(categoriesList), BorderLayout.CENTER);

        // Category modification buttons
        VerticalButtonList categoryModPanel = new VerticalButtonList("+", String.valueOf(UnicodeSymbols.ELLIPSIS), String.valueOf(UnicodeSymbols.MINUS));
        categoryModPanel.get("+").addActionListener((e) -> {
            CategorySpec spec = CategoryEditorPanel.showCategoryEditor(this);
            if (spec != null)
                categoriesList.addCategory(spec);
        });
        categoryModPanel.get(String.valueOf(UnicodeSymbols.ELLIPSIS)).addActionListener((e) -> {
            if (categoriesList.getSelectedIndex() >= 0)
            {
                CategorySpec spec = CategoryEditorPanel.showCategoryEditor(this, categoriesList.getCategoryAt(categoriesList.getSelectedIndex()));
                if (spec != null)
                    categoriesList.setCategoryAt(categoriesList.getSelectedIndex(), spec);
            }
        });
        categoryModPanel.get(String.valueOf(UnicodeSymbols.MINUS)).addActionListener((e) -> {
            if (categoriesList.getSelectedIndex() >= 0)
                categoriesList.removeCategoryAt(categoriesList.getSelectedIndex());
        });
        categoriesPanel.add(categoryModPanel, BorderLayout.EAST);

        // Editor appearance
        JPanel editorAppearancePanel = new JPanel();
        editorAppearancePanel.setLayout(new BoxLayout(editorAppearancePanel, BoxLayout.Y_AXIS));
        settingsPanel.add(editorAppearancePanel, new TreePath(editorAppearanceNode.getPath()).toString());

        // Editor category rows
        JPanel rowsPanel = new JPanel();
        rowsPanel.setLayout(new BoxLayout(rowsPanel, BoxLayout.X_AXIS));
        rowsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));
        rowsPanel.add(new JLabel("Maximum displayed rows in categories:"));
        rowsPanel.add(Box.createHorizontalStrut(5));
        rowsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        rowsSpinner.getModel().setValue(Integer.valueOf(getAsString(CATEGORY_ROWS)));
        rowsPanel.add(rowsSpinner);
        rowsPanel.setMaximumSize(rowsPanel.getPreferredSize());
        rowsPanel.setAlignmentX(LEFT_ALIGNMENT);
        editorAppearancePanel.add(rowsPanel);

        // Editor table columns
        JPanel editorColumnsPanel = new JPanel(new GridLayout(0, 5));
        editorColumnsPanel.setBorder(BorderFactory.createTitledBorder("Columns"));
        editorColumnCheckBoxes = new ArrayList<>();
        for (CardAttribute characteristic : CardAttribute.values())
        {
            JCheckBox checkBox = new JCheckBox(characteristic.toString());
            editorColumnCheckBoxes.add(checkBox);
            editorColumnsPanel.add(checkBox);
            checkBox.setSelected(getAsString(EDITOR_COLUMNS).contains(characteristic.toString()));
        }
        editorColumnsPanel.setAlignmentX(LEFT_ALIGNMENT);
        editorAppearancePanel.add(editorColumnsPanel);

        // Editor table stripe color
        JPanel editorColorPanel = new JPanel(new BorderLayout());
        editorColorPanel.setBorder(BorderFactory.createTitledBorder("Stripe Color"));
        editorStripeColor = new JColorChooser(getAsColor(EDITOR_STRIPE));
        createStripeChooserPreview(editorStripeColor);
        editorColorPanel.add(editorStripeColor);
        editorColorPanel.setAlignmentX(LEFT_ALIGNMENT);
        editorAppearancePanel.add(editorColorPanel);

        editorAppearancePanel.add(Box.createVerticalGlue());

        // Sample hand
        ScrollablePanel sampleHandPanel = new ScrollablePanel(ScrollablePanel.TRACK_WIDTH);
        sampleHandPanel.setLayout(new BoxLayout(sampleHandPanel, BoxLayout.Y_AXIS));
        JScrollPane sampleHandScroll = new JScrollPane(sampleHandPanel);
        sampleHandScroll.setBorder(BorderFactory.createEmptyBorder());
        settingsPanel.add(sampleHandScroll, new TreePath(handAppearanceNode.getPath()).toString());

        sampleHandPanel.add(Box.createVerticalStrut(5));

        // Starting Size
        JPanel startingSizePanel = new JPanel();
        startingSizePanel.setLayout(new BoxLayout(startingSizePanel, BoxLayout.X_AXIS));
        startingSizePanel.add(Box.createHorizontalStrut(5));
        startingSizePanel.add(new JLabel("Starting Size:"));
        startingSizePanel.add(Box.createHorizontalStrut(5));
        startingSizeSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        startingSizeSpinner.getModel().setValue(Integer.valueOf(getAsString(HAND_SIZE)));
        startingSizePanel.add(startingSizeSpinner);
        startingSizePanel.add(Box.createHorizontalGlue());
        startingSizePanel.setMaximumSize(startingSizePanel.getPreferredSize());
        startingSizePanel.setAlignmentX(LEFT_ALIGNMENT);
        sampleHandPanel.add(startingSizePanel);

        sampleHandPanel.add(Box.createVerticalStrut(5));

        // Expected counts round mode
        JPanel expectedRoundPanel = new JPanel();
        expectedRoundPanel.setLayout(new BoxLayout(expectedRoundPanel, BoxLayout.X_AXIS));
        expectedRoundPanel.add(Box.createHorizontalStrut(5));
        expectedRoundPanel.add(new JLabel("Expected Category Count Round Mode:"));
        expectedRoundPanel.add(Box.createHorizontalStrut(5));
        ButtonGroup roundGroup = new ButtonGroup();
        modeButtons = new ArrayList<>();
        for (String mode : CalculateHandPanel.ROUND_MODE.keySet().stream().sorted().collect(Collectors.toList()))
        {
            JRadioButton modeButton = new JRadioButton(mode);
            roundGroup.add(modeButton);
            expectedRoundPanel.add(modeButton);
            expectedRoundPanel.add(Box.createHorizontalStrut(5));
            modeButton.setSelected(mode.equals(getAsString(EXPECTED_ROUND_MODE)));
            modeButtons.add(modeButton);
        }
        expectedRoundPanel.setMaximumSize(expectedRoundPanel.getPreferredSize());
        expectedRoundPanel.setAlignmentX(LEFT_ALIGNMENT);
        sampleHandPanel.add(expectedRoundPanel);

        sampleHandPanel.add(Box.createVerticalStrut(5));

        // Sample hand background color
        JPanel handBGColorPanel = new JPanel(new BorderLayout());
        handBGColorPanel.setBorder(BorderFactory.createTitledBorder("Background Color"));
        handBGColor = new JColorChooser(getAsColor(HAND_BGCOLOR));
        handBGColor.getSelectionModel().addChangeListener((e) -> parent.setHandBackground(handBGColor.getColor()));
        handBGColorPanel.add(handBGColor);
        handBGColorPanel.setAlignmentX(LEFT_ALIGNMENT);
        sampleHandPanel.add(handBGColorPanel);

        // Tree panel
        JPanel treePanel = new JPanel(new BorderLayout());
        JTree tree = new JTree(root);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        ((DefaultTreeCellRenderer)tree.getCellRenderer()).setLeafIcon(null);
        tree.addTreeSelectionListener((e) -> ((CardLayout)settingsPanel.getLayout()).show(settingsPanel, e.getPath().toString()));
        treePanel.add(tree, BorderLayout.CENTER);
        treePanel.add(new JSeparator(SwingConstants.VERTICAL), BorderLayout.EAST);
        treePanel.setPreferredSize(new Dimension(130, 0));
        add(treePanel, BorderLayout.WEST);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton applyButton = new JButton("Apply");
        applyButton.addActionListener((e) -> confirmSettings());
        buttonPanel.add(applyButton);

        JButton okButton = new JButton("OK");
        okButton.addActionListener((e) -> {
            confirmSettings();
            dispose();
        });
        buttonPanel.add(okButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener((e) -> {
            rejectSettings();
            dispose();
        });
        buttonPanel.add(cancelButton);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    /**
     * Confirm the settings applied by the components of the dialog and send them to the parent
     * MainFrame.
     */
    public void confirmSettings()
    {
        SETTINGS.put(INVENTORY_SOURCE, inventorySiteField.getText());
        SETTINGS.put(INVENTORY_FILE, inventoryFileField.getText());
        SETTINGS.put(INVENTORY_LOCATION, inventoryDirField.getText());
        SETTINGS.put(INITIAL_CHECK, Boolean.toString(updateCheckBox.isSelected()));
        SETTINGS.put(SUPPRESS_LOAD_WARNINGS, Boolean.toString(suppressCheckBox.isSelected()));
        StringJoiner join = new StringJoiner(",");
        for (JCheckBox box : inventoryColumnCheckBoxes)
            if (box.isSelected())
                join.add(box.getText());
        SETTINGS.put(INVENTORY_COLUMNS, join.toString());
        SETTINGS.put(INVENTORY_STRIPE, colorToString(inventoryStripeColor.getColor()));
        SETTINGS.put(RECENT_COUNT, recentSpinner.getValue().toString());
        SETTINGS.put(EXPLICITS_ROWS, explicitsSpinner.getValue().toString());
        SETTINGS.put(CATEGORY_ROWS, rowsSpinner.getValue().toString());
        join = new StringJoiner(",");
        for (JCheckBox box : editorColumnCheckBoxes)
            if (box.isSelected())
                join.add(box.getText());
        SETTINGS.put(EDITOR_COLUMNS, join.toString());
        SETTINGS.put(EDITOR_STRIPE, colorToString(editorStripeColor.getColor()));
        SETTINGS.put(EDITOR_PRESETS, presetsFileField.getText());
        PRESET_CATEGORIES.clear();
        for (int i = 0; i < categoriesList.getCount(); i++)
            addPresetCategory(categoriesList.getCategoryAt(i));
        SETTINGS.put(HAND_SIZE, startingSizeSpinner.getValue().toString());
        for (JRadioButton modeButton : modeButtons)
            if (modeButton.isSelected())
                SETTINGS.put(EXPECTED_ROUND_MODE, modeButton.getText());
        SETTINGS.put(CARD_SCANS, scansDirField.getText());
        SETTINGS.put(IMAGE_BGCOLOR, colorToString(scanBGChooser.getColor()));
        SETTINGS.put(HAND_BGCOLOR, colorToString(handBGColor.getColor()));

        parent.applySettings();
    }

    /**
     * Reject any changes that were made as a result of using the settings dialog.
     */
    public void rejectSettings()
    {
        parent.setImageBackground(getAsColor(IMAGE_BGCOLOR));
        parent.setHandBackground(getAsColor(HAND_BGCOLOR));
    }
}
