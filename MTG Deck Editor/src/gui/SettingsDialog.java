package gui;

import gui.editor.CategoryEditorPanel;
import gui.filter.FilterGroupPanel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import database.characteristics.CardCharacteristic;

/**
 * This class is a dialog that allows the user to change various properties about
 * the program.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class SettingsDialog extends JDialog
{
	public static final Properties settings = new Properties();
	/**
	 * Pattern to match when parsing an ARGB color from a string to a @link{java.awt.Color}
	 */
	public static final Pattern COLOR_PATTERN = Pattern.compile("^#([0-9a-fA-F]{2})?([0-9a-fA-F]{6})$");
	/**
	 * Name of the file to get settings from.
	 */
	public static final String PROPERTIES_FILE = "settings.txt";
	/**
	 * File to download to check the latest version of the inventory.
	 */
	public static final String VERSION_FILE = "inventory.version_file";
	/**
	 * Website to connect to for downloading the inventory.
	 */
	public static final String INVENTORY_SOURCE = "inventory.source";
	/**
	 * File to download containing the inventory.
	 */
	public static final String INVENTORY_FILE = "inventory.file";
	/**
	 * Current inventory version.
	 */
	public static final String VERSION = "inventory.version";
	/**
	 * Whether or not to check for the latest inventory version on startup.
	 */
	public static final String INITIAL_CHECK = "inventory.initialcheck";
	/**
	 * Directory to store the inventory file in.
	 */
	public static final String INVENTORY_LOCATION = "inventory.location";
	/**
	 * Columns to display in the inventory table.
	 */
	public static final String INVENTORY_COLUMNS = "inventory.columns";
	/**
	 * Code for the color of the stripes of the inventory table.
	 */
	public static final String INVENTORY_STRIPE = "inventory.stripe";
	/**
	 * Directory to start the file chooser in.
	 */
	public static final String INITIALDIR = "initialdir";
	/**
	 * Number of recently-opened files to save.
	 */
	public static final String RECENT_COUNT = "recents.count";
	/**
	 * Recently-opened files paths.
	 */
	public static final String RECENT_FILES = "recents.files";
	/**
	 * Columns to display in editor tables.
	 */
	public static final String EDITOR_COLUMNS = "editor.columns";
	/**
	 * Stripe color for editor tables.
	 */
	public static final String EDITOR_STRIPE = "editor.stripe";
	/**
	 * Preset categories that can be added to editors.
	 */
	public static final String EDITOR_PRESETS = "editor.presets";
	/**
	 * Default initial size for a hand.
	 */
	public static final String HAND_SIZE = "hand.size";
	/**
	 * Columns to display in the sample hand tab.
	 */
	public static final String HAND_COLUMNS = "hand.columns";
	/**
	 * Location to find card scans.
	 */
	public static final String CARD_SCANS = "scans";
	
	/**
	 * Set program settings back to their default values
	 */
	public static void resetDefaultSettings()
	{
		settings.clear();
		settings.put(VERSION_FILE, "version.json");
		settings.put(INVENTORY_SOURCE, "http://mtgjson.com/json/");
		settings.put(VERSION, "");
		settings.put(INVENTORY_FILE, "AllSets-x.json");
		settings.put(INITIAL_CHECK, "true");
		settings.put(INVENTORY_LOCATION, "./");
		settings.put(INVENTORY_COLUMNS, "Name,Mana Cost,Type,Expansion");
		settings.put(INVENTORY_STRIPE, "#FFCCCCCC");
		settings.put(INITIALDIR, "./");
		settings.put(RECENT_COUNT, "4");
		settings.put(RECENT_FILES, "");
		settings.put(EDITOR_COLUMNS, "Name,Count,Mana Cost,Type,Expansion,Rarity,Categories,Date Added");
		settings.put(EDITOR_STRIPE, "#FFCCCCCC");
		settings.put(EDITOR_PRESETS, "\u00ABArtifacts\u00BB \u00AB\u00BB \u00AB\u00BB \u00AB\u00BB \u00ABAND \u00ABtype:contains any of\"artifact\"\u00BB \u00ABtype:contains none of\"creature\"\u00BB\u00BB\u220E\u00ABCreatures\u00BB \u00AB\u00BB \u00AB\u00BB \u00AB\u00BB \u00ABAND \u00ABtype:contains any of\"creature\"\u00BB\u00BB\u220E\u00ABLands\u00BB \u00AB\u00BB \u00AB\u00BB \u00AB\u00BB \u00ABAND \u00ABtype:contains any of\"land\"\u00BB\u00BB\u220E\u00ABInstants/Sorceries\u00BB \u00AB\u00BB \u00AB\u00BB \u00AB\u00BB \u00ABAND \u00ABtype:contains any of\"instant sorcery\"\u00BB\u00BB");
		settings.put(HAND_SIZE, "7");
		settings.put(HAND_COLUMNS, "Name,Mana Cost,Type,Expansion,Rarity,Power,Toughness,Loyalty");
		settings.put(CARD_SCANS, "images/cards");
	}
	
	/**
	 * Convert a @link{java.awt.Color} to a String in the format <code>#AARRGGBB</code>.
	 * 
	 * @param col Color to convert
	 * @param width Minimum width of the color string
	 * @return String code of the color.
	 */
	public static String colorToString(Color col, int width)
	{
		return String.format("#%0" + (width*2) + "X", col.getRGB()&((1L << (width*8)) - 1));
	}
	
	/**
	 * Convert a @link{java.awt.Color} to a String in the format <code>#AARRGGBB</code>.
	 * 
	 * @param col Color to convert
	 * @return String code of the color.
	 */
	public static String colorToString(Color col)
	{
		return colorToString(col, 4);
	}
	
	/**
	 * Decode an ARGB @link{java.awt.Color} from a String of either the format
	 * <code>#AARRGGBB</code> or <code>#RRGGBB</code>.
	 * 
	 * @param s String to parse
	 * @return The Color corresponding to the String.
	 */
	public static Color stringToColor(String s)
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
	 * MainFrame showing the dialog.
	 */
	private MainFrame parent;
	/**
	 * Text field controlling the web site that the inventory should be downloaded from.
	 */
	private JTextField inventorySiteField;
	/**
	 * Text field controlling the name of the file to be downloaded.
	 */
	private JTextField inventoryFileField;
	/**
	 * Text field controlling the directory to store the inventory in once it is downloaded.
	 */
	private JTextField inventoryDirField;
	/**
	 * Check box indicating whether or not to perform a check for updates on program start.
	 */
	private JCheckBox updateCheckBox;
	/**
	 * Check boxes indicating which columns to show in the inventory table.
	 */
	private List<JCheckBox> inventoryColumnCheckBoxes;
	/**
	 * Color chooser for the color of alternate inventory table stripes.
	 */
	private JColorChooser inventoryStripeColor;
	/**
	 * Spinner for the number of recent files to save.
	 */
	private JSpinner recentSpinner;
	/**
	 * Check boxes indicating which columns to show in editor tables.
	 */
	private List<JCheckBox> editorColumnCheckBoxes;
	/**
	 * Color chooser for the color of editor tables' alternate stripes.
	 */
	private JColorChooser editorStripeColor;
	/**
	 * List of preset categories.
	 */
	private CategoryListModel categoriesListModel;
	/**
	 * Columns to display in the sample hand table.
	 */
	private List<JCheckBox> handColumnCheckBoxes;
	/**
	 * Number of cards to draw in the starting hand.
	 */
	private JSpinner startingSizeSpinner;
	/**
	 * Delimiter for preset categories in the settings file.
	 */
	public static final String CATEGORY_DELIMITER = "∎";
	private JTextField scansDirField;
	
	/**
	 * Create a new SettingsDialog.
	 * 
	 * @param owner Parent of the dialog
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
		inventoryPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		settingsPanel.add(inventoryPanel, new TreePath(inventoryNode.getPath()).toString());
		
		// Inventory site
		JPanel inventorySitePanel = new JPanel();
		inventorySitePanel.setLayout(new BoxLayout(inventorySitePanel, BoxLayout.X_AXIS));
		inventorySitePanel.add(new JLabel("Inventory Site:"));
		inventorySitePanel.add(Box.createHorizontalStrut(5));
		inventorySiteField = new JTextField(15);
		inventorySiteField.setText(settings.getProperty(INVENTORY_SOURCE));
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
		inventoryFileField.setText(settings.getProperty(INVENTORY_FILE));
		inventoryFilePanel.add(inventoryFileField);
		inventoryFilePanel.add(Box.createHorizontalStrut(5));
		JLabel currentVersionLabel = new JLabel("(Current version: " + settings.getProperty(VERSION) + ")");
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
		inventoryDirField.setText(settings.getProperty(INVENTORY_LOCATION));
		inventoryChooser.setCurrentDirectory(new File(inventoryDirField.getText()).getAbsoluteFile());
		inventoryDirPanel.add(inventoryDirField);
		inventoryDirPanel.add(Box.createHorizontalStrut(5));
		JButton inventoryDirButton = new JButton("…");
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
		scansDirField.setText(settings.getProperty(CARD_SCANS));
		scansChooser.setCurrentDirectory(new File(scansDirField.getText()).getAbsoluteFile());
		scansDirPanel.add(scansDirField);
		scansDirPanel.add(Box.createHorizontalStrut(5));
		JButton scansDirButton = new JButton("…");
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
		JPanel updatePanel = new JPanel(new BorderLayout());
		updateCheckBox = new JCheckBox("Check for update on program start");
		updateCheckBox.setSelected(Boolean.valueOf(settings.getProperty(INITIAL_CHECK)));
		updatePanel.add(updateCheckBox, BorderLayout.WEST);
		updatePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, updatePanel.getPreferredSize().height));
		inventoryPanel.add(updatePanel);
		
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
		JPanel inventoryAppearancePanel = new JPanel();
		inventoryAppearancePanel.setLayout(new BoxLayout(inventoryAppearancePanel, BoxLayout.Y_AXIS));
		settingsPanel.add(inventoryAppearancePanel, new TreePath(inventoryAppearanceNode.getPath()).toString());
		
		// Columns
		JPanel inventoryColumnsPanel = new JPanel(new GridLayout(0, 5));
		inventoryColumnsPanel.setBorder(new TitledBorder("Columns"));
		inventoryColumnCheckBoxes = new ArrayList<JCheckBox>();
		for (CardCharacteristic characteristic: CardCharacteristic.inventoryValues())
		{
			JCheckBox checkBox = new JCheckBox(characteristic.toString());
			inventoryColumnCheckBoxes.add(checkBox);
			inventoryColumnsPanel.add(checkBox);
			checkBox.setSelected(settings.getProperty(INVENTORY_COLUMNS).contains(characteristic.toString()));
		}
		inventoryAppearancePanel.add(inventoryColumnsPanel);
		
		// Stripe color
		JPanel inventoryColorPanel = new JPanel(new BorderLayout());
		inventoryColorPanel.setBorder(new TitledBorder("Stripe Color"));
		inventoryStripeColor = new JColorChooser(stringToColor(settings.getProperty(INVENTORY_STRIPE)));
		inventoryColorPanel.add(inventoryStripeColor);
		inventoryAppearancePanel.add(inventoryColorPanel);
		
		inventoryAppearancePanel.add(Box.createVerticalGlue());
		
		// Editor
		JPanel editorPanel = new JPanel();
		editorPanel.setLayout(new BoxLayout(editorPanel, BoxLayout.Y_AXIS));
		editorPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		settingsPanel.add(editorPanel, new TreePath(editorNode.getPath()).toString());
		
		// Recent count
		JPanel recentPanel = new JPanel();
		recentPanel.setLayout(new BoxLayout(recentPanel, BoxLayout.X_AXIS));
		recentPanel.add(new JLabel("Recent file count:"));
		recentPanel.add(Box.createHorizontalStrut(5));
		recentSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
		recentSpinner.getModel().setValue(Integer.valueOf(settings.getProperty(RECENT_COUNT)));
		recentPanel.add(recentSpinner);
		recentPanel.add(Box.createHorizontalStrut(5));
		JLabel recentInfoLabel = new JLabel("(Changes will not be visible until program restart)");
		recentInfoLabel.setFont(new Font(recentInfoLabel.getFont().getFontName(), Font.ITALIC, recentInfoLabel.getFont().getSize()));
		recentPanel.add(recentInfoLabel);
		recentPanel.setMaximumSize(recentPanel.getPreferredSize());
		recentPanel.setAlignmentX(LEFT_ALIGNMENT);
		editorPanel.add(recentPanel);
		
		editorPanel.add(Box.createVerticalGlue());
		
		// Editor categories
		JPanel categoriesPanel = new JPanel();
		categoriesPanel.setLayout(new BorderLayout(5, 0));
		categoriesPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		settingsPanel.add(categoriesPanel, new TreePath(editorCategoriesNode.getPath()).toString());
		
		categoriesListModel = new CategoryListModel();
		if (!settings.getProperty(EDITOR_PRESETS).isEmpty())
			for (String category: settings.getProperty(EDITOR_PRESETS).split(CATEGORY_DELIMITER))
				categoriesListModel.addElement(category);
		JList<String> categoriesList = new JList<String>(categoriesListModel);
		categoriesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		categoriesList.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseReleased(MouseEvent e)
			{
				int index = categoriesList.locationToIndex(e.getPoint());
				Rectangle rec = categoriesList.getCellBounds(index, index);
				if (rec == null || !rec.contains(e.getPoint()))
				{
					if (e.getClickCount() == 2)
					{
						categoriesList.clearSelection();
						CategoryEditorPanel editor = CategoryEditorPanel.showCategoryEditor();
						if (editor != null)
							categoriesListModel.addElement(editor.toString());
					}
				}
				else
				{
					if (e.getClickCount() == 2)
					{
						CategoryEditorPanel editor = CategoryEditorPanel.showCategoryEditor(categoriesListModel.getCategoryAt(index));
						if (editor != null)
							categoriesListModel.set(index, editor.toString());
					}
				}
			}
		});
		categoriesPanel.add(new JScrollPane(categoriesList), BorderLayout.CENTER);
		
		// Category modification buttons
		JPanel categoryModPanel = new JPanel();
		GridBagLayout categoryModLayout = new GridBagLayout();
		categoryModLayout.columnWidths = new int[] {0};
		categoryModLayout.columnWeights = new double[] {0.0};
		categoryModLayout.rowHeights = new int[] {0, 0, 0, 0, 0};
		categoryModLayout.rowWeights = new double[] {1.0, 0.0, 0.0, 0.0, 1.0};
		categoryModPanel.setLayout(categoryModLayout);
		categoriesPanel.add(categoryModPanel, BorderLayout.EAST);
		
		JButton addButton = new JButton("+");
		addButton.addActionListener((e) -> {
			CategoryEditorPanel editor = CategoryEditorPanel.showCategoryEditor();
			if (editor != null)
				categoriesListModel.addElement(editor.toString());
		});
		GridBagConstraints addConstraints = new GridBagConstraints();
		addConstraints.gridx = 0;
		addConstraints.gridy = 1;
		addConstraints.fill = GridBagConstraints.BOTH;
		categoryModPanel.add(addButton, addConstraints);
		
		JButton editButton = new JButton("…");
		editButton.addActionListener((e) -> {
			if (categoriesList.getSelectedIndex() >= 0)
			{
				CategoryEditorPanel editor = CategoryEditorPanel.showCategoryEditor(categoriesListModel.getCategoryAt(categoriesList.getSelectedIndex()));
				if (editor != null)
					categoriesListModel.set(categoriesList.getSelectedIndex(), editor.toString());
			}
		});
		GridBagConstraints editConstraints = new GridBagConstraints();
		editConstraints.gridx = 0;
		editConstraints.gridy = 2;
		editConstraints.fill = GridBagConstraints.BOTH;
		categoryModPanel.add(editButton, editConstraints);
		
		JButton removeButton = new JButton("−");
		removeButton.addActionListener((e) -> {
			if (categoriesList.getSelectedIndex() >= 0)
				categoriesListModel.remove(categoriesList.getSelectedIndex());
		});
		GridBagConstraints removeConstraints = new GridBagConstraints();
		removeConstraints.gridx = 0;
		removeConstraints.gridy = 3;
		removeConstraints.fill = GridBagConstraints.BOTH;
		categoryModPanel.add(removeButton, removeConstraints);
		
		// Editor appearance
		JPanel editorAppearancePanel = new JPanel();
		editorAppearancePanel.setLayout(new BoxLayout(editorAppearancePanel, BoxLayout.Y_AXIS));
		settingsPanel.add(editorAppearancePanel, new TreePath(editorAppearanceNode.getPath()).toString());
		
		// Editor table columns
		JPanel editorColumnsPanel = new JPanel(new GridLayout(0, 5));
		editorColumnsPanel.setBorder(new TitledBorder("Columns"));
		editorColumnCheckBoxes = new ArrayList<JCheckBox>();
		for (CardCharacteristic characteristic: CardCharacteristic.values())
		{
			JCheckBox checkBox = new JCheckBox(characteristic.toString());
			editorColumnCheckBoxes.add(checkBox);
			editorColumnsPanel.add(checkBox);
			checkBox.setSelected(settings.getProperty(EDITOR_COLUMNS).contains(characteristic.toString()));
		}
		editorAppearancePanel.add(editorColumnsPanel);
		
		// Editor table stripe color
		JPanel editorColorPanel = new JPanel(new BorderLayout());
		editorColorPanel.setBorder(new TitledBorder("Stripe Color"));
		editorStripeColor = new JColorChooser(stringToColor(settings.getProperty(EDITOR_STRIPE)));
		editorColorPanel.add(editorStripeColor);
		editorAppearancePanel.add(editorColorPanel);
		
		editorAppearancePanel.add(Box.createVerticalGlue());
		
		// Sample hand
		JPanel sampleHandPanel = new JPanel();
		sampleHandPanel.setLayout(new BoxLayout(sampleHandPanel, BoxLayout.Y_AXIS));
		settingsPanel.add(sampleHandPanel, new TreePath(handAppearanceNode.getPath()).toString());
		
		sampleHandPanel.add(Box.createVerticalStrut(5));
		
		// Starting Size
		JPanel startingSizePanel = new JPanel();
		startingSizePanel.add(Box.createHorizontalStrut(5));
		startingSizePanel.setLayout(new BoxLayout(startingSizePanel, BoxLayout.X_AXIS));
		startingSizePanel.add(new JLabel("Starting Size:"));
		startingSizePanel.add(Box.createHorizontalStrut(5));
		startingSizeSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
		startingSizeSpinner.getModel().setValue(Integer.valueOf(settings.getProperty(HAND_SIZE)));
		startingSizePanel.add(startingSizeSpinner);
		startingSizePanel.add(Box.createHorizontalGlue());
		startingSizePanel.setMaximumSize(startingSizePanel.getPreferredSize());
		startingSizePanel.setAlignmentX(LEFT_ALIGNMENT);
		sampleHandPanel.add(startingSizePanel);
		
		sampleHandPanel.add(Box.createVerticalStrut(5));
		
		// Columns
		JPanel handColumnsPanel = new JPanel(new GridLayout(0, 5));
		handColumnsPanel.setBorder(new TitledBorder("Columns"));
		handColumnCheckBoxes = new ArrayList<JCheckBox>();
		for (CardCharacteristic characteristic: CardCharacteristic.values())
		{
			JCheckBox checkBox = new JCheckBox(characteristic.toString());
			handColumnCheckBoxes.add(checkBox);
			handColumnsPanel.add(checkBox);
			checkBox.setSelected(settings.getProperty(HAND_COLUMNS).contains(characteristic.toString()));
		}
		handColumnsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, handColumnsPanel.getPreferredSize().height));
		handColumnsPanel.setAlignmentX(LEFT_ALIGNMENT);
		sampleHandPanel.add(handColumnsPanel);
		
		sampleHandPanel.add(Box.createVerticalGlue());
		
		// Tree panel
		JPanel treePanel = new JPanel(new BorderLayout());
		JTree tree = new JTree(root);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		((DefaultTreeCellRenderer)tree.getCellRenderer()).setLeafIcon(null);
		tree.addTreeSelectionListener((e) -> {
			((CardLayout)settingsPanel.getLayout()).show(settingsPanel, e.getPath().toString());
		});
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
		okButton.addActionListener((e) -> {confirmSettings(); dispose();});
		buttonPanel.add(okButton);
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener((e) -> dispose());
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
		settings.put(INVENTORY_SOURCE, inventorySiteField.getText());
		settings.put(INVENTORY_FILE, inventoryFileField.getText());
		settings.put(INVENTORY_LOCATION, inventoryDirField.getText());
		settings.put(INITIAL_CHECK, Boolean.toString(updateCheckBox.isSelected()));
		StringJoiner join = new StringJoiner(",");
		for (JCheckBox box: inventoryColumnCheckBoxes)
			if (box.isSelected())
				join.add(box.getText());
		settings.put(INVENTORY_COLUMNS, join.toString());
		settings.put(INVENTORY_STRIPE, colorToString(inventoryStripeColor.getColor()));
		settings.put(RECENT_COUNT, recentSpinner.getValue().toString());
		join = new StringJoiner(",");
		for (JCheckBox box: editorColumnCheckBoxes)
			if (box.isSelected())
				join.add(box.getText());
		settings.put(EDITOR_COLUMNS, join.toString());
		settings.put(EDITOR_STRIPE, colorToString(editorStripeColor.getColor()));
		join = new StringJoiner(SettingsDialog.CATEGORY_DELIMITER);
		for (int i = 0; i < categoriesListModel.getSize(); i++)
			join.add(categoriesListModel.getCategoryAt(i));
		settings.put(EDITOR_PRESETS, join.toString());
		settings.put(HAND_SIZE, startingSizeSpinner.getValue().toString());
		join = new StringJoiner(",");
		for (JCheckBox box: handColumnCheckBoxes)
			if (box.isSelected())
				join.add(box.getText());
		settings.put(HAND_COLUMNS, join.toString());
		settings.put(CARD_SCANS, scansDirField.getText());
		parent.applySettings();
	}
	
	/**
	 * If the given file is contained the current directory, make it a relative file to
	 * the current working directory.  If it is the current working directory, make it
	 * "."  Otherwise, keep it what it is.
	 * 
	 * @param f File to relativize
	 * @return Relativized version of the given file.
	 */
	private File relativize(File f)
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
	 * This class represents a list model for displaying categories.
	 * 
	 * @author Alec Roelke
	 */
	private class CategoryListModel extends DefaultListModel<String>
	{
		/**
		 * Create a new CategoryListModel.
		 */
		public CategoryListModel()
		{
			super();
		}
		
		/**
		 * @param Index into the list to look at.
		 * @return The name of the category at the index.
		 */
		@Override
		public String getElementAt(int index)
		{
			String category = super.getElementAt(index);
			return category.substring(category.indexOf(FilterGroupPanel.BEGIN_GROUP) + 1, category.indexOf(FilterGroupPanel.END_GROUP));
		}
		
		/**
		 * @param index Index into the list to look at.
		 * @return The String representation of the category at the index.
		 */
		public String getCategoryAt(int index)
		{
			return super.getElementAt(index);
		}
	}
}
