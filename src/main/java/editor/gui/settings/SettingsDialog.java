package editor.gui.settings;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import editor.collection.deck.CategorySpec;
import editor.database.FormatConstraints;
import editor.database.attributes.CardAttribute;
import editor.database.card.Card;
import editor.database.version.DatabaseVersion;
import editor.database.version.UpdateFrequency;
import editor.gui.MainFrame;
import editor.gui.display.CardTable;
import editor.gui.display.CategoryList;
import editor.gui.editor.CalculateHandPanel;
import editor.gui.editor.CategoryEditorPanel;
import editor.gui.generic.ScrollablePanel;
import editor.gui.generic.VerticalButtonList;
import editor.util.UnicodeSymbols;

/**
 * This class is a dialog that allows the user to change various properties about
 * the program.
 *
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class SettingsDialog extends JDialog
{
    /**
     * Settings structure containing global settings.
     */
    private static Settings settings;
    private static List<String> inventoryWarnings;

    /**
     * Pattern to match when parsing an ARGB color from a string to a @link{java.awt.Color}
     */
    public static final Pattern COLOR_PATTERN = Pattern.compile("^#([0-9a-fA-F]{2})?([0-9a-fA-F]{6})$");
    /**
     * Number of cards in a playset.
     */
    public static final int PLAYSET_SIZE = 4;
    /**
     * File containing serialized settings.
     */
    public static final String PROPERTIES_FILE = "settings.json";

    /**
     * Create the preview panel for a color chooser that customizes the stripe color
     * of a #CardTable.
     *
     * @param chooser #JColorChooser to create the new preview panel for
     */
    private static void createStripeChooserPreview(JColorChooser chooser)
    {
        Box preview = new Box(BoxLayout.Y_AXIS);
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
        settings = new SettingsBuilder(settings).addPresetCategory(category).build();
    }

    /**
     * Load global settings from the settings file.  This does not affect card tags.
     *
     * @throws IOException if an error occurred during loading
     */
    public static void load() throws IOException
    {
        if (Files.exists(Path.of(PROPERTIES_FILE)))
            settings = MainFrame.SERIALIZER.fromJson(String.join("\n", Files.readAllLines(Path.of(PROPERTIES_FILE))), Settings.class);
        else
            resetDefaultSettings();
    }

    /**
     * Set program settings back to their default values.  This does not change card tags.
     */
    public static void resetDefaultSettings()
    {
        settings = new SettingsBuilder().defaults().build();
    }

    /**
     * Save preferences to file whose name is specified by the value of {@link #PROPERTIES_FILE}.
     * Also save tags to the separate tags file.
     *
     * @throws IOException if an exception occurred during saving.
     */
    public static void save() throws IOException
    {
        if (!Card.tags.isEmpty())
            Files.writeString(Paths.get(settings.inventory.tags), MainFrame.SERIALIZER.toJson(Card.tags.entrySet().stream().collect(Collectors.toMap((e) -> e.getKey().multiverseid().get(0), Map.Entry::getValue))));
        else
            Files.deleteIfExists(Paths.get(settings.inventory.tags));
        Files.writeString(Paths.get(PROPERTIES_FILE), MainFrame.SERIALIZER.toJson(settings));
    }

    /**
     * Update the recently-edited files.
     * 
     * @param files list of recently-edited files
     */
    public static void setRecents(List<String> files)
    {
        settings = new SettingsBuilder(settings).recentsFiles(files).build();
    }

    /**
     * Update the directory to start the file chooser in.
     * 
     * @param dir starting directory for file chooser
     */
    public static void setStartingDir(String dir)
    {
        settings = new SettingsBuilder(settings).cwd(dir).build();
    }

    /**
     * @return The global settings structure.
     * @see Settings
     */
    public static Settings settings()
    {
        return settings;
    }

    /**
     * Set the version of the inventory to know when it is out of date.
     * 
     * @param version new version of the inventory
     */
    public static void setInventoryVersion(DatabaseVersion version)
    {
        settings = new SettingsBuilder(settings).inventoryVersion(version).build();
    }

    /**
     * Set whether or not to show warnings after finishing loading the inventory.
     * 
     * @param warn whether or not to show warnings
     */
    public static void setShowInventoryWarnings(boolean warn)
    {
        settings = new SettingsBuilder(settings).inventoryWarn(warn).build();
    }

    public static void setInventoryWarnings(List<String> warnings)
    {
        inventoryWarnings = new ArrayList<>(warnings);
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
     * Combo box indicating how often to download updates.
     */
    private JComboBox<UpdateFrequency> updateBox;

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
        DefaultMutableTreeNode formatsNode = new DefaultMutableTreeNode("Formats");
        editorNode.add(formatsNode);
        root.add(editorNode);

        // Settings panels
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new CardLayout());
        add(settingsPanel, BorderLayout.CENTER);

        // Inventory paths
        Box inventoryPanel = new Box(BoxLayout.Y_AXIS);
        inventoryPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        settingsPanel.add(inventoryPanel, new TreePath(inventoryNode.getPath()).toString());

        // Inventory site
        Box inventorySitePanel = new Box(BoxLayout.X_AXIS);
        inventorySitePanel.add(new JLabel("Inventory Site:"));
        JLabel siteStarLabel = new JLabel("*");
        siteStarLabel.setForeground(Color.RED);
        inventorySitePanel.add(siteStarLabel);
        inventorySitePanel.add(Box.createHorizontalStrut(5));
        inventorySiteField = new JTextField(15);
        inventorySiteField.setText(settings.inventory.source);
        inventorySitePanel.add(inventorySiteField);
        inventorySitePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, inventorySitePanel.getPreferredSize().height));
        inventoryPanel.add(inventorySitePanel);
        inventoryPanel.add(Box.createVerticalStrut(5));

        // Inventory file name
        Box inventoryFilePanel = new Box(BoxLayout.X_AXIS);
        inventoryFilePanel.add(new JLabel("Inventory File:"));
        JLabel fileStarLabel = new JLabel("*");
        fileStarLabel.setForeground(Color.RED);
        inventoryFilePanel.add(fileStarLabel);
        inventoryFilePanel.add(Box.createHorizontalStrut(5));
        inventoryFileField = new JTextField(10);
        inventoryFileField.setText(settings.inventory.file);
        inventoryFilePanel.add(inventoryFileField);
        inventoryFilePanel.add(Box.createHorizontalStrut(5));
        JLabel currentVersionLabel = new JLabel("(Current version: " + settings.inventory.version + ")");
        currentVersionLabel.setFont(new Font(currentVersionLabel.getFont().getFontName(), Font.ITALIC, currentVersionLabel.getFont().getSize()));
        inventoryFilePanel.add(currentVersionLabel);
        inventoryFilePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, inventoryFilePanel.getPreferredSize().height));
        inventoryPanel.add(inventoryFilePanel);
        inventoryPanel.add(Box.createVerticalStrut(5));

        // Inventory file directory
        Box inventoryDirPanel = new Box(BoxLayout.X_AXIS);
        inventoryDirPanel.add(new JLabel("Inventory File Location:"));
        inventoryDirPanel.add(Box.createHorizontalStrut(5));
        inventoryDirField = new JTextField(25);
        JFileChooser inventoryChooser = new JFileChooser();
        inventoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        inventoryChooser.setAcceptAllFileFilterUsed(false);
        inventoryDirField.setText(settings.inventory.location);
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
        Box scansDirPanel = new Box(BoxLayout.X_AXIS);
        scansDirPanel.add(new JLabel("Card Images Location:"));
        scansDirPanel.add(Box.createHorizontalStrut(5));
        scansDirField = new JTextField(25);
        JFileChooser scansChooser = new JFileChooser();
        scansChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        scansChooser.setAcceptAllFileFilterUsed(false);
        scansDirField.setText(settings.inventory.scans);
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
        JLabel updateLabel = new JLabel("Update inventory on:");
        updatePanel.add(updateLabel);
        updatePanel.add(Box.createHorizontalStrut(5));
        updateBox = new JComboBox<>(UpdateFrequency.values());
        updateBox.setSelectedIndex(settings.inventory.update.ordinal());
        updatePanel.add(updateBox);
        updatePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, updatePanel.getPreferredSize().height));
        inventoryPanel.add(updatePanel);
        inventoryPanel.add(Box.createVerticalStrut(5));

        // Show warnings from loading inventory
        JPanel suppressPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        suppressCheckBox = new JCheckBox("Show warnings after loading inventory", settings.inventory.warn);
        suppressPanel.add(suppressCheckBox);
        suppressPanel.add(Box.createHorizontalStrut(5));
        JButton viewWarningsButton = new JButton("View Warnings");
        viewWarningsButton.setEnabled(!inventoryWarnings.isEmpty());
        viewWarningsButton.addActionListener((e) -> {
            StringJoiner join = new StringJoiner("<li>", "<html>", "</ul></html>");
            join.add("Warnings from last inventory load:<ul style=\"margin-top:0;margin-left:20pt\">");
            for (String warning : inventoryWarnings)
                join.add(warning);
            JOptionPane.showMessageDialog(this, join.toString(), "Inventory Warnings", JOptionPane.WARNING_MESSAGE);
        });
        suppressPanel.add(viewWarningsButton);
        suppressPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, suppressPanel.getPreferredSize().height));
        inventoryPanel.add(suppressPanel);

        inventoryPanel.add(Box.createVerticalGlue());

        // Warning panel
        JPanel pathWarningPanel = new JPanel(new BorderLayout());
        JLabel pathWarningLabel = new JLabel("*Warning:  Changing these settings may break functionality");
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
        var inventoryAttributes = Arrays.stream(CardAttribute.inventoryValues()).sorted((a, b) -> {
            return a.toString().compareTo(b.toString());
        }).collect(Collectors.toList());
        for (CardAttribute characteristic : inventoryAttributes)
        {
            JCheckBox checkBox = new JCheckBox(characteristic.toString());
            inventoryColumnCheckBoxes.add(checkBox);
            inventoryColumnsPanel.add(checkBox);
            checkBox.setSelected(settings.inventory.columns.contains(characteristic));
        }
        inventoryAppearancePanel.add(inventoryColumnsPanel);

        // Stripe color
        JPanel inventoryColorPanel = new JPanel(new BorderLayout());
        inventoryColorPanel.setBorder(BorderFactory.createTitledBorder("Stripe Color"));
        inventoryStripeColor = new JColorChooser(settings.inventory.stripe);
        createStripeChooserPreview(inventoryStripeColor);
        inventoryColorPanel.add(inventoryStripeColor);
        inventoryAppearancePanel.add(inventoryColorPanel);

        // Card image background color
        JPanel scanBGPanel = new JPanel(new BorderLayout());
        scanBGPanel.setBorder(BorderFactory.createTitledBorder("Image Background Color"));
        scanBGChooser = new JColorChooser(settings.inventory.background);
        scanBGChooser.getSelectionModel().addChangeListener((e) -> parent.setImageBackground(scanBGChooser.getColor()));
        scanBGPanel.add(scanBGChooser);
        inventoryAppearancePanel.add(scanBGPanel);

        // Editor
        Box editorPanel = new Box(BoxLayout.Y_AXIS);
        editorPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        settingsPanel.add(editorPanel, new TreePath(editorNode.getPath()).toString());

        // Recent count
        Box recentPanel = new Box(BoxLayout.X_AXIS);
        recentPanel.add(new JLabel("Recent file count:"));
        recentPanel.add(Box.createHorizontalStrut(5));
        recentSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        recentSpinner.getModel().setValue(settings.editor.recents.count);
        recentPanel.add(recentSpinner);
        recentPanel.add(Box.createHorizontalStrut(5));
        JLabel recentInfoLabel = new JLabel("(Changes will not be visible until program restart)");
        recentInfoLabel.setFont(new Font(recentInfoLabel.getFont().getFontName(), Font.ITALIC, recentInfoLabel.getFont().getSize()));
        recentPanel.add(recentInfoLabel);
        recentPanel.setMaximumSize(new Dimension(recentPanel.getPreferredSize().width + 10, recentPanel.getPreferredSize().height));
        recentPanel.setAlignmentX(LEFT_ALIGNMENT);
        editorPanel.add(recentPanel);
        editorPanel.add(Box.createVerticalStrut(5));

        // Whitelist and blacklist rows to show
        Box explicitsPanel = new Box(BoxLayout.X_AXIS);
        explicitsPanel.add(new JLabel("Blacklist/Whitelist rows to display:"));
        explicitsPanel.add(Box.createHorizontalStrut(5));
        explicitsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        explicitsSpinner.getModel().setValue(Integer.valueOf(settings.editor.categories.explicits));
        explicitsPanel.add(explicitsSpinner);
        explicitsPanel.setMaximumSize(new Dimension(explicitsPanel.getPreferredSize().width + 5, explicitsPanel.getPreferredSize().height));
        explicitsPanel.setAlignmentX(LEFT_ALIGNMENT);
        editorPanel.add(explicitsPanel);
        editorPanel.add(Box.createVerticalStrut(5));

        editorPanel.add(Box.createVerticalGlue());

        // Editor categories
        JPanel categoriesPanel = new JPanel();
        categoriesPanel.setLayout(new BorderLayout(5, 0));
        categoriesPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        settingsPanel.add(categoriesPanel, new TreePath(editorCategoriesNode.getPath()).toString());
        categoriesList = new CategoryList("<html><i>&lt;Double-click to add or edit&gt;</i></html>");
        for (CategorySpec preset : settings.editor.categories.presets)
            categoriesList.addCategory(new CategorySpec(preset));
        categoriesPanel.add(new JScrollPane(categoriesList), BorderLayout.CENTER);

        // Category modification buttons
        VerticalButtonList categoryModPanel = new VerticalButtonList("+", String.valueOf(UnicodeSymbols.ELLIPSIS), String.valueOf(UnicodeSymbols.MINUS));
        categoryModPanel.get("+").addActionListener((e) -> CategoryEditorPanel.showCategoryEditor(this).ifPresent(categoriesList::addCategory));
        categoryModPanel.get(String.valueOf(UnicodeSymbols.ELLIPSIS)).addActionListener((e) -> {
            if (categoriesList.getSelectedIndex() >= 0)
            {
                CategoryEditorPanel.showCategoryEditor(this, Optional.of(categoriesList.getCategoryAt(categoriesList.getSelectedIndex()))).ifPresent((s) -> {
                    categoriesList.setCategoryAt(categoriesList.getSelectedIndex(), s);
                });
            }
        });
        categoryModPanel.get(String.valueOf(UnicodeSymbols.MINUS)).addActionListener((e) -> {
            if (categoriesList.getSelectedIndex() >= 0)
                categoriesList.removeCategoryAt(categoriesList.getSelectedIndex());
        });
        categoriesPanel.add(categoryModPanel, BorderLayout.EAST);

        // Editor appearance
        Box editorAppearancePanel = new Box(BoxLayout.Y_AXIS);
        settingsPanel.add(editorAppearancePanel, new TreePath(editorAppearanceNode.getPath()).toString());

        // Editor category rows
        Box rowsPanel = new Box(BoxLayout.X_AXIS);
        rowsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));
        rowsPanel.add(new JLabel("Initial displayed rows in categories:"));
        rowsPanel.add(Box.createHorizontalStrut(5));
        rowsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        rowsSpinner.getModel().setValue(settings.editor.categories.rows);
        rowsPanel.add(rowsSpinner);
        rowsPanel.setMaximumSize(new Dimension(rowsPanel.getPreferredSize().width + 5, rowsPanel.getPreferredSize().height));
        rowsPanel.setAlignmentX(LEFT_ALIGNMENT);
        editorAppearancePanel.add(rowsPanel);

        // Editor table columns
        JPanel editorColumnsPanel = new JPanel(new GridLayout(0, 5));
        editorColumnsPanel.setBorder(BorderFactory.createTitledBorder("Columns"));
        editorColumnCheckBoxes = new ArrayList<>();
        var editorAttributes = Arrays.stream(CardAttribute.displayableValues()).sorted((a, b) -> {
            return a.toString().compareTo(b.toString());
        }).collect(Collectors.toList());
        for (CardAttribute characteristic : editorAttributes)
        {
            JCheckBox checkBox = new JCheckBox(characteristic.toString());
            editorColumnCheckBoxes.add(checkBox);
            editorColumnsPanel.add(checkBox);
            checkBox.setSelected(settings.editor.columns.contains(characteristic));
        }
        editorColumnsPanel.setAlignmentX(LEFT_ALIGNMENT);
        editorAppearancePanel.add(editorColumnsPanel);

        // Editor table stripe color
        JPanel editorColorPanel = new JPanel(new BorderLayout());
        editorColorPanel.setBorder(BorderFactory.createTitledBorder("Stripe Color"));
        editorStripeColor = new JColorChooser(settings.editor.stripe);
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
        Box startingSizePanel = new Box(BoxLayout.X_AXIS);
        startingSizePanel.add(Box.createHorizontalStrut(5));
        startingSizePanel.add(new JLabel("Starting Size:"));
        startingSizePanel.add(Box.createHorizontalStrut(5));
        startingSizeSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        startingSizeSpinner.getModel().setValue(settings.editor.hand.size);
        startingSizePanel.add(startingSizeSpinner);
        startingSizePanel.add(Box.createHorizontalGlue());
        startingSizePanel.setMaximumSize(new Dimension(startingSizePanel.getPreferredSize().width + 5, startingSizePanel.getPreferredSize().height));
        startingSizePanel.setAlignmentX(LEFT_ALIGNMENT);
        sampleHandPanel.add(startingSizePanel);

        sampleHandPanel.add(Box.createVerticalStrut(5));

        // Expected counts round mode
        Box expectedRoundPanel = new Box(BoxLayout.X_AXIS);
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
            modeButton.setSelected(mode.equals(settings.editor.hand.rounding));
            modeButtons.add(modeButton);
        }
        expectedRoundPanel.setMaximumSize(expectedRoundPanel.getPreferredSize());
        expectedRoundPanel.setAlignmentX(LEFT_ALIGNMENT);
        sampleHandPanel.add(expectedRoundPanel);

        sampleHandPanel.add(Box.createVerticalStrut(5));

        // Sample hand background color
        JPanel handBGColorPanel = new JPanel(new BorderLayout());
        handBGColorPanel.setBorder(BorderFactory.createTitledBorder("Background Color"));
        handBGColor = new JColorChooser(settings.editor.hand.background);
        handBGColor.getSelectionModel().addChangeListener((e) -> parent.setHandBackground(handBGColor.getColor()));
        handBGColorPanel.add(handBGColor);
        handBGColorPanel.setAlignmentX(LEFT_ALIGNMENT);
        sampleHandPanel.add(handBGColorPanel);

        // Format constraints
        JPanel formatsPanel = new JPanel(new BorderLayout());
        settingsPanel.add(formatsPanel, new TreePath(formatsNode.getPath()).toString());

        // Formats table
        JTable formatsTable = new JTable(new DefaultTableModel(FormatConstraints.FORMAT_NAMES.stream().map((f) -> FormatConstraints.CONSTRAINTS.get(f).toArray(f)).toArray(Object[][]::new), FormatConstraints.DATA_NAMES.toArray(String[]::new))
        {
            @Override
            public Class<?> getColumnClass(int column) { return FormatConstraints.CLASSES.get(column); }
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }
        });
        formatsPanel.add(new JScrollPane(formatsTable), BorderLayout.CENTER);

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
        try
        {
            recentSpinner.commitEdit();
            explicitsSpinner.commitEdit();
            rowsSpinner.commitEdit();
            startingSizeSpinner.commitEdit();

            var presets = new ArrayList<CategorySpec>(categoriesList.getCount());
            for (int i = 0; i < categoriesList.getCount(); i++)
                presets.add(categoriesList.getCategoryAt(i));

            settings = new SettingsBuilder(settings)
                .inventorySource(inventorySiteField.getText())
                .inventoryFile(inventoryFileField.getText())
                .inventoryLocation(inventoryDirField.getText())
                .inventoryUpdate(updateBox.getItemAt(updateBox.getSelectedIndex()))
                .inventoryWarn(suppressCheckBox.isSelected())
                .inventoryColumns(inventoryColumnCheckBoxes.stream().filter(JCheckBox::isSelected).map((c) -> CardAttribute.fromString(c.getText())).sorted().collect(Collectors.toList()))
                .inventoryStripe(inventoryStripeColor.getColor())
                .recentsCount((Integer)recentSpinner.getValue())
                .explicits((Integer)explicitsSpinner.getValue())
                .categoryRows((Integer)rowsSpinner.getValue())
                .editorColumns(editorColumnCheckBoxes.stream().filter(JCheckBox::isSelected).map((c) -> CardAttribute.fromString(c.getText())).sorted().collect(Collectors.toList()))
                .editorStripe(editorStripeColor.getColor())
                .presetCategories(presets)
                .handSize((Integer)startingSizeSpinner.getValue())
                .handRounding(modeButtons.stream().filter(JRadioButton::isSelected).map(JRadioButton::getText).findAny().orElse("No rounding"))
                .inventoryScans(scansDirField.getText())
                .inventoryBackground(scanBGChooser.getColor())
                .build();
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

        if (settings.inventory.columns.isEmpty())
            settings = new SettingsBuilder(settings).inventoryColumns(new SettingsBuilder().defaults().build().inventory.columns).build();
        if (settings.editor.columns.isEmpty())
            settings = new SettingsBuilder(settings).editorColumns(new SettingsBuilder().defaults().build().editor.columns).build();

        parent.applySettings();
    }

    /**
     * Reject any changes that were made as a result of using the settings dialog.
     */
    public void rejectSettings()
    {
        parent.setImageBackground(settings.inventory.background);
        parent.setHandBackground(settings.editor.hand.background);
    }
}
