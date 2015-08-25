package gui.editor;

import gui.CardTable;
import gui.CardTableModel;
import gui.MainFrame;
import gui.ScrollablePanel;
import gui.SettingsDialog;
import gui.TableMouseAdapter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
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
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import database.Card;
import database.Deck;
import database.Hand;
import database.LegalityChecker;
import database.characteristics.CardCharacteristic;

/**
 * This class represents an internal frame for editing a deck.  It contains a table that shows all cards
 * and their counts in the deck as well as zero or more tables for categories within it.  It can add cards
 * to a deck and add, edit, and delete categories.  It is contained within the main frame, which has the
 * inventory from which cards can be added.
 * 
 * TODO: Try to figure out a more elegant way of handling the undo/redo buffer
 * TODO: Use a LinkedHashSet or LinkedHashMap to maintain ordering of categories
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
	 * Parent MainFrame.
	 */
	private MainFrame parent;
	/**
	 * Master decklist to which cards are added.
	 */
	protected Deck deck;
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
	private Stack<DeckAction> undoBuffer;
	/**
	 * Stack containing future actions that have been performed on the deck to represent
	 * the redo buffer.  This contains only things that have been on the undo buffer
	 * and have been undone, and is cleared when a new action is performed.
	 */
	private Stack<DeckAction> redoBuffer;
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
		file = null;
		unsaved = false;
		undoBuffer = new Stack<DeckAction>();
		redoBuffer = new Stack<DeckAction>();
		startingHandSize = Integer.valueOf(parent.getSetting(SettingsDialog.HAND_SIZE));

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

		model = new CardTableModel(this, deck, Arrays.stream(parent.getSetting(SettingsDialog.EDITOR_COLUMNS).split(",")).map(CardCharacteristic::get).collect(Collectors.toList()));

		// Create the table so that it resizes if the window is too big but not if it's too small
		table = new CardTable(model);
		table.setStripeColor(SettingsDialog.stringToColor(parent.getSetting(SettingsDialog.EDITOR_STRIPE)));
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
				else
				{
					setCategoriesMenu.setEnabled(true);
					setCategoriesMenu.removeAll();
					for (CategoryPanel category: categories.stream().sorted((a, b) -> a.name().compareTo(b.name())).collect(Collectors.toList()))
					{
						JCheckBoxMenuItem containsBox = new JCheckBoxMenuItem(category.name());
						containsBox.setSelected(category.contains(cards.get(0)));
						containsBox.addActionListener((a) -> {
							if (((JCheckBoxMenuItem)a.getSource()).isSelected())
							{
								if (category.include(cards.get(0)))
								{
									setUnsaved();
									update();
									undoBuffer.push(new IncludeCardAction(EditorFrame.this, category, cards.get(0)));
									redoBuffer.clear();
								}
							}
							else
							{
								if (category.exclude(cards.get(0)))
								{
									setUnsaved();
									update();
									undoBuffer.push(new ExcludeCardAction(EditorFrame.this, category, cards.get(0)));
									redoBuffer.clear();
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
		JPanel categoryHeaderPanel = new JPanel(new GridLayout(1, 2));
		categoriesPanel.add(categoryHeaderPanel, BorderLayout.NORTH);
		
		// Button to add a new category
		JPanel addCategoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JButton addCategoryButton = new JButton("Add");
		addCategoryButton.addActionListener((e) -> createCategory());
		addCategoryPanel.add(addCategoryButton);
		categoryHeaderPanel.add(addCategoryPanel);
		
		// Combo box to switch to a different category
		JPanel switchCategoryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		switchCategoryBox = new JComboBox<String>(switchCategoryModel = new DefaultComboBoxModel<String>());
		switchCategoryBox.setEnabled(false);
		switchCategoryBox.addActionListener((e) -> {
			CategoryPanel toView = getCategory(switchCategoryBox.getItemAt(switchCategoryBox.getSelectedIndex()));
			if (toView != null)
			{
				toView.scrollRectToVisible(new Rectangle(toView.getSize()));
				toView.flash();
			}
		});
		switchCategoryPanel.add(new JLabel("Go to category:"));
		switchCategoryPanel.add(switchCategoryBox);
		categoryHeaderPanel.add(switchCategoryPanel);

		// Make sure all parts of the category panel fit inside the window (this is necessary because
		// JScrollPanes do weird things with non-scroll-savvy components)
		JPanel categoriesSuperContainer = new ScrollablePanel(new BorderLayout())
		{
			@Override
			public boolean getScrollableTracksViewportWidth()
			{
				return true;
			}
		};
		categoriesContainer = new JPanel();
		categoriesContainer.setLayout(new BoxLayout(categoriesContainer, BoxLayout.Y_AXIS));
		categories = new ArrayList<CategoryPanel>();
		
		// The category panel is a vertically-scrollable panel that contains all categories stacked vertically
		// The categories should have a constant height, but fit the container horizontally
		categoriesSuperContainer.add(categoriesContainer, BorderLayout.NORTH);
		categoriesPanel.add(new JScrollPane(categoriesSuperContainer, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
		listTabs.addTab("Categories", categoriesPanel);
		
		// Panel containing sample hands
		JPanel handPanel = new JPanel(new BorderLayout());
		
		// Table showing the cards in hand
		// TODO: when card images are implemented, make this table unselectable
		hand = new Hand(deck);
		handModel = new CardTableModel(this, hand, Arrays.stream(parent.getSetting(SettingsDialog.HAND_COLUMNS).split(",")).map(CardCharacteristic::get).collect(Collectors.toList()));
		handTable = new CardTable(handModel);
		handTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		handTable.getSelectionModel().addListSelectionListener((e) -> {
			ListSelectionModel lsm = (ListSelectionModel)e.getSource();
			if (!lsm.isSelectionEmpty())
				parent.selectCard(parent.getCard(hand.get(handTable.convertRowIndexToModel(lsm.getMinSelectionIndex())).id()));
		});
		handTable.setStripeColor(SettingsDialog.stringToColor(parent.getSetting(SettingsDialog.EDITOR_STRIPE)));
		handPanel.add(new JScrollPane(handTable), BorderLayout.CENTER);
		
		// Control panel for manipulating the sample hand
		JPanel handModPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
		JButton newHandButton = new JButton("New Hand");
		newHandButton.addActionListener((e) -> {
			hand.newHand(startingHandSize);
			handModel.fireTableDataChanged();
		});
		handModPanel.add(newHandButton);
		JButton mulliganButton = new JButton("Mulligan");
		mulliganButton.addActionListener((e) -> {
			hand.mulligan();
			handModel.fireTableDataChanged();
		});
		handModPanel.add(mulliganButton);
		JButton drawCardButton = new JButton("Draw a Card");
		drawCardButton.addActionListener((e) -> {
			hand.draw();
			handModel.fireTableDataChanged();
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
			excludeTable.setStripeColor(SettingsDialog.stringToColor(parent.getSetting(SettingsDialog.EDITOR_STRIPE)));
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
		probabilityButton.addActionListener((e) -> new CalculateHandDialog(parent, deck, hand.excluded(), startingHandSize,
				SettingsDialog.stringToColor(parent.getSetting(SettingsDialog.EDITOR_STRIPE))).setVisible(true));
		handModPanel.add(probabilityButton);
		handPanel.add(handModPanel, BorderLayout.SOUTH);
		
		listTabs.addTab("Sample Hand", handPanel);
		
		// TODO: Add tabs for deck analysis
		// - category pie chart
		// - mana curve
		// - color distribution (cards/devotion[max,avg,total])
		// - mana production distribution

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
			updateCategorySwitch();
			update();
		}
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
		List<String> names = categories.stream().map(CategoryPanel::name).collect(Collectors.toList());
		Collections.sort(names);
		return names.toArray(new String[names.size()]);
	}

	/**
	 * Get the category with the specified name in the deck.
	 * 
	 * @param name Name of the category to search for
	 * @return The category with the specified name, or <code>null</code> if there is none.
	 */
	public CategoryPanel getCategory(String name)
	{
		for (CategoryPanel category: categories)
			if (category.name().equals(name))
				return category;
		return null;
	}
	
	/**
	 * Open the dialog to create a new category for the deck and then add it.
	 */
	public void createCategory()
	{
		CategoryEditorPanel editor = null;
		do
		{
			editor = CategoryEditorPanel.showCategoryEditor(editor != null ? editor.toString() : "");
			if (editor != null && deck.containsCategory(editor.name()))
				JOptionPane.showMessageDialog(null, "Categories must have unique names.", "Error", JOptionPane.ERROR_MESSAGE);
		} while (editor != null && deck.containsCategory(editor.name()));
		if (editor != null)
			addCategory(new CategoryPanel(editor.name(), editor.color(), editor.repr(), editor.filter(), this));
	}

	/**
	 * Add a category to the deck and update the undo and redo buffers.
	 * 
	 * @param newCategory Category to add.
	 */
	public void addCategory(CategoryPanel newCategory)
	{
		if (addCategoryUnbuffered(newCategory))
		{
			undoBuffer.push(new AddCategoryAction(this, newCategory));
			redoBuffer.clear();
		}
	}
	
	/**
	 * Add a category to the deck but don't update the undo or redo buffers.
	 * 
	 * @param newCategory Category to add.
	 * @return <code>true</code> if the category was successfully added and
	 * <code>false</code> otherwise.
	 */
	protected boolean addCategoryUnbuffered(CategoryPanel newCategory)
	{
		if (newCategory != null)
		{
			categories.add(newCategory);
			categoriesContainer.add(newCategory);
			// When a card is selected in a category, the others should deselect
			newCategory.table.getSelectionModel().addListSelectionListener((e) -> {
				ListSelectionModel lsm = (ListSelectionModel)e.getSource();
				if (!lsm.isSelectionEmpty())
				{
					if (!e.getValueIsAdjusting())
						parent.selectCard(deck.getCategory(newCategory.name()).get(newCategory.table.convertRowIndexToModel(lsm.getMinSelectionIndex())));
					for (CategoryPanel c: categories)
						if (newCategory != c)
							c.table.clearSelection();
					table.clearSelection();
				}
			});
			// Add the behavior for the edit category button
			newCategory.editButton.addActionListener((e) -> {
				editCategory(newCategory.name());
			});
			// Add the behavior for the remove category button
			newCategory.removeButton.addActionListener((e) -> removeCategory(newCategory));
			// Add the behavior for the color edit button
			newCategory.colorButton.addActionListener((e) -> {
				Color newColor = JColorChooser.showDialog(null, "Choose a Color", newCategory.colorButton.color());
				if (newColor != null && !newColor.equals(newCategory.colorButton.color()))
				{
					newCategory.colorButton.setColor(newColor);
					String oldRepr = newCategory.toString();
					CategoryEditorPanel editor = new CategoryEditorPanel(oldRepr);
					newCategory.edit(editor.name(), newCategory.colorButton.color(), editor.repr(), editor.filter());
					setUnsaved();
					undoBuffer.push(new EditCategoryAction(this, oldRepr, newCategory.filter(), newCategory.toString(), newCategory.filter()));
					redoBuffer.clear();
					newCategory.update();
				}
			});
			
			// Add the behavior for clicking on the category's table
			// Table popup menu
			JPopupMenu tableMenu = new JPopupMenu();
			newCategory.table.addMouseListener(new TableMouseAdapter(newCategory.table, tableMenu));
			
			// Add single copy item
			JMenuItem addSinglePopupItem = new JMenuItem("Add Single Copy");
			addSinglePopupItem.addActionListener((e) -> {addCards(newCategory.getSelectedCards(), 1);});
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
				if (selectedCards.size() == 1)
				{
					if (newCategory.exclude(selectedCards.get(0)))
					{
						undoBuffer.push(new ExcludeCardAction(EditorFrame.this, newCategory, selectedCards.get(0)));
						redoBuffer.clear();
						setUnsaved();
					}
				}
			});
			tableMenu.addPopupMenuListener(new PopupMenuListener()
			{
				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent e)
				{
					removeFromCategoryItem.setEnabled(newCategory.getSelectedCards().size() == 1);
				}

				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
				{}
				
				@Override
				public void popupMenuCanceled(PopupMenuEvent e)
				{}
			});
			tableMenu.add(removeFromCategoryItem);
			
			newCategory.table.addMouseListener(new TableMouseAdapter(newCategory.table, tableMenu));
			updateCategorySwitch();
			update();
			setUnsaved();
			listTabs.setSelectedIndex(CATEGORIES);
			return true;
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
			String oldRepr = toEdit.toString();
			Predicate<Card> oldFilter = toEdit.filter();
			CategoryEditorPanel editor = CategoryEditorPanel.showCategoryEditor(oldRepr);
			if (editor != null)
			{
				toEdit.edit(editor.name(), editor.color(), editor.repr(), editor.filter());
				updateCategorySwitch();
				update();
				setUnsaved();
				undoBuffer.push(new EditCategoryAction(this, oldRepr, oldFilter, toEdit.toString(), toEdit.filter()));
				redoBuffer.clear();
				listTabs.setSelectedIndex(CATEGORIES);
			}
		}
	}

	/**
	 * If the given category exists in this EditorFrame, remove it and
	 * remove it from the deck and then update the undo and redo buffers.
	 * 
	 * @param category Panel representing the category to be removed
	 * @return <code>true</code> if the category was successfully removed,
	 * and <code>false</code> otherwise.
	 */
	public boolean removeCategory(CategoryPanel category)
	{
		if (removeCategoryUnbuffered(category))
		{
			undoBuffer.push(new RemoveCategoryAction(this, category));
			redoBuffer.clear();
			return true;
		}
		else
			return false;
	}

	/**
	 * If the given category exists in this EditorFrame, remove it and
	 * remove it from the deck but don't update the undo and redo buffers.
	 * 
	 * @param category Panel representing the category to be removed
	 * @return <code>true</code> if the category was successfully removed,
	 * and <code>false</code> otherwise.
	 */
	protected boolean removeCategoryUnbuffered(CategoryPanel category)
	{
		if (!deck.containsCategory(category.name()))
			return false;
		else
		{
			boolean removed = true;

			removed &= deck.removeCategory(category.name());
			removed &= categories.remove(category);
			if (removed)
				categoriesContainer.remove(category);
			updateCategorySwitch();
			update();
			setUnsaved();
			listTabs.setSelectedIndex(CATEGORIES);
			return removed;
		}
	}
	
	/**
	 * If a category with the given name exists in the deck, remove it
	 * and then update the undo and redo buffers.
	 * 
	 * @param name Name of the category to look for
	 * @return The category that was removed, or <code>null</code> if
	 * none was removed.
	 * @see EditorFrame#removeCategory(CategoryPanel)
	 */
	public CategoryPanel removeCategory(String name)
	{
		CategoryPanel removed = removeCategoryUnbuffered(name);
		if (removed != null)
		{
			undoBuffer.push(new RemoveCategoryAction(this, removed));
			redoBuffer.clear();
		}
		return removed;
	}

	/**
	 * If a category with the given name exists in the deck, remove it
	 * but don't update the undo and redo buffers.
	 * 
	 * @param name Name of the category to look for
	 * @return The category that was removed, or <code>null</code> if
	 * none was removed.
	 * @see EditorFrame#removeCategory(CategoryPanel)
	 */
	protected CategoryPanel removeCategoryUnbuffered(String name)
	{
		CategoryPanel toRemove = getCategory(name);
		if (toRemove != null)
		{
			removeCategoryUnbuffered(toRemove);
			return toRemove;
		}
		else
		{
			JOptionPane.showMessageDialog(null, "Deck " + deckName() + " has no category named " + name + ".", "Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
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
	public void updateCategorySwitch()
	{
		switchCategoryModel.removeAllElements();
		if (categories.isEmpty())
			switchCategoryBox.setEnabled(false);
		else
		{
			switchCategoryBox.setEnabled(true);
			for (CategoryPanel category: categories.stream().sorted((a, b) -> a.name().compareTo(b.name())).collect(Collectors.toList()))
				switchCategoryModel.addElement(category.name());
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
		double avgCMC = deck.stream().filter((c) -> !c.typeContains("land")).mapToDouble(Card::cmc).average().orElse(0.0);
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
		if (addCardsUnbuffered(toAdd, n))
		{
			undoBuffer.push(new AddCardsAction(this, toAdd, n));
			redoBuffer.clear();
			return true;
		}
		else
			return false;
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
	protected boolean addCardsUnbuffered(List<Card> toAdd, int n)
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
				deck.increaseAll(toAdd, n);
				model.fireTableDataChanged();
				for (CategoryPanel c: categories)
					c.update();
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
	 * Add the given number of copies of the given Card to the deck.  Selections
	 * are maintained, but the undo/redo buffer are not updated.
	 * 
	 * @param toAdd Card to add
	 * @param n Number of copies to add
	 * @return <code>true</code> if the deck changed as a result, which is
	 * always.
	 */
	protected boolean addCardUnbuffered(Card toAdd, int n)
	{
		return addCardsUnbuffered(Arrays.asList(toAdd), n);
	}

	/**
	 * Remove a number of copies of the specified Cards from the deck.  The current selections
	 * for any cards remaining in them in the category and main tables are maintained.  Then
	 * update the undo buffer.
	 * 
	 * @param toRemove List of cards to remove
	 * @param n Number of copies to remove
	 * @return <code>true</code> if the deck was changed as a result, and <code>false</code>
	 * otherwise.
	 */
	public boolean removeCards(Collection<Card> toRemove, int n)
	{
		Map<Card, Integer> removed = removeCardsUnbuffered(toRemove, n);
		if (!removed.isEmpty())
		{
			undoBuffer.push(new RemoveCardsAction(this, removed));
			redoBuffer.clear();
			return true;
		}
		else
			return false;
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
	protected Map<Card, Integer> removeCardsUnbuffered(Collection<Card> toRemove, int n)
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
							categorySelectedCards.add(deck.getCategory(category.name()).get(category.table.convertRowIndexToModel(row)));
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
						if (deck.getCategory(category.name()).contains(c))
						{
							int row = category.table.convertRowIndexToView(deck.getCategory(category.name()).indexOf(c));
							category.table.addRowSelectionInterval(row, row);
						}
					}
				}
				model.fireTableDataChanged();
				break;
			default:
				for (Card c: toRemove)
					deck.decrease(c, n);
				for (CategoryPanel c: categories)
					c.update();
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
			if (n != deck.count(c))
			{
				undoBuffer.push(new SetCardCountAction(this, c, deck.count(c), n));
				deck.setCount(c, n);
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
			DeckAction action = undoBuffer.pop();
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
			DeckAction action = redoBuffer.pop();
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
	 * @param properties Properties containing the settings to set
	 * @see MainFrame#resetDefaultSettings()
	 */
	public void setSettings(Properties properties)
	{
		List<CardCharacteristic> columns = Arrays.stream(properties.getProperty(SettingsDialog.EDITOR_COLUMNS).split(",")).map(CardCharacteristic::get).collect(Collectors.toList());
		List<CardCharacteristic> handColumns = Arrays.stream(properties.getProperty(SettingsDialog.HAND_COLUMNS).split(",")).map(CardCharacteristic::get).collect(Collectors.toList());
		Color stripe = SettingsDialog.stringToColor(properties.getProperty(SettingsDialog.EDITOR_STRIPE));
		model.setColumns(columns);
		table.setStripeColor(stripe);
		for (CategoryPanel category: categories)
		{
			category.setColumns(columns);
			category.setStripeColor(stripe);
		}
		handModel.setColumns(handColumns);
		handTable.setStripeColor(stripe);
		startingHandSize = Integer.valueOf(parent.getSetting(SettingsDialog.HAND_SIZE));
		update();
	}
	
	/**
	 * @return The Properties containing the program's settings.
	 */
	public Properties getSettings()
	{
		return parent.getSettings();
	}
	
	/**
	 * @param name Name of the property to get
	 * @return A String containing the value of the setting with the given name,
	 * or null if there is no such setting.
	 */
	public String getSetting(String name)
	{
		return parent.getSetting(name);
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
					CategoryEditorPanel editor = new CategoryEditorPanel(rd.readLine().trim());
					Set<Card> whitelist = editor.whitelist().stream().map(parent::getCard).collect(Collectors.toSet());
					Set<Card> blacklist = editor.blacklist().stream().map(parent::getCard).collect(Collectors.toSet());
					SwingUtilities.invokeLater(() -> {
						if (!isCancelled())
							addCategory(new CategoryPanel(editor.name(), editor.repr(), whitelist, blacklist, editor.color(), editor.filter(), EditorFrame.this));
					});
					publish(50 + 50*(i + 1)/categories);
				}
			}
			return null;
		}
		
		/**
		 * When the task is over, close the file and update the frame.
		 */
		@Override
		protected void done()
		{
			updateCount();
			unsaved = false;
			setFile(file);
			undoBuffer.clear();
			redoBuffer.clear();
			dialog.dispose();
		}
	}
}
