package gui;

import gui.editor.EditorFrame;
import gui.filter.FilterGroupPanel;
import gui.inventory.InventoryDownloadDialog;
import gui.inventory.InventoryLoadDialog;
import gui.inventory.InventoryTableModel;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.html.HTMLDocument;

import util.StripedTable;
import util.TableMouseAdapter;
import database.Card;
import database.Inventory;
import database.ManaCost;
import database.characteristics.CardCharacteristic;

/**
 * This class represents the main frame of the editor.  It contains several tabs that display information
 * about decks.
 * 
 * The editor tab is divided into three sections:  On the left side is a database of all cards that can be
 * added to decks with a window below it that displays the Oracle text of the currently-selected card.  On
 * the right side is a pane which contains internal frames that allow the user to open, close, and edit
 * multiple decks at once.  See @link{gui.editor.EditorFrame} for details on the editor frames.
 * 
 * TODO: Add some indication that a card is in the deck in the inventory table if it is
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class MainFrame extends JFrame
{
	/**
	 * Name of the file to get settings from.
	 */
	public static final String PROPERTIES_FILE = "settings.txt";
	/**
	 * Update status value: update needed.
	 */
	public static final int UPDATE_NEEDED = 0;
	/**
	 * Update status value: update not needed.
	 */
	public static final int NO_UPDATE = 1;
	/**
	 * Update status value: update needed, but was not requested.
	 */
	public static final int UPDATE_CANCELLED = 2;
	
	/**
	 * Inventory of all cards.
	 */
	private Inventory inventory;
	/**
	 * Table displaying the inventory of all cards.
	 */
	private StripedTable inventoryTable;
	/**
	 * Model for the table displaying the inventory of all cards.
	 */
	private InventoryTableModel inventoryModel;
	/**
	 * Panel for editing the inventory filter.
	 */
	private FilterGroupPanel filter;
	/**
	 * Currently-selected card which will be added to decks.
	 */
	private Card selectedCard;
	/**
	 * Pane for showing the Oracle text of the currently-selected card.
	 */
	private JTextPane oracleTextPane;
	/**
	 * Desktop pane containing internal editor frames.
	 */
	private JDesktopPane decklistDesktop;
	/**
	 * Number to append to the end of untitled decks that have just been created.
	 */
	private int untitled;
	/**
	 * Currently-selected editor frame.
	 */
	private EditorFrame selectedFrame;
	/**
	 * List of open editor frames.
	 */
	private List<EditorFrame> editors;
	/**
	 * File chooser for opening and saving.
	 */
	private JFileChooser fileChooser;
	/**
	 * Properties object storing settings.
	 */
	private Properties properties;
	/**
	 * URL pointing to the site to get the latest version of the
	 * inventory from.
	 */
	private URL versionSite;
	/**
	 * File to store the inventory in.
	 */
	private File inventoryFile;
	/**
	 * URL pointing to the site to get the inventory from.
	 */
	private URL inventorySite;
	/**
	 * Number of recent files to display.
	 */
	private int recentCount;
	/**
	 * Menu items showing recent files to open.
	 */
	private Queue<JMenuItem> recentItems;
	/**
	 * Map of those menu items onto the files they should open.
	 */
	private Map<JMenuItem, File> recents;
	/**
	 * Menu containing the recent menu items.
	 */
	private JMenu recentsMenu;
	/**
	 * List of CardCharacteristics to show in the inventory table.
	 */
	private List<CardCharacteristic> inventoryCharacteristics;
	
	/**
	 * Create a new MainFrame.
	 */
	public MainFrame()
	{
		super();
		
		selectedCard = null;
		untitled = 0;
		selectedFrame = null;
		editors = new ArrayList<EditorFrame>();
		filter = new FilterGroupPanel();
		recentItems = new LinkedList<JMenuItem>();
		recents = new HashMap<JMenuItem, File>();
		inventoryCharacteristics = new ArrayList<CardCharacteristic>();
		
		// Initialize properties to their default values, then load the current values
		// from the properties file
		resetDefaultSettings();
		try (FileInputStream in = new FileInputStream(PROPERTIES_FILE))
		{
			properties.load(in);
		}
		catch (FileNotFoundException e)
		{
//			JOptionPane.showMessageDialog(null, "File " + PROPERTIES_FILE + " not found.  Using default settings.", "Warning", JOptionPane.WARNING_MESSAGE);
		}
		catch (IOException e)
		{
			JOptionPane.showMessageDialog(null, "Error opening " + PROPERTIES_FILE + ": " + e.getMessage() + ".", "Warning", JOptionPane.WARNING_MESSAGE);
		}
		try
		{
			versionSite = new URL(properties.getProperty("inventory.source") + properties.getProperty("inventory.version_file"));
		}
		catch (MalformedURLException e)
		{
			JOptionPane.showMessageDialog(null, "Bad version URL: " + properties.getProperty("inventory.source") + properties.getProperty("inventory.version_file"), "Warning", JOptionPane.WARNING_MESSAGE);
		}
		try
		{
			inventorySite = new URL(properties.getProperty("inventory.source") + properties.getProperty("inventory.file"));
		}
		catch (MalformedURLException e)
		{
			JOptionPane.showMessageDialog(null, "Bad file URL: " + properties.getProperty("inventory.source") + properties.getProperty("inventory.file"), "Warning", JOptionPane.WARNING_MESSAGE);
		}
		inventoryFile = new File(properties.getProperty("inventory.location") + properties.getProperty("inventory.file"));
		recentCount = Integer.valueOf(properties.getProperty("recents.count"));
		if (!properties.getProperty("inventory.columns").isEmpty())
			inventoryCharacteristics = Arrays.stream(properties.getProperty("inventory.columns").split(",")).map(CardCharacteristic::get).collect(Collectors.toList());
		
		setTitle("MTG Deck Editor");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		Dimension screenRes = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds(50, 50, screenRes.width - 100, screenRes.height - 100);
		
		/* MENU BAR */
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		// File menu
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		// TODO: Add items for importing and exporting from/to different deck formats
		
		// New file menu item
		JMenuItem newItem = new JMenuItem("New");
		newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK));
		newItem.addActionListener((e) -> newEditor());
		fileMenu.add(newItem);
		
		// Open file menu item
		JMenuItem openItem = new JMenuItem("Open...");
		openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
		openItem.addActionListener((e) -> open());
		fileMenu.add(openItem);
		
		// Close file menu item
		JMenuItem closeItem = new JMenuItem("Close");
		closeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_MASK));
		closeItem.addActionListener((e) -> {if (selectedFrame != null) close(selectedFrame); else exit();});
		fileMenu.add(closeItem);
		
		// Close all files menu item
		JMenuItem closeAllItem = new JMenuItem("Close All");
		closeAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK));
		closeAllItem.addActionListener((e) -> closeAll());
		fileMenu.add(closeAllItem);
		
		fileMenu.add(new JSeparator());
		
		// Save file menu item
		JMenuItem saveItem = new JMenuItem("Save");
		saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
		saveItem.addActionListener((e) -> {if (selectedFrame != null) save(selectedFrame);});
		fileMenu.add(saveItem);
		
		// Save file as menu item
		JMenuItem saveAsItem = new JMenuItem("Save As...");
		saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));
		saveAsItem.addActionListener((e) -> {if (selectedFrame != null) saveAs(selectedFrame);});
		fileMenu.add(saveAsItem);
		
		// Save all files menu item
		JMenuItem saveAllItem = new JMenuItem("Save All");
		saveAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK));
		saveAllItem.addActionListener((e) -> saveAll());
		fileMenu.add(saveAllItem);
		
		// Recent files menu
		recentsMenu = new JMenu("Open Recent");
		recentsMenu.setEnabled(false);
		if (!properties.getProperty("recents.files").isEmpty())
			for (String fname: properties.getProperty("recents.files").split("\\|"))
				updateRecents(new File(fname));
		fileMenu.add(recentsMenu);
		
		fileMenu.add(new JSeparator());
		
		// Exit menu item
		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener((e) -> exit());
		exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_MASK));
		fileMenu.add(exitItem);
		
		// Edit menu
		JMenu editMenu = new JMenu("Edit");
		menuBar.add(editMenu);
		// TODO: Add a "Preferences" items to the edit menu with an accompanying dialog
		
		// Undo menu item
		JMenuItem undoItem = new JMenuItem("Undo");
		undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_MASK));
		undoItem.addActionListener((e) -> {if (selectedFrame != null) selectedFrame.undo();});
		editMenu.add(undoItem);
		
		// Redo menu item
		JMenuItem redoItem = new JMenuItem("Redo");
		redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_MASK));
		redoItem.addActionListener((e) -> {if (selectedFrame != null) selectedFrame.redo();});
		editMenu.add(redoItem);
		
		// Deck menu
		JMenu deckMenu = new JMenu("Deck");
		menuBar.add(deckMenu);
		
		// Add card menu
		JMenu addMenu = new JMenu("Add Cards");
		deckMenu.add(addMenu);
		
		// Add single copy item
		JMenuItem addSingleItem = new JMenuItem("Add Single Copy");
		addSingleItem.setAccelerator(KeyStroke.getKeyStroke('+'));
		addSingleItem.addActionListener((e) -> {if (selectedFrame != null) selectedFrame.addCards(getSelectedCards(), 1);});
		addMenu.add(addSingleItem);
		
		// Fill playset item
		JMenuItem playsetItem = new JMenuItem("Fill Playset");
		playsetItem.addActionListener((e) -> {
			if (selectedFrame != null)
				for (Card c: getSelectedCards())
					selectedFrame.addCard(c, 4 - selectedFrame.count(c));
		});
		addMenu.add(playsetItem);
		
		// Add variable item
		JMenuItem addNItem = new JMenuItem("Add Copies...");
		addNItem.addActionListener((e) -> {
			if (selectedFrame != null)
			{
				JPanel contentPanel = new JPanel(new BorderLayout());
				contentPanel.add(new JLabel("Copies to add:"), BorderLayout.WEST);
				JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 1));
				contentPanel.add(spinner, BorderLayout.SOUTH);
				if (JOptionPane.showOptionDialog(null, contentPanel, "Add Cards", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null) == JOptionPane.OK_OPTION)
					selectedFrame.addCards(getSelectedCards(), (Integer)spinner.getValue());
			}
		});
		addMenu.add(addNItem);
		
		// Remove card menu
		JMenu removeMenu = new JMenu("Remove Cards");
		deckMenu.add(removeMenu);
		
		// Remove single copy item
		JMenuItem removeSingleItem = new JMenuItem("Remove Single Copy");
		removeSingleItem.setAccelerator(KeyStroke.getKeyStroke('-'));
		removeSingleItem.addActionListener((e) -> {if (selectedFrame != null) selectedFrame.removeSelectedCards(1);});
		removeMenu.add(removeSingleItem);
		
		// Remove all item
		JMenuItem removeAllItem = new JMenuItem("Remove All Copies");
		removeAllItem.addActionListener((e) -> {if (selectedFrame != null) selectedFrame.removeSelectedCards(Integer.MAX_VALUE);});
		removeMenu.add(removeAllItem);
		
		// Remove variable item
		JMenuItem removeNItem = new JMenuItem("Remove Copies...");
		removeNItem.addActionListener((e) -> {
			if (selectedFrame != null)
			{
				JPanel contentPanel = new JPanel(new BorderLayout());
				contentPanel.add(new JLabel("Copies to remove:"), BorderLayout.WEST);
				JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 1));
				contentPanel.add(spinner, BorderLayout.SOUTH);
				if (JOptionPane.showOptionDialog(null, contentPanel, "Add Cards", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null) == JOptionPane.OK_OPTION)
					selectedFrame.removeSelectedCards((Integer)spinner.getValue());
			}
		});
		removeMenu.add(removeNItem);
		
		// Category menu
		JMenu categoryMenu = new JMenu("Category");
		deckMenu.add(categoryMenu);
		
		// Add category item
		JMenuItem addCategoryItem = new JMenuItem("Add...");
		addCategoryItem.addActionListener((e) -> {if (selectedFrame != null) selectedFrame.createCategory();});
		categoryMenu.add(addCategoryItem);
		
		// Edit category item
		JMenuItem editCategoryItem = new JMenuItem("Edit...");
		editCategoryItem.addActionListener((e) -> {
			if (selectedFrame != null)
			{
				JPanel contentPanel = new JPanel(new BorderLayout());
				contentPanel.add(new JLabel("Choose a category to edit:"), BorderLayout.NORTH);
				JList<String> categories = new JList<String>(selectedFrame.categoryNames());
				categories.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				contentPanel.add(new JScrollPane(categories), BorderLayout.CENTER);
				if (JOptionPane.showOptionDialog(null, contentPanel, "Edit Category", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null) == JOptionPane.OK_OPTION)
					selectedFrame.editCategory(categories.getSelectedValue());
			}
		});
		categoryMenu.add(editCategoryItem);
		
		// Remove category item
		JMenuItem removeCategoryItem = new JMenuItem("Remove...");
		removeCategoryItem.addActionListener((e) -> {
			if (selectedFrame != null)
			{
				JPanel contentPanel = new JPanel(new BorderLayout());
				contentPanel.add(new JLabel("Choose a category to remove:"), BorderLayout.NORTH);
				JList<String> categories = new JList<String>(selectedFrame.categoryNames());
				categories.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				contentPanel.add(new JScrollPane(categories), BorderLayout.CENTER);
				if (JOptionPane.showOptionDialog(null, contentPanel, "Edit Category", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null) == JOptionPane.OK_OPTION)
					selectedFrame.removeCategory(categories.getSelectedValue());
			}
		});
		categoryMenu.add(removeCategoryItem);
		
		// TODO: Add a section for preset categories
		// Creatures
		// Artifacts
		// Lands
		// Commander (?)
		// Instants/Sorceries
		
		// Help menu
		JMenu helpMenu = new JMenu("Help");
		menuBar.add(helpMenu);
		// TODO: Add a help dialog
		
		JMenuItem updateInventoryItem = new JMenuItem("Check for inventory update...");
		updateInventoryItem.addActionListener((e) -> {
			switch (checkForUpdate())
			{
			case UPDATE_NEEDED:
				updateInventory();
				loadInventory();
				break;
			case NO_UPDATE:
				JOptionPane.showMessageDialog(null, "Inventory is up to date.");
				break;
			case UPDATE_CANCELLED:
				break;
			default:
				break;
			}
		});
		helpMenu.add(updateInventoryItem);
		
		/* CONTENT PANE */
		// Panel containing all content
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.CTRL_MASK), "Next Frame");
		contentPane.getActionMap().put("Next Frame", new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (!editors.isEmpty() && selectedFrame != null)
					selectFrame(editors.get((editors.indexOf(selectedFrame) + 1)%editors.size()));
			}
		});
		contentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ActionEvent.CTRL_MASK), "Previous Frame");
		contentPane.getActionMap().put("Previous Frame", new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (!editors.isEmpty() && selectedFrame != null)
				{
					int next = editors.indexOf(selectedFrame) - 1;
					selectFrame(editors.get(next < 0 ? editors.size() - 1 : next));
				}
			}
		});
		setContentPane(contentPane);
		
		// DesktopPane containing editor frames
		decklistDesktop = new JDesktopPane();
		decklistDesktop.setBackground(SystemColor.controlShadow);
		
		// Panel containing inventory and image of currently-selected card
		JPanel inventoryPanel = new JPanel(new BorderLayout(0, 0));
		inventoryPanel.setPreferredSize(new Dimension(getWidth()/4, getHeight()*3/4));
		
		// Panel showing the image of the currently-selected card
		JPanel imagePanel = new JPanel();
		inventoryPanel.add(imagePanel, BorderLayout.NORTH);
		
		// Panel containing the inventory and the quick-filter bar
		JPanel tablePanel = new JPanel(new BorderLayout(0, 0));
		inventoryPanel.add(tablePanel, BorderLayout.CENTER);
		
		// Panel containing the quick-filter bar
		JPanel filterPanel = new JPanel();
		filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.X_AXIS));
		
		// Text field for quickly filtering by name
		JTextField nameFilterField = new JTextField();
		filterPanel.add(nameFilterField);
		
		// Button for clearing the filter
		JButton clearButton = new JButton("X");
		filterPanel.add(clearButton);
		
		// Button for opening the advanced filter dialog
		JButton advancedFilterButton = new JButton("Advanced...");
		filterPanel.add(advancedFilterButton);
		tablePanel.add(filterPanel, BorderLayout.NORTH);
		
		// Panel displaying the Oracle text of the currently-selected card
		JPanel textPanel = new JPanel(new BorderLayout(0, 0));
		
		// Label saying "Oracle Text:"
		JPanel oracleLabelPanel = new JPanel(new BorderLayout(0, 0));
		textPanel.add(oracleLabelPanel, BorderLayout.NORTH);
		oracleLabelPanel.add(new JLabel("Oracle Text:"));
		
		// Pane displaying the Oracle text
		oracleTextPane = new JTextPane();
		oracleTextPane.setEditable(false);
		oracleTextPane.setContentType("text/html");
		oracleTextPane.setFont(UIManager.getFont("Label.font"));
		oracleTextPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
		try
		{
			((HTMLDocument)oracleTextPane.getDocument()).setBase(new File(".").toURI().toURL());
		}
		catch (MalformedURLException e)
		{}
		textPanel.add(new JScrollPane(oracleTextPane), BorderLayout.CENTER);
		
		// Create the inventory and put it in the table
		inventoryTable = new StripedTable();
		inventoryTable.setAutoCreateRowSorter(true);
		inventoryTable.setDefaultRenderer(ManaCost.class, new ManaCostRenderer());
		inventoryTable.setFillsViewportHeight(true);
		inventoryTable.setShowGrid(false);
		tablePanel.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);
		
		// Table popup menu
		JPopupMenu inventoryMenu = new JPopupMenu();
		inventoryTable.addMouseListener(new TableMouseAdapter(inventoryTable, inventoryMenu));
		
		// Add single copy item
		JMenuItem addSinglePopupItem = new JMenuItem("Add Single Copy");
		addSinglePopupItem.addActionListener((e) -> {if (selectedFrame != null) selectedFrame.addCards(getSelectedCards(), 1);});
		inventoryMenu.add(addSinglePopupItem);
		
		// Fill playset item
		JMenuItem playsetPopupItem = new JMenuItem("Fill Playset");
		playsetPopupItem.addActionListener((e) -> {
			if (selectedFrame != null)
				for (Card c: getSelectedCards())
					selectedFrame.addCard(c, 4 - selectedFrame.count(c));
		});
		inventoryMenu.add(playsetPopupItem);
		
		// Add variable item
		JMenuItem addNPopupItem = new JMenuItem("Add Copies...");
		addNPopupItem.addActionListener((e) -> {
			if (selectedFrame != null)
			{
				JPanel contentPanel = new JPanel(new BorderLayout());
				contentPanel.add(new JLabel("Copies to add:"), BorderLayout.WEST);
				JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 1));
				contentPanel.add(spinner, BorderLayout.SOUTH);
				if (JOptionPane.showOptionDialog(null, contentPanel, "Add Cards", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null) == JOptionPane.OK_OPTION)
					selectedFrame.addCards(getSelectedCards(), (Integer)spinner.getValue());
			}
		});
		inventoryMenu.add(addNPopupItem);
		
		inventoryMenu.add(new JSeparator());
		
		// Remove single copy item
		JMenuItem removeSinglePopupItem = new JMenuItem("Remove Single Copy");
		removeSinglePopupItem.addActionListener((e) -> {if (selectedFrame != null) selectedFrame.removeCards(getSelectedCards(), 1);});
		inventoryMenu.add(removeSinglePopupItem);
		
		// Remove all item
		JMenuItem removeAllPopupItem = new JMenuItem("Remove All Copies");
		removeAllPopupItem.addActionListener((e) -> {if (selectedFrame != null) selectedFrame.removeCards(getSelectedCards(), Integer.MAX_VALUE);});
		inventoryMenu.add(removeAllPopupItem);
		
		// Remove variable item
		JMenuItem removeNPopupItem = new JMenuItem("Remove Copies...");
		removeNPopupItem.addActionListener((e) -> {
			if (selectedFrame != null)
			{
				JPanel contentPanel = new JPanel(new BorderLayout());
				contentPanel.add(new JLabel("Copies to remove:"), BorderLayout.WEST);
				JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 1));
				contentPanel.add(spinner, BorderLayout.SOUTH);
				if (JOptionPane.showOptionDialog(null, contentPanel, "Add Cards", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null) == JOptionPane.OK_OPTION)
					selectedFrame.removeCards(getSelectedCards(), (Integer)spinner.getValue());
			}
		});
		inventoryMenu.add(removeNPopupItem);
		
		// Action to be taken when the user presses the Enter key after entering text into the quick-filter
		// bar
		nameFilterField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				filter = new FilterGroupPanel();
				inventory.updateFilter((c) -> c.normalizedName().contains(nameFilterField.getText()));
				inventoryModel.fireTableDataChanged();
			}	
		});
		
		// Action to be taken when the clear button is pressed (reset the filter)
		clearButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				nameFilterField.setText("");
				filter = new FilterGroupPanel();
				inventory.updateFilter((c) -> true);
				inventoryModel.fireTableDataChanged();
			}
		});
		
		// Action to be taken when the advanced filter button is pressed (show the advanced filter
		// dialog)
		advancedFilterButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (JOptionPane.showOptionDialog(null, filter, "Advanced Filter", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null) == JOptionPane.OK_OPTION)
				{
					nameFilterField.setText("");
					inventory.updateFilter(filter.filter());
					inventoryModel.fireTableDataChanged();
				}
			}
		});
		
		// Action to be taken when a selection is made in the inventory table (update the relevant
		// panels)
		inventoryTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				if (!e.getValueIsAdjusting())
				{
					ListSelectionModel lsm = (ListSelectionModel)e.getSource();
					if (!lsm.isSelectionEmpty())
						selectCard(inventory.get(inventoryTable.convertRowIndexToModel(lsm.getMinSelectionIndex())));
				}
			}
		});
		
		// Split panes dividing the panel into three sections.  They can be resized at will.
		JSplitPane inventorySplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inventoryPanel, textPanel);
		inventorySplit.setOneTouchExpandable(true);
		inventorySplit.setContinuousLayout(true);
		JSplitPane editorSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inventorySplit, decklistDesktop);
		editorSplit.setOneTouchExpandable(true);
		editorSplit.setContinuousLayout(true);
		contentPane.add(editorSplit, BorderLayout.CENTER);
		
		// File chooser
		fileChooser = new JFileChooser(properties.getProperty("initialdir"));
		fileChooser.setMultiSelectionEnabled(false);
		
		// Handle what happens when the window tries to close and when it opens.
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowOpened(WindowEvent e)
			{
				if (Boolean.valueOf(properties.getProperty("inventory.initialcheck")) && checkForUpdate() == UPDATE_NEEDED)
					updateInventory();
				loadInventory();
			}
			
			@Override
			public void windowClosing(WindowEvent e)
			{
				exit();
			}
		});
	}
	
	/**
	 * Exit the application if all open editors successfully close.
	 */
	public void exit()
	{
		if (closeAll())
		{
			saveSettings();
			System.exit(0);
		}
	}
	
	/**
	 * Load the inventory and initialize the inventory table.
	 * @see InventoryLoadDialog
	 */
	public void loadInventory()
	{
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		InventoryLoadDialog loadDialog = new InventoryLoadDialog(this);
		loadDialog.setLocationRelativeTo(this);
		inventory = loadDialog.createInventory(inventoryFile);
		inventory.sort((a, b) -> a.compareName(b));
		inventoryModel = new InventoryTableModel(inventory, inventoryCharacteristics);
		inventoryTable.setModel(inventoryModel);
		
		setCursor(Cursor.getDefaultCursor());
	}
	
	/**
	 * Check to see if the inventory needs to be updated.  If it does, ask the user if it should be.
	 * 
	 * @return an integer value representing the state of the update.  It can be:
	 * UPDATE_NEEDED
	 * NO_UPDATE
	 * UPDATE_CANCELLED
	 */
	public int checkForUpdate()
	{
		if (!inventoryFile.exists())
		{
			JOptionPane.showMessageDialog(null, inventoryFile.getName() + " not found.  It will be downloaded.", "Update", JOptionPane.WARNING_MESSAGE);
			try (BufferedReader in = new BufferedReader(new InputStreamReader(versionSite.openStream())))
			{
				String newestVersion = in.readLine();
				newestVersion = newestVersion.substring(1, newestVersion.length() - 1);
				properties.setProperty("inventory.version", newestVersion);
			}
			catch (IOException e)
			{
				JOptionPane.showMessageDialog(null, "Error connecting to server: " + e.getMessage() + ".", "Connection Error", JOptionPane.ERROR_MESSAGE);
				return NO_UPDATE;
			}
			return UPDATE_NEEDED;
		}
		else
		{
			try (BufferedReader in = new BufferedReader(new InputStreamReader(versionSite.openStream())))
			{
				String newestVersion = in.readLine();
				newestVersion = newestVersion.substring(1, newestVersion.length() - 1);
				if (!newestVersion.equals(properties.get("inventory.version")))
				{
					if (JOptionPane.showConfirmDialog(null, "Inventory is out of date.  Download update?", "Update", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
					{
						properties.setProperty("inventory.version", newestVersion);
						return UPDATE_NEEDED;
					}
					else
						return UPDATE_CANCELLED;
				}
				else
					return NO_UPDATE;
			}
			catch (IOException e)
			{
				JOptionPane.showMessageDialog(null, "Error connecting to server: " + e.getMessage() + ".", "Connection Error", JOptionPane.ERROR_MESSAGE);
				return NO_UPDATE;
			}
		}
	}
	
	/**
	 * Download the latest list of cards from the inventory site (default mtgjson.com).  If the
	 * download is taking a while, a progress bar will appear.
	 * 
	 * TODO: If the inventory cannot be found, give the user the option to search for it
	 * 
	 * @return <code>true</code> if the download was successful, and <code>false</code>
	 * otherwise.
	 */
	public boolean updateInventory()
	{
		InventoryDownloadDialog downloadDialog = new InventoryDownloadDialog(this);
		downloadDialog.setLocationRelativeTo(this);
		return downloadDialog.downloadInventory(inventorySite, inventoryFile);
	}
	
	/**
	 * Set program settings back to their default values
	 */
	public void resetDefaultSettings()
	{
		// TODO: Add the following properties:
		// - table stripe color
		// - inventory table columns
		// - deck table columns
		// - category table columns
		// - preset categories
		
		properties = new Properties();
		properties.put("inventory.version_file", "version.json");
		properties.put("inventory.source", "http://mtgjson.com/json/");
		properties.put("inventory.version", "");
		properties.put("inventory.file", "AllSets-x.json");
		properties.put("inventory.initialcheck", "true");
		properties.put("inventory.location", "./");
		properties.put("initialdir", "./");
		properties.put("recents.count", "4");
		properties.put("recents.files", "");
		properties.put("inventory.columns", "Name,Expansion,Mana Cost,Type");
	}
	
	/**
	 * Write the latest values of the settings to the settings file.
	 */
	public void saveSettings()
	{
		try (FileOutputStream out = new FileOutputStream(PROPERTIES_FILE))
		{
			// TODO: Write a header comment
			StringJoiner str = new StringJoiner("|");
			for (JMenuItem recent: recentItems)
				str.add(recents.get(recent).getPath());
			properties.put("recents.files", str.toString());
			str = new StringJoiner(",");
			for (CardCharacteristic c: inventoryCharacteristics)
				str.add(c.toString());
			properties.put("inventory.columns", str.toString());
			properties.store(out, "");
		}
		catch (IOException e)
		{
			JOptionPane.showMessageDialog(null, "Error writing " + PROPERTIES_FILE + ": " + e.getMessage() + ".", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * Update the recently-opened files to add the most recently-opened one, and delete
	 * the oldest one if too many are there.
	 * 
	 * @param f File to add to the list
	 */
	public void updateRecents(File f)
	{
		if (!recents.containsValue(f))
		{
			recentsMenu.setEnabled(true);
			if (recentItems.size() >= recentCount)
			{
				JMenuItem eldest = recentItems.poll();
				recents.remove(eldest);
				recentsMenu.remove(eldest);
			}
			JMenuItem mostRecent = new JMenuItem(f.getPath());
			recentItems.offer(mostRecent);
			recents.put(mostRecent, f);
			mostRecent.addActionListener((e) -> open(f));
			recentsMenu.add(mostRecent);
		}
	}
	
	/**
	 * Create a new editor frame.
	 * 
	 * @param name Name of the new frame (also the name of the file)
	 * @see gui.editor.EditorFrame
	 */
	public void newEditor()
	{
		EditorFrame frame = new EditorFrame(++untitled, this);
		frame.setVisible(true);
		editors.add(frame);
		decklistDesktop.add(frame);
		selectFrame(frame);
	}
	
	/**
	 * Open the file chooser to select a file, and if a file was selected,
	 * parse it and initialize a Deck from it.
	 */
	public void open()
	{
		switch (fileChooser.showOpenDialog(this))
		{
		case JFileChooser.APPROVE_OPTION:
			open(fileChooser.getSelectedFile());
			updateRecents(fileChooser.getSelectedFile());
			break;
		case JFileChooser.CANCEL_OPTION:
		case JFileChooser.ERROR_OPTION:
			break;
		default:
			break;
		}
	}
	
	/**
	 * Open the specified file and create an editor for it.
	 * 
	 * @param f File to open.
	 */
	public void open(File f)
	{
		EditorFrame frame = null;
		for (EditorFrame e: editors)
		{
			if (e.file().equals(f))
			{
				frame = e;
				break;
			}
		}
		if (frame == null)
		{
			frame = new EditorFrame(f, ++untitled, this);
			frame.setVisible(true);
			editors.add(frame);
			decklistDesktop.add(frame);
		}
		properties.put("initialdir", fileChooser.getCurrentDirectory().getPath());
		try
		{
			frame.setSelected(true);
		}
		catch (PropertyVetoException e)
		{
			JOptionPane.showMessageDialog(null, "Error creating new editor: " + e.getMessage() + ".", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * Attempt to close the specified frame.
	 * 
	 * @param frame Frame to close
	 * @return <code>true</code> if the frame was closed, and <code>false</code>
	 * otherwise.
	 */
	public boolean close(EditorFrame frame)
	{
		if (!editors.contains(frame) || !frame.close())
			return false;
		else
		{
			editors.remove(frame);
			if (editors.size() > 0)
				selectFrame(editors.get(0));
			else
				selectedFrame = null;
			return true;
		}
	}
	
	/**
	 * Attempts to close all of the open editors.  If any can't be closed for
	 * whatever reason, they will remain open, but the rest will still be closed.
	 * 
	 * @return <code>true</code> if all open editors were successfully closed, and
	 * <code>false</code> otherwise.
	 */
	public boolean closeAll()
	{
		List<EditorFrame> e = new ArrayList<EditorFrame>(editors);
		boolean closedAll = true;
		for (EditorFrame editor: e)
			closedAll &= close(editor);
		return closedAll;
	}
	
	/**
	 * If specified editor frame has a file associated with it, save
	 * it to that file.  Otherwise, open the file dialog and save it
	 * to whatever is chosen (save as).
	 * 
	 * @param frame EditorFrame to save
	 */
	public void save(EditorFrame frame)
	{
		if (!frame.save())
			saveAs(frame);
	}
	
	/**
	 * Save the specified editor frame to a file chosen from a JFileChooser.
	 * 
	 * @param frame Frame to save.
	 */
	public void saveAs(EditorFrame frame)
	{
		// If the file exists, let the user choose whether or not to overwrite.  If he or she chooses not to,
		// ask for a new file.  If he or she cancels at any point, stop asking and don't open a file.
		boolean done;
		do
		{
			done = false;
			switch (fileChooser.showSaveDialog(this))
			{
			case JFileChooser.APPROVE_OPTION:
				File f = fileChooser.getSelectedFile();
				boolean write;
				if (f.exists())
				{
					int option = JOptionPane.showConfirmDialog(null, "File " + f.getName() + " already exists.  Overwrite?", "Warning", JOptionPane.YES_NO_CANCEL_OPTION);
					write = (option == JOptionPane.YES_NO_CANCEL_OPTION);
					done = (option != JOptionPane.NO_OPTION);
				}
				else
				{
					write = true;
					done = true;
				}
				if (write)
					frame.save(f);
				break;
			case JFileChooser.CANCEL_OPTION:
			case JFileChooser.ERROR_OPTION:
				done = true;
				break;
			default:
				break;
			}
		} while (!done);
		properties.put("initialdir", fileChooser.getCurrentDirectory().getPath());
	}
	
	/**
	 * Attempt to save all open editors.  For each that needs a file, ask for a file
	 * to save to.
	 */
	public void saveAll()
	{
		for (EditorFrame editor: editors)
			save(editor);
	}
	
	/**
	 * @param id UID of the Card to look for
	 * @return The Card with the given UID.
	 */
	public Card getCard(String id)
	{
		return inventory.get(id);
	}
	
	/**
	 * Update the currently-selected card.
	 * 
	 * @param card Card to select
	 */
	public void selectCard(Card card)
	{
		if (selectedCard == null || !selectedCard.equals(card))
		{
			selectedCard = card;
			oracleTextPane.setText("<html>" + card.toHTMLString() + "</html>");
			oracleTextPane.setCaretPosition(0);
		}
	}
	
	/**
	 * @return The currently-selected Card.
	 */
	public Card getSelectedCard()
	{
		return selectedCard;
	}
	
	/**
	 * @return A List containing each currently-selected card in the inventory table.
	 */
	public List<Card> getSelectedCards()
	{
		return Arrays.stream(inventoryTable.getSelectedRows())
					 .mapToObj((r) -> inventory.get(inventoryTable.convertRowIndexToModel(r)))
					 .collect(Collectors.toList());
	}
	
	/**
	 * Set the currently-active frame.  This is the one that will be operated on
	 * when single-deck actions are taken from the main frame, such as saving
	 * and closing.
	 * 
	 * @param frame EditorFrame to operate on from now on
	 */
	public void selectFrame(EditorFrame frame)
	{
		try
		{
			frame.setSelected(true);
			selectedFrame = frame;
		}
		catch (PropertyVetoException e)
		{}
	}
}
