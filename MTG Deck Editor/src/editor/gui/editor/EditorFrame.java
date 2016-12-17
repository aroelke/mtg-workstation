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
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
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
import editor.gui.generic.CardMenuItems;
import editor.gui.generic.ScrollablePanel;
import editor.gui.generic.TableMouseAdapter;
import editor.gui.generic.VerticalButtonList;
import editor.util.MouseListenerFactory;
import editor.util.PopupMenuListenerFactory;
import editor.util.UnicodeSymbols;

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
	 * This enum represents an order that category panels can be sorted in.
	 * 
	 * @author Alec Roelke
	 */
	private enum CategoryOrder
	{
		/**
		 * Sort categories in ascending alphabetical order.
		 */
		A_Z("A-Z", (d) -> (a, b) -> a.getName().compareToIgnoreCase(b.getName())),
		/**
		 * Sort categories in order of increasing card count.
		 */
		ASCENDING("Ascending Size", (d) -> (a, b) -> d.getCategoryList(a.getName()).total() - d.getCategoryList(b.getName()).total()),
		/**
		 * Sort categories in order of decreasing card count.
		 */
		DESCENDING("Descending Size", (d) -> (a, b) -> d.getCategoryList(b.getName()).total() - d.getCategoryList(a.getName()).total()),
		/**
		 * Sort categories in order of increasing rank.
		 */
		PRIORITY("Increasing Rank", (d) -> (a, b) -> d.getCategoryRank(a.getName()) - d.getCategoryRank(b.getName())),
		/**
		 * Sort categories in order of decreasing rank.
		 */
		REVERSE("Decreasing Rank", (d) -> (a, b) -> d.getCategoryRank(b.getName()) - d.getCategoryRank(a.getName())),
		/**
		 * Sort categories in descending alphabetical order.
		 */
		Z_A("Z-A", (d) -> (a, b) -> -a.getName().compareToIgnoreCase(b.getName()));
		
		/**
		 * String to display when a String representation of this
		 * CategoryOrder is called for.
		 */
		private final String name;
		/**
		 * Function comparing two {@link CategorySpec}s from a deck
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
		 * Compare the two given {@link CategorySpec}s from the given Deck.
		 * 
		 * @param d deck containing the categories
		 * @param a first category
		 * @param b second category
		 * @return a negative number if the first category comes before the second in the given deck,
		 * a positive number if it comes after, and 0 if there is no relative order.
		 */
		public int compare(Deck d, CategorySpec a, CategorySpec b)
		{
			return order.apply(d).compare(a, b);
		}

		@Override
		public String toString()
		{
			return name;
		}
	}
	
	/**
	 * Struct containing the current state of a deck, the original state of it as it was
	 * loaded, and the table model and table for displaying the deck.
	 * 
	 * @author Alec Roelke
	 */
	private static final class DeckData
	{
		/**
		 * Current state of the deck.
		 */
		private Deck current;
		/**
		 * Model backing the table.
		 */
		private CardTableModel model;
		/**
		 * Original state of the deck just after loading it.
		 */
		private Deck original;
		/**
		 * Table displaying the deck.
		 */
		private CardTable table;
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
		 * Make changes to the main deck (true) or sideboard (false).
		 */
		private boolean main;
		
		/**
		 * Create a new EditorImportHandler that imports from the given list.
		 * 
		 * @param m Import from the main deck (true) or sideboard (false)
		 */
		public EditorImportHandler(boolean m)
		{
			super();
			main = m;
		}
		
		/**
		 * {@inheritDoc}
		 * Data can only be imported if it is of the card or entry flavors.
		 */
		@Override
		public boolean canImport(TransferSupport supp)
		{
			return supp.isDataFlavorSupported(CardList.entryFlavor) || supp.isDataFlavorSupported(Card.cardFlavor);
		}
		
		/**
		 * {@inheritDoc}
		 * If the data can be imported, copy the cards from the source to the target deck.
		 */
		@Override
		public boolean importData(TransferSupport supp)
		{
			try
			{
				if (!canImport(supp))
					return false;
				else if (supp.isDataFlavorSupported(CardList.entryFlavor))
				{
					@SuppressWarnings("unchecked")
					Map<Card, Integer> data = (Map<Card, Integer>)supp.getTransferable().getTransferData(CardList.entryFlavor);
					return performAction(() -> {
							boolean undone = false;
							for (Map.Entry<Card, Integer> entry: data.entrySet())
								undone |= !deleteCards(Arrays.asList(entry.getKey()), entry.getValue(), main).isEmpty();
							return undone;
						}, () -> insertCards(data, main));
				}
				else if (supp.isDataFlavorSupported(Card.cardFlavor))
				{
					Card[] data = (Card[])supp.getTransferable().getTransferData(Card.cardFlavor);
					addCards(Arrays.asList(data), 1, main);
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
		 * Handle transfers for the main deck (true) or sideboard (false).
		 */
		private boolean main;
		
		/**
		 * Create a new EditorTableTransferHandler that handles transfers to or from
		 * the main deck or sideboard.
		 * 
		 * @param m handle transfers for the main deck (true) or sideboard (false)
		 */
		public EditorTableTransferHandler(boolean m)
		{
			super(m);
			main = m;
		}

		@Override
		public Transferable createTransferable(JComponent c)
		{
			return new Deck.TransferData((main ? deck : sideboard).current, getSelectedCards());
		}

		@Override
		public void exportDone(JComponent c, Transferable t, int action)
		{
			if (action == TransferHandler.MOVE)
				removeSelectedCards(Integer.MAX_VALUE, main);
		}

		/**
		 * {@inheritDoc}
		 * Only copying is supported.
		 * TODO: Support moving
		 */
		@Override
		public int getSourceActions(JComponent c)
		{
			return TransferHandler.COPY;
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
		 * Dialog containing the progress bar.
		 */
		private JDialog dialog;
		/**
		 * File to load the deck from.
		 */
		private File file;
		/**
		 * Progress bar to display progress to.
		 */
		private JProgressBar progressBar;
		
		/**
		 * Create a new LoadWorker.
		 * 
		 * @param f file to load the deck from
		 * @param b progress bar showing progress
		 * @param d dialog containing the progress bar
		 */
		public LoadWorker(File f, JProgressBar b, JDialog d)
		{
			file = f;
			progressBar = b;
			dialog = d;
		}

		/**
		 * {@inheritDoc}
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
						deck.current.add(c, Integer.valueOf(card[1]), Deck.DATE_FORMAT.parse(card[2]));
					else
						throw new IllegalStateException("Card with UID \"" + card[0] + "\" not found");
					publish(33*(i + 1)/cards);
				}
				int categories = Integer.valueOf(rd.readLine().trim());
				for (int i = 0; i < categories; i++)
				{
					if (isCancelled())
						return null;
					CategorySpec spec = new CategorySpec(rd.readLine());
					addCategory(spec);
					publish(33 + 33*(i + 1)/categories);
				}
				cards = Integer.valueOf(rd.readLine().trim());
				for (int i = 0; i < cards; i++)
				{
					if (isCancelled())
						return null;
					String[] card = rd.readLine().trim().split("\t");
					Card c = parent.getCard(card[0]);
					if (c != null)
						sideboard.current.add(c, Integer.valueOf(card[1]), Deck.DATE_FORMAT.parse(card[2]));
					else
						throw new IllegalStateException("Card with UID \"" + card[0] + "\" not found");
					publish(66 + 33*(i + 1)/cards);
				}
				String line;
				while ((line = rd.readLine()) != null)
					changelogArea.append(line + "\n");
			}
			return null;
		}
		
		/**
		 * {@inheritDoc}
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
		
		@Override
		protected void process(List<Integer> chunks)
		{
			int progress = chunks.get(chunks.size() - 1);
			progressBar.setValue(progress);
		}
	}
	
	/**
	 * Popup menu listener for a CardTable of this EditorFrame.  It controls the visibility
	 * and contents of the include and exclude options.
	 * 
	 * @author Alec Roelke
	 */
	private class TableCategoriesPopupListener implements PopupMenuListener
	{
		/**
		 * Submenu for quickly adding cards to categories.
		 */
		private JMenu addToCategoryMenu;
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
		 * Submenu for quickly removing cards from categories.
		 */
		private JMenu removeFromCategoryMenu;
		/**
		 * Table in which the menu appears.
		 */
		private CardTable table;
		
		/**
		 * Create a new TablePopupListener.
		 * 
		 * @param add submenu for adding cards
		 * @param remove submenu for removing cards
		 * @param edit item for editing card categories
		 * @param t table which will contain the popup
		 */
		public TableCategoriesPopupListener(JMenu add, JMenu remove, JMenuItem edit, JSeparator sep, CardTable t)
		{
			addToCategoryMenu = add;
			removeFromCategoryMenu = remove;
			editCategoriesItem = edit;
			menuSeparator = sep;
			table = t;
		}

		@Override
		public void popupMenuCanceled(PopupMenuEvent e)
		{}

		/**
		 * {@inheritDoc}
		 * When the popup menu becomes invisible (something is selected or it
		 * is canceled), the submenus should be cleared so they don't get
		 * duplicate items.
		 */
		@Override
		public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
		{
			addToCategoryMenu.removeAll();
			removeFromCategoryMenu.removeAll();
		}

		/**
		 * {@inheritDoc}
		 * Just before the popup menu becomes visible, its submenus for adding
		 * the selected cards to categories and removing them from categories
		 * should be populated.  If any of them are empty, they become invisible.
		 * If there are no selected cards or there are no categories, the edit
		 * categories item also becomes invisible.
		 */
		@Override
		public void popupMenuWillBecomeVisible(PopupMenuEvent e)
		{
			if (selectedTable == table)
			{
				if (getSelectedCards().size() == 1)
				{
					Card card = getSelectedCards().get(0);
					
					for (CategorySpec category: deck.current.categories())
					{
						if (!category.includes(card))
						{
							JMenuItem categoryItem = new JMenuItem(category.getName());
							categoryItem.addActionListener((e2) -> performAction(() -> category.exclude(card), () -> category.include(card)));
							addToCategoryMenu.add(categoryItem);
						}
					}
					addToCategoryMenu.setVisible(addToCategoryMenu.getItemCount() > 0);
					
					for (CategorySpec category: deck.current.categories())
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
				
				editCategoriesItem.setVisible(!getSelectedCards().isEmpty() && !deck.current.categories().isEmpty());
				
				menuSeparator.setVisible(addToCategoryMenu.isVisible() || removeFromCategoryMenu.isVisible() || editCategoriesItem.isVisible());
			}
		}
	}
	
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
	 * Label showing the average CMC of nonland cards in the deck.
	 */
	private JLabel avgCMCLabel;
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
	 * Text area to show the changelog.
	 */
	private JTextArea changelogArea;
	/**
	 * Label showing the total number of cards in the deck.
	 */
	private JLabel countLabel;
	/**
	 * Main deck data
	 */
	private DeckData deck;
	/**
	 * File where the deck was last saved.
	 */
	private File file;
	/**
	 * Hand containing cards to show in the sample hand tab.
	 */
	private Hand hand;
	/**
	 * Panel showing a sample hand and a table showing probabilities of category requirements.
	 */
	private CalculateHandPanel handCalculations;
	/**
	 * Scroll pane containing the sample hand image panel.
	 */
	private JScrollPane imagePane;
	/**
	 * Panel containing images for the sample hand.
	 */
	private ScrollablePanel imagePanel;
	/**
	 * Label showing the total number of land cards in the deck.
	 */
	private JLabel landLabel;
	/**
	 * Tabbed pane for choosing whether to display the entire deck or the categories.
	 */
	private JTabbedPane listTabs;
	/**
	 * Label showing the median CMC of nonland cards in the deck.
	 */
	private JLabel medCMCLabel;
	/**
	 * Label showing the total number of nonland cards in the deck.
	 */
	private JLabel nonlandLabel;
	/**
	 * Whether or not a file is being opened (used to prevent some actions when changing the deck).
	 */
	private boolean opening;
	/**
	 * Parent {@link MainFrame}.
	 */
	private MainFrame parent;
	/**
	 * Stack containing future actions that have been performed on the deck to represent
	 * the redo buffer.  This contains only things that have been on the undo buffer
	 * and have been undone, and is cleared when a new action is performed.
	 */
	private Stack<UndoableAction> redoBuffer;
	/**
	 * Saved list of selected cards from the active table.
	 */
	private List<Card> selectedCards;
	/**
	 * list of currently-selected cards.
	 */
	private CardList selectedSource;
	/**
	 * Table containing currently-selected cards.
	 */
	private CardTable selectedTable;
	/**
	 * Sideboard data
	 */
	private DeckData sideboard;
	/**
	 * Combo box allowing changes to be made in the order that categories are display in.
	 */
	private JComboBox<CategoryOrder> sortCategoriesBox;
	/**
	 * Size of starting hands.
	 */
	private int startingHandSize;
	/**
	 * Combo box showing categories to jump between them.
	 */
	private JComboBox<String> switchCategoryBox;
	/**
	 * Model for the combo box to display items.
	 */
	private DefaultComboBoxModel<String> switchCategoryModel;
	/**
	 * Stack containing past actions performed on the deck to represent the undo buffer.
	 */
	private Stack<UndoableAction> undoBuffer;
	/**
	 * Whether or not the deck has been saved since it has last been changed.
	 */
	private boolean unsaved;
	
	/**
	 * Create an EditorFrame with the specified {@link MainFrame} as its parent and the name
	 * of the specified file.  The deck will be loaded from the file.
	 * 
	 * @param f file to load a deck from
	 * @param u number of the new EditorFrame (determines initial position in the window)
	 * @param p parent of the new EditorFrame
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
			deck.current.clear();
			categoriesContainer.removeAll();
		}
		deck.original.addAll(deck.current);
		listTabs.setSelectedIndex(MAIN_TABLE);
		hand.refresh();
	}

	/**
	 * Create a new EditorFrame inside the specified {@link MainFrame} and with the name
	 * "Untitled [u] *"
	 * 
	 * @param u number of the untitled deck
	 * @param p parent MainFrame
	 */
	public EditorFrame(int u, MainFrame p)
	{
		super("Untitled " + u, true, true, true, true);
		setBounds(((u - 1)%5)*30, ((u - 1)%5)*30, 600, 600);
		setLayout(new BorderLayout(0, 0));
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		deck = new DeckData();
		deck.current = new Deck();
		deck.original = new Deck();
		sideboard = new DeckData();
		sideboard.current = new Deck();
		sideboard.original = new Deck();
		
		parent = p;
		file = null;
		unsaved = false;
		undoBuffer = new Stack<UndoableAction>();
		redoBuffer = new Stack<UndoableAction>();
		startingHandSize = SettingsDialog.getAsInt(SettingsDialog.HAND_SIZE);
		selectedCards = new ArrayList<Card>();
		selectedSource = null;
		selectedTable = null;
		opening = false;
		
		listTabs = new JTabbedPane(SwingConstants.TOP);
		add(listTabs, BorderLayout.CENTER);

		JPanel mainPanel = new JPanel(new BorderLayout());
		
		deck.model = new CardTableModel(this, deck.current, SettingsDialog.getAsCharacteristics(SettingsDialog.EDITOR_COLUMNS));
		deck.table = new CardTable(deck.model);
		deck.table.setStripeColor(SettingsDialog.getAsColor(SettingsDialog.EDITOR_STRIPE));
		// When a card is selected in the master list table, select it for adding
		deck.table.getSelectionModel().addListSelectionListener((e) -> { 
			if (!e.getValueIsAdjusting())
			{
				ListSelectionModel lsm = (ListSelectionModel)e.getSource();
				if (!lsm.isSelectionEmpty())
				{
					clearTableSelections(deck.table);
					parent.clearSelectedCards();
					setSelectedSource(deck.table, deck.current);
					if (hasSelectedCards())
						parent.selectCard(getSelectedCards().get(0));
				}
			}
		});
		for (int i = 0; i < deck.table.getColumnCount(); i++)
			if (deck.model.isCellEditable(0, i))
				deck.table.getColumn(deck.model.getColumnName(i)).setCellEditor(CardTable.createCellEditor(this, deck.model.getColumnData(i)));
		deck.table.setTransferHandler(new EditorTableTransferHandler(true));
		deck.table.setDragEnabled(true);
		deck.table.setDropMode(DropMode.ON);
		
		JScrollPane mainDeckPane = new JScrollPane(deck.table);
		mainDeckPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		JPanel mainDeckPanel = new JPanel(new BorderLayout());
		mainDeckPanel.add(mainDeckPane, BorderLayout.CENTER);

		VerticalButtonList deckButtons = new VerticalButtonList("+", String.valueOf(UnicodeSymbols.MINUS), "X");
		deckButtons.get("+").addActionListener((e) -> addSelectedCards(1, true));
		deckButtons.get(String.valueOf(UnicodeSymbols.MINUS)).addActionListener((e) -> removeSelectedCards(1, true));
		deckButtons.get("X").addActionListener((e) -> removeSelectedCards(Integer.MAX_VALUE, true));
		mainDeckPanel.add(deckButtons, BorderLayout.WEST);
		mainPanel.add(mainDeckPanel, BorderLayout.CENTER);
		
		JPanel sideboardPanel = new JPanel(new BorderLayout());
		mainPanel.add(sideboardPanel, BorderLayout.SOUTH);
		
		JPanel showHidePanel = new JPanel(new BorderLayout());
		JLabel showHideButton = new JLabel(String.join("", Collections.nCopies(3, String.valueOf(UnicodeSymbols.DOWN_TRIANGLE))));
		showHideButton.setHorizontalAlignment(JLabel.CENTER);
		showHideButton.setFont(showHideButton.getFont().deriveFont(showHideButton.getFont().getSize()*2F/3F));
		showHidePanel.add(showHideButton, BorderLayout.CENTER);
		sideboardPanel.add(showHidePanel, BorderLayout.NORTH);
		
		VerticalButtonList sideboardButtons = new VerticalButtonList("+", String.valueOf(UnicodeSymbols.MINUS), "X");
		sideboardButtons.get("+").addActionListener((e) -> addSelectedCards(1, false));
		sideboardButtons.get(String.valueOf(UnicodeSymbols.MINUS)).addActionListener((e) -> removeSelectedCards(1, false));
		sideboardButtons.get("X").addActionListener((e) -> removeSelectedCards(Integer.MAX_VALUE, false));
		sideboardPanel.add(sideboardButtons, BorderLayout.WEST);
		
		sideboard.model = new CardTableModel(this, sideboard.current, SettingsDialog.getAsCharacteristics(SettingsDialog.EDITOR_COLUMNS));
		sideboard.table = new CardTable(sideboard.model)
		{
			@Override
			public Dimension getPreferredScrollableViewportSize()
			{
				Dimension s = super.getPreferredScrollableViewportSize();
				return new Dimension(s.width, getRowHeight()*5);
			}
		};
		sideboard.table.setStripeColor(SettingsDialog.getAsColor(SettingsDialog.EDITOR_STRIPE));
		// When a card is selected in the sideboard table, select it for adding
		sideboard.table.getSelectionModel().addListSelectionListener((e) -> { 
			if (!e.getValueIsAdjusting())
			{
				ListSelectionModel lsm = (ListSelectionModel)e.getSource();
				if (!lsm.isSelectionEmpty())
				{
					clearTableSelections(sideboard.table);
					parent.clearSelectedCards();
					setSelectedSource(sideboard.table, sideboard.current);
					if (hasSelectedCards())
						parent.selectCard(getSelectedCards().get(0));
				}
			}
		});
		for (int i = 0; i < sideboard.table.getColumnCount(); i++)
			if (sideboard.model.isCellEditable(0, i))
				sideboard.table.getColumn(sideboard.model.getColumnName(i)).setCellEditor(CardTable.createCellEditor(this, sideboard.model.getColumnData(i)));
		sideboard.table.setTransferHandler(new EditorTableTransferHandler(false));
		sideboard.table.setDragEnabled(true);
		sideboard.table.setDropMode(DropMode.ON);
		
		JScrollPane sideboardPane = new JScrollPane(sideboard.table);
		sideboardPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		sideboardPanel.add(sideboardPane, BorderLayout.CENTER);
		
		showHideButton.addMouseListener(MouseListenerFactory.createPressListener((e) -> {
			sideboardPane.setVisible(!sideboardPane.isVisible());
			sideboardButtons.setVisible(sideboardPane.isVisible());
			showHideButton.setText(String.join("", Collections.nCopies(3, String.valueOf(sideboardPane.isVisible() ? UnicodeSymbols.DOWN_TRIANGLE : UnicodeSymbols.UP_TRIANGLE))));
		}));
		
		listTabs.addTab("Cards", mainPanel);
		
		// Main table popup menu
		JPopupMenu tableMenu = new JPopupMenu();
		deck.table.addMouseListener(new TableMouseAdapter(deck.table, tableMenu));
		
		// Add/remove cards
		CardMenuItems tableMenuCardItems = new CardMenuItems(this,
				(n) -> addSelectedCards(n, true),
				() -> {
					for (Card c: getSelectedCards())
						addCard(c, 4 - deck.current.getData(c).count(), true);
					},
				(n) -> removeSelectedCards(n, true));
		tableMenu.add(tableMenuCardItems.get(CardMenuItems.ADD_SINGLE));
		tableMenu.add(tableMenuCardItems.get(CardMenuItems.FILL_PLAYSET));
		tableMenu.add(tableMenuCardItems.get(CardMenuItems.ADD_N));
		tableMenu.add(new JSeparator());
		tableMenu.add(tableMenuCardItems.get(CardMenuItems.REMOVE_SINGLE));
		tableMenu.add(tableMenuCardItems.get(CardMenuItems.REMOVE_ALL));
		tableMenu.add(tableMenuCardItems.get(CardMenuItems.REMOVE_N));
		tableMenu.add(new JSeparator());
		
		// Move cards to sideboard
		JMenuItem moveToSideboardItem = new JMenuItem("Move to Sideboard");
		moveToSideboardItem.addActionListener((e) -> {
			List<Card> selected = getSelectedCards();
			removeCards(selected, 1, true);
			addCards(selected, 1, false);
		});
		tableMenu.add(moveToSideboardItem);
		JMenuItem moveAllToSideboardItem = new JMenuItem("Move All to Sideboard");
		moveAllToSideboardItem.addActionListener((e) -> {
			for (Card c: getSelectedCards())
			{
				int n = deck.current.getData(c).count();
				removeCard(c, n, true);
				addCard(c, n, false);
			}
		});
		tableMenu.add(moveAllToSideboardItem);
		tableMenu.add(new JSeparator());
		
		// Quick edit categories
		JMenu addToCategoryMenu = new JMenu("Include in");
		tableMenu.add(addToCategoryMenu);
		JMenu removeFromCategoryMenu = new JMenu("Exclude from");
		tableMenu.add(removeFromCategoryMenu);
		
		// Edit categories item
		JMenuItem editCategoriesItem = new JMenuItem("Edit Categories...");
		editCategoriesItem.addActionListener((e) -> {
			IncludeExcludePanel iePanel = new IncludeExcludePanel(deck.current.categories().stream().sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName())).collect(Collectors.toList()), getSelectedCards());
			if (JOptionPane.showConfirmDialog(this, new JScrollPane(iePanel), "Set Categories", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
				editInclusion(iePanel.getIncluded(), iePanel.getExcluded());
		});
		tableMenu.add(editCategoriesItem);
		
		JSeparator categoriesSeparator = new JSeparator();
		tableMenu.add(categoriesSeparator);
		
		// Edit card tags item
		JMenuItem editTagsItem = new JMenuItem("Edit Tags...");
		editTagsItem.addActionListener((e) -> parent.editTags(getSelectedCards()));
		tableMenu.add(editTagsItem);
		
		tableMenu.addPopupMenuListener(new TableCategoriesPopupListener(addToCategoryMenu, removeFromCategoryMenu,
				editCategoriesItem, categoriesSeparator, deck.table));
		
		// Sideboard table popup menu
		JPopupMenu sideboardMenu = new JPopupMenu();
		sideboard.table.addMouseListener(new TableMouseAdapter(sideboard.table, sideboardMenu));
		
		// Add/remove cards from sideboard
		CardMenuItems sideboardMenuCardItems = new CardMenuItems(this,
				(n) -> addSelectedCards(n, false),
				() -> {
					for (Card c: getSelectedCards())
						addCard(c, 4 - sideboard.current.getData(c).count(), false);
					},
				(n) -> removeSelectedCards(n, false));
		sideboardMenu.add(sideboardMenuCardItems.get(CardMenuItems.ADD_SINGLE));
		sideboardMenu.add(sideboardMenuCardItems.get(CardMenuItems.FILL_PLAYSET));
		sideboardMenu.add(sideboardMenuCardItems.get(CardMenuItems.ADD_N));
		sideboardMenu.add(new JSeparator());
		sideboardMenu.add(sideboardMenuCardItems.get(CardMenuItems.REMOVE_SINGLE));
		sideboardMenu.add(sideboardMenuCardItems.get(CardMenuItems.REMOVE_ALL));
		sideboardMenu.add(sideboardMenuCardItems.get(CardMenuItems.REMOVE_N));
		sideboardMenu.add(new JSeparator());
		
		// Move cards to main deck
		JMenuItem moveToMainItem = new JMenuItem("Move to Main Deck");
		moveToMainItem.addActionListener((e) -> {
			List<Card> selected = getSelectedCards();
			removeCards(selected, 1, false);
			addCards(selected, 1, true);
		});
		sideboardMenu.add(moveToMainItem);
		JMenuItem moveAllToMainItem = new JMenuItem("Move All to Main Deck");
		moveAllToMainItem.addActionListener((e) -> {
			for (Card c: getSelectedCards())
			{
				int n = sideboard.current.getData(c).count();
				removeCard(c, n, false);
				addCard(c, n, true);
			}
		});
		sideboardMenu.add(moveAllToMainItem);
		sideboardMenu.add(new JSeparator());
		
		// Edit card tags item in sideboard
		JMenuItem sBeditTagsItem = new JMenuItem("Edit Tags...");
		sBeditTagsItem.addActionListener((e) -> parent.editTags(getSelectedCards()));
		sideboardMenu.add(sBeditTagsItem);
		
		// Panel containing categories
		JPanel categoriesPanel = new JPanel(new BorderLayout());
		JPanel categoriesMainPanel = new JPanel(new BorderLayout());
		categoriesPanel.add(categoriesMainPanel, BorderLayout.CENTER);
		listTabs.addTab("Categories", categoriesPanel);

		// Panel containing components above the category panel
		JPanel categoryHeaderPanel = new JPanel();
		categoryHeaderPanel.setLayout(new BoxLayout(categoryHeaderPanel, BoxLayout.X_AXIS));
		categoriesMainPanel.add(categoryHeaderPanel, BorderLayout.NORTH);
		
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
		categoriesMainPanel.add(new JScrollPane(categoriesSuperContainer, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
		
		VerticalButtonList categoryButtons = new VerticalButtonList("+", String.valueOf(UnicodeSymbols.MINUS), "X");
		categoryButtons.get("+").addActionListener((e) -> addSelectedCards(1, true));
		categoryButtons.get(String.valueOf(UnicodeSymbols.MINUS)).addActionListener((e) -> removeSelectedCards(1, true));
		categoryButtons.get("X").addActionListener((e) -> removeSelectedCards(Integer.MAX_VALUE, true));
		categoriesPanel.add(categoryButtons, BorderLayout.WEST);
		
		// Sample hands
		JPanel handPanel = new JPanel(new BorderLayout());
		
		// Table showing the cards in hand
		hand = new Hand(deck.current);
		
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
			if (hand.size() < deck.current.total())
			{
				hand.draw();	
				CardImagePanel panel = new CardImagePanel();
				panel.setBackground(SettingsDialog.getAsColor(SettingsDialog.HAND_BGCOLOR));
				imagePanel.add(panel);
				panel.setCard(hand.get(hand.size() - 1));
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
			
			CardTableModel excludeTableModel = new CardTableModel(deck.current, Arrays.asList(CardData.NAME, CardData.COUNT));
			CardTable excludeTable = new CardTable(excludeTableModel);
			excludeTable.setStripeColor(SettingsDialog.getAsColor(SettingsDialog.EDITOR_STRIPE));
			excludePanel.add(new JScrollPane(excludeTable));
			
			addExclusionButton.addActionListener((a) -> {
				for (Card c: Arrays.stream(excludeTable.getSelectedRows()).mapToObj((r) -> deck.current.get(excludeTable.convertRowIndexToModel(r))).collect(Collectors.toList()))
				{
					int n = 0;
					for (int i = 0; i < excludeModel.size(); i++)
						if (excludeModel.elementAt(i).equals(c))
							n++;
					if (n < deck.current.getData(c).count())
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
				hand.exclude(excludeModel.get(i));
		});
		handModPanel.add(excludeButton);
		
		handCalculations = new CalculateHandPanel(deck.current);
		
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
			checker.checkLegality(deck.current);
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
		
		setTransferHandler(new EditorImportHandler(true));
		
		deck.current.addDeckListener((e) -> {
			// Cards
			if (e.cardsChanged())
			{
				updateStats();
				
				if (!opening)
					parent.updateCardsInDeck();
				deck.model.fireTableDataChanged();
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
				if (deck.table.isEditing())
					deck.table.getCellEditor().cancelCellEditing();
				for (CategoryPanel c: categoryPanels)
					if (c.table.isEditing())
						c.table.getCellEditor().cancelCellEditing();
				
				hand.refresh();
				handCalculations.update();
			}
			// Categories
			if (e.categoryAdded())
			{
				CategoryPanel category = createCategoryPanel(deck.current.getCategorySpec(e.addedName()));
				categoryPanels.add(category);
				
				for (CategoryPanel c: categoryPanels)
					if (c != category)
						c.rankBox.addItem(deck.current.categories().size() - 1);
				
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
					panel.rankBox.setSelectedIndex(deck.current.getCategoryRank(panel.getCategoryName()));
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
		
		sideboard.current.addDeckListener((e) -> {
			if (e.cardsChanged())
			{
				updateStats();
				
				if (!opening)
					parent.updateCardsInDeck();
				sideboard.model.fireTableDataChanged();
				for (Card c: selectedCards)
				{
					if (getSelectedSource().contains(c))
					{
						int row = selectedTable.convertRowIndexToView(getSelectedSource().indexOf(c));
						selectedTable.addRowSelectionInterval(row, row);
					}
				}
				if (sideboard.table.isEditing())
					sideboard.table.getCellEditor().cancelCellEditing();
			}
			
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
	 * Add the given number of copies of the given card to the deck.  The current
	 * selections in the category and main tables are maintained.
	 * 
	 * @param toAdd card to add
	 * @param n number of copies to add
	 * @param main add to the main deck (true) or sideboard (false)
	 * @return true if the deck changed as a result, which is always.
	 */
	public boolean addCard(Card toAdd, int n, boolean main)
	{
		return addCards(Arrays.asList(toAdd), n, main);
	}
	
	/**
	 * Add the given number of copies of the given cards to the deck.  The current
	 * selections in the category and main tables are maintained.  Then update the
	 * undo and redo buffers.
	 * 
	 * @param toAdd cards to add
	 * @param n number of copies to add
	 * @param main add to the main deck (true) or sideboard (false)
	 * @return true if the deck changed as a result, and false otherwise, which is
	 * only true if the list is empty.
	 */
	public boolean addCards(List<Card> toAdd, final int n, final boolean main)
	{
		final Map<Card, Integer> cards = toAdd.stream().collect(Collectors.toMap(Function.identity(), (c) -> n));
		return performAction(() -> !deleteCards(toAdd, n, main).isEmpty(), () -> insertCards(cards, main));
	}
	
	/**
	 * Add a category to the deck.
	 * 
	 * @param spec specification for the category to add
	 * @return true if the category was successfully added and false otherwise.
	 */
	public boolean addCategory(CategorySpec spec)
	{
		if (spec != null && !deck.current.containsCategory(spec.getName()))
			return performAction(() -> deleteCategory(spec), () -> insertCategory(spec));
		else
			return false;
	}
	
	/**
	 * Add the currently-selected cards from the currently-selected table to the
	 * Deck.
	 * 
	 * @param n number of each card to add
	 * @param main add to the main deck (true) or sideboard (false)
	 * @return true if the Deck was changed as a result, and false otherwise.
	 */
	public boolean addSelectedCards(int n, boolean main)
	{
		if (hasSelectedCards())
			return addCards(getSelectedCards(), n, main);
		else if (parent.getSelectedCard() != null)
			return addCard(parent.getSelectedCard(), n, main);
		else
			return false;
	}
	
	/**
	 * Set the settings of this EditorFrame
	 */
	public void applySettings()
	{
		List<CardData> columns = SettingsDialog.getAsCharacteristics(SettingsDialog.EDITOR_COLUMNS);
		Color stripe = SettingsDialog.getAsColor(SettingsDialog.EDITOR_STRIPE);
		deck.model.setColumns(columns);
		deck.table.setStripeColor(stripe);
		for (int i = 0; i < deck.table.getColumnCount(); i++)
			if (deck.model.isCellEditable(0, i))
				deck.table.getColumn(deck.model.getColumnName(i)).setCellEditor(CardTable.createCellEditor(this, deck.model.getColumnData(i)));
		for (CategoryPanel category: categoryPanels)
			category.applySettings(this);
		startingHandSize = SettingsDialog.getAsInt(SettingsDialog.HAND_SIZE);
		update();
	}
	
	/**
	 * Clear the selection in all of the tables in this EditorFrame except
	 * for the given one.
	 * 
	 * @param except table to not clear
	 */
	public void clearTableSelections(CardTable except)
	{
		if (deck.table != except)
			deck.table.clearSelection();
		if (sideboard.table != except)
			sideboard.table.clearSelection();
		for (CategoryPanel c: categoryPanels)
			if (c.table != except)
				c.table.clearSelection();
	}

	/**
	 * If the deck has unsaved changes, allow the user to choose to save it or keep the
	 * frame open.  If the user saves or declines to save, close the frame.
	 * 
	 * @return true if the frame was closed and false otherwise.
	 */
	public boolean close()
	{
		if (unsaved)
		{
			String msg = "Deck \"" + getTitle().substring(0, getTitle().length() - 2) + "\" has unsaved changes.  Save?";
			switch (JOptionPane.showConfirmDialog(this, msg, "Unsaved Changes", JOptionPane.YES_NO_CANCEL_OPTION))
			{
			case JOptionPane.YES_OPTION:
				parent.save(this);
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
	 * Open the dialog to create a new specification for a deck category.
	 * 
	 * @return the {@link CategorySpec} created by the dialog, or null if it was
	 * canceled.
	 */
	public CategorySpec createCategory()
	{
		CategorySpec spec = null;
		do
		{
			spec = CategoryEditorPanel.showCategoryEditor(this, spec);
			if (spec != null && deck.current.containsCategory(spec.getName()))
				JOptionPane.showMessageDialog(this, "Categories must have unique names.", "Error", JOptionPane.ERROR_MESSAGE);
		} while (spec != null && deck.current.containsCategory(spec.getName()));
		return spec;
	}
	
	/**
	 * Create a new {@link CategoryPanel} out of the given specification.
	 * 
	 * @param spec specification for the category of the new {@link CategoryPanel}
	 * @return the new {@link CategoryPanel}.
	 */
	private CategoryPanel createCategoryPanel(CategorySpec spec)
	{
		CategoryPanel newCategory = new CategoryPanel(deck.current, spec.getName(), this);
		// When a card is selected in a category, the others should deselect
		newCategory.table.getSelectionModel().addListSelectionListener((e) -> {
			ListSelectionModel lsm = (ListSelectionModel)e.getSource();
			if (!lsm.isSelectionEmpty())
			{
				if (!e.getValueIsAdjusting())
				{
					clearTableSelections(newCategory.table);
					parent.clearSelectedCards();
					setSelectedSource(newCategory.table, deck.current.getCategoryList(spec.getName()));
					if (hasSelectedCards())
						parent.selectCard(getSelectedCards().get(0));
				}
			}
		});
		// Add the behavior for the edit category button
		newCategory.rankBox.addActionListener((e) -> {
			if (newCategory.rankBox.isPopupVisible())
			{
				int oldRank = deck.current.getCategoryRank(newCategory.getCategoryName());
				int newRank = newCategory.rankBox.getSelectedIndex();
				performAction(() -> deck.current.swapCategoryRanks(newCategory.getCategoryName(), oldRank),
						() -> deck.current.swapCategoryRanks(newCategory.getCategoryName(), newRank));
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
		
		newCategory.table.setTransferHandler(new EditorTableTransferHandler(true));
		newCategory.table.setDragEnabled(true);
		
		// Add the behavior for clicking on the category's table
		// Table popup menu
		JPopupMenu tableMenu = new JPopupMenu();
		newCategory.table.addMouseListener(new TableMouseAdapter(newCategory.table, tableMenu));
		
		CardMenuItems tableMenuCardItems = new CardMenuItems(this,
				(n) -> addSelectedCards(n, true),
				() -> {
					for (Card c: newCategory.getSelectedCards())
						addCard(c, 4 - deck.current.getData(c).count(), true);
					},
				(n) -> removeSelectedCards(n, true));
		tableMenu.add(tableMenuCardItems.get(CardMenuItems.ADD_SINGLE));
		tableMenu.add(tableMenuCardItems.get(CardMenuItems.FILL_PLAYSET));
		tableMenu.add(tableMenuCardItems.get(CardMenuItems.ADD_N));
		tableMenu.add(new JSeparator());
		tableMenu.add(tableMenuCardItems.get(CardMenuItems.REMOVE_SINGLE));
		tableMenu.add(tableMenuCardItems.get(CardMenuItems.REMOVE_ALL));
		tableMenu.add(tableMenuCardItems.get(CardMenuItems.REMOVE_N));
		tableMenu.add(new JSeparator());
		
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
			IncludeExcludePanel iePanel = new IncludeExcludePanel(deck.current.categories().stream().sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName())).collect(Collectors.toList()), getSelectedCards());
			if (JOptionPane.showConfirmDialog(this, new JScrollPane(iePanel), "Set Categories", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
				editInclusion(iePanel.getIncluded(), iePanel.getExcluded());
		});
		tableMenu.add(editCategoriesItem);
		
		tableMenu.add(new JSeparator());
		
		// Edit tags item
		JMenuItem editTagsItem = new JMenuItem("Edit Tags...");
		editTagsItem.addActionListener((e) -> parent.editTags(getSelectedCards()));
		tableMenu.add(editTagsItem);
		
		tableMenu.addPopupMenuListener(new TableCategoriesPopupListener(addToCategoryMenu, removeFromCategoryMenu,
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
	 * Get this EditorFrame's deck.
	 * 
	 * @return the deck.
	 */
	public Deck deck()
	{
		return deck.current;
	}

	/**
	 * Get the file name of the deck.
	 * 
	 * @return the name of the deck being edited (its file name).
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
	 * Remove a number of copies of the specified Cards from the deck.  The current selections
	 * for any cards remaining in them in the category and main tables are maintained.  Don't
	 * update the undo buffer.
	 * 
	 * @param toRemove list of cards to remove
	 * @param n number of copies to remove
	 * @param main remove from the main deck (true) or to the sideboard (false)
	 * @return a map containing the Cards removed and the number of each that was removed.
	 */
	private Map<Card, Integer> deleteCards(Collection<Card> toRemove, int n, boolean main)
	{
		saveSelectedCards();
		Map<Card, Integer> removed = new HashMap<Card, Integer>();
		for (Card c: toRemove)
			removed.put(c, (main ? deck : sideboard).current.remove(c, n));
		return removed;
	}
	
	/**
	 * Delete the category with the given specification from the deck
	 * and remove its panel.
	 * 
	 * @param category specification of the category to remove
	 * @return true if the category was successfully removed, and false
	 * otherwise (such as if there was no such category).
	 */
	private boolean deleteCategory(CategorySpec category)
	{
		CategoryPanel panel = getCategory(category.getName());
		return panel != null && deck.current.remove(category.getName());
	}
	
	/**
	 * Change the given category so it has the parameters of the other given category.
	 * The category to edit is edited in place, while the one representing new values
	 * is unchanged.
	 * 
	 * @param toEdit category to edit
	 * @param newValues new values for the category
	 */
	private void editCategory(CategorySpec toEdit, CategorySpec newValues)
	{
		CategorySpec oldSpec = new CategorySpec(toEdit);
		performAction(() -> toEdit.copy(oldSpec), () -> toEdit.copy(newValues));
	}
	
	/**
	 * Open the category dialog to edit the category with the given
	 * name, if there is one, and then update the undo buffer.
	 * 
	 * @param name name of the category to edit
	 */
	public void editCategory(String name)
	{
		CategorySpec toEdit = deck.current.getCategorySpec(name);
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
	 * Change inclusion of cards in categories according to the given maps.
	 * 
	 * @param included map of cards onto the set of categories they should become included in
	 * @param excluded map of cards onto the set of categories they should become excluded from
	 */
	public void editInclusion(Map<Card, Set<CategorySpec>> included, Map<Card, Set<CategorySpec>> excluded)
	{
		performAction(() -> {
			boolean changed = false;
			for (Card card: included.keySet())
				for (CategorySpec category: included.get(card))
					changed |= category.exclude(card);
			for (Card card: excluded.keySet())
				for (CategorySpec category: excluded.get(card))
					changed |= category.include(card);
			return changed;
		}, () -> {
			boolean changed = false;
			for (Card card: included.keySet())
				for (CategorySpec category: included.get(card))
					changed |= category.include(card);
			for (Card card: excluded.keySet())
				for (CategorySpec category: excluded.get(card))
					changed |= category.exclude(card);
			return changed;
		});
	}
	
	/**
	 * Get the {@link File} for the deck.
	 * 
	 * @return the {@link File} containing the deck being edited.
	 */
	public File file()
	{
		return file;
	}

	/**
	 * Get the card at the given index in the given table.
	 * 
	 * @param t table to get the card from
	 * @param index index into the given table to get a card from
	 * @return the card in the deck at the given index in the given table, if the table is in this EditorFrame.
	 */
	public Card getCardAt(CardTable t, int index)
	{
		if (t == deck.table)
			return deck.current.get(deck.table.convertRowIndexToModel(index));
		else
		{
			for (CategoryPanel panel: categoryPanels)
				if (t == panel.table)
					return deck.current.getCategoryList(panel.getCategoryName()).get(panel.table.convertRowIndexToModel(index));
			throw new IllegalArgumentException("Table not in deck " + deckName());
		}
	}
	
	/**
	 * Get the panel for the category with the specified name in the deck.
	 * 
	 * @param name name of the category to search for
	 * @return the panel for the category with the specified name, or null if there is none.
	 */
	private CategoryPanel getCategory(String name)
	{
		for (CategoryPanel category: categoryPanels)
			if (category.getCategoryName().equals(name))
				return category;
		return null;
	}
	
	/**
	 * Get the currently-selected cards.
	 * 
	 * @return The list of cards corresponding to the selection in the table that is selected.
	 */
	public List<Card> getSelectedCards()
	{
		return Arrays.stream(selectedTable.getSelectedRows())
				  .mapToObj((r) -> selectedSource.get(selectedTable.convertRowIndexToModel(r)))
				  .collect(Collectors.toList());
	}
	
	/**
	 * Get the {@link CardList} corresponding to the table with the current selection
	 * in it.
	 * 
	 * @return the {@link CardList} containing the selected Cards.
	 */
	public CardList getSelectedSource()
	{
		return selectedSource;
	}
	
	/**
	 * Check if the deck has been saved since its last change.
	 * 
	 * @return true if there are unsaved changes in the deck, and false otherwise.
	 */
	public boolean getUnsaved()
	{
		return unsaved;
	}
	
	/**
	 * Check if there is a table with a selection in it.
	 * 
	 * @return true if there is a selected table and it has a selection, and false otherwise.
	 */
	public boolean hasSelectedCards()
	{
		return selectedTable != null && selectedTable.getSelectedRowCount() > 0;
	}
	
	/**
	 * Add cards to the deck, maintaining the selection in the currently-selected
	 * table.
	 * 
	 * @param cards map containing cards and amounts to add
	 * @param main add cards to the main deck (true) or sideboard (false)
	 * @return true if the deck changed as a result, and false otherwise, which is
	 * only true if the list is empty.
	 */
	private boolean insertCards(Map<Card, Integer> cards, boolean main)
	{
		saveSelectedCards();
		cards.values().removeAll(Arrays.asList(0));
		if (cards.isEmpty())
			return false;
		else
		{
			for (Map.Entry<Card, Integer> entry: cards.entrySet())
				(main ? deck : sideboard).current.add(entry.getKey(), entry.getValue());
			return true;
		}
	}
	
	/**
	 * Add a category to the deck.
	 * 
	 * @param spec specification for the new category
	 * @return true if a category was created, and false otherwise
	 * (such as if there was one with the same name already).
	 */
	private boolean insertCategory(CategorySpec spec)
	{
		if (!deck.current.containsCategory(spec.getName()))
		{
			deck.current.addCategory(spec);
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
	 * Perform an action.  Then, if it succeeds, push it onto the undo buffer
	 * and clear the redo buffer.
	 * 
	 * @param undo what to do to undo the action
	 * @param redo the action to perform
	 * @return true if the action succeeded, and false otherwise.
	 */
	private boolean performAction(BooleanSupplier undo, BooleanSupplier redo)
	{
		undoBuffer.push(new UndoableAction()
		{
			@Override
			public boolean redo()
			{
				return redo.getAsBoolean();
			}

			@Override
			public boolean undo()
			{
				return undo.getAsBoolean();
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
	 * Remove a number of copies of the specified Card from the deck.  The current selections
	 * for any cards remaining in the category and main tables are maintained.  Then update the
	 * undo buffer.
	 * 
	 * @param toRemove card to remove
	 * @param n number of copies to remove
	 * @param main remove from the main deck (true) or sideboard (false)
	 * @return true if the deck was changed as a result, and false otherwise.
	 */
	public boolean removeCard(Card toRemove, int n, boolean main)
	{
		return removeCards(Arrays.asList(toRemove), n, main);
	}
	
	/**
	 * Remove a number of copies of the specified cards from the deck.  The current selections
	 * for any cards remaining in the category and main tables are maintained.  Then update the
	 * undo buffer.
	 * 
	 * @param toRemove list of cards to remove
	 * @param n number of copies to remove
	 * @param main add to the main deck (true) or sideboard (false)
	 * @return true if the deck was changed as a result, and false otherwise.
	 */
	public boolean removeCards(Collection<Card> toRemove, final int n, final boolean main)
	{
		CardList list = (main ? deck : sideboard).current;
		Map<Card, Integer> removed = toRemove.stream().filter(list::contains).collect(Collectors.toMap(Function.identity(), (c) -> Math.min(n, list.getData(c).count())));
		return performAction(() -> insertCards(removed, main), () -> !deleteCards(toRemove, n, main).isEmpty());
	}

	/**
	 * If the given category exists in this EditorFrame's deck, remove it from
	 * the deck.
	 * 
	 * @param category panel representing the category to be removed
	 * @return true if the category was successfully removed, and false otherwise.
	 */
	public boolean removeCategory(CategorySpec category)
	{
		return deck.current.containsCategory(category.getName())
				&& performAction(() -> insertCategory(category), () -> deleteCategory(category));
	}

	/**
	 * If a category with the given name exists in the deck, remove it
	 * and then update the undo and redo buffers.
	 * 
	 * @param name name of the category to look for
	 * @return the specification of the category that was removed, or null if no
	 * category was removed
	 */
	public CategorySpec removeCategory(String name)
	{
		CategoryPanel removed = getCategory(name);
		if (removed != null)
		{
			CategorySpec spec = deck.current.getCategorySpec(name);
			removeCategory(spec);
			return spec;
		}
		else
			return null;
	}
	
	/**
	 * Remove the currently-selected cards in the currently-selected table from
	 * the deck.
	 * 
	 * @param n number of copies of the cards to remove
	 * @param main remove from the main deck (true) or sideboard (false)
	 * @return true if the deck changed as a result, and false otherwise.
	 */
	public boolean removeSelectedCards(int n, boolean main)
	{
		if (hasSelectedCards())
			return removeCards(getSelectedCards(), n, main);
		else if (parent.getSelectedCard() != null)
			return removeCard(parent.getSelectedCard(), n, main);
		else
			return false;
	}

	/**
	 * Save the deck to the current file.
	 * 
	 * @return true if the file was successfully saved, and false otherwise.
	 */
	public boolean save()
	{
		return file == null ? false : save(file);
	}

	/**
	 * Save the deck to the given file (like Save As).
	 * 
	 * @param f file to save to
	 * @return true if the file was successfully saved, and false otherwise.
	 */
	public boolean save(File f)
	{
		try (PrintWriter wr = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f, false), "UTF8")))
		{
			wr.println(String.valueOf(deck.current.size()));
			for (Card c: deck.current)
				wr.println(c.id() + "\t" + deck.current.getData(c).count() + "\t" + Deck.DATE_FORMAT.format(deck.current.getData(c).dateAdded()));
			wr.println(String.valueOf(deck.current.categories().size()));
			for (CategorySpec c: deck.current.categories())
				wr.println(c.toString());
			wr.println(String.valueOf(sideboard.current.size()));
			for (Card c: sideboard.current)
				wr.println(c.id() + "\t" + sideboard.current.getData(c).count() + "\t" + Deck.DATE_FORMAT.format(sideboard.current.getData(c).dateAdded()));
			
			String changes = "";
			for (Card c: deck.original)
			{
				int had = deck.original.contains(c) ? deck.original.getData(c).count() : 0;
				int has = deck.current.contains(c) ? deck.current.getData(c).count() : 0;
				if (has < had)
					changes += ("-" + (had - has) + "x " + c.unifiedName() + " (" + c.expansion().name + ")\n");
			}
			for (Card c: deck.current)
			{
				int had = deck.original.contains(c) ? deck.original.getData(c).count() : 0;
				int has = deck.current.contains(c) ? deck.current.getData(c).count() : 0;
				if (had < has)
					changes += ("+" + (has - had) + "x " + c.unifiedName() + " (" + c.expansion().name + ")\n");
			}
			if (!changes.isEmpty())
			{
				SimpleDateFormat format = new SimpleDateFormat("MMMM d, yyyy HH:mm:ss");
				changelogArea.append("~~~~~" + format.format(new Date()) + "~~~~~\n");
				changelogArea.append(changes + "\n");
			}
			wr.print(changelogArea.getText());
			
			deck.original = new Deck();
			deck.original.addAll(deck.current);
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
	 * Save the list of selected cards from the active table for later selection
	 * restoration.
	 */
	private void saveSelectedCards()
	{
		if (selectedTable != null)
			selectedCards = Arrays.stream(selectedTable.getSelectedRows())
					.mapToObj((r) -> selectedSource.get(selectedTable.convertRowIndexToModel(r)))
					.collect(Collectors.toList());
		else
			selectedCards = new ArrayList<Card>();
	}
	
	/**
	 * Set the number of copies of the given card if the deck contains it.  Otherwise
	 * add the card to the deck.
	 * 
	 * @param c card to set (or add if it isn't present)
	 * @param n number of copies to set to (or add if the card isn't present)
	 * @param main set the count for the main deck (true) or sideboard (false)
	 */
	public void setCardCount(Card c, int n, boolean main)
	{
		CardList list = (main ? deck : sideboard).current;
		if (list.contains(c))
		{
			if (n != list.getData(c).count())
			{
				int old = list.getData(c).count();
				performAction(() -> list.set(c, old), () -> list.set(c, n));
			}
		}
		else
			addCard(c, n, main);
	}
	
	/**
	 * Set the number of copies of the given card if the deck contains it.  Otherwise
	 * add the card to the deck.
	 * 
	 * @param c card to set (or add if it isn't present)
	 * @param n number of copies to set to (or add if the card isn't present)
	 * @param list list to add cards to (deck or table)
	 */
	public void setCardCount(Card c, int n, CardList list)
	{
		if (list != deck.current && list != sideboard.current)
			setCardCount(c, n, list == deck.current);
		else
			throw new IllegalArgumentException("Can't add cards to that list");
	}
	
	/**
	 * Change the file this EditorFrame is associated with.  If the file has
	 * not been saved, an error will be thrown instead.
	 * 
	 * @param f file to associate with
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
	 * Set the background color for the sample hand panel.
	 * 
	 * @param col new background color for the sample hand panel.
	 */
	public void setHandBackground(Color col)
	{
		imagePanel.setBackground(col);
		for (Component c: imagePanel.getComponents())
			c.setBackground(col);
		imagePane.getViewport().setBackground(col);
	}
	
	/**
	 * Set the table and {@link CardList} that contains the current selection.
	 * 
	 * @param table table to select
	 * @param source {@link CardList} to select
	 */
	public void setSelectedSource(CardTable table, CardList source)
	{
		selectedTable = table;
		selectedSource = source;
	}
	
	/**
	 * Get this EditorFrame's sideboard.
	 * 
	 * @return the sideboard
	 */
	public CardList sideboard()
	{
		return sideboard.current;
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
	 * Update the categories combo box with all of the current categories.
	 */
	public void updateCategoryPanel()
	{
		categoriesContainer.removeAll();
		switchCategoryModel.removeAllElements();
		
		if (deck.current.categories().isEmpty())
			switchCategoryBox.setEnabled(false);
		else
		{
			switchCategoryBox.setEnabled(true);
			List<CategorySpec> categories = new ArrayList<CategorySpec>(deck.current.categories());
			categories.sort((a, b) -> sortCategoriesBox.getItemAt(sortCategoriesBox.getSelectedIndex()).compare(deck.current, a, b));
			
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
		countLabel.setText("Total cards: " + deck.current.total());
		landLabel.setText("Lands: " + deck.current.land());
		nonlandLabel.setText("Nonlands: " + deck.current.nonland());

		double avgCMC = 0.0;
		for (Card card: deck.current)
			if (!card.typeContains("land"))
				avgCMC += card.minCmc()*deck.current.getData(card).count();
		if (deck.current.nonland() > 0)
			avgCMC /= deck.current.nonland();
		if ((int)avgCMC == avgCMC)
			avgCMCLabel.setText("Average CMC: " + (int)avgCMC);
		else
			avgCMCLabel.setText(String.format("Average CMC: %.2f", avgCMC));
		
		double medCMC = 0.0;
		List<Double> cmc = new ArrayList<Double>();
		for (Card card: deck.current)
			if (!card.typeContains("land"))
				for (int i = 0; i < deck.current.getData(card).count(); i++)
					cmc.add(card.minCmc());
		Collections.sort(cmc);
		if (!cmc.isEmpty())
		{
			if (cmc.size()%2 == 0)
				medCMC = (cmc.get(cmc.size()/2 - 1) + cmc.get(cmc.size()/2))/2;
			else
				medCMC = cmc.get(cmc.size()/2);
		}
		if ((int)medCMC == medCMC)
			medCMCLabel.setText("Median CMC: " + (int)medCMC);
		else
			medCMCLabel.setText(String.format("Median CMC: %.1f", medCMC));
	}
}
