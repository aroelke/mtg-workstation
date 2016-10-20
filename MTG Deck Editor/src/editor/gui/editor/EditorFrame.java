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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
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
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.table.AbstractTableModel;

import editor.collection.CardList;
import editor.collection.LegalityChecker;
import editor.collection.category.CategorySpec;
import editor.collection.deck.Deck;
import editor.collection.deck.Hand;
import editor.database.card.Card;
import editor.database.characteristics.CardData;
import editor.gui.MainFrame;
import editor.gui.SettingsDialog;
import editor.gui.display.CardImagePanel;
import editor.gui.display.CardTable;
import editor.gui.display.CardTableModel;
import editor.gui.generic.ScrollablePanel;
import editor.gui.generic.TableMouseAdapter;
import editor.util.PopupMenuListenerFactory;

/**
 * This class represents an internal frame for editing a deck.  It contains a table that shows all cards
 * and their counts in the deck as well as zero or more tables for categories within it.  It can add cards
 * to a deck and add, edit, and delete categories.  It is contained within the main frame, which has the
 * inventory from which cards can be added.
 * 
 * TODO: Add a filter bar to the main tab just like the inventory has
 * TODO: Add a second table to the main panel showing commander/sideboard/extra cards
 * TODO: Add something for calculating probability for multiple categories at once
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
	private enum CategoryOrder
	{
		A_Z("A-Z", (d) -> (a, b) -> a.getName().compareToIgnoreCase(b.getName())),
		Z_A("Z-A", (d) -> (a, b) -> -a.getName().compareToIgnoreCase(b.getName())),
		ASCENDING("Ascending Size", (d) -> (a, b) -> d.total(a.getName()) - d.total(b.getName())),
		DESCENDING("Descending Size", (d) -> (a, b) -> d.total(b.getName()) - d.total(a.getName())),
		PRIORITY("Increasing Rank", (d) -> (a, b) -> d.getCategoryRank(a.getName()) - d.getCategoryRank(b.getName())),
		REVERSE("Decreasing Rank", (d) -> (a, b) -> d.getCategoryRank(b.getName()) - d.getCategoryRank(a.getName()));
		
		/**
		 * String to display when a String representation of this
		 * CategoryOrder is called for.
		 */
		private final String name;
		/**
		 * Function comparing two CategorySpecs from a deck
		 */
		private final Function<Deck, Comparator<CategorySpec>> order;
		
		/**
		 * Create a new CategoryOrder.
		 * 
		 * @param n Name of the new CategoryOrder
		 * @param o Function comparing two CategorySpecs from a Deck
		 */
		private CategoryOrder(String n, Function<Deck, Comparator<CategorySpec>> o)
		{
			name = n;
			order = o;
		}
		
		/**
		 * Compare the two given CategorySpecs from the given Deck.
		 * 
		 * @param d Deck containing the categories
		 * @param a First category
		 * @param b Second category
		 * @return A negative number if the first category comes before the second in the given Deck,
		 * a positive number if it comes after, and 0 if there is no relative order.
		 */
		public int compare(Deck d, CategorySpec a, CategorySpec b)
		{
			return order.apply(d).compare(a, b);
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
	 * TODO: Comment this
	 */
	private Deck sideboard;
	/**
	 * Last-saved version of the deck, used for the changelog.
	 */
	private Deck originalDeck;
	/**
	 * TODO: Comment this
	 */
	private Deck originalSideboard;
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
	private CardList selectedSource;
	/**
	 * Saved list of selected cards from the active table.
	 */
	private List<Card> selectedCards;
	/**
	 * Whether or not a file is being opened (used to prevent some actions when changing the deck).
	 */
	private boolean opening;
	/**
	 * Label showing the median CMC of nonland cards in the deck.
	 */
	private JLabel medCMCLabel;
	/**
	 * Panel showing a sample hand and a table showing probabilities of category requirements.
	 */
	private CalculateHandPanel handCalculations;

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
		sideboard = new Deck();
		originalDeck = new Deck();
		originalSideboard = new Deck();
		file = null;
		unsaved = false;
		undoBuffer = new Stack<UndoableAction>();
		redoBuffer = new Stack<UndoableAction>();
		startingHandSize = SettingsDialog.getAsInt(SettingsDialog.HAND_SIZE);
		selectedCards = new ArrayList<Card>();
		selectedSource = null;
		selectedTable = null;
		opening = false;

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
		
		listTabs = new JTabbedPane(SwingConstants.TOP);
		getContentPane().add(listTabs, BorderLayout.CENTER);

		JPanel mainPanel = new JPanel();
		GridBagLayout deckLayout = new GridBagLayout();
		deckLayout.columnWidths = new int[] {0};
		deckLayout.columnWeights = new double[] {1.0};
		deckLayout.rowHeights = new int[] {0, 0, 0};
		deckLayout.rowWeights = new double[] {1.0, 0.0, 0.0};
		mainPanel.setLayout(deckLayout);
		mainPanel.setBackground(UIManager.getColor("window"));
		
		model = new CardTableModel(this, deck, SettingsDialog.getAsCharacteristics(SettingsDialog.EDITOR_COLUMNS));

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
						parent.selectCard(getSelectedCards()[0]);
				}
			}
		});
		for (int i = 0; i < table.getColumnCount(); i++)
			if (model.isCellEditable(0, i))
				table.getColumn(model.getColumnName(i)).setCellEditor(model.getColumnCharacteristic(i).createCellEditor(this));
		table.setTransferHandler(new EditorTableTransferHandler());
		table.setDragEnabled(true);
		table.setDropMode(DropMode.ON);
		
		JScrollPane mainDeckPane = new JScrollPane(table);
		mainDeckPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		GridBagConstraints mainConstraints = new GridBagConstraints();
		mainConstraints.gridx = 0;
		mainConstraints.gridy = 0;
		mainConstraints.fill = GridBagConstraints.BOTH;
		mainPanel.add(mainDeckPane, mainConstraints);
		
		JPanel showHidePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		showHidePanel.setBackground(UIManager.getColor("window"));
		BasicArrowButton showHideButton = new BasicArrowButton(BasicArrowButton.SOUTH);
		showHidePanel.add(showHideButton);
		GridBagConstraints showHideConstraints = new GridBagConstraints();
		showHideConstraints.gridx = 0;
		showHideConstraints.gridy = 1;
		showHideConstraints.fill = GridBagConstraints.BOTH;
		mainPanel.add(showHidePanel, showHideConstraints);
		
		JPanel sideboardPanel = new JPanel(new BorderLayout());
		sideboardPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		GridBagConstraints sideboardConstraints = new GridBagConstraints();
		sideboardConstraints.gridx = 0;
		sideboardConstraints.gridy = 2;
		sideboardConstraints.fill = GridBagConstraints.BOTH;
		mainPanel.add(sideboardPanel, sideboardConstraints);
		
		showHideButton.addActionListener((e) -> {
			sideboardPanel.setVisible(!sideboardPanel.isVisible());
			showHideButton.setDirection(sideboardPanel.isVisible() ? BasicArrowButton.SOUTH : BasicArrowButton.NORTH);
		});
		
		listTabs.addTab("Cards", mainPanel);
		
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
				addCard(c, 4 - deck.getData(c).count());
		});
		tableMenu.add(playsetPopupItem);
		
		// Add variable item
		JMenuItem addNPopupItem = new JMenuItem("Add Copies...");
		addNPopupItem.addActionListener((e) -> {
			JPanel contentPanel = new JPanel(new BorderLayout());
			contentPanel.add(new JLabel("Copies to add:"), BorderLayout.WEST);
			JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 1));
			contentPanel.add(spinner, BorderLayout.SOUTH);
			if (JOptionPane.showConfirmDialog(this, contentPanel, "Add Cards", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
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
			if (JOptionPane.showConfirmDialog(this, contentPanel, "Add Cards", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
				removeSelectedCards((Integer)spinner.getValue());
		});
		tableMenu.add(removeNPopupItem);
		
		JSeparator categoriesSeparator = new JSeparator();
		tableMenu.add(categoriesSeparator);
		
		// Quick edit categories
		JMenu addToCategoryMenu = new JMenu("Include in");
		tableMenu.add(addToCategoryMenu);
		JMenu removeFromCategoryMenu = new JMenu("Exclude from");
		tableMenu.add(removeFromCategoryMenu);
		
		// Edit categories item
		JMenuItem editCategoriesItem = new JMenuItem("Edit Categories...");
		editCategoriesItem.addActionListener((e) -> {
			IncludeExcludePanel iePanel = new IncludeExcludePanel(deck.categories().stream().sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName())).collect(Collectors.toList()), getSelectedCards());
			if (JOptionPane.showConfirmDialog(this, new JScrollPane(iePanel), "Set Categories", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
				editInclusion(iePanel.getIncluded(), iePanel.getExcluded());
		});
		tableMenu.add(editCategoriesItem);
		
		tableMenu.add(new JSeparator());
		
		// Edit card tags item
		JMenuItem editTagsItem = new JMenuItem("Edit Tags...");
		editTagsItem.addActionListener((e) -> parent.editTags(getSelectedCards()));
		tableMenu.add(editTagsItem);
		
		tableMenu.addPopupMenuListener(new TablePopupListener(addToCategoryMenu, removeFromCategoryMenu,
				editCategoriesItem, categoriesSeparator, table));
		
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
		
		imagePanel = new ScrollablePanel(ScrollablePanel.TRACK_HEIGHT);
		imagePanel.setLayout(new BoxLayout(imagePanel, BoxLayout.X_AXIS));
		imagePane = new JScrollPane(imagePanel);
		imagePane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		setHandBackground(SettingsDialog.getAsColor(SettingsDialog.HAND_BGCOLOR));
		
		// Control panel for manipulating the sample hand
		JPanel handModPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
		JButton newHandButton = new JButton("New Hand");
		newHandButton.addActionListener((e) -> {
			hand.newHand(startingHandSize);
			
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
			if (hand.size() < deck.total())
			{
				hand.draw();	
				CardImagePanel panel = new CardImagePanel();
				panel.setBackground(SettingsDialog.getAsColor(SettingsDialog.HAND_BGCOLOR));
				imagePanel.add(panel);
				panel.setCard(hand[hand.size() - 1]);
				imagePanel.add(Box.createHorizontalStrut(10));
				imagePanel.validate();
				update();
			}
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
			
			CardTableModel excludeTableModel = new CardTableModel(deck, Arrays.asList(CardData.NAME, CardData.COUNT));
			CardTable excludeTable = new CardTable(excludeTableModel);
			excludeTable.setStripeColor(SettingsDialog.getAsColor(SettingsDialog.EDITOR_STRIPE));
			excludePanel.add(new JScrollPane(excludeTable));
			
			addExclusionButton.addActionListener((a) -> {
				for (Card c: Arrays.stream(excludeTable.getSelectedRows()).mapToObj((r) -> deck[excludeTable.convertRowIndexToModel(r)]).collect(Collectors.toList()))
				{
					int n = 0;
					for (int i = 0; i < excludeModel.size(); i++)
						if (excludeModel.elementAt(i).equals(c))
							n++;
					if (n < deck.getData(c).count())
						excludeModel.addElement(c);
				}
			});
			removeExclusionButton.addActionListener((f) -> {
				for (Card c: Arrays.stream(exclude.getSelectedIndices()).mapToObj((r) -> excludeModel.getElementAt(r)).collect(Collectors.toList()))
					excludeModel.removeElement(c);
			});
			
			for (Card c: hand.excluded())
				excludeModel.addElement(c);
			JOptionPane.showMessageDialog(this, excludePanel, "Exclude Cards", JOptionPane.PLAIN_MESSAGE);
			
			hand.clearExclusion();
			for (int i = 0; i < excludeModel.size(); i++)
				hand.exclude(excludeModel[i]);
		});
		handModPanel.add(excludeButton);
		
		handCalculations = new CalculateHandPanel(deck);
		
		JSplitPane handSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, imagePane, handCalculations);
		handSplit.setOneTouchExpandable(true);
		handSplit.setContinuousLayout(true);
		SwingUtilities.invokeLater(() -> handSplit.setDividerLocation(0.5));
		handSplit.setResizeWeight(0.5);
		handPanel.add(handModPanel, BorderLayout.NORTH);
		handPanel.add(handSplit, BorderLayout.CENTER);
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
		medCMCLabel = new JLabel();
		statsPanel.add(medCMCLabel);
		updateStats();
		GridBagConstraints statsConstraints = new GridBagConstraints();
		statsConstraints.anchor = GridBagConstraints.WEST;
		bottomPanel.add(statsPanel, statsConstraints);
		
		// Check legality button
		JPanel legalityPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
		JButton legalityButton = new JButton("Show Legality");
		legalityButton.addActionListener((e) -> {
			LegalityChecker checker = new LegalityChecker();
			checker.checkLegality(deck);
			JOptionPane.showMessageDialog(this, new LegalityPanel(checker), "Legality of " + deckName(), JOptionPane.PLAIN_MESSAGE);
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
					&& JOptionPane.showConfirmDialog(EditorFrame.this, "Change log cannot be restored once saved.  Clear change log?", "Clear Change Log?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
			{
				String text = changelogArea.getText();
				performAction(() -> {
					changelogArea.setText(text);
					return true;
				}, () -> {
					changelogArea.setText("");
					return true;
				});
			}
		});
		clearLogPanel.add(clearLogButton);
		changelogPanel.add(clearLogPanel, BorderLayout.SOUTH);
		listTabs.addTab("Change Log", changelogPanel);
		
		setTransferHandler(new EditorImportHandler());
		
		deck.addDeckListener((e) -> {
			// Cards
			if (e.cardsChanged())
			{
				updateStats();
				
				if (!opening)
					parent.updateCardsInDeck();
				((AbstractTableModel)table.getModel()).fireTableDataChanged();
				for (CategoryPanel c: categoryPanels)
					((AbstractTableModel)c.table.getModel()).fireTableDataChanged();
				for (Card c: selectedCards)
				{
					if (getSelectedSource().contains(c))
					{
						int row = selectedTable.convertRowIndexToView(getSelectedSource().indexOf(c));
						selectedTable.addRowSelectionInterval(row, row);
					}
				}
				if (table.isEditing())
					table.getCellEditor().cancelCellEditing();
				for (CategoryPanel c: categoryPanels)
					if (c.table.isEditing())
						c.table.getCellEditor().cancelCellEditing();
				
				hand.refresh();
				handCalculations.update();
			}
			// Categories
			if (e.categoryAdded())
			{
				CategoryPanel category = createCategoryPanel(deck.getCategorySpec(e.addedName()));
				categoryPanels.add(category);
				
				for (CategoryPanel c: categoryPanels)
					if (c != category)
						c.rankBox.addItem(deck.categories().size() - 1);
				
				listTabs.setSelectedIndex(CATEGORIES);
				updateCategoryPanel();
				SwingUtilities.invokeLater(() -> {
					switchCategoryBox.setSelectedItem(category.getCategoryName());
					category.scrollRectToVisible(new Rectangle(category.getSize()));
					category.flash();
				});
				handCalculations.update();
			}
			if (e.categoriesRemoved())
			{
				categoryPanels = categoryPanels.stream()
						.filter((panel) -> !e.removedNames().contains(panel.getCategoryName()))
						.collect(Collectors.toList());
				for (CategoryPanel panel: categoryPanels)
					for (int i = 0; i < e.removedNames().size(); i++)
						panel.rankBox.removeItemAt(categoryPanels.size());
				
				listTabs.setSelectedIndex(CATEGORIES);
				updateCategoryPanel();
				handCalculations.update();
			}
			if (e.ranksChanged())
			{
				for (CategoryPanel panel: categoryPanels)
					panel.rankBox.setSelectedIndex(deck.getCategoryRank(panel.getCategoryName()));
				listTabs.setSelectedIndex(CATEGORIES);
				updateCategoryPanel();
			}
			if (e.categoryChanged())
			{
				CategorySpec.Event event = e.categoryChanges();
				if (event.nameChanged())
					getCategory(event.oldName()).setCategoryName(event.newName());
				for (CategoryPanel c: categoryPanels)
				{
					((AbstractTableModel)c.table.getModel()).fireTableDataChanged();
					c.update();
				}
				
				updateCategoryPanel();
				handCalculations.update();
				SwingUtilities.invokeLater(() -> {
					CategoryPanel category = event.nameChanged() ? getCategory(event.newName()) : getCategory(e.categoryName());
					switchCategoryBox.setSelectedItem(category.getCategoryName());
					if (!category.getBounds().intersects(categoriesContainer.getVisibleRect()))
					{
						category.scrollRectToVisible(new Rectangle(category.getSize()));
						category.flash();
					}
				});
			}
			
			// Clean up
			if (!unsaved)
			{
				setTitle(getTitle() + " *");
				unsaved = true;
			}
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
		progressPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
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
			JOptionPane.showMessageDialog(this, "Error opening " + f.getName() + ": " + e.getCause().getMessage() + ".", "Error", JOptionPane.ERROR_MESSAGE);
			deck.clear();
			categoriesContainer.removeAll();
		}
		originalDeck.addAll(deck);
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
	 * @return The specifications for the categories in the deck.
	 */
	public Collection<CategorySpec> categories()
	{
		return deck.categories();
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
			spec = CategoryEditorPanel.showCategoryEditor(this, spec);
			if (spec != null && deck.containsCategory(spec.getName()))
				JOptionPane.showMessageDialog(this, "Categories must have unique names.", "Error", JOptionPane.ERROR_MESSAGE);
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
						parent.selectCard(getSelectedCards()[0]);
				}
			}
		});
		// Add the behavior for the edit category button
		newCategory.rankBox.addActionListener((e) -> {
			if (newCategory.rankBox.isPopupVisible())
			{
				int oldRank = deck.getCategoryRank(newCategory.getCategoryName());
				int newRank = newCategory.rankBox.getSelectedIndex();
				performAction(() -> deck.swapCategoryRanks(newCategory.getCategoryName(), oldRank),
						() -> deck.swapCategoryRanks(newCategory.getCategoryName(), newRank));
			}
		});
		newCategory.editButton.addActionListener((e) -> editCategory(spec.getName()));
		// Add the behavior for the remove category button
		newCategory.removeButton.addActionListener((e) -> removeCategory(spec));
		// Add the behavior for the color edit button
		newCategory.colorButton.addActionListener((e) -> {
			Color newColor = JColorChooser.showDialog(this, "Choose a Color", newCategory.colorButton.color());
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
				addCard(c, 4 - deck.getData(c).count());
		});
		tableMenu.add(playsetPopupItem);
		
		// Add variable item
		JMenuItem addNPopupItem = new JMenuItem("Add Copies...");
		addNPopupItem.addActionListener((e) -> {
			JPanel contentPanel = new JPanel(new BorderLayout());
			contentPanel.add(new JLabel("Copies to add:"), BorderLayout.WEST);
			JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 1));
			contentPanel.add(spinner, BorderLayout.SOUTH);
			if (JOptionPane.showConfirmDialog(this, contentPanel, "Add Cards", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
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
			if (JOptionPane.showConfirmDialog(this, contentPanel, "Add Cards", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
				removeSelectedCards((Integer)spinner.getValue());
		});
		tableMenu.add(removeNPopupItem);
		
		JSeparator categoriesSeparator = new JSeparator();
		tableMenu.add(categoriesSeparator);
		
		// Quick edit categories
		JMenu addToCategoryMenu = new JMenu("Include in");
		tableMenu.add(addToCategoryMenu);
		JMenuItem removeFromCategoryItem = new JMenuItem("Exclude from " + spec.getName());
		removeFromCategoryItem.addActionListener((e) -> {
			List<Card> selectedCards = newCategory.getSelectedCards();
			performAction(() -> {
				for (Card c: selectedCards)
					spec.include(c);
				((AbstractTableModel)newCategory.table.getModel()).fireTableDataChanged();
				return true;
			}, () -> {
				for (Card c: selectedCards)
					spec.exclude(c);
				((AbstractTableModel)newCategory.table.getModel()).fireTableDataChanged();
				return true;
			});
		});
		tableMenu.add(removeFromCategoryItem);
		JMenu removeFromCategoryMenu = new JMenu("Exclude from");
		tableMenu.add(removeFromCategoryMenu);
		
		// Edit categories item
		JMenuItem editCategoriesItem = new JMenuItem("Edit Categories...");
		editCategoriesItem.addActionListener((e) -> {
			IncludeExcludePanel iePanel = new IncludeExcludePanel(deck.categories().stream().sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName())).collect(Collectors.toList()), getSelectedCards());
			if (JOptionPane.showConfirmDialog(this, new JScrollPane(iePanel), "Set Categories", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
				editInclusion(iePanel.getIncluded(), iePanel.getExcluded());
		});
		tableMenu.add(editCategoriesItem);
		
		tableMenu.add(new JSeparator());
		
		// Edit tags item
		JMenuItem editTagsItem = new JMenuItem("Edit Tags...");
		editTagsItem.addActionListener((e) -> parent.editTags(getSelectedCards()));
		tableMenu.add(editTagsItem);
		
		tableMenu.addPopupMenuListener(new TablePopupListener(addToCategoryMenu, removeFromCategoryMenu,
				editCategoriesItem, categoriesSeparator, newCategory.table));
		tableMenu.addPopupMenuListener(PopupMenuListenerFactory.createVisibleListener((e) -> removeFromCategoryItem.setText("Exclude from " + spec.getName())));
		
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
				if (JOptionPane.showConfirmDialog(this,
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
	 * Add a category to the deck.
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
		return panel != null && deck.removeCategory(category.getName());
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
			return performAction(() -> deleteCategory(spec), () -> insertCategory(spec));
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
			JOptionPane.showMessageDialog(this, "Deck " + deckName() + " has no category named " + name + ".",
					"Error", JOptionPane.ERROR_MESSAGE);
		else
		{
			CategorySpec spec = CategoryEditorPanel.showCategoryEditor(this, toEdit);
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
		CategorySpec oldSpec = new CategorySpec(toEdit);
		performAction(() -> toEdit.copy(oldSpec), () -> toEdit.copy(newValues));
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
		return deck.containsCategory(category.getName())
				&& performAction(() -> insertCategory(category), () -> deleteCategory(category));
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
		categoriesContainer.removeAll();
		switchCategoryModel.removeAllElements();
		
		if (deck.categories().isEmpty())
			switchCategoryBox.setEnabled(false);
		else
		{
			switchCategoryBox.setEnabled(true);
			List<CategorySpec> categories = new ArrayList<CategorySpec>(deck.categories());
			categories.sort((a, b) -> sortCategoriesBox.getItemAt(sortCategoriesBox.getSelectedIndex()).compare(deck, a, b));
			
			for (CategorySpec c: categories)
				categoriesContainer.add(getCategory(c.getName()));
			for (CategorySpec c: categories)
				switchCategoryModel.addElement(c.getName());
		}
		
		categoriesContainer.revalidate();
		categoriesContainer.repaint();
	}
	
	/**
	 * Update the card counter to reflect the total number of cards in the deck.
	 */
	public void updateStats()
	{
		countLabel.setText("Total cards: " + deck.total());
		landLabel.setText("Lands: " + deck.land());
		nonlandLabel.setText("Nonlands: " + deck.nonland());

		double avgCMC = 0.0;
		for (Card card: deck)
			if (!card.typeContains("land"))
				avgCMC += card.minCmc()*deck.getData(card).count();
		if (deck.nonland() > 0)
			avgCMC /= deck.nonland();
		if ((int)avgCMC == avgCMC)
			avgCMCLabel.setText("Average CMC: " + (int)avgCMC);
		else
			avgCMCLabel.setText(String.format("Average CMC: %.2f", avgCMC));
		
		double medCMC = 0.0;
		List<Double> cmc = new ArrayList<Double>();
		for (Card card: deck)
			if (!card.typeContains("land"))
				for (int i = 0; i < deck.getData(card).count(); i++)
					cmc.add(card.minCmc());
		Collections.sort(cmc);
		if (!cmc.isEmpty())
		{
			if (cmc.size()%2 == 0)
				medCMC = (cmc[cmc.size()/2 - 1] + cmc[cmc.size()/2])/2;
			else
				medCMC = cmc[cmc.size()/2];
		}
		if ((int)medCMC == medCMC)
			medCMCLabel.setText("Median CMC: " + (int)medCMC);
		else
			medCMCLabel.setText(String.format("Median CMC: %.1f", medCMC));
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
	 * @param index Index into the given table to get a Card from
	 * @return The Card in the deck at the given index in the given table, if the table is in this EditorFrame.
	 */
	public Card getCardAt(CardTable t, int tableIndex)
	{
		if (t == table)
			return deck[table.convertRowIndexToModel(tableIndex)];
		else
		{
			for (CategoryPanel panel: categoryPanels)
				if (t == panel.table)
					return deck.getCategoryCards(panel.getCategoryName())[panel.table.convertRowIndexToModel(tableIndex)];
			throw new IllegalArgumentException("Table not in deck " + deckName());
		}
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
		saveSelectedCards();
		cards.values().removeAll(Arrays.asList(0));
		if (cards.isEmpty())
			return false;
		else
		{
			for (Map.Entry<Card, Integer> entry: cards.entrySet())
				deck.add(entry.getKey(), entry.getValue());
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
		saveSelectedCards();
		Map<Card, Integer> removed = new HashMap<Card, Integer>();
		if (toRemove.isEmpty())
			return removed;
		else
		{
			for (Card c: toRemove)
				removed[c] = deck.remove(c, n);
			return removed;
		}
	}
	
	/**
	 * Save the list of selected cards from the active table for later selection
	 * restoration.
	 */
	private void saveSelectedCards()
	{
		if (selectedTable != null)
			selectedCards = Arrays.stream(selectedTable.getSelectedRows())
					.mapToObj((r) -> selectedSource[selectedTable.convertRowIndexToModel(r)])
					.collect(Collectors.toList());
		else
			selectedCards = new ArrayList<Card>();
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
		Map<Card, Integer> cards = toAdd.stream().collect(Collectors.toMap((c) -> c, (c) -> n));
		return performAction(() -> !deleteCards(toAdd, n).isEmpty(), () -> insertCards(cards));
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
		Map<Card, Integer> removed = toRemove.stream().collect(Collectors.toMap(Function.identity(), (c) -> Math.min(n, deck.getData(c).count())));
		return performAction(() -> insertCards(removed), () -> !deleteCards(toRemove, n).isEmpty());
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
	 * @param c Card to set (or add if it isn't present)
	 * @param n Number of copies to set to (or add if the card isn't present)
	 */
	public void setCardCount(Card c, int n)
	{
		if (deck.contains(c))
		{
			if (n != deck.getData(c).count())
			{
				int old = deck.getData(c).count();
				performAction(() -> deck.set(c, old), () -> deck.set(c, n));
			}
		}
		else
			addCard(c, n);
	}
	
	/**
	 * Change inclusion of cards in categories according to the given maps.
	 * 
	 * @param included Map of cards onto the set of categories they should become included in
	 * @param excluded Map of cards onto the set of categories they should become excluded from
	 */
	public void editInclusion(Map<Card, Set<CategorySpec>> included, Map<Card, Set<CategorySpec>> excluded)
	{
		performAction(() -> {
			boolean changed = false;
			for (Card card: included.keySet())
				for (CategorySpec category: included[card])
					changed |= category.exclude(card);
			for (Card card: excluded.keySet())
				for (CategorySpec category: excluded[card])
					changed |= category.include(card);
			return changed;
		}, () -> {
			boolean changed = false;
			for (Card card: included.keySet())
				for (CategorySpec category: included[card])
					changed |= category.include(card);
			for (Card card: excluded.keySet())
				for (CategorySpec category: excluded[card])
					changed |= category.exclude(card);
			return changed;
		});
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
				  .mapToObj((r) -> selectedSource[selectedTable.convertRowIndexToModel(r)])
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
	public void setSelectedSource(CardTable table, CardList source)
	{
		selectedTable = table;
		selectedSource = source;
	}
	
	/**
	 * @return The CardCollection containing the selected Cards.
	 */
	public CardList getSelectedSource()
	{
		return selectedSource;
	}

	/**
	 * TODO: Replace this with getting the card's metadata
	 * @param c Card to look for
	 * @return The number of copies of the given Card in the deck.
	 */
	public int count(Card c)
	{
		CardList.Entry data = deck.getData(c);
		return data == null ? 0 : data.count();
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
			for (Card c: originalDeck)
				if (deck.getData(c).count() < originalDeck.getData(c).count())
					changes += ("-" + (originalDeck.getData(c).count() - deck.getData(c).count()) + "x " + c.unifiedName() + " (" + c.expansion().name + ")\n");
			for (Card c: deck)
				if (originalDeck.getData(c).count() < deck.getData(c).count())
					changes += ("+" + (deck.getData(c).count() - originalDeck.getData(c).count()) + "x " + c.unifiedName() + " (" + c.expansion().name + ")\n");
			if (!changes.isEmpty())
			{
				SimpleDateFormat format = new SimpleDateFormat("MMMM d, yyyy HH:mm:ss");
				changelogArea.append("~~~~~" + format.format(new Date()) + "~~~~~\n");
				changelogArea.append(changes + "\n");
			}
			PrintWriter wr = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f, true), "UTF8"));
			wr.print(changelogArea.getText());
			wr.close();
			
			originalDeck = new Deck();
			originalDeck.addAll(deck);
			unsaved = false;
			setFile(f);
			return true;
		}
		catch (IOException e)
		{
			JOptionPane.showMessageDialog(this, "Error saving " + f.getName() + ": " + e.getMessage() + ".", "Error", JOptionPane.ERROR_MESSAGE);
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
			switch(JOptionPane.showConfirmDialog(this, msg, "Unsaved Changes", JOptionPane.YES_NO_CANCEL_OPTION))
			{
			case JOptionPane.YES_OPTION:
				parent.save(EditorFrame.this);
			case JOptionPane.NO_OPTION:
				dispose();
				return true;
			case JOptionPane.CANCEL_OPTION: case JOptionPane.CLOSED_OPTION:
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
		List<CardData> columns = SettingsDialog.getAsCharacteristics(SettingsDialog.EDITOR_COLUMNS);
		Color stripe = SettingsDialog.getAsColor(SettingsDialog.EDITOR_STRIPE);
		model.setColumns(columns);
		table.setStripeColor(stripe);
		for (int i = 0; i < table.getColumnCount(); i++)
			if (model.isCellEditable(0, i))
				table.getColumn(model.getColumnName(i)).setCellEditor(model.getColumnCharacteristic(i).createCellEditor(this));
		for (CategoryPanel category: categoryPanels)
			category.applySettings(this);
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
	 * Perform an action.  Then, if it succeeds, push it onto the undo buffer
	 * and clear the redo buffer.
	 * 
	 * @param undo What to do to undo the action
	 * @param redo The action to perform
	 * @return <code>true</code> if the action succeeded, and <code>false</code>
	 * otherwise.
	 */
	private boolean performAction(BooleanSupplier undo, BooleanSupplier redo)
	{
		undoBuffer.push(new UndoableAction()
		{
			@Override
			public boolean undo()
			{
				return undo.getAsBoolean();
			}

			@Override
			public boolean redo()
			{
				return redo.getAsBoolean();
			}	
		});
		if (undoBuffer.peek().redo())
		{
			redoBuffer.clear();
			return true;
		}
		else
		{
			undoBuffer.pop();
			return false;
		}
	}
	
	/**
	 * Popup menu listener for a CardTable of this EditorFrame.  It controls the visibility
	 * and contents of the include and exclude options.
	 * 
	 * @author Alec Roelke
	 */
	private class TablePopupListener implements PopupMenuListener
	{
		/**
		 * Submenu for quickly adding cards to categories.
		 */
		private JMenu addToCategoryMenu;
		/**
		 * Submenu for quickly removing cards from categories.
		 */
		private JMenu removeFromCategoryMenu;
		/**
		 * Item for editing the categories of cards.
		 */
		private JMenuItem editCategoriesItem;
		/**
		 * Separator between category edit and card edit sections of the table
		 * popup menu.
		 */
		private JSeparator menuSeparator;
		/**
		 * Table in which the menu appears.
		 */
		private CardTable table;
		
		/**
		 * Create a new TablePopupListener.
		 * 
		 * @param add Submenu for adding cards
		 * @param remove Submenu for removing cards
		 * @param edit Item for editing card categories
		 * @param t Table which will contain the popup
		 */
		public TablePopupListener(JMenu add, JMenu remove, JMenuItem edit, JSeparator sep, CardTable t)
		{
			addToCategoryMenu = add;
			removeFromCategoryMenu = remove;
			editCategoriesItem = edit;
			menuSeparator = sep;
			table = t;
		}

		/**
		 * When the popup menu becomes invisible (something is selected or it
		 * is canceled), the submenus should be cleared so they don't get
		 * duplicate items.
		 * 
		 * @param e Event containing information about the closing
		 */
		@Override
		public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
		{
			addToCategoryMenu.removeAll();
			removeFromCategoryMenu.removeAll();
		}

		/**
		 * Just before the popup menu becomes visible, its submenus for adding
		 * the selected cards to categories and removing them from categories
		 * should be populated.  If any of them are empty, they become invisible.
		 * If there are no selected cards or there are no categories, the edit
		 * categories item also becomes invisible.
		 * 
		 * @param e
		 */
		@Override
		public void popupMenuWillBecomeVisible(PopupMenuEvent e)
		{
			if (selectedTable == table)
			{
				if (getSelectedCards().size() == 1)
				{
					Card card = getSelectedCards()[0];
					
					for (CategorySpec category: deck.categories())
					{
						if (!category.includes(card))
						{
							JMenuItem categoryItem = new JMenuItem(category.getName());
							categoryItem.addActionListener((e2) -> performAction(() -> category.exclude(card), () -> category.include(card)));
							addToCategoryMenu.add(categoryItem);
						}
					}
					addToCategoryMenu.setVisible(addToCategoryMenu.getItemCount() > 0);
					
					for (CategorySpec category: deck.categories())
					{
						if (category.includes(card))
						{
							JMenuItem categoryItem = new JMenuItem(category.getName());
							categoryItem.addActionListener((e2) -> performAction(() -> category.include(card), () -> category.exclude(card)));
							removeFromCategoryMenu.add(categoryItem);
						}
					}
					removeFromCategoryMenu.setVisible(removeFromCategoryMenu.getItemCount() > 0);
				}
				else
				{
					addToCategoryMenu.setVisible(false);
					removeFromCategoryMenu.setVisible(false);
				}
				
				editCategoriesItem.setVisible(!getSelectedCards().isEmpty() && !deck.categories().isEmpty());
				
				menuSeparator.setVisible(addToCategoryMenu.isVisible() || removeFromCategoryMenu.isVisible() || editCategoriesItem.isVisible());
			}
		}

		@Override
		public void popupMenuCanceled(PopupMenuEvent e)
		{}
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
					return performAction(() -> {
							boolean undone = false;
							for (Map.Entry<Card, Integer> entry: data.entrySet())
								undone |= !deleteCards(Arrays.asList(entry.getKey()), entry.getValue()).isEmpty();
							return undone;
						}, () -> insertCards(data));
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
			int progress = chunks[chunks.size() - 1];
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
				opening = true;
				int cards = Integer.valueOf(rd.readLine().trim());
				for (int i = 0; i < cards; i++)
				{
					if (isCancelled())
						return null;
					String[] card = rd.readLine().trim().split("\t");
					Card c = parent.getCard(card[0]);
					if (c != null)
						deck.add(c, Integer.valueOf(card[1]), Deck.DATE_FORMAT.parse(card[2]));
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
			opening = false;
			dialog.dispose();
			unsaved = false;
			setFile(file);
			undoBuffer.clear();
			redoBuffer.clear();
		}
	}
}
