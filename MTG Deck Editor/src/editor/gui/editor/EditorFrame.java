package editor.gui.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import editor.category.CategorySpec;
import editor.database.Card;
import editor.database.Deck;
import editor.database.Hand;
import editor.database.LegalityChecker;
import editor.database.characteristics.CardCharacteristic;
import editor.filter.Filter;
import editor.gui.CardImagePanel;
import editor.gui.CardTable;
import editor.gui.CardTableModel;
import editor.gui.MainFrame;
import editor.gui.ScrollablePanel;
import editor.gui.SettingsDialog;
import editor.gui.TableMouseAdapter;

/**
 * This class represents an internal frame for editing a deck.  It contains a table that shows all cards
 * and their counts in the deck as well as zero or more tables for categories within it.  It can add cards
 * to a deck and add, edit, and delete categories.  It is contained within the main frame, which has the
 * inventory from which cards can be added.
 * 
 * TODO: Make popup menu category setting work for multiple selection in the main table
 * TODO: Change the category tab's exclude popup option to set categories
 * TODO: Add a filter bar to the main tab just like the inventory has
 * TODO: Add a second table to the main panel showing commander/sideboard/extra cards
 * TODO: Overhaul the category system so that changes are made directly to the deck, and then the category panel is refreshed (or the whole tab if necessary)
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class EditorFrame extends JInternalFrame
{
	/**
	 * Tab number containing the main list of cards.
	 */
	public static final int MAIN_TABLE = 0;
	/**
	 * Tab number containing categories.
	 */
	public static final int CATEGORIES = 1;
	/**
	 * Tab number containing sample hands.
	 */
	public static final int SAMPLE_HANDS = 2;
	/**
	 * Tab number containing the changelog.
	 */
	public static final int CHANGELOG = 3;
	
	/**
	 * This enum represents an order that category panels can be sorted in.
	 * 
	 * @author Alec Roelke
	 */
	public static enum CategoryOrder
	{
		A_Z("A-Z"),
		Z_A("Z-A"),
		ASCENDING("Ascending Size"),
		DESCENDING("Descending Size");
		
		/**
		 * String to display when a String representation of this
		 * CategoryOrder is called for.
		 */
		private final String name;
		
		/**
		 * Create a new CategoryOrder.
		 * 
		 * @param n Name of the new CategoryOrder
		 */
		private CategoryOrder(String n)
		{
			name = n;
		}
		
		/**
		 * @return The String representation of this CategoryOrder
		 */
		@Override
		public String toString()
		{
			return name;
		}
	}
	
	/**
	 * Parent MainFrame.
	 */
	private MainFrame parent;
	/**
	 * Master decklist to which cards are added.
	 */
	private Deck deck;
	/**
	 * Last-saved version of the deck, used for the changelog.
	 */
	private Deck original;
	/**
	 * Main table showing the cards in the deck.
	 */
	private CardTable table;
	/**
	 * CardListTableModel for showing the deck list.
	 */
	private CardTableModel model;
	/**
	 * Panel containing categories.
	 */
	private JPanel categoriesContainer;
	/**
	 * List of categories in this deck.
	 */
	private List<CategoryPanel> categories;
	/**
	 * Label showing the total number of cards in the deck.
	 */
	private JLabel countLabel;
	/**
	 * Label showing the total number of land cards in the deck.
	 */
	private JLabel landLabel;
	/**
	 * Label showing the total number of nonland cards in the deck.
	 */
	private JLabel nonlandLabel;
	/**
	 * Label showing the average CMC of nonland cards in the deck.
	 */
	private JLabel avgCMCLabel;
	/**
	 * Tabbed pane for choosing whether to display the entire deck or the categories.
	 */
	private JTabbedPane listTabs;
	/**
	 * File where the deck was last saved.
	 */
	private File file;
	/**
	 * Whether or not the deck has been saved since it has last been changed.
	 */
	private boolean unsaved;
	/**
	 * Stack containing past actions performed on the deck to represent the undo buffer.
	 */
	private Stack<UndoableAction> undoBuffer;
	/**
	 * Stack containing future actions that have been performed on the deck to represent
	 * the redo buffer.  This contains only things that have been on the undo buffer
	 * and have been undone, and is cleared when a new action is performed.
	 */
	private Stack<UndoableAction> redoBuffer;
	/**
	 * Combo box showing categories to jump between them.
	 */
	private JComboBox<String> switchCategoryBox;
	/**
	 * Model for the combo box to display items.
	 */
	private DefaultComboBoxModel<String> switchCategoryModel;
	/**
	 * Hand containing cards to show in the sample hand tab.
	 */
	private Hand hand;
	/**
	 * Model for displaying the sample hand.
	 */
	private CardTableModel handModel;
	/**
	 * Table displaying the sample hand.
	 */
	private CardTable handTable;
	/**
	 * Size of starting hands.
	 */
	private int startingHandSize;
	/**
	 * Panel containing images for the sample hand.
	 */
	private ScrollablePanel imagePanel;
	/**
	 * Scroll pane containing the sample hand image panel.
	 */
	private JScrollPane imagePane;
	/**
	 * Combo box allowing changes to be made in the order that categories are display in.
	 */
	private JComboBox<CategoryOrder> sortCategoriesBox;
	/**
	 * Text area to show the changelog.
	 */
	private JTextArea changelogArea;

	/**
	 * Create a new EditorFrame inside the specified MainFrame and with the name
	 * "Untitled [u] *"
	 * 
	 * @param u Number of the untitled deck
	 * @param p Parent MainFrame
	 */
	public EditorFrame(int u, MainFrame p)
	{
		super("Untitled " + u, true, true, true, true);
		setBounds(((u - 1)%5)*30, ((u - 1)%5)*30, 600, 600);
		getContentPane().setLayout(new BorderLayout(0, 0));
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		parent = p;
		deck = new Deck();
		original = new Deck();
		file = null;
		unsaved = false;
		undoBuffer = new Stack<UndoableAction>();
		redoBuffer = new Stack<UndoableAction>();
		startingHandSize = Integer.valueOf(SettingsDialog.getSetting(SettingsDialog.HAND_SIZE));

		// Panel for showing buttons to add and remove cards
		// The buttons are concentrated in the middle of the panel
		JPanel buttonPanel = new JPanel();
		getContentPane().add(buttonPanel, BorderLayout.WEST);
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		
		buttonPanel.add(Box.createVerticalGlue());
		
		// Add button to add one copy of the currently-selected card to the deck
		JButton addButton = new JButton("+");
		addButton.addActionListener((e) -> addCards(parent.getSelectedCards(), 1));
		addButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, addButton.getMaximumSize().height));
		buttonPanel.add(addButton);

		// Remove button to remove one copy of each selected card from the deck
		JButton removeButton = new JButton("−");
		removeButton.addActionListener((e) -> removeSelectedCards(1));
		removeButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, removeButton.getMaximumSize().height));
		buttonPanel.add(removeButton);

		// Delete button to remove all copies of each selected card from the deck
		JButton deleteButton = new JButton("X");
		deleteButton.addActionListener((e) -> removeSelectedCards(Integer.MAX_VALUE));
		deleteButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, deleteButton.getMaximumSize().height));
		buttonPanel.add(deleteButton);

		buttonPanel.add(Box.createVerticalGlue());
		
		// The first tab is the master list tab, and the second tab is the categories tab
		listTabs = new JTabbedPane(SwingConstants.TOP);
		getContentPane().add(listTabs, BorderLayout.CENTER);

		model = new CardTableModel(this, deck, Arrays.stream(SettingsDialog.getSetting(SettingsDialog.EDITOR_COLUMNS).split(",")).map(CardCharacteristic::get).collect(Collectors.toList()));

		// Create the table so that it resizes if the window is too big but not if it's too small
		table = new CardTable(model);
		table.setStripeColor(SettingsDialog.stringToColor(SettingsDialog.getSetting(SettingsDialog.EDITOR_STRIPE)));
		// When a card is selected in the master list table, select it for adding
		table.getSelectionModel().addListSelectionListener((e) -> { 
			if (!e.getValueIsAdjusting())
			{
				ListSelectionModel lsm = (ListSelectionModel)e.getSource();
				if (!lsm.isSelectionEmpty())
				{
					parent.selectCard(deck.get(table.convertRowIndexToModel(lsm.getMinSelectionIndex())));
					for (CategoryPanel c: categories)
						c.table.clearSelection();
				}
			}
		});
		for (int i = 0; i < table.getColumnCount(); i++)
			if (model.isCellEditable(0, i))
				table.getColumn(model.getColumnName(i)).setCellEditor(model.getColumnCharacteristic(i).createCellEditor(this));
		table.setTransferHandler(new EditorTableTransferHandler());
		table.setDragEnabled(true);
		table.setDropMode(DropMode.ON);
		listTabs.addTab("Cards", new JScrollPane(table));
		
		// Table popup menu
		JPopupMenu tableMenu = new JPopupMenu();
		table.addMouseListener(new TableMouseAdapter(table, tableMenu));
		
		// Add single copy item
		JMenuItem addSinglePopupItem = new JMenuItem("Add Single Copy");
		addSinglePopupItem.addActionListener((e) -> {addCards(getSelectedCards(), 1);});
		tableMenu.add(addSinglePopupItem);
		
		// Fill playset item
		JMenuItem playsetPopupItem = new JMenuItem("Fill Playset");
		playsetPopupItem.addActionListener((e) -> {
			for (Card c: getSelectedCards())
				addCard(c, 4 - deck.count(c));
		});
		tableMenu.add(playsetPopupItem);
		
		// Add variable item
		JMenuItem addNPopupItem = new JMenuItem("Add Copies...");
		addNPopupItem.addActionListener((e) -> {
			JPanel contentPanel = new JPanel(new BorderLayout());
			contentPanel.add(new JLabel("Copies to add:"), BorderLayout.WEST);
			JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 1));
			contentPanel.add(spinner, BorderLayout.SOUTH);
			if (JOptionPane.showOptionDialog(null, contentPanel, "Add Cards", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null) == JOptionPane.OK_OPTION)
				addCards(getSelectedCards(), (Integer)spinner.getValue());
		});
		tableMenu.add(addNPopupItem);
		
		tableMenu.add(new JSeparator());
		
		// Remove single copy item
		JMenuItem removeSinglePopupItem = new JMenuItem("Remove Single Copy");
		removeSinglePopupItem.addActionListener((e) -> removeSelectedCards(1));
		tableMenu.add(removeSinglePopupItem);
		
		// Remove all item
		JMenuItem removeAllPopupItem = new JMenuItem("Remove All Copies");
		removeAllPopupItem.addActionListener((e) -> removeSelectedCards(Integer.MAX_VALUE));
		tableMenu.add(removeAllPopupItem);
		
		// Remove variable item
		JMenuItem removeNPopupItem = new JMenuItem("Remove Copies...");
		removeNPopupItem.addActionListener((e) -> {
			JPanel contentPanel = new JPanel(new BorderLayout());
			contentPanel.add(new JLabel("Copies to remove:"), BorderLayout.WEST);
			JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 1));
			contentPanel.add(spinner, BorderLayout.SOUTH);
			if (JOptionPane.showOptionDialog(null, contentPanel, "Add Cards", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null) == JOptionPane.OK_OPTION)
				removeSelectedCards((Integer)spinner.getValue());
		});
		tableMenu.add(removeNPopupItem);
		
		tableMenu.add(new JSeparator());
		
		// Set categories submenu
		JMenu setCategoriesMenu = new JMenu("Set Categories");
		tableMenu.add(setCategoriesMenu);
		tableMenu.addPopupMenuListener(new PopupMenuListener()
		{
			@Override
			public void popupMenuCanceled(PopupMenuEvent e)
			{}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
			{}

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e)
			{
				List<Card> cards = getSelectedCards();
				if (cards.size() != 1 && !categories.isEmpty())
					setCategoriesMenu.setEnabled(false);
				else if (!categories.isEmpty())
				{
					setCategoriesMenu.setEnabled(true);
					setCategoriesMenu.removeAll();
					for (CategoryPanel category: categories.stream().sorted((a, b) -> a.spec().name.compareTo(b.spec().name)).collect(Collectors.toList()))
					{
						JCheckBoxMenuItem containsBox = new JCheckBoxMenuItem(category.spec().name);
						containsBox.setSelected(category.contains(cards.get(0)));
						containsBox.addActionListener((a) -> {
							if (((JCheckBoxMenuItem)a.getSource()).isSelected())
							{
								UndoableAction include = new UndoableAction()
								{
									@Override
									public boolean undo()
									{
										return category.exclude(cards.get(0));
									}
									
									@Override
									public boolean redo()
									{
										return category.include(cards.get(0));
									}
								};
								if (include.redo())
								{
									undoBuffer.push(include);
									redoBuffer.clear();
									setUnsaved();
									update();
								}
							}
							else
							{
								UndoableAction exclude = new UndoableAction()
								{
									@Override
									public boolean undo()
									{
										return category.include(cards.get(0));
									}
									
									@Override
									public boolean redo()
									{
										return category.exclude(cards.get(0));
									}
								};
								if (exclude.redo())
								{
									undoBuffer.push(exclude);
									redoBuffer.clear();
									setUnsaved();
									update();
								}
							}
						});
						setCategoriesMenu.add(containsBox);
					}
				}
			}
		});
		
		// Panel containing categories
		JPanel categoriesPanel = new JPanel(new BorderLayout());

		// Panel containing components above the category panel
		JPanel categoryHeaderPanel = new JPanel();
		categoryHeaderPanel.setLayout(new BoxLayout(categoryHeaderPanel, BoxLayout.X_AXIS));
		categoriesPanel.add(categoryHeaderPanel, BorderLayout.NORTH);
		
		// Button to add a new category
		JPanel addCategoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JButton addCategoryButton = new JButton("Add");
		addCategoryButton.addActionListener((e) -> addCategory(createCategory()));
		addCategoryPanel.add(addCategoryButton);
		categoryHeaderPanel.add(addCategoryPanel);
		
		// Combo box to change category sort order
		JPanel sortCategoriesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		sortCategoriesPanel.add(new JLabel("Display order:"));
		sortCategoriesBox = new JComboBox<CategoryOrder>(CategoryOrder.values());
		sortCategoriesBox.addActionListener((e) -> {
			if (sortCategoriesBox.isPopupVisible())
			{
				sortCategories(sortCategoriesBox.getItemAt(sortCategoriesBox.getSelectedIndex()));
				update();
			}
		});
		sortCategoriesPanel.add(sortCategoriesBox);
		categoryHeaderPanel.add(sortCategoriesPanel);
		
		// Combo box to switch to a different category
		JPanel switchCategoryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		switchCategoryBox = new JComboBox<String>(switchCategoryModel = new DefaultComboBoxModel<String>());
		switchCategoryBox.setEnabled(false);
		switchCategoryBox.addActionListener((e) -> {
			if (switchCategoryBox.isPopupVisible())
			{
				CategoryPanel toView = getCategory(switchCategoryBox.getItemAt(switchCategoryBox.getSelectedIndex()));
				if (toView != null)
				{
					toView.scrollRectToVisible(new Rectangle(toView.getSize()));
					toView.flash();
				}
			}
		});
		switchCategoryPanel.add(new JLabel("Go to category:"));
		switchCategoryPanel.add(switchCategoryBox);
		categoryHeaderPanel.add(switchCategoryPanel);

		// Make sure all parts of the category panel fit inside the window (this is necessary because
		// JScrollPanes do weird things with non-scroll-savvy components)
		JPanel categoriesSuperContainer = new ScrollablePanel(new BorderLayout(), ScrollablePanel.TRACK_WIDTH);
		categoriesContainer = new JPanel();
		categoriesContainer.setLayout(new BoxLayout(categoriesContainer, BoxLayout.Y_AXIS));
		categories = new ArrayList<CategoryPanel>();
		
		// The category panel is a vertically-scrollable panel that contains all categories stacked vertically
		// The categories should have a constant height, but fit the container horizontally
		categoriesSuperContainer.add(categoriesContainer, BorderLayout.NORTH);
		categoriesPanel.add(new JScrollPane(categoriesSuperContainer, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
		listTabs.addTab("Categories", categoriesPanel);
		
		// Sample hands
		JPanel handPanel = new JPanel(new BorderLayout());
		
		// Table showing the cards in hand
		hand = new Hand(deck);
		handModel = new CardTableModel(this, hand, Arrays.stream(SettingsDialog.getSetting(SettingsDialog.HAND_COLUMNS).split(",")).map(CardCharacteristic::get).collect(Collectors.toList()));
		handTable = new CardTable(handModel);
		handTable.setCellSelectionEnabled(false);
		handTable.setStripeColor(SettingsDialog.stringToColor(SettingsDialog.getSetting(SettingsDialog.EDITOR_STRIPE)));
		handTable.setPreferredScrollableViewportSize(new Dimension(handTable.getPreferredSize().width, handTable.getRowHeight()*10));
		
		imagePanel = new ScrollablePanel(ScrollablePanel.TRACK_HEIGHT);
		imagePanel.setLayout(new BoxLayout(imagePanel, BoxLayout.X_AXIS));
		imagePane = new JScrollPane(imagePanel);
		setHandBackground(SettingsDialog.stringToColor(SettingsDialog.getSetting(SettingsDialog.HAND_BGCOLOR)));
		
		// Control panel for manipulating the sample hand
		JPanel handModPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
		JButton newHandButton = new JButton("New Hand");
		newHandButton.addActionListener((e) -> {
			hand.newHand(startingHandSize);
			handModel.fireTableDataChanged();
			
			imagePanel.removeAll();
			for (Card c: hand)
			{
				CardImagePanel panel = new CardImagePanel();
				imagePanel.add(panel);
				panel.setCard(c);
				panel.setBackground(SettingsDialog.stringToColor(SettingsDialog.getSetting(SettingsDialog.HAND_BGCOLOR)));
				imagePanel.add(Box.createHorizontalStrut(10));
			}
			imagePanel.validate();
			update();
		});
		handModPanel.add(newHandButton);
		JButton mulliganButton = new JButton("Mulligan");
		mulliganButton.addActionListener((e) -> {
			hand.mulligan();
			handModel.fireTableDataChanged();
			
			imagePanel.removeAll();
			for (Card c: hand)
			{
				CardImagePanel panel = new CardImagePanel();
				imagePanel.add(panel);
				panel.setCard(c);
				panel.setBackground(SettingsDialog.stringToColor(SettingsDialog.getSetting(SettingsDialog.HAND_BGCOLOR)));
				imagePanel.add(Box.createHorizontalStrut(10));
			}
			imagePanel.validate();
			update();
		});
		handModPanel.add(mulliganButton);
		JButton drawCardButton = new JButton("Draw a Card");
		drawCardButton.addActionListener((e) -> {
			hand.draw();
			handModel.fireTableDataChanged();
			
			CardImagePanel panel = new CardImagePanel();
			panel.setBackground(SettingsDialog.stringToColor(SettingsDialog.getSetting(SettingsDialog.HAND_BGCOLOR)));
			imagePanel.add(panel);
			panel.setCard(hand.get(hand.size() - 1));
			imagePanel.add(Box.createHorizontalStrut(10));
			imagePanel.validate();
			update();
		});
		handModPanel.add(drawCardButton);
		JButton excludeButton = new JButton("Exclude...");
		excludeButton.addActionListener((e) -> {
			JPanel excludePanel = new JPanel();
			excludePanel.setLayout(new BoxLayout(excludePanel, BoxLayout.X_AXIS));
			
			DefaultListModel<Card> excludeModel = new DefaultListModel<Card>();
			JList<Card> exclude = new JList<Card>(excludeModel);
			excludePanel.add(new JScrollPane(exclude));
			
			JPanel excludeButtonPanel = new JPanel();
			excludeButtonPanel.setLayout(new BoxLayout(excludeButtonPanel, BoxLayout.Y_AXIS));
			excludeButtonPanel.add(Box.createVerticalGlue());
			JButton addExclusionButton = new JButton("<");
			excludeButtonPanel.add(addExclusionButton);
			JButton removeExclusionButton = new JButton(">");
			excludeButtonPanel.add(removeExclusionButton);
			excludeButtonPanel.add(Box.createVerticalGlue());
			excludePanel.add(excludeButtonPanel);
			
			CardTableModel excludeTableModel = new CardTableModel(deck, Arrays.asList(CardCharacteristic.NAME, CardCharacteristic.COUNT));
			CardTable excludeTable = new CardTable(excludeTableModel);
			excludeTable.setStripeColor(SettingsDialog.stringToColor(SettingsDialog.getSetting(SettingsDialog.EDITOR_STRIPE)));
			excludePanel.add(new JScrollPane(excludeTable));
			
			addExclusionButton.addActionListener((a) -> {
				for (Card c: Arrays.stream(excludeTable.getSelectedRows()).mapToObj((r) -> deck.get(excludeTable.convertRowIndexToModel(r))).collect(Collectors.toList()))
				{
					int n = 0;
					for (int i = 0; i < excludeModel.size(); i++)
						if (excludeModel.elementAt(i).equals(c))
							n++;
					if (n < deck.count(c))
						excludeModel.addElement(c);
				}
			});
			removeExclusionButton.addActionListener((f) -> {
				for (Card c: Arrays.stream(exclude.getSelectedIndices()).mapToObj((r) -> excludeModel.getElementAt(r)).collect(Collectors.toList()))
					excludeModel.removeElement(c);
			});
			
			for (Card c: hand.excluded())
				excludeModel.addElement(c);
			JOptionPane.showMessageDialog(null, excludePanel, "Exclude Cards", JOptionPane.PLAIN_MESSAGE);
			
			hand.clearExclusion();
			for (int i = 0; i < excludeModel.size(); i++)
				hand.exclude(excludeModel.get(i));
		});
		handModPanel.add(excludeButton);
		JButton probabilityButton = new JButton("Calculate...");
		probabilityButton.addActionListener((e) -> {
			JOptionPane pane = new JOptionPane(new CalculateHandPanel(deck, SettingsDialog.stringToColor(SettingsDialog.getSetting(SettingsDialog.EDITOR_STRIPE))));
			Dialog dialog = pane.createDialog(this, "Card Draw Probability");
			dialog.setResizable(true);
			dialog.setVisible(true);
		});
		handModPanel.add(probabilityButton);
		
		JSplitPane handSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(handTable), imagePane);
		handSplit.setOneTouchExpandable(true);
		handSplit.setContinuousLayout(true);
		handPanel.add(handSplit, BorderLayout.CENTER);
		handPanel.add(handModPanel, BorderLayout.SOUTH);
		listTabs.addTab("Sample Hand", handPanel);
		
		// TODO: Add tabs for deck analysis
		// - category pie chart
		// - mana curve
		// - color distribution (cards/devotion[max,avg,total])
		// - mana production distribution
		// - notes

		// Panel to show the stats of the deck
		JPanel bottomPanel = new JPanel();
		GridBagLayout bottomLayout = new GridBagLayout();
		bottomLayout.columnWidths = new int[] {0, 0};
		bottomLayout.columnWeights = new double[] {1.0, 1.0};
		bottomLayout.rowHeights = new int[] {0};
		bottomLayout.rowWeights = new double[] {1.0};
		bottomPanel.setLayout(bottomLayout);
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);

		// Labels to counts for total cards, lands, and nonlands
		JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
		countLabel = new JLabel();
		statsPanel.add(countLabel);
		landLabel = new JLabel();
		statsPanel.add(landLabel);
		nonlandLabel = new JLabel();
		statsPanel.add(nonlandLabel);
		avgCMCLabel = new JLabel();
		statsPanel.add(avgCMCLabel);
		updateCount();
		GridBagConstraints statsConstraints = new GridBagConstraints();
		statsConstraints.anchor = GridBagConstraints.WEST;
		bottomPanel.add(statsPanel, statsConstraints);
		
		// Check legality button
		JPanel legalityPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
		JButton legalityButton = new JButton("Show Legality");
		legalityButton.addActionListener((e) -> {
			LegalityChecker checker = new LegalityChecker();
			checker.checkLegality(deck);
			JOptionPane.showMessageDialog(null, new LegalityPanel(checker), "Legality of " + deckName(), JOptionPane.PLAIN_MESSAGE);
		});
		legalityPanel.add(legalityButton);
		GridBagConstraints legalityConstraints = new GridBagConstraints();
		legalityConstraints.anchor = GridBagConstraints.EAST;
		bottomPanel.add(legalityPanel, legalityConstraints);

		// Changelog
		JPanel changelogPanel = new JPanel(new BorderLayout());
		changelogArea = new JTextArea();
		changelogArea.setEditable(false);
		changelogPanel.add(new JScrollPane(changelogArea), BorderLayout.CENTER);
		JPanel clearLogPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton clearLogButton = new JButton("Clear Change Log");
		clearLogButton.addActionListener((e) -> {
			if (!changelogArea.getText().isEmpty()
					&& JOptionPane.showInternalConfirmDialog(EditorFrame.this, "Change log cannot be restored once saved.  Clear change log?", "Clear Change Log?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
			{
				undoBuffer.push(new UndoableAction()
				{
					String text = changelogArea.getText();
					
					@Override
					public boolean undo()
					{
						changelogArea.setText(text);
						return true;
					}
	
					@Override
					public boolean redo()
					{
						changelogArea.setText("");
						return true;
					}
				});
				undoBuffer.peek().redo();
				redoBuffer.clear();
			}
		});
		clearLogPanel.add(clearLogButton);
		changelogPanel.add(clearLogPanel, BorderLayout.SOUTH);
		listTabs.addTab("Change Log", changelogPanel);
		
		setTransferHandler(new EditorImportHandler());
		
		// Handle various frame events, including selecting and closing
		addInternalFrameListener(new InternalFrameAdapter()
		{
			@Override
			public void internalFrameActivated(InternalFrameEvent e)
			{
				parent.selectFrame(EditorFrame.this);
			}

			@Override
			public void internalFrameClosing(InternalFrameEvent e)
			{
				parent.close(EditorFrame.this);
			}
		});
	}

	/**
	 * Create an EditorFrame with the specified MainFrame as its parent and the name
	 * of the specified file.  The deck will be loaded from the file.
	 * 
	 * @param f File to load a deck from
	 * @param u Number of the new EditorFrame (determines initial position in the window)
	 * @param p Parent of the new EditorFrame
	 */
	public EditorFrame(File f, int u, MainFrame p)
	{
		this(u, p);
		
		JDialog progressDialog = new JDialog(null, Dialog.ModalityType.APPLICATION_MODAL);
		JProgressBar progressBar = new JProgressBar();
		LoadWorker worker = new LoadWorker(f, progressBar, progressDialog);
		
		JPanel progressPanel = new JPanel(new BorderLayout(0, 5));
		progressDialog.setContentPane(progressPanel);
		progressPanel.add(new JLabel("Opening " + f.getName() + "..."), BorderLayout.NORTH);
		progressPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		progressBar.setIndeterminate(false);
		progressPanel.add(progressBar, BorderLayout.CENTER);
		JPanel cancelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener((e) -> worker.cancel(false));
		cancelPanel.add(cancelButton);
		progressPanel.add(cancelPanel, BorderLayout.SOUTH);
		progressDialog.pack();
		
		worker.execute();
		progressDialog.setLocationRelativeTo(parent);
		progressDialog.setVisible(true);
		try
		{
			worker.get();
		}
		catch (InterruptedException | ExecutionException e)
		{
			JOptionPane.showMessageDialog(null, "Error opening " + f.getName() + ": " + e.getCause().getMessage() + ".", "Error", JOptionPane.ERROR_MESSAGE);
			deck.clear();
			updateCount();
			categories.clear();
			categoriesContainer.removeAll();
			updateCategoryPanel();
			update();
		}
		original.addAll(deck);
		listTabs.setSelectedIndex(MAIN_TABLE);
		hand.refresh();
	}

	/**
	 * @return The name of the deck being edited (its file name).
	 */
	public String deckName()
	{
		String deckName;
		if (unsaved)
			deckName = getTitle().substring(0, getTitle().length() - 2);
		else
			deckName = getTitle();
		return deckName;
	}
	
	/**
	 * @return The File containing the deck being edited.
	 */
	public File file()
	{
		return file;
	}
	
	/**
	 * @return The names of all the categories in the deck, sorted alphabetically.
	 */
	public String[] categoryNames()
	{
		List<String> names = categories.stream().map((c) -> c.spec().name).collect(Collectors.toList());
		Collections.sort(names);
		return names.toArray(new String[names.size()]);
	}

	/**
	 * Get the category with the specified name in the deck.
	 * 
	 * @param name Name of the category to search for
	 * @return The category with the specified name, or <code>null</code> if there is none.
	 */
	private CategoryPanel getCategory(String name)
	{
		for (CategoryPanel category: categories)
			if (category.spec().name.equals(name))
				return category;
		return null;
	}
	
	/**
	 * Open the dialog to create a new specification for a deck category.
	 */
	public CategorySpec createCategory()
	{
		CategoryEditorPanel editor = null;
		do
		{
			editor = CategoryEditorPanel.showCategoryEditor(editor != null ? editor.spec() : null);
			if (editor != null && deck.containsCategory(editor.spec().name))
				JOptionPane.showMessageDialog(null, "Categories must have unique names.", "Error", JOptionPane.ERROR_MESSAGE);
		} while (editor != null && deck.containsCategory(editor.spec().name));
		if (editor != null)
			return editor.spec();
		else
			return null;
	}
	
	/**
	 * Create a new CategoryPanel out of the given specification.
	 * 
	 * @param spec Specification for the category of the new CategoryPanel
	 * @return The new CategoryPanel.
	 */
	private CategoryPanel createCategory(CategorySpec spec)
	{
		if (!deck.containsCategory(spec.name))
		{
			deck.addCategory(spec);
			CategoryPanel newCategory = new CategoryPanel(deck.getCategory(spec.name), this);
			// When a card is selected in a category, the others should deselect
			newCategory.table.getSelectionModel().addListSelectionListener((e) -> {
				ListSelectionModel lsm = (ListSelectionModel)e.getSource();
				if (!lsm.isSelectionEmpty())
				{
					if (!e.getValueIsAdjusting())
						parent.selectCard(deck.getCategory(newCategory.spec().name).get(newCategory.table.convertRowIndexToModel(lsm.getMinSelectionIndex())));
					for (CategoryPanel c: categories)
						if (newCategory != c)
							c.table.clearSelection();
					table.clearSelection();
				}
			});
			// Add the behavior for the edit category button
			newCategory.editButton.addActionListener((e) -> editCategory(newCategory.spec().name));
			// Add the behavior for the remove category button
			newCategory.removeButton.addActionListener((e) -> removeCategory(newCategory.spec()));
			// Add the behavior for the color edit button
			newCategory.colorButton.addActionListener((e) -> {
				Color newColor = JColorChooser.showDialog(null, "Choose a Color", newCategory.colorButton.color());
				if (newColor != null && !newColor.equals(newCategory.colorButton.color()))
				{
					newCategory.colorButton.setColor(newColor);
					CategorySpec s = newCategory.spec();
					editCategory(newCategory, s.name, newCategory.colorButton.color(), s.filter);
				}
			});
			
			newCategory.table.setTransferHandler(new EditorTableTransferHandler());
			newCategory.table.setDragEnabled(true);
			
			// Add the behavior for clicking on the category's table
			// Table popup menu
			JPopupMenu tableMenu = new JPopupMenu();
			newCategory.table.addMouseListener(new TableMouseAdapter(newCategory.table, tableMenu));
			
			// Add single copy item
			JMenuItem addSinglePopupItem = new JMenuItem("Add Single Copy");
			addSinglePopupItem.addActionListener((e) -> addCards(newCategory.getSelectedCards(), 1));
			tableMenu.add(addSinglePopupItem);
			
			// Fill playset item
			JMenuItem playsetPopupItem = new JMenuItem("Fill Playset");
			playsetPopupItem.addActionListener((e) -> {
				for (Card c: newCategory.getSelectedCards())
					addCard(c, 4 - deck.count(c));
			});
			tableMenu.add(playsetPopupItem);
			
			// Add variable item
			JMenuItem addNPopupItem = new JMenuItem("Add Copies...");
			addNPopupItem.addActionListener((e) -> {
				JPanel contentPanel = new JPanel(new BorderLayout());
				contentPanel.add(new JLabel("Copies to add:"), BorderLayout.WEST);
				JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 1));
				contentPanel.add(spinner, BorderLayout.SOUTH);
				if (JOptionPane.showOptionDialog(null, contentPanel, "Add Cards", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null) == JOptionPane.OK_OPTION)
					addCards(newCategory.getSelectedCards(), (Integer)spinner.getValue());
			});
			tableMenu.add(addNPopupItem);
			
			tableMenu.add(new JSeparator());
			
			// Remove single copy item
			JMenuItem removeSinglePopupItem = new JMenuItem("Remove Single Copy");
			removeSinglePopupItem.addActionListener((e) -> removeSelectedCards(1));
			tableMenu.add(removeSinglePopupItem);
			
			// Remove all item
			JMenuItem removeAllPopupItem = new JMenuItem("Remove All Copies");
			removeAllPopupItem.addActionListener((e) -> removeSelectedCards(Integer.MAX_VALUE));
			tableMenu.add(removeAllPopupItem);
			
			// Remove variable item
			JMenuItem removeNPopupItem = new JMenuItem("Remove Copies...");
			removeNPopupItem.addActionListener((e) -> {
				JPanel contentPanel = new JPanel(new BorderLayout());
				contentPanel.add(new JLabel("Copies to remove:"), BorderLayout.WEST);
				JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 1));
				contentPanel.add(spinner, BorderLayout.SOUTH);
				if (JOptionPane.showOptionDialog(null, contentPanel, "Add Cards", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null) == JOptionPane.OK_OPTION)
					removeSelectedCards((Integer)spinner.getValue());
			});
			tableMenu.add(removeNPopupItem);
			
			tableMenu.add(new JSeparator());
			
			// Remove from category item
			JMenuItem removeFromCategoryItem = new JMenuItem("Exclude from Category");
			removeFromCategoryItem.addActionListener((e) -> {
				List<Card> selectedCards = newCategory.getSelectedCards();
				undoBuffer.push(new UndoableAction()
				{
					@Override
					public boolean undo()
					{
						for (Card c: selectedCards)
							newCategory.include(c);
						return true;
					}
	
					@Override
					public boolean redo()
					{
						for (Card c: selectedCards)
							newCategory.exclude(c);
						return true;
					}
				});
				undoBuffer.peek().redo();
				redoBuffer.clear();
				update();
				setUnsaved();
			});
			tableMenu.add(removeFromCategoryItem);
			
			// Category popup menu
			JPopupMenu categoryMenu = new JPopupMenu();
			newCategory.setComponentPopupMenu(categoryMenu);
			
			// Cut item
			JMenuItem cutItem = new JMenuItem("Cut");
			cutItem.setEnabled(false); // TODO: Implement this
			categoryMenu.add(cutItem);
			
			// Copy item
			JMenuItem copyItem = new JMenuItem("Copy");
			copyItem.setEnabled(false); // TODO: Implement this
			categoryMenu.add(copyItem);
			
			// Paste item
			JMenuItem pasteItem = new JMenuItem("Paste");
			pasteItem.setEnabled(false); // TODO: Implement this
			categoryMenu.add(pasteItem);
			
			// Edit item
			JMenuItem editItem = new JMenuItem("Edit...");
			editItem.addActionListener((e) -> editCategory(newCategory.spec().name));
			categoryMenu.add(editItem);
			
			// Delete item
			JMenuItem deleteItem = new JMenuItem("Delete");
			deleteItem.addActionListener((e) -> removeCategory(newCategory.spec()));
			categoryMenu.add(deleteItem);
			
			// Add to presets item
			JMenuItem addPresetItem = new JMenuItem("Add to presets");
			addPresetItem.addActionListener((e) -> parent.addPreset(newCategory.toString()));
			categoryMenu.add(addPresetItem);
			
			newCategory.table.addMouseListener(new TableMouseAdapter(newCategory.table, tableMenu));
			
			return newCategory;
		}
		else
			return getCategory(spec.name);
	}
	
	/**
	 * Add a category to the deck and create a panel for it.
	 * 
	 * @param category Specification for the new category
	 * @return <code>true</code> if a category was created, and <code>false</code>
	 * otherwise (such as if there was one with the same name already).
	 */
	private boolean insertCategory(CategorySpec spec)
	{
		CategoryPanel category = createCategory(spec);
		categories.add(category);
		updateCategoryPanel();
		update();
		setUnsaved();
		listTabs.setSelectedIndex(CATEGORIES);
		//TODO: Make this work
		category.scrollRectToVisible(new Rectangle(category.getSize()));
		category.flash();
		return true;
	}
	
	/**
	 * Delete the category with the given specification from the deck
	 * and remove its panel.
	 * 
	 * @param category Specification of the category to remove
	 * @return <code>true</code> if the category was successfully removed, and
	 * <code>false</code> otherwise (such as if there was no such category).
	 */
	private boolean deleteCategory(CategorySpec category)
	{
		CategoryPanel panel = getCategory(category.name);
		if (panel == null)
			return false;
		else
		{
			boolean removed = deck.removeCategory(category.name);
			removed &= categories.remove(panel);
			updateCategoryPanel();
			update();
			setUnsaved();
			listTabs.setSelectedIndex(CATEGORIES);
			
			return removed;
		}
	}
	
	/**
	 * Change characteristics of the given CategoryPanel and its category.
	 * 
	 * @param category CategoryPanel to change
	 * @param n New name of the category
	 * @param c New color of the category
	 * @param f New filter for the category
	 * @return <code>true</code> if the category was successfully changed, and
	 * <code>false</code> otherwise.
	 */
	private boolean changeCategory(CategoryPanel category, String n, Color c, Filter f)
	{
		if (!containsCategory(category.spec().name))
			return false;
		else
		{
			category.edit(n, c, f);
			updateCategoryPanel();
			update();
			setUnsaved();
			listTabs.setSelectedIndex(CATEGORIES);
			return true;
		}
	}
	
	/**
	 * Add a category to the deck.
	 * 
	 * @param spec Specification for the category to add
	 * @return <code>true</code> if the category was successfully added and
	 * <code>false</code> otherwise.
	 */
	public boolean addCategory(CategorySpec spec)
	{
		if (spec != null && !deck.containsCategory(spec.name))
		{
			undoBuffer.push(new UndoableAction()
			{
				@Override
				public boolean undo()
				{
					return deleteCategory(spec);
				}

				@Override
				public boolean redo()
				{
					return insertCategory(spec);
				}
			});
			redoBuffer.clear();
			return undoBuffer.peek().redo();
		}
		else
			return false;
	}
	
	/**
	 * Open the category dialog to edit the category with the given
	 * name, if there is one, and then update the undo buffer.
	 * 
	 * @param name Name of the category to edit
	 */
	public void editCategory(String name)
	{
		CategoryPanel toEdit = getCategory(name);
		if (toEdit == null)
			JOptionPane.showMessageDialog(null, "Deck " + deckName() + " has no category named " + name + ".", "Error", JOptionPane.ERROR_MESSAGE);
		else
		{
			CategoryEditorPanel editor = CategoryEditorPanel.showCategoryEditor(toEdit.spec());
			if (editor != null)
				editCategory(toEdit, editor.spec().name, editor.spec().color, editor.spec().filter);
		}
	}
	
	/**
	 * Change the given category to give it the given name, color, filter, and String representation.
	 * 
	 * @param toEdit CategoryPanel showing the category to edit
	 * @param n New name for the category
	 * @param c New color for the category
	 * @param f New filter for the category
	 */
	private void editCategory(CategoryPanel toEdit, String n, Color c, Filter f)
	{
		undoBuffer.push(new UndoableAction()
		{
			private CategorySpec spec = new CategorySpec(toEdit.toString(), parent.inventory());
			private Filter oldFilter = toEdit.spec().filter;
			
			@Override
			public boolean undo()
			{
				return changeCategory(toEdit, spec.name, spec.color, oldFilter);
			}

			@Override
			public boolean redo()
			{
				return changeCategory(toEdit, n, c, f);
			}
		});
		undoBuffer.peek().redo();
		redoBuffer.clear();
	}

	/**
	 * If the given category exists in this EditorFrame, remove it and
	 * remove it from the deck.
	 * 
	 * @param category Panel representing the category to be removed
	 * @return <code>true</code> if the category was successfully removed,
	 * and <code>false</code> otherwise.
	 */
	public boolean removeCategory(CategorySpec category)
	{
		if (!deck.containsCategory(category.name))
			return false;
		else
		{
			undoBuffer.push(new UndoableAction()
			{
				@Override
				public boolean undo()
				{
					return insertCategory(category);
				}

				@Override
				public boolean redo()
				{
					return deleteCategory(category);
				}
			});
			redoBuffer.clear();
			return undoBuffer.peek().redo();
		}
	}
	
	/**
	 * If a category with the given name exists in the deck, remove it
	 * and then update the undo and redo buffers.
	 * 
	 * @param name Name of the category to look for
	 * @return The specification of the category that was removed, or null if no
	 * category was removed
	 * @see EditorFrame#removeCategory(CategoryPanel)
	 */
	public CategorySpec removeCategory(String name)
	{
		CategoryPanel removed = getCategory(name);
		if (removed != null)
		{
			CategorySpec spec = removed.spec();
			removeCategory(spec);
			return spec;
		}
		else
			return null;
	}
	
	/**
	 * @param name Name of the category to search for
	 * @return <code>true</code> if the deck contains a category with the given name,
	 * and <code>false</code> otherwise.
	 */
	public boolean containsCategory(String name)
	{
		return deck.containsCategory(name);
	}
	
	/**
	 * Sort the categories in proper order.
	 * 
	 * @param order Order to sort the categories in.
	 */
	public void sortCategories(CategoryOrder order)
	{
		categoriesContainer.removeAll();
		switch (order)
		{
		case A_Z:
			categories.sort((a, b) -> a.spec().name.compareTo(b.spec().name));
			break;
		case Z_A:
			categories.sort((a, b) -> -a.spec().name.compareTo(b.spec().name));
			break;
		case ASCENDING:
			categories.sort((a, b) -> deck.getCategory(a.spec().name).size() - deck.getCategory(b.spec().name).size());
			break;
		case DESCENDING:
			categories.sort((a, b) -> deck.getCategory(b.spec().name).size() - deck.getCategory(a.spec().name).size());
			break;
		default:
			break;
		}
		for (CategoryPanel c: categories)
			categoriesContainer.add(c);
	}
	
	/**
	 * Update the categories combo box with all of the current categories.
	 */
	public void updateCategoryPanel()
	{
		sortCategories(sortCategoriesBox.getItemAt(sortCategoriesBox.getSelectedIndex()));
		switchCategoryModel.removeAllElements();
		if (categories.isEmpty())
			switchCategoryBox.setEnabled(false);
		else
		{
			switchCategoryBox.setEnabled(true);
			for (CategoryPanel category: categories.stream().sorted((a, b) -> a.spec().name.compareTo(b.spec().name)).collect(Collectors.toList()))
				switchCategoryModel.addElement(category.spec().name);
		}
	}
	
	/**
	 * Update the card counter to reflect the total number of cards in the deck.
	 */
	public void updateCount()
	{
		countLabel.setText("Total cards: " + deck.total());
		landLabel.setText("Lands: " + deck.land());
		nonlandLabel.setText("Nonlands: " + deck.nonland());
		double avgCMC = deck.stream().filter((c) -> !c.typeContains("land")).mapToDouble(Card::minCmc).average().orElse(0.0);
		if ((int)avgCMC == avgCMC)
			avgCMCLabel.setText("Average CMC: " + (int)avgCMC);
		else
			avgCMCLabel.setText(String.format("Average CMC: %.2f", avgCMC));
	}

	/**
	 * Change the file this EditorFrame is associated with.  If the file has
	 * not been saved, an error will be thrown instead.
	 * 
	 * @param f File to associate with
	 */
	public void setFile(File f)
	{
		if (unsaved)
			throw new RuntimeException("Can't change the file of an unsaved deck");
		file = f;
		setTitle(f.getName());
		unsaved = false;
	}

	/**
	 * Add the given number of copies of the given Cards to the deck.  The current
	 * selections in the category and main tables are maintained.  Don't update the
	 * undo and redo buffers.
	 * 
	 * @param toAdd Cards to add
	 * @param n Number of copies to add
	 * @return <code>true</code> if the deck changed as a result, and
	 * <code>false</code> otherwise, which is only true if the list is empty.
	 */
	private boolean insertCards(List<Card> toAdd, int n)
	{
		if (toAdd.isEmpty())
			return false;
		else
		{
			deck.increaseAll(toAdd, n);

			switch (listTabs.getSelectedIndex())
			{
			case MAIN_TABLE:
				// Maintain the selection in the master list
				int[] selectedRows = table.getSelectedRows();
				model.fireTableDataChanged();
				for (int row: selectedRows)
					table.addRowSelectionInterval(row, row);
				for (CategoryPanel c: categories)
					c.update();
				break;
			case CATEGORIES:
				// Maintain the selection in each category
				for (CategoryPanel c: categories)
				{
					selectedRows = c.table.getSelectedRows();
					c.update();
					for (int row: selectedRows)
						c.table.addRowSelectionInterval(row, row);
				}
				model.fireTableDataChanged();
				break;
			default:
				model.fireTableDataChanged();
				break;
			}
			parent.selectCard(toAdd.get(0));
			if (table.isEditing())
				table.getCellEditor().cancelCellEditing();
			for (CategoryPanel c: categories)
				if (c.table.isEditing())
					c.table.getCellEditor().cancelCellEditing();
			hand.refresh();
			handModel.fireTableDataChanged();
			updateCount();
			setUnsaved();
			update();
			parent.revalidate();
			parent.repaint();
			return true;
		}
	}
	
	/**
	 * Remove a number of copies of the specified Cards from the deck.  The current selections
	 * for any cards remaining in them in the category and main tables are maintained.  Don't
	 * update the undo buffer.
	 * 
	 * @param toRemove List of cards to remove
	 * @param n Number of copies to remove
	 * @return 
	 */
	private Map<Card, Integer> deleteCards(Collection<Card> toRemove, int n)
	{
		Map<Card, Integer> removed = new HashMap<Card, Integer>();
		if (toRemove.isEmpty())
			return removed;
		else
		{
			switch (listTabs.getSelectedIndex())
			{
			case MAIN_TABLE:
				// Get the selected cards first
				List<Card> selectedCards = new ArrayList<Card>();
				for (int row: table.getSelectedRows())
					selectedCards.add(deck.get(table.convertRowIndexToModel(row)));
				// Remove cards from the deck
				for (Card c: toRemove)
				{
					int r = deck.decrease(c, n);
					if (r > 0)
						removed.put(c, r);
				}
				// Update the table and then restore as much of the selection as possible
				model.fireTableDataChanged();
				for (Card c: selectedCards)
				{
					if (deck.contains(c))
					{
						int row = table.convertRowIndexToView(deck.indexOf(c));
						table.addRowSelectionInterval(row, row);
					}
				}
				for (CategoryPanel c: categories)
					c.update();
				break;
			case CATEGORIES:
				// Get all of the selected cards from each category (only one category should
				// have selected cards, but just in case)
				Map<CategoryPanel, List<Card>> selectedCardsMap = new HashMap<CategoryPanel, List<Card>>();
				for (CategoryPanel category: categories)
				{
					List<Card> categorySelectedCards = new ArrayList<Card>();
					if (category.table.getSelectedRows().length > 0)
						for (int row: category.table.getSelectedRows())
							categorySelectedCards.add(deck.getCategory(category.spec().name).get(category.table.convertRowIndexToModel(row)));
					selectedCardsMap.put(category, categorySelectedCards);
				}
				// Remove cards from the deck
				for (Card c: toRemove)
				{
					int r = deck.decrease(c, n);
					if (r > 0)
						removed.put(c, r);
				}
				// Update each category panel and then restore the selection as much as possible
				for (CategoryPanel category: categories)
				{
					category.update();
					for (Card c: selectedCardsMap.get(category))
					{
						if (deck.getCategory(category.spec().name).contains(c))
						{
							int row = category.table.convertRowIndexToView(deck.getCategory(category.spec().name).indexOf(c));
							category.table.addRowSelectionInterval(row, row);
						}
					}
				}
				model.fireTableDataChanged();
				break;
			default:
				// Remove cards from the deck
				for (Card c: toRemove)
				{
					int r = deck.decrease(c, n);
					if (r > 0)
						removed.put(c, r);
				}
				model.fireTableDataChanged();
				break;
			}
			
			if (table.isEditing())
				table.getCellEditor().cancelCellEditing();
			for (CategoryPanel c: categories)
				if (c.table.isEditing())
					c.table.getCellEditor().cancelCellEditing();
			hand.refresh();
			handModel.fireTableDataChanged();
			if (!removed.isEmpty())
			{
				updateCount();
				setUnsaved();
			}
			update();
			parent.revalidate();
			parent.repaint();
			return removed;
		}
	}
	
	/**
	 * Add the given number of copies of the given Cards to the deck.  The current
	 * selections in the category and main tables are maintained.  Then update the
	 * undo and redo buffers.
	 * 
	 * @param toAdd Cards to add
	 * @param n Number of copies to add
	 * @return <code>true</code> if the deck changed as a result, and
	 * <code>false</code> otherwise, which is only true if the list is empty.
	 */
	public boolean addCards(List<Card> toAdd, int n)
	{
		UndoableAction addAction = new UndoableAction()
		{
			@Override
			public boolean undo()
			{
				return !deleteCards(toAdd, n).isEmpty();
			}

			@Override
			public boolean redo()
			{
				return insertCards(toAdd, n);
			}
		};
		if (addAction.redo())
		{
			undoBuffer.push(addAction);
			redoBuffer.clear();
			return true;
		}
		else
			return false;
	}
	
	/**
	 * Add the given number of copies of the given Card to the deck.  The current
	 * selections in the category and main tables are maintained.
	 * 
	 * @param toAdd Card to add
	 * @param n Number of copies to add
	 * @return <code>true</code> if the deck changed as a result, which
	 * is always.
	 */
	public boolean addCard(Card toAdd, int n)
	{
		return addCards(Arrays.asList(toAdd), n);
	}

	/**
	 * Remove a number of copies of the specified Cards from the deck.  The current selections
	 * for any cards remaining in the category and main tables are maintained.  Then update the
	 * undo buffer.
	 * 
	 * @param toRemove List of cards to remove
	 * @param n Number of copies to remove
	 * @return <code>true</code> if the deck was changed as a result, and <code>false</code>
	 * otherwise.
	 */
	public boolean removeCards(Collection<Card> toRemove, int n)
	{
		UndoableAction remove = new UndoableAction()
		{
			private Map<Card, Integer> removed = new HashMap<Card, Integer>();
			
			@Override
			public boolean undo()
			{
				if (removed.isEmpty())
					return false;
				else
				{
					for (Card c: removed.keySet())
						insertCards(Arrays.asList(c), removed.get(c));
					return true;
				}
			}

			@Override
			public boolean redo()
			{
				return !(removed = deleteCards(toRemove, n)).isEmpty();
			}
		};
		if (remove.redo())
		{
			undoBuffer.add(remove);
			redoBuffer.clear();
			return true;
		}
		else
			return false;
	}

	/**
	 * Remove a number of copies of the specified Card from the deck.  The current selections
	 * for any cards remaining in the category and main tables are maintained.  Then update the
	 * undo buffer.
	 * 
	 * @param toRemove Card to remove
	 * @param n Number of copies to remove
	 * @return <code>true</code> if the deck was changed as a result, and <code>false</code>
	 * otherwise.
	 */
	public boolean removeCard(Card toRemove, int n)
	{
		return removeCards(Arrays.asList(toRemove), n);
	}
	
	/**
	 * Set the number of copies of the given card if the deck contains it.  Otherwise
	 * add the card to the deck.
	 * 
	 * TODO: Add and remove may be able to be implemented in terms of this
	 * 
	 * @param c Card to set (or add if it isn't present)
	 * @param n Number of copies to set to (or add if the card isn't present)
	 */
	public void setCardCount(Card c, int n)
	{
		if (deck.contains(c))
		{
			if (n != deck.count(c))
			{
				undoBuffer.push(new UndoableAction()
				{
					private int old = deck.count(c);
					
					@Override
					public boolean undo()
					{
						boolean set = deck.setCount(c, old);
						update();
						return set;
					}

					@Override
					public boolean redo()
					{
						boolean set = deck.setCount(c, n);
						update();
						return set;
					}
				});
				undoBuffer.peek().redo();
				model.fireTableDataChanged();
				for (CategoryPanel category: categories)
					category.update();
				hand.refresh();
				handModel.fireTableDataChanged();
				
				redoBuffer.clear();
				updateCount();
				setUnsaved();
				if (table.isEditing())
					table.getCellEditor().cancelCellEditing();
				update();
				parent.revalidate();
				parent.repaint();
			}
		}
		else
			addCard(c, n);
	}
	
	/**
	 * @param c Card to look for
	 * @return <code>true</code> if the specified Card is in the deck, and <code>false</code>
	 * otherwise.
	 */
	public boolean containsCard(Card c)
	{
		return deck.contains(c);
	}
	
	/**
	 * @return The list of Cards corresponding to the current main table selection
	 * interval.
	 */
	public List<Card> getSelectedCards()
	{
		return Arrays.stream(table.getSelectedRows())
				  .mapToObj((r) -> deck.get(table.convertRowIndexToModel(r)))
				  .collect(Collectors.toList());
	}
	
	/**
	 * Helper function which removes a given number of copies of all cards
	 * selected in the various tables of this EditorFrame.
	 * 
	 * @param n Number of copies of the cards to remove
	 * @return <code>true</code> if the deck changed as a result, and
	 * <code>false</code> otherwise.
	 */
	public boolean removeSelectedCards(int n)
	{
		List<Card> selectedCards;
		switch (listTabs.getSelectedIndex())
		{
		case MAIN_TABLE:
			selectedCards = getSelectedCards();
			break;
		case CATEGORIES:
			selectedCards = new ArrayList<Card>();
			for (CategoryPanel category: categories)
				selectedCards.addAll(category.getSelectedCards());
			break;
		default:
			selectedCards = new ArrayList<Card>();
			break;
		}
		return removeCards(selectedCards, n);
	}

	/**
	 * @param c Card to look for
	 * @return The number of copies of the given Card in the deck.
	 */
	public int count(Card c)
	{
		return deck.count(c);
	}
	
	/**
	 * Save the deck to the given File (like Save As).
	 * 
	 * @param f File to save to
	 * @return <code>true</code> if the file was successfully saved, and <code>false</code>
	 * otherwise.
	 */
	public boolean save(File f)
	{
		try
		{
			deck.save(f);
			String changes = "";
			for (Card c: original)
				if (deck.count(c) < original.count(c))
					changes += ("-" + (original.count(c) - deck.count(c)) + "x " + c.name() + " (" + c.expansion().name + ")\n");
			for (Card c: deck)
				if (original.count(c) < deck.count(c))
					changes += ("+" + (deck.count(c) - original.count(c)) + "x " + c.name() + " (" + c.expansion().name + ")\n");
			if (!changes.isEmpty())
			{
				SimpleDateFormat format = new SimpleDateFormat("MMMM d, yyyy HH:mm:ss");
				changelogArea.append("~~~~~" + format.format(new Date()) + "~~~~~\n");
				changelogArea.append(changes + "\n");
			}
			PrintWriter wr = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f, true), "UTF8"));
			wr.print(changelogArea.getText());
			wr.close();
			
			original = new Deck();
			original.addAll(deck);
			unsaved = false;
			setFile(f);
			return true;
		}
		catch (IOException e)
		{
			JOptionPane.showMessageDialog(null, "Error saving " + f.getName() + ": " + e.getMessage() + ".", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	/**
	 * Save the deck to the current file.
	 * 
	 * @return <code>true</code> if the file was successfully saved, and <code>false</code>
	 * otherwise.
	 */
	public boolean save()
	{
		if (file == null)
			return false;
		else
			return save(file);
	}

	/**
	 * Undo the last action that was performed on the deck.
	 */
	public void undo()
	{
		if (!undoBuffer.isEmpty())
		{
			UndoableAction action = undoBuffer.pop();
			action.undo();
			redoBuffer.push(action);
		}
	}
	
	/**
	 * Redo the last action that was undone, assuming nothing was done
	 * between then and now.
	 */
	public void redo()
	{
		if (!redoBuffer.isEmpty())
		{
			UndoableAction action = redoBuffer.pop();
			action.redo();
			undoBuffer.push(action);
		}
	}
	
	/**
	 * Mark the deck as having been changed since it has last been saved.
	 */
	public void setUnsaved()
	{
		if (!unsaved)
		{
			setTitle(getTitle() + " *");
			unsaved = true;
		}
	}

	/**
	 * @return <code>true</code> if there are unsaved changes in the deck, and <code>false</code>
	 * otherwise.
	 */
	public boolean getUnsaved()
	{
		return unsaved;
	}

	/**
	 * If the deck has unsaved changes, allow the user to choose to save it or keep the
	 * frame open.  If the user saves or declines to save, close the frame.
	 * 
	 * @return <code>true</code> if the frame was closed and <code>false</code>
	 * otherwise.
	 */
	public boolean close()
	{
		if (unsaved)
		{
			String msg = "Deck \"" + getTitle().substring(0, getTitle().length() - 2) + "\" has unsaved changes.  Save?";
			switch(JOptionPane.showConfirmDialog(null, msg, "Unsaved Changes", JOptionPane.YES_NO_CANCEL_OPTION))
			{
			case JOptionPane.YES_OPTION:
				parent.save(EditorFrame.this);
			case JOptionPane.NO_OPTION:
				dispose();
				return true;
			case JOptionPane.CANCEL_OPTION:
			case JOptionPane.CLOSED_OPTION:
				return false;
			default:
				return false;
			}
		}
		else
		{
			dispose();
			return true;
		}
	}
	
	/**
	 * Set the settings of this EditorFrame
	 * 
	 * @see MainFrame#resetDefaultSettings()
	 */
	public void applySettings()
	{
		List<CardCharacteristic> columns = Arrays.stream(SettingsDialog.getSetting(SettingsDialog.EDITOR_COLUMNS).split(",")).map(CardCharacteristic::get).collect(Collectors.toList());
		List<CardCharacteristic> handColumns = Arrays.stream(SettingsDialog.getSetting(SettingsDialog.HAND_COLUMNS).split(",")).map(CardCharacteristic::get).collect(Collectors.toList());
		Color stripe = SettingsDialog.stringToColor(SettingsDialog.getSetting(SettingsDialog.EDITOR_STRIPE));
		model.setColumns(columns);
		table.setStripeColor(stripe);
		for (CategoryPanel category: categories)
		{
			category.setColumns(columns);
			category.setStripeColor(stripe);
		}
		handModel.setColumns(handColumns);
		handTable.setStripeColor(stripe);
		startingHandSize = Integer.valueOf(SettingsDialog.getSetting(SettingsDialog.HAND_SIZE));
		update();
	}
	
	/**
	 * Set the background color for the sample hand panel.
	 * 
	 * @param col New background color for the sample hand panel.
	 */
	public void setHandBackground(Color col)
	{
		imagePanel.setBackground(col);
		for (Component c: imagePanel.getComponents())
			c.setBackground(col);
		imagePane.getViewport().setBackground(col);
	}
	
	/**
	 * Update the GUI to show the latest state of the deck.
	 * XXX: Graphical errors could be attributed to this function
	 */
	public void update()
	{
		revalidate();
		repaint();
		for (CategoryPanel panel: categories)
			panel.update();
	}
	
	/**
	 * This class represents a transfer handler for transferring cards to and from
	 * a table in the editor frame.
	 * 
	 * @author Alec Roelke
	 */
	private class EditorImportHandler extends TransferHandler
	{
		/**
		 * Data can only be imported if it is of the card or entry flavors.
		 * 
		 * @param supp TransferSupport providing information about what is being transferred
		 * @return <code>true</code> if the data is of the correct flavor, and <code>false</code>
		 * otherwise.
		 */
		@Override
		public boolean canImport(TransferSupport supp)
		{
			return supp.isDataFlavorSupported(Deck.entryFlavor) || supp.isDataFlavorSupported(Card.cardFlavor);
		}
		
		/**
		 * If the data can be imported, copy the cards from the source to the target deck.
		 * 
		 * @param supp TransferSupport providing information about what is being transferred
		 * @return <code>true</code> if the import was successful, and <code>false</code>
		 * otherwise.
		 */
		@Override
		public boolean importData(TransferSupport supp)
		{
			try
			{
				if (!canImport(supp))
					return false;
				else if (supp.isDataFlavorSupported(Deck.entryFlavor))
				{
					Deck.Entry[] data = (Deck.Entry[])supp.getTransferable().getTransferData(Deck.entryFlavor);
					UndoableAction addAction = new UndoableAction()
					{
						@Override
						public boolean undo()
						{
							boolean undone = false;
							for (Deck.Entry e: data)
								undone |= !deleteCards(Arrays.asList(e.card()), e.count()).isEmpty();
							return undone;
						}

						@Override
						public boolean redo()
						{
							boolean done = false;
							for (Deck.Entry e: data)
								done |= insertCards(Arrays.asList(e.card()), e.count());
							return done;
						}
					};
					if (addAction.redo())
					{
						undoBuffer.push(addAction);
						redoBuffer.clear();
						return true;
					}
					else
						return false;
				}
				else if (supp.isDataFlavorSupported(Card.cardFlavor))
				{
					Card[] data = (Card[])supp.getTransferable().getTransferData(Card.cardFlavor);
					addCards(Arrays.asList(data), 1);
					return true;
				}
				else
					return false;
			}
			catch (UnsupportedFlavorException e)
			{
				return false;
			}
			catch (IOException e)
			{
				return false;
			}
		}
	}
	
	/**
	 * This class represents a transfer handler for moving data to and from
	 * a table in the editor.  It can import or export data of the card or
	 * entry flavors.
	 * 
	 * @author Alec Roelke
	 */
	private class EditorTableTransferHandler extends EditorImportHandler
	{
		/**
		 * Tables support copying or moving cards from one place to another.
		 * TODO: Make move work when the source and target decks are the same
		 */
		@Override
		public int getSourceActions(JComponent c)
		{
			return TransferHandler.COPY;
		}
		
		/**
		 * Create the transferable to handle data transfer from a table
		 * to its destination.
		 * 
		 * @param c The component containing the data to be transferred
		 * @return The transferable containing the data to transfer.
		 */
		@Override
		public Transferable createTransferable(JComponent c)
		{
			List<Card> selectedCards;
			switch (listTabs.getSelectedIndex())
			{
			case MAIN_TABLE:
				selectedCards = getSelectedCards();
				break;
			case CATEGORIES:
				selectedCards = new ArrayList<Card>();
				for (CategoryPanel category: categories)
					selectedCards.addAll(category.getSelectedCards());
				break;
			default:
				selectedCards = new ArrayList<Card>();
				break;
			}
			return new Deck.TransferData(deck, selectedCards);
		}
		
		/**
		 * Perform actions that need to be completed when the export
		 * is complete.
		 * 
		 * @param c Component originating data to transfer
		 * @param t Transferable containing data transferred
		 * @param action Action performed on the data
		 */
		@Override
		public void exportDone(JComponent c, Transferable t, int action)
		{
			if (action == TransferHandler.MOVE)
				removeSelectedCards(Integer.MAX_VALUE);
		}
	}
	
	/**
	 * This class is a worker for loading a deck.
	 * 
	 * @author Alec Roelke
	 */
	private class LoadWorker extends SwingWorker<Void, Integer>
	{
		/**
		 * File to load the deck from.
		 */
		private File file;
		/**
		 * Progress bar to display progress to.
		 */
		private JProgressBar progressBar;
		/**
		 * Dialog containing the progress bar.
		 */
		private JDialog dialog;
		
		/**
		 * Create a new LoadWorker.
		 * 
		 * @param f File to load the deck from
		 * @param b Progress bar showing progress
		 * @param d Dialog containing the progress bar
		 */
		public LoadWorker(File f, JProgressBar b, JDialog d)
		{
			file = f;
			progressBar = b;
			dialog = d;
		}
		
		/**
		 * Update the progress bar with the latest progress.
		 * 
		 * @param chunks Progress that has been made
		 */
		@Override
		protected void process(List<Integer> chunks)
		{
			int progress = chunks.get(chunks.size() - 1);
			progressBar.setValue(progress);
		}
		
		/**
		 * Load the deck, updating the progress bar all the while.
		 */
		@Override
		protected Void doInBackground() throws Exception
		{
			try (BufferedReader rd = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8")))
			{
				int cards = Integer.valueOf(rd.readLine().trim());
				for (int i = 0; i < cards; i++)
				{
					if (isCancelled())
						return null;
					String[] card = rd.readLine().trim().split("\t");
					Card c = parent.getCard(card[0]);
					if (c != null)
						deck.increase(c, Integer.valueOf(card[1]), Deck.DATE_FORMAT.parse(card[2]));
					else
						throw new IllegalStateException("Card with UID \"" + card[0] + "\" not found");
					publish(50*(i + 1)/cards);
				}
				int categories = Integer.valueOf(rd.readLine().trim());
				for (int i = 0; i < categories; i++)
				{
					if (isCancelled())
						return null;
					CategorySpec spec = new CategorySpec(rd.readLine(), parent.inventory());
					addCategory(spec);
					for (Card c: spec.whitelist)
						deck.getCategory(spec.name).include(c);
					for (Card c: spec.blacklist)
						deck.getCategory(spec.name).exclude(c);
					publish(50 + 50*(i + 1)/categories);
				}
				String line;
				while ((line = rd.readLine()) != null)
					changelogArea.append(line + "\n");
			}
			return null;
		}
		
		/**
		 * When the task is over, close the file and update the frame.
		 */
		@Override
		protected void done()
		{
			dialog.dispose();
			updateCount();
			unsaved = false;
			setFile(file);
			undoBuffer.clear();
			redoBuffer.clear();
		}
	}
}
