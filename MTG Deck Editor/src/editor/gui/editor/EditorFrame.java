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
import javax.swing.table.AbstractTableModel;

import editor.collection.CardCollection;
import editor.collection.LegalityChecker;
import editor.collection.category.CategorySpec;
import editor.collection.deck.Deck;
import editor.collection.deck.Hand;
import editor.database.Card;
import editor.database.characteristics.CardCharacteristic;
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
	 * Panels showing categories in this deck (individual panels should not be operated
	 * on except for GUI-related functions).
	 */
	private Collection<CategoryPanel> categoryPanels;
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
	 * Table containing currently-selected cards.
	 */
	private CardTable selectedTable;
	/**
	 * CardCollection containing currently-selected cards.
	 */
	private CardCollection selectedSource;

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
		startingHandSize = SettingsDialog.getAsInt(SettingsDialog.HAND_SIZE);

		// Panel for showing buttons to add and remove cards
		// The buttons are concentrated in the middle of the panel
		JPanel buttonPanel = new JPanel();
		getContentPane().add(buttonPanel, BorderLayout.WEST);
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		
		buttonPanel.add(Box.createVerticalGlue());
		
		// Add button to add one copy of the currently-selected card to the deck
		JButton addButton = new JButton("+");
		addButton.addActionListener((e) -> addSelectedCards(1));
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

		model = new CardTableModel(this, deck, SettingsDialog.getAsCharacteristics(SettingsDialog.EDITOR_COLUMNS));

		// Create the table so that it resizes if the window is too big but not if it's too small
		table = new CardTable(model);
		table.setStripeColor(SettingsDialog.getAsColor(SettingsDialog.EDITOR_STRIPE));
		// When a card is selected in the master list table, select it for adding
		table.getSelectionModel().addListSelectionListener((e) -> { 
			if (!e.getValueIsAdjusting())
			{
				ListSelectionModel lsm = (ListSelectionModel)e.getSource();
				if (!lsm.isSelectionEmpty())
				{
					clearTableSelections(table);
					parent.clearSelectedCards();
					setSelectedSource(table, deck);
					if (hasSelectedCards())
						parent.selectCard(getSelectedCards().get(0));
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
		addSinglePopupItem.addActionListener((e) -> addSelectedCards(1));
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
				addSelectedCards((Integer)spinner.getValue());
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
				if (cards.size() != 1 && !deck.categories().isEmpty())
					setCategoriesMenu.setEnabled(false);
				else if (!deck.categories().isEmpty())
				{
					setCategoriesMenu.setEnabled(true);
					setCategoriesMenu.removeAll();
					for (CategorySpec category: deck.categories().stream().sorted((a, b) -> a.getName().compareTo(b.getName())).collect(Collectors.toList()))
					{
						JCheckBoxMenuItem containsBox = new JCheckBoxMenuItem(category.getName());
						containsBox.setSelected(deck.contains(category.getName(), cards.get(0)));
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
				updateCategoryPanel();
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
		categoryPanels = new ArrayList<CategoryPanel>();
		
		// The category panel is a vertically-scrollable panel that contains all categories stacked vertically
		// The categories should have a constant height, but fit the container horizontally
		categoriesSuperContainer.add(categoriesContainer, BorderLayout.NORTH);
		categoriesPanel.add(new JScrollPane(categoriesSuperContainer, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
		listTabs.addTab("Categories", categoriesPanel);
		
		// Sample hands
		JPanel handPanel = new JPanel(new BorderLayout());
		
		// Table showing the cards in hand
		hand = new Hand(deck);
		handModel = new CardTableModel(this, hand, SettingsDialog.getAsCharacteristics(SettingsDialog.HAND_COLUMNS));
		handTable = new CardTable(handModel);
		handTable.setCellSelectionEnabled(false);
		handTable.setStripeColor(SettingsDialog.getAsColor(SettingsDialog.EDITOR_STRIPE));
		handTable.setPreferredScrollableViewportSize(new Dimension(handTable.getPreferredSize().width, handTable.getRowHeight()*10));
		
		imagePanel = new ScrollablePanel(ScrollablePanel.TRACK_HEIGHT);
		imagePanel.setLayout(new BoxLayout(imagePanel, BoxLayout.X_AXIS));
		imagePane = new JScrollPane(imagePanel);
		setHandBackground(SettingsDialog.getAsColor(SettingsDialog.HAND_BGCOLOR));
		
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
				panel.setBackground(SettingsDialog.getAsColor(SettingsDialog.HAND_BGCOLOR));
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
				panel.setBackground(SettingsDialog.getAsColor(SettingsDialog.HAND_BGCOLOR));
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
			panel.setBackground(SettingsDialog.getAsColor(SettingsDialog.HAND_BGCOLOR));
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
			excludeTable.setStripeColor(SettingsDialog.getAsColor(SettingsDialog.EDITOR_STRIPE));
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
			JOptionPane pane = new JOptionPane(new CalculateHandPanel(deck, SettingsDialog.getAsColor(SettingsDialog.EDITOR_STRIPE)));
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
		
		deck.addDeckListener((e) -> {
			if (e.cardsChanged())
				updateCount();
			if (e.categoriesRemoved())
				updateCategoryPanel();
			setUnsaved();
			update();
		});
		
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
			categoriesContainer.removeAll();
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
		List<String> names = deck.categories().stream().map(CategorySpec::getName).collect(Collectors.toList());
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
		for (CategoryPanel category: categoryPanels)
			if (category.getCategoryName().equals(name))
				return category;
		return null;
	}
	
	/**
	 * Open the dialog to create a new specification for a deck category.
	 * 
	 * @return The CategorySpec created by the dialog, or null if it was
	 * canceled.
	 */
	public CategorySpec createCategory()
	{
		CategorySpec spec = null;
		do
		{
			spec = CategoryEditorPanel.showCategoryEditor(spec != null ? spec : null);
			if (spec != null && deck.containsCategory(spec.getName()))
				JOptionPane.showMessageDialog(null, "Categories must have unique names.", "Error", JOptionPane.ERROR_MESSAGE);
		} while (spec != null && deck.containsCategory(spec.getName()));
		return spec;
	}
	
	/**
	 * Create a new CategoryPanel out of the given specification.
	 * 
	 * @param spec Specification for the category of the new CategoryPanel
	 * @return The new CategoryPanel.
	 */
	private CategoryPanel createCategoryPanel(CategorySpec spec)
	{
		CategoryPanel newCategory = new CategoryPanel(deck, spec.getName(), this);
		// When a card is selected in a category, the others should deselect
		newCategory.table.getSelectionModel().addListSelectionListener((e) -> {
			ListSelectionModel lsm = (ListSelectionModel)e.getSource();
			if (!lsm.isSelectionEmpty())
			{
				if (!e.getValueIsAdjusting())
				{
					clearTableSelections(newCategory.table);
					parent.clearSelectedCards();
					setSelectedSource(newCategory.table, deck.getCategoryCards(spec.getName()));
					if (hasSelectedCards())
						parent.selectCard(getSelectedCards().get(0));
				}
			}
		});
		// Add the behavior for the edit category button
		newCategory.editButton.addActionListener((e) -> editCategory(spec.getName()));
		// Add the behavior for the remove category button
		newCategory.removeButton.addActionListener((e) -> removeCategory(spec));
		// Add the behavior for the color edit button
		newCategory.colorButton.addActionListener((e) -> {
			Color newColor = JColorChooser.showDialog(null, "Choose a Color", newCategory.colorButton.color());
			if (newColor != null)
			{
				spec.setColor(newColor);
				newCategory.colorButton.setColor(newColor);
				newCategory.colorButton.repaint();
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
		addSinglePopupItem.addActionListener((e) -> addSelectedCards(1));
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
				addSelectedCards((Integer)spinner.getValue());
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
						spec.include(c);
					return true;
				}

				@Override
				public boolean redo()
				{
					for (Card c: selectedCards)
						spec.exclude(c);
					return true;
				}
			});
			undoBuffer.peek().redo();
			redoBuffer.clear();
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
		editItem.addActionListener((e) -> editCategory(spec.getName()));
		categoryMenu.add(editItem);
		
		// Delete item
		JMenuItem deleteItem = new JMenuItem("Delete");
		deleteItem.addActionListener((e) -> removeCategory(spec));
		categoryMenu.add(deleteItem);
		
		// Add to presets item
		JMenuItem addPresetItem = new JMenuItem("Add to presets");
		addPresetItem.addActionListener((e) -> {
			if (!spec.getWhitelist().isEmpty() || !spec.getBlacklist().isEmpty())
			{
				if (JOptionPane.showConfirmDialog(null,
						"Category "
						+ spec.getName()
						+ " contains cards in its whitelist or blacklist which will not be included in the preset category."
						+ "  Make this category a preset category?",
						"Add to Presets",
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
					parent.addPreset(spec.toListlessString());
			}
			else
				parent.addPreset(spec.toListlessString());
		});
		categoryMenu.add(addPresetItem);
		
		newCategory.table.addMouseListener(new TableMouseAdapter(newCategory.table, tableMenu));
		
		return newCategory;
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
		if (!deck.containsCategory(spec.getName()))
		{
			deck.addCategory(spec);
			CategoryPanel category = createCategoryPanel(spec);
			categoryPanels.add(category);
			updateCategoryPanel();
			listTabs.setSelectedIndex(CATEGORIES);
			//TODO: Make this work
			category.scrollRectToVisible(new Rectangle(category.getSize()));
			category.flash();
			return true;
		}
		else
		{
			listTabs.setSelectedIndex(CATEGORIES);
			CategoryPanel panel = getCategory(spec.getName());
			panel.scrollRectToVisible(new Rectangle(panel.getSize()));
			panel.flash();
			return false;
		}
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
		CategoryPanel panel = getCategory(category.getName());
		if (panel == null)
			return false;
		else
		{
			boolean removed = categoryPanels.remove(panel);
			removed |= deck.removeCategory(category.getName());
			listTabs.setSelectedIndex(CATEGORIES);
			
			return removed;
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
		if (spec != null && !deck.containsCategory(spec.getName()))
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
		CategorySpec toEdit = deck.getCategorySpec(name);
		if (toEdit == null)
			JOptionPane.showMessageDialog(null, "Deck " + deckName() + " has no category named " + name + ".", "Error", JOptionPane.ERROR_MESSAGE);
		else
		{
			CategorySpec spec = CategoryEditorPanel.showCategoryEditor(toEdit);
			if (spec != null)
				editCategory(toEdit, spec);
		}
	}
	
	/**
	 * Change the given category so it has the parameters of the other given category.
	 * The category to edit is edited in place, while the one representing new values
	 * is unchanged.
	 * 
	 * @param toEdit Category to edit
	 * @param newValues New values for the category
	 */
	private void editCategory(CategorySpec toEdit, CategorySpec newValues)
	{
		undoBuffer.push(new UndoableAction()
		{
			CategorySpec oldSpec = new CategorySpec(toEdit);
			
			@Override
			public boolean undo()
			{
				return toEdit.copy(oldSpec);
			}

			@Override
			public boolean redo()
			{
				return toEdit.copy(newValues);
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
		if (!deck.containsCategory(category.getName()))
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
			CategorySpec spec = deck.getCategorySpec(name);
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
	 * Update the categories combo box with all of the current categories.
	 */
	public void updateCategoryPanel()
	{
		List<CategorySpec> categories = new ArrayList<CategorySpec>(deck.categories());
		if (categories.isEmpty())
			switchCategoryBox.setEnabled(false);
		else
		{
			switchCategoryBox.setEnabled(true);
			
			categoriesContainer.removeAll();
			switchCategoryModel.removeAllElements();
			
			switch (sortCategoriesBox.getItemAt(sortCategoriesBox.getSelectedIndex()))
			{
			case A_Z:
				categories.sort((a, b) -> a.getName().compareTo(b.getName()));
				break;
			case Z_A:
				categories.sort((a, b) -> -a.getName().compareTo(b.getName()));
				break;
			case ASCENDING:
				categories.sort((a, b) -> deck.total(a.getName()) - deck.total(b.getName()));
				break;
			case DESCENDING:
				categories.sort((a, b) -> deck.total(b.getName()) - deck.total(a.getName()));
				break;
			default:
				break;
			}
			
			for (CategorySpec c: categories)
				categoriesContainer.add(getCategory(c.getName()));
			for (CategorySpec category: categories)
				switchCategoryModel.addElement(category.getName());
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
	 * Add cards to the deck, maintaining the selection in the currently-selected
	 * table.
	 * 
	 * @param cards Map containing cards and amounts to add
	 * @return <code>true</code> if the deck changed as a result, and
	 * <code>false</code> otherwise, which is only true if the list is empty.
	 */
	private boolean insertCards(Map<Card, Integer> cards)
	{
		cards.values().removeAll(Arrays.asList(0));
		if (cards.isEmpty())
			return false;
		else
		{
			int[] selectedRows = selectedTable.getSelectedRows();
			
			for (Map.Entry<Card, Integer> entry: cards.entrySet())
				deck.increase(entry.getKey(), entry.getValue());

			((AbstractTableModel)selectedTable.getModel()).fireTableDataChanged();
			for (int row: selectedRows)
				selectedTable.addRowSelectionInterval(row, row);
			update();
			
			if (table.isEditing())
				table.getCellEditor().cancelCellEditing();
			for (CategoryPanel c: categoryPanels)
				if (c.table.isEditing())
					c.table.getCellEditor().cancelCellEditing();
			hand.refresh();
			handModel.fireTableDataChanged();
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
	 * @return A Map containing the Cards removed and the number of each that was removed.
	 */
	private Map<Card, Integer> deleteCards(Collection<Card> toRemove, int n)
	{
		Map<Card, Integer> removed = new HashMap<Card, Integer>();
		if (toRemove.isEmpty())
			return removed;
		else
		{
			List<Card> selectedCards = Arrays.stream(selectedTable.getSelectedRows())
					.mapToObj((r) -> selectedSource.get(selectedTable.convertRowIndexToModel(r)))
					.collect(Collectors.toList());
			for (Card c: toRemove)
				deck.decrease(c, n);
			((AbstractTableModel)selectedTable.getModel()).fireTableDataChanged();
			for (Card c: selectedCards)
			{
				if (getSelectedSource().contains(c))
				{
					int row = selectedTable.convertRowIndexToModel(getSelectedSource().indexOf(c));
					selectedTable.addRowSelectionInterval(row, row);
				}
			}
			update();
			
			if (table.isEditing())
				table.getCellEditor().cancelCellEditing();
			for (CategoryPanel c: categoryPanels)
				if (c.table.isEditing())
					c.table.getCellEditor().cancelCellEditing();
			hand.refresh();
			handModel.fireTableDataChanged();
			parent.revalidate();
			parent.repaint();
			return removed;
		}
	}
	
	/**
	 * Add the currently-selected cards from the currently-selected table to the
	 * Deck.
	 * 
	 * @param n Number of each card to add
	 * @return <code>true</code> if the Deck was changed as a result, and
	 * <code>false</code> otherwise.
	 */
	public boolean addSelectedCards(int n)
	{
		if (hasSelectedCards())
			return addCards(getSelectedCards(), n);
		else if (parent.getSelectedCard() != null)
			return addCard(parent.getSelectedCard(), n);
		else
			return false;
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
			private Map<Card, Integer> cards = toAdd.stream().collect(Collectors.toMap((c) -> c, (c) -> n));
			
			@Override
			public boolean undo()
			{
				return !deleteCards(toAdd, n).isEmpty();
			}

			@Override
			public boolean redo()
			{
				return insertCards(cards);
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
	 * Remove the currently-selected cards in the currently-selected table from
	 * the Deck.
	 * 
	 * @param n Number of copies of the cards to remove
	 * @return <code>true</code> if the deck changed as a result, and
	 * <code>false</code> otherwise.
	 */
	public boolean removeSelectedCards(int n)
	{
		if (hasSelectedCards())
			return removeCards(getSelectedCards(), n);
		else if (parent.getSelectedCard() != null)
			return removeCard(parent.getSelectedCard(), n);
		else
			return false;
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
				return insertCards(removed);
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
						return deck.setCount(c, old);
					}

					@Override
					public boolean redo()
					{
						return deck.setCount(c, n);
					}
				});
				undoBuffer.peek().redo();
				model.fireTableDataChanged();
				for (CategoryPanel category: categoryPanels)
					category.update();
				hand.refresh();
				handModel.fireTableDataChanged();
				
				redoBuffer.clear();
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
		return Arrays.stream(selectedTable.getSelectedRows())
				  .mapToObj((r) -> selectedSource.get(selectedTable.convertRowIndexToModel(r)))
				  .collect(Collectors.toList());
	}
	
	/**
	 * @return <code>true</code> if there is a selected table and it has a selection,
	 * and <code>false</code> otherwise.
	 */
	public boolean hasSelectedCards()
	{
		return selectedTable != null && selectedTable.getSelectedRowCount() > 0;
	}
	
	/**
	 * Clear the selection in all of the tables in this EditorFrame except
	 * for the given one.
	 * 
	 * @param except CardTable to not clear
	 */
	public void clearTableSelections(CardTable except)
	{
		if (table != except)
			table.clearSelection();
		for (CategoryPanel c: categoryPanels)
			if (c.table != except)
				c.table.clearSelection();
	}
	
	/**
	 * Set the table and CardCollection that contains the current selection.
	 * 
	 * @param table Table to select
	 * @param source CardCollection to select
	 */
	public void setSelectedSource(CardTable table, CardCollection source)
	{
		selectedTable = table;
		selectedSource = source;
	}
	
	/**
	 * @return The CardCollection containing the selected Cards.
	 */
	public CardCollection getSelectedSource()
	{
		return selectedSource;
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
		List<CardCharacteristic> columns = SettingsDialog.getAsCharacteristics(SettingsDialog.EDITOR_COLUMNS);
		List<CardCharacteristic> handColumns = SettingsDialog.getAsCharacteristics(SettingsDialog.HAND_COLUMNS);
		Color stripe = SettingsDialog.getAsColor(SettingsDialog.EDITOR_STRIPE);
		model.setColumns(columns);
		table.setStripeColor(stripe);
		for (CategoryPanel category: categoryPanels)
		{
			category.setColumns(columns);
			category.setStripeColor(stripe);
		}
		handModel.setColumns(handColumns);
		handTable.setStripeColor(stripe);
		startingHandSize = SettingsDialog.getAsInt(SettingsDialog.HAND_SIZE);
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
		for (CategoryPanel panel: categoryPanels)
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
		 * If the data c)an be imported, copy the cards from the source to the target deck.
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
					@SuppressWarnings("unchecked")
					Map<Card, Integer> data = (Map<Card, Integer>)supp.getTransferable().getTransferData(Deck.entryFlavor);
					UndoableAction addAction = new UndoableAction()
					{
						@Override
						public boolean undo()
						{
							boolean undone = false;
							for (Map.Entry<Card, Integer> entry: data.entrySet())
								undone |= !deleteCards(Arrays.asList(entry.getKey()), entry.getValue()).isEmpty();
							return undone;
						}

						@Override
						public boolean redo()
						{
							return insertCards(data);
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
			return new Deck.TransferData(deck, getSelectedCards());
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
			unsaved = false;
			setFile(file);
			undoBuffer.clear();
			redoBuffer.clear();
		}
	}
}
