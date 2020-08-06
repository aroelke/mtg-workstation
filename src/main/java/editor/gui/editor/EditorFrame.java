package editor.gui.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.OverlayLayout;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.AbstractTableModel;

import editor.collection.CardList;
import editor.collection.LegalityChecker;
import editor.collection.deck.CategorySpec;
import editor.collection.deck.Deck;
import editor.collection.deck.Hand;
import editor.collection.export.CardListFormat;
import editor.database.card.Card;
import editor.gui.CardTagPanel;
import editor.gui.MainFrame;
import editor.gui.TableSelectionListener;
import editor.gui.ccp.CCPItems;
import editor.gui.ccp.data.CategoryTransferData;
import editor.gui.ccp.data.DataFlavors;
import editor.gui.ccp.handler.CategoryListTransferHandler;
import editor.gui.ccp.handler.EditorTableTransferHandler;
import editor.gui.ccp.handler.EditorFrameTransferHandler;
import editor.gui.display.CardImagePanel;
import editor.gui.display.CardTable;
import editor.gui.display.CardTableModel;
import editor.gui.generic.CardMenuItems;
import editor.gui.generic.ChangeTitleListener;
import editor.gui.generic.ComponentUtils;
import editor.gui.generic.EditablePanel;
import editor.gui.generic.ScrollablePanel;
import editor.gui.generic.TableMouseAdapter;
import editor.gui.generic.VerticalButtonList;
import editor.gui.settings.SettingsDialog;
import editor.util.MouseListenerFactory;
import editor.util.PopupMenuListenerFactory;
import editor.util.UndoableAction;
import editor.util.UnicodeSymbols;

/**
 * This class represents an internal frame for editing a deck.  It contains a table that shows all cards
 * and their counts in the deck as well as zero or more tables for categories within it.  It can add cards
 * to a deck and add, edit, and delete categories.  It is contained within the main frame, which has the
 * inventory from which cards can be added.
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
         * Sort categories in descending alphabetical order.
         */
        Z_A("Z-A", (d) -> (a, b) -> -a.getName().compareToIgnoreCase(b.getName())),
        /**
         * Sort categories in order of increasing card count.
         */
        ASCENDING("Ascending Size", (d) -> Comparator.comparingInt((a) -> d.getCategoryList(a.getName()).total())),
        /**
         * Sort categories in order of decreasing card count.
         */
        DESCENDING("Descending Size", (d) -> (a, b) -> d.getCategoryList(b.getName()).total() - d.getCategoryList(a.getName()).total()),
        /**
         * Sort categories in order of increasing rank.
         */
        PRIORITY("Increasing Rank", (d) -> Comparator.comparingInt((a) -> d.getCategoryRank(a.getName()))),
        /**
         * Sort categories in order of decreasing rank.
         */
        REVERSE("Decreasing Rank", (d) -> (a, b) -> d.getCategoryRank(b.getName()) - d.getCategoryRank(a.getName()));

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
        CategoryOrder(String n, Function<Deck, Comparator<CategorySpec>> o)
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
         * Name of the deck, if it isn't the main deck.
         */
        public Optional<String> name;
        /**
         * Current state of the deck.
         */
        public final Deck current;
        /**
         * Original state of the deck just after loading it.
         */
        public Deck original;
        /**
         * Model backing the table.
         */
        public CardTableModel model;
        /**
         * Table displaying the deck.
         */
        public CardTable table;

        /**
         * Create a copy of a DeckData.
         * 
         * @param d DeckData to copy
         */
        public DeckData(DeckData d)
        {
            current = new Deck();
            current.addAll(d.current);
            original = new Deck();
            original.addAll(d.original);
            name = d.name;
        }

        /**
         * Create a new DeckData with the given optional name. Don't use this
         * constructor.
         * 
         * @param deck deck to use as backing data
         * @param n name of the deck if it isn't the main deck
         */
        private DeckData(Deck deck, Optional<String> n)
        {
            current = deck;
            original = new Deck();
            original.addAll(deck);
            name = n;
        }

        /**
         * Create a new DeckData to represent the main deck. Do not use this constructor
         * to create an extra list.
         * 
         * @param deck list of cards to put in the deck
         */
        public DeckData(Deck deck)
        {
            this(deck, Optional.empty());
        }

        /**
         * Create a new DeckData using the given Deck.  The original deck
         * will be a copy of it. Do not use this constructor for the main deck.
         * 
         * @param deck Deck to use as backing data
         * @param n name of the deck
         */
        public DeckData(Deck deck, String n)
        {
            this(deck, Optional.of(n));
        }

        /**
         * Create a new DeckData for a list with the given name. Do not use this
         * constructor for the main deck.
         * 
         * @param n name of the new list
         */
        public DeckData(String n)
        {
            this(new Deck(), n);
        }

        /**
         * @return A String containing the cards that are different between the current deck
         * and the original one from the last save, preceded by "-Nx" or "+Nx" to indicate
         * count changes, where N is the number of copies added or removed.
         */
        public String getChanges()
        {
            StringBuilder changes = new StringBuilder();
            for (Card c : original)
            {
                int had = original.contains(c) ? original.getEntry(c).count() : 0;
                int has = current.contains(c) ? current.getEntry(c).count() : 0;
                if (has < had)
                    changes.append("-").append(had - has).append("x ").append(c.unifiedName()).append(" (").append(c.expansion().name).append(")\n");
            }
            for (Card c : current)
            {
                int had = original.contains(c) ? original.getEntry(c).count() : 0;
                int has = current.contains(c) ? current.getEntry(c).count() : 0;
                if (had < has)
                    changes.append("+").append(has - had).append("x ").append(c.unifiedName()).append(" (").append(c.expansion().name).append(")\n");
            }
            return changes.toString();
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
         * @param sep separator between menu items
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
            if (parent.getSelectedTable().filter((f) -> f == table).isPresent())
            {
                if (parent.getSelectedCards().size() == 1)
                {
                    Card card = parent.getSelectedCards().get(0);

                    for (CategorySpec category : deck().current.categories())
                    {
                        if (!category.includes(card))
                        {
                            JMenuItem categoryItem = new JMenuItem(category.getName());
                            categoryItem.addActionListener((e2) -> includeIn(card, category));
                            addToCategoryMenu.add(categoryItem);
                        }
                    }
                    addToCategoryMenu.setVisible(addToCategoryMenu.getItemCount() > 0);

                    for (CategorySpec category : deck().current.categories())
                    {
                        if (category.includes(card))
                        {
                            JMenuItem categoryItem = new JMenuItem(category.getName());
                            categoryItem.addActionListener((e2) -> excludeFrom(card, category));
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

                editCategoriesItem.setVisible(!parent.getSelectedCards().isEmpty() && !deck().current.categories().isEmpty());

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
     * Name denoting the main deck for making modifications.
     */
    public static final int MAIN_DECK = 0;

    /**
     * Label showing the average CMC of nonland cards in the deck.
     */
    private JLabel avgCMCLabel;
    /**
     * Panel containing categories.
     */
    private Box categoriesContainer;
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
     * Panel containing components to display when there are no extra lists.
     */
    private JPanel emptyPanel;
    /**
     * Panel containing extra lists.
     */
    private JPanel extrasPanel;
    /**
     * Tabs showing extra lists.
     */
    private JTabbedPane extrasPane;
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
     * All lists in the editor. The index into this list of a card list is that card
     * list's ID. The main deck always has ID 0, and will never be null. Other lists
     * will have IDs starting from 1, and any empty spots will be indicated with a null
     * value here.
     */
    private List<DeckData> lists;
    /**
     * Tabbed pane for choosing whether to display the entire deck or the categories.
     */
    private JTabbedPane listTabs;
    /**
     * Label showing the median CMC of nonland cards in the deck.
     */
    private JLabel medCMCLabel;
    /**
     * Menu containing sideboards to move cards from the main deck to one at a time.
     */
    private JMenu moveToMenu;
    /**
     * Menu containing sideboards to move cards from the main deck to all at once.
     */
    private JMenu moveAllToMenu;
    /**
     * Label showing the total number of nonland cards in the deck.
     */
    private JLabel nonlandLabel;
    /**
     * Parent {@link MainFrame}.
     */
    private MainFrame parent;
    /**
     * Buffer containing actions to redo.  Clears whenever an action is performed
     * (but not when it is redone).
     */
    private Stack<UndoableAction<Boolean, Boolean>> redoBuffer;
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
     * Stack containing actions to undo.
     */
    private Stack<UndoableAction<Boolean, Boolean>> undoBuffer;
    /**
     * Whether or not the deck has been saved since it has last been changed.
     */
    private boolean unsaved;

    /**
     * Create a new EditorFrame to edit a deck.
     * 
     * @param p MainFrame parent of the new EditorFrame
     * @param u index of the new frame, used for initial bounds and title if it's a new deck
     * @param manager #DeckSerializer to read the loaded deck from
     */
    public EditorFrame(MainFrame p, int u, DeckSerializer manager)
    {
        super(!manager.canSaveFile() ? "Untitled " + u : manager.file().getName(), true, true, true, true);
        setBounds(((u - 1)%5)*30, ((u - 1)%5)*30, 600, 600);
        setLayout(new BorderLayout(0, 0));
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        lists = new ArrayList<>(2);
        lists.add(new DeckData(manager.deck()));

        parent = p;
        unsaved = false;
        undoBuffer = new Stack<>();
        redoBuffer = new Stack<>();
        startingHandSize = SettingsDialog.settings().editor.hand.size;
        if (manager.canSaveFile())
            setFile(manager.file());
        else
            setUnsaved();

        listTabs = new JTabbedPane(SwingConstants.TOP);
        add(listTabs, BorderLayout.CENTER);

        JPanel mainPanel = new JPanel(new BorderLayout());

        deck().model = new CardTableModel(this, deck().current, SettingsDialog.settings().editor.columns);
        deck().table = new CardTable(deck().model);
        deck().table.setStripeColor(SettingsDialog.settings().editor.stripe);

        TableSelectionListener listener = new TableSelectionListener(parent, deck().table, deck().current);
        deck().table.addMouseListener(listener);
        deck().table.getSelectionModel().addListSelectionListener(listener);
        for (int i = 0; i < deck().table.getColumnCount(); i++)
            if (deck().model.isCellEditable(0, i))
                deck().table.getColumn(deck().model.getColumnName(i)).setCellEditor(CardTable.createCellEditor(this, deck().model.getColumnData(i)));
        deck().table.setTransferHandler(new EditorTableTransferHandler(this, MAIN_DECK));
        deck().table.setDragEnabled(true);
        deck().table.setDropMode(DropMode.ON);

        JScrollPane mainDeckPane = new JScrollPane(deck().table);
        mainDeckPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        JPanel mainDeckPanel = new JPanel(new BorderLayout());
        mainDeckPanel.add(mainDeckPane, BorderLayout.CENTER);

        VerticalButtonList deckButtons = new VerticalButtonList("+", String.valueOf(UnicodeSymbols.MINUS), "X");
        deckButtons.get("+").addActionListener((e) -> {
            addCards(MAIN_DECK, parent.getSelectedCards(), 1);
        });
        deckButtons.get(String.valueOf(UnicodeSymbols.MINUS)).addActionListener((e) -> removeCards(MAIN_DECK,  parent.getSelectedCards(), 1));
        deckButtons.get("X").addActionListener((e) -> removeCards(MAIN_DECK,  parent.getSelectedCards(), parent.getSelectedCards().stream().mapToInt((c) -> deck().current.getEntry(c).count()).reduce(0, Math::max)));
        mainDeckPanel.add(deckButtons, BorderLayout.WEST);
        mainPanel.add(mainDeckPanel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel();
        southPanel.setLayout(new OverlayLayout(southPanel));
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        extrasPanel = new JPanel(new BorderLayout());
        extrasPanel.setVisible(false);
        southPanel.add(extrasPanel, BorderLayout.SOUTH);

        VerticalButtonList extrasButtons = new VerticalButtonList("+", String.valueOf(UnicodeSymbols.MINUS), "X");
        extrasButtons.get("+").addActionListener((e) -> getSelectedExtraID().ifPresent((id) -> addCards(id, parent.getSelectedCards(), 1)));
        extrasButtons.get(String.valueOf(UnicodeSymbols.MINUS)).addActionListener((e) -> {
            getSelectedExtraID().ifPresent((id) -> removeCards(id, parent.getSelectedCards(), 1));
        });
        extrasButtons.get("X").addActionListener((e) -> getSelectedExtraID().ifPresent((id) -> {
            removeCards(id, parent.getSelectedCards(), parent.getSelectedCards().stream().mapToInt((c) -> sideboard().getEntry(c).count()).reduce(0, Math::max));
        }));
        extrasPanel.add(extrasButtons, BorderLayout.WEST);

        extrasPane = new JTabbedPane();
        extrasPanel.add(extrasPane, BorderLayout.CENTER);

        emptyPanel = new JPanel(new BorderLayout());
        emptyPanel.setBorder(BorderFactory.createEtchedBorder());
        JLabel emptyLabel = new JLabel("Click to add a sideboard.");
        emptyLabel.setHorizontalAlignment(JLabel.CENTER);
        emptyPanel.add(emptyLabel, BorderLayout.CENTER);
        southPanel.add(emptyPanel);

        listTabs.addTab("Cards", mainPanel);

        // Main table popup menu
        JPopupMenu tableMenu = new JPopupMenu();
        deck().table.addMouseListener(new TableMouseAdapter(deck().table, tableMenu));

        // Cut, copy, paste
        CCPItems ccp = new CCPItems(deck().table, true);
        tableMenu.add(ccp.cut);
        tableMenu.add(ccp.copy);
        tableMenu.add(ccp.paste);
        tableMenu.add(new JSeparator());

        // Add/remove cards
        CardMenuItems tableMenuCardItems = new CardMenuItems(() -> Optional.of(this), parent::getSelectedCards, true);
        tableMenuCardItems.addAddItems(tableMenu);
        tableMenu.add(new JSeparator());
        tableMenuCardItems.addRemoveItems(tableMenu);
        tableMenu.add(new JSeparator());

        // Move cards to sideboard
        tableMenu.add(moveToMenu = new JMenu("Move to"));
        tableMenu.add(moveAllToMenu = new JMenu("Move all to"));
        JSeparator moveSeparator = new JSeparator();
        tableMenu.add(moveSeparator);

        // Quick edit categories
        JMenu addToCategoryMenu = new JMenu("Include in");
        tableMenu.add(addToCategoryMenu);
        JMenu removeFromCategoryMenu = new JMenu("Exclude from");
        tableMenu.add(removeFromCategoryMenu);

        // Edit categories item
        JMenuItem editCategoriesItem = new JMenuItem("Edit Categories...");
        editCategoriesItem.addActionListener((e) -> {
            IncludeExcludePanel iePanel = new IncludeExcludePanel(deck().current.categories().stream().sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName())).collect(Collectors.toList()), parent.getSelectedCards());
            if (JOptionPane.showConfirmDialog(this, new JScrollPane(iePanel), "Set Categories", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
                editInclusion(iePanel.getIncluded(), iePanel.getExcluded());
        });
        tableMenu.add(editCategoriesItem);

        JSeparator categoriesSeparator = new JSeparator();
        tableMenu.add(categoriesSeparator);

        // Edit card tags item
        JMenuItem editTagsItem = new JMenuItem("Edit Tags...");
        editTagsItem.addActionListener((e) -> CardTagPanel.editTags(parent.getSelectedCards(), parent));
        tableMenu.add(editTagsItem);

        // Table memu popup listeners
        tableMenu.addPopupMenuListener(new TableCategoriesPopupListener(addToCategoryMenu, removeFromCategoryMenu,
                editCategoriesItem, categoriesSeparator, deck().table));
        tableMenu.addPopupMenuListener(PopupMenuListenerFactory.createVisibleListener((e) -> {
            ccp.cut.setEnabled(!parent.getSelectedCards().isEmpty());
            ccp.copy.setEnabled(!parent.getSelectedCards().isEmpty());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            ccp.paste.setEnabled(clipboard.isDataFlavorAvailable(DataFlavors.entryFlavor) || clipboard.isDataFlavorAvailable(DataFlavors.cardFlavor));
            tableMenuCardItems.setEnabled(!parent.getSelectedCards().isEmpty());
            moveToMenu.setVisible(!extras().isEmpty());
            moveAllToMenu.setVisible(!extras().isEmpty());
            moveSeparator.setVisible(!extras().isEmpty());
            addToCategoryMenu.setEnabled(!categoryPanels.isEmpty());
            removeFromCategoryMenu.setEnabled(!categoryPanels.isEmpty());
            editCategoriesItem.setEnabled(!categoryPanels.isEmpty());
            editTagsItem.setEnabled(!parent.getSelectedCards().isEmpty());

            moveToMenu.removeAll();
            moveAllToMenu.removeAll();
            for (int i = 1; i < lists.size(); i++)
            {
                if (lists.get(i) != null)
                {
                    final int id = i;
                    JMenuItem moveToItem = new JMenuItem(lists.get(i).name.get());
                    moveToItem.addActionListener((e2) -> moveCards(MAIN_DECK, id, parent.getSelectedCards().stream().collect(Collectors.toMap(Function.identity(), (c) -> 1))));
                    moveToMenu.add(moveToItem);
                    JMenuItem moveAllToItem = new JMenuItem(lists.get(i).name.get());
                    moveAllToItem.addActionListener((e2) -> moveCards(MAIN_DECK, id, parent.getSelectedCards().stream().collect(Collectors.toMap(Function.identity(), (c) -> deck().current.getEntry(c).count()))));
                    moveAllToMenu.add(moveAllToItem);
                }
            }
        }));

        // Panel containing categories
        JPanel categoriesPanel = new JPanel(new BorderLayout());
        JPanel categoriesMainPanel = new JPanel(new BorderLayout());
        categoriesPanel.add(categoriesMainPanel, BorderLayout.CENTER);
        listTabs.addTab("Categories", categoriesPanel);

        // Panel containing components above the category panel
        Box categoryHeaderPanel = new Box(BoxLayout.X_AXIS);
        categoriesMainPanel.add(categoryHeaderPanel, BorderLayout.NORTH);

        // Button to add a new category
        JPanel addCategoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addCategoryButton = new JButton("Add");
        addCategoryButton.addActionListener((e) -> createCategory().ifPresent(this::addCategory));
        addCategoryPanel.add(addCategoryButton);
        categoryHeaderPanel.add(addCategoryPanel);

        // Combo box to change category sort order
        JPanel sortCategoriesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        sortCategoriesPanel.add(new JLabel("Display order:"));
        sortCategoriesBox = new JComboBox<>(CategoryOrder.values());
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
        switchCategoryBox = new JComboBox<>(switchCategoryModel = new DefaultComboBoxModel<>());
        switchCategoryBox.setEnabled(false);
        switchCategoryBox.addActionListener((e) -> {
            if (switchCategoryBox.isPopupVisible())
            {
                getCategoryPanel(switchCategoryBox.getItemAt(switchCategoryBox.getSelectedIndex())).ifPresent((c) -> {
                    c.scrollRectToVisible(new Rectangle(c.getSize()));
                    c.flash();
                });
            }
        });
        switchCategoryPanel.add(new JLabel("Go to category:"));
        switchCategoryPanel.add(switchCategoryBox);
        categoryHeaderPanel.add(switchCategoryPanel);

        // Make sure all parts of the category panel fit inside the window (this is necessary because
        // JScrollPanes do weird things with non-scroll-savvy components)
        JPanel categoriesSuperContainer = new ScrollablePanel(new BorderLayout(), ScrollablePanel.TRACK_WIDTH);
        categoriesContainer = new Box(BoxLayout.Y_AXIS);
        categoryPanels = new ArrayList<>();

        // The category panel is a vertically-scrollable panel that contains all categories stacked vertically
        // The categories should have a constant height, but fit the container horizontally
        categoriesSuperContainer.add(categoriesContainer, BorderLayout.NORTH);
        JScrollPane categoriesPane = new JScrollPane(categoriesSuperContainer, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        categoriesMainPanel.add(categoriesPane, BorderLayout.CENTER);

        // Transfer handler for the category box
        // We explicitly use null here to cause exceptions if cutting or copying, as that should never happen
        categoriesPane.setTransferHandler(new CategoryListTransferHandler(null, (c) -> containsCategory(c.getName()), this::addCategory, null));

        // Popup menu for category container
        JPopupMenu categoriesMenu = new JPopupMenu();
        CCPItems categoriesCCP = new CCPItems(categoriesPane, false);
        categoriesMenu.add(categoriesCCP.paste);
        categoriesMenu.add(new JSeparator());
        JMenuItem categoriesCreateItem = new JMenuItem("Add Category...");
        categoriesCreateItem.addActionListener((e) -> createCategory().ifPresent(this::addCategory));
        categoriesMenu.add(categoriesCreateItem);
        categoriesMenu.addPopupMenuListener(PopupMenuListenerFactory.createVisibleListener((e) -> {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            try
            {
                categoriesCCP.paste.setEnabled(!containsCategory(((CategoryTransferData)clipboard.getData(DataFlavors.categoryFlavor)).data.getName()));
            }
            catch (UnsupportedFlavorException | IOException x)
            {
                categoriesCCP.paste.setEnabled(false);
            }
        }));
        categoriesPane.setComponentPopupMenu(categoriesMenu);

        VerticalButtonList categoryButtons = new VerticalButtonList("+", String.valueOf(UnicodeSymbols.MINUS), "X");
        categoryButtons.get("+").addActionListener((e) -> addCards(MAIN_DECK, parent.getSelectedCards(), 1));
        categoryButtons.get(String.valueOf(UnicodeSymbols.MINUS)).addActionListener((e) -> removeCards(MAIN_DECK, parent.getSelectedCards(), 1));
        categoryButtons.get("X").addActionListener((e) -> removeCards(MAIN_DECK, parent.getSelectedCards(), parent.getSelectedCards().stream().mapToInt((c) -> deck().current.getEntry(c).count()).reduce(0, Math::max)));
        categoriesPanel.add(categoryButtons, BorderLayout.WEST);

        // Sample hands
        JPanel handPanel = new JPanel(new BorderLayout());

        // Table showing the cards in hand
        hand = new Hand(deck().current);

        imagePanel = new ScrollablePanel(ScrollablePanel.TRACK_HEIGHT);
        imagePanel.setLayout(new BoxLayout(imagePanel, BoxLayout.X_AXIS));
        imagePane = new JScrollPane(imagePanel);
        imagePane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        setHandBackground(SettingsDialog.settings().editor.hand.background);

        // Control panel for manipulating the sample hand
        JPanel handModPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        JButton newHandButton = new JButton("New Hand");
        newHandButton.addActionListener((e) -> {
            hand.newHand(startingHandSize);

            imagePanel.removeAll();
            for (Card c : hand)
            {
                CardImagePanel panel = new CardImagePanel();
                panel.setCard(c);
                panel.setBackground(SettingsDialog.settings().editor.hand.background);
                imagePanel.add(panel);
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
            for (Card c : hand)
            {
                CardImagePanel panel = new CardImagePanel();
                imagePanel.add(panel);
                panel.setCard(c);
                panel.setBackground(SettingsDialog.settings().editor.hand.background);
                imagePanel.add(Box.createHorizontalStrut(10));
            }
            imagePanel.validate();
            update();
        });
        handModPanel.add(mulliganButton);
        JButton drawCardButton = new JButton("Draw a Card");
        drawCardButton.addActionListener((e) -> {
            if (hand.size() < deck().current.total())
            {
                hand.draw();
                CardImagePanel panel = new CardImagePanel();
                panel.setBackground(SettingsDialog.settings().editor.hand.background);
                imagePanel.add(panel);
                panel.setCard(hand.get(hand.size() - 1));
                imagePanel.add(Box.createHorizontalStrut(10));
                imagePanel.validate();
                update();
            }
        });
        handModPanel.add(drawCardButton);
        List.of(
            newHandButton.getPreferredSize(),
            mulliganButton.getPreferredSize(),
            drawCardButton.getPreferredSize()
        ).stream().mapToInt((d) -> d.width).max().ifPresent(w -> {
            newHandButton.setPreferredSize(new Dimension(w, newHandButton.getPreferredSize().height));
            mulliganButton.setPreferredSize(new Dimension(w, mulliganButton.getPreferredSize().height));
            drawCardButton.setPreferredSize(new Dimension(w, drawCardButton.getPreferredSize().height));
        });

        handCalculations = new CalculateHandPanel(deck().current);

        JSplitPane handSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, imagePane, handCalculations);
        handSplit.setOneTouchExpandable(true);
        handSplit.setContinuousLayout(true);
        SwingUtilities.invokeLater(() -> handSplit.setDividerLocation(0.5));
        handSplit.setResizeWeight(0.5);
        handPanel.add(handModPanel, BorderLayout.NORTH);
        handPanel.add(handSplit, BorderLayout.CENTER);
        listTabs.addTab("Sample Hand", handPanel);
        hand.refresh();

        // Panel to show the stats of the deck
        JPanel bottomPanel = new JPanel(new BorderLayout());
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        // Labels to counts for total cards, lands, and nonlands
        Box statsPanel = new Box(BoxLayout.X_AXIS);
        statsPanel.add(Box.createHorizontalStrut(10));
        countLabel = new JLabel();
        statsPanel.add(countLabel);
        statsPanel.add(ComponentUtils.createHorizontalSeparator(10, ComponentUtils.TEXT_SIZE));
        landLabel = new JLabel();
        statsPanel.add(landLabel);
        statsPanel.add(ComponentUtils.createHorizontalSeparator(10, ComponentUtils.TEXT_SIZE));
        nonlandLabel = new JLabel();
        statsPanel.add(nonlandLabel);
        statsPanel.add(ComponentUtils.createHorizontalSeparator(10, ComponentUtils.TEXT_SIZE));
        avgCMCLabel = new JLabel();
        statsPanel.add(avgCMCLabel);
        statsPanel.add(ComponentUtils.createHorizontalSeparator(10, ComponentUtils.TEXT_SIZE));
        medCMCLabel = new JLabel();
        statsPanel.add(medCMCLabel);
        statsPanel.add(Box.createHorizontalGlue());
        updateStats();
        GridBagConstraints statsConstraints = new GridBagConstraints();
        statsConstraints.anchor = GridBagConstraints.WEST;
        bottomPanel.add(statsPanel, BorderLayout.WEST);

        // Check legality button
        JPanel legalityPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton legalityButton = new JButton("Show Legality");
        legalityButton.addActionListener((e) -> {
            LegalityChecker checker = new LegalityChecker();
            checker.checkLegality(deck().current);
            JOptionPane.showMessageDialog(this, new LegalityPanel(checker), "Legality of " + deckName(), JOptionPane.PLAIN_MESSAGE);
        });
        legalityButton.setEnabled(false);
        legalityPanel.add(legalityButton);
        GridBagConstraints legalityConstraints = new GridBagConstraints();
        legalityConstraints.anchor = GridBagConstraints.EAST;
        bottomPanel.add(legalityPanel, BorderLayout.EAST);

        // Changelog
        JPanel changelogPanel = new JPanel(new BorderLayout());
        changelogArea = new JTextArea(manager.changelog());
        changelogArea.setEditable(false);
        changelogPanel.add(new JScrollPane(changelogArea), BorderLayout.CENTER);
        JPanel clearLogPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton clearLogButton = new JButton("Clear Change Log");
        clearLogButton.addActionListener((e) -> {
            if (!changelogArea.getText().isEmpty()
                    && JOptionPane.showConfirmDialog(EditorFrame.this, "This change is permanent.  Clear change log?", "Clear Change Log?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
            {
                changelogArea.setText("");
                setUnsaved();
            }
        });
        clearLogPanel.add(clearLogButton);
        changelogPanel.add(clearLogPanel, BorderLayout.SOUTH);
        listTabs.addTab("Change Log", changelogPanel);

        changelogArea.setText(manager.changelog());

        setTransferHandler(new EditorFrameTransferHandler(this, MAIN_DECK));

        for (CategorySpec spec: deck().current.categories())
            categoryPanels.add(createCategoryPanel(spec));
        updateCategoryPanel();
        handCalculations.update();

        // Initialize extra lists
        extrasPane.addTab("+", null);
        for (var extra : manager.sideboards().entrySet())
        {
            final int id = lists.size();
            createExtra(extra.getKey(), id, extrasPane.getTabCount() - 1);
            lists.get(id).current.addAll(extra.getValue());
            lists.get(id).original.addAll(extra.getValue());
        }
        extrasPane.setSelectedIndex(0);
        Consumer<MouseEvent> addSideboard = (e) -> {
            int index = extrasPanel.isVisible() ? extrasPane.indexAtLocation(e.getX(), e.getY()) : 0;
            int last = extrasPane.getTabCount() - 1;
            if (index == last)
            {
                final int id = lists.size();
                performAction(() -> createExtra("Sideboard " + id, id, last), () -> deleteExtra(id, last));
                parent.setSelectedComponents(lists.get(id).table, lists.get(id).current);
            }
        };
        extrasPane.addMouseListener(MouseListenerFactory.createPressListener(addSideboard));
        emptyPanel.addMouseListener(MouseListenerFactory.createClickListener(addSideboard));

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
     * Create a new EditorFrame with no deck.
     * 
     * @param p MainFrame parent of the new EditorFrame
     * @param u index of the new frame, used for initial bounds and title if it's a new deck
     */
    public EditorFrame(MainFrame p, int u)
    {
        this(p, u, new DeckSerializer());
    }

    /**
     * Add copies of a collection of cards to the specified list.
     * 
     * @param id ID of the list to add to
     * @param cards cards to add
     * @param n number of copies of each card to add
     * @return <code>true</code> if the cards were added, and <code>false</code> otherwise.
     */
    public boolean addCards(int id, Collection<Card> cards, int n)
    {
        return modifyCards(id, cards.stream().collect(Collectors.toMap(Function.identity(), (c) -> n)));
    }

    /**
     * Add a new category to the main deck.
     * 
     * @param spec specification for the new category
     * @return <code>true</code> if adding the category was successful, and <code>false</code>
     * otherwise.
     */
    public boolean addCategory(CategorySpec spec)
    {
        if (deck().current.containsCategory(spec.getName()))
            return false;
        else
        {
            return performAction(() -> {
                if (deck().current.containsCategory(spec.getName()))
                    throw new RuntimeException("attempting to add duplicate category " + spec.getName());
                else
                    return do_addCategory(spec);
            }, () -> do_removeCategory(spec));
        }
    }

    /**
     * Set the settings of this EditorFrame
     */
    public void applySettings()
    {
        var columns = SettingsDialog.settings().editor.columns;
        Color stripe = SettingsDialog.settings().editor.stripe;
        deck().model.setColumns(columns);
        deck().table.setStripeColor(stripe);
        for (int i = 0; i < deck().table.getColumnCount(); i++)
            if (deck().model.isCellEditable(0, i))
                deck().table.getColumn(deck().model.getColumnName(i)).setCellEditor(CardTable.createCellEditor(this, deck().model.getColumnData(i)));
        for (CategoryPanel category : categoryPanels)
            category.applySettings(this);
        startingHandSize = SettingsDialog.settings().editor.hand.size;
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
        lists.stream().filter((l) -> l != null && l.table != except).forEach((l) -> l.table.clearSelection());
        for (CategoryPanel c : categoryPanels)
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
     * Open the dialog to create a new specification for a deck category.
     *
     * @return the {@link CategorySpec} created by the dialog, or null if it was
     * canceled.
     */
    public Optional<CategorySpec> createCategory()
    {
        Optional<CategorySpec> spec = Optional.empty();
        do
        {
            (spec = CategoryEditorPanel.showCategoryEditor(this, spec)).ifPresent((s) -> {
            if (deck().current.containsCategory(s.getName()))
                JOptionPane.showMessageDialog(this, "Categories must have unique names.", "Error", JOptionPane.ERROR_MESSAGE);
            });
        } while (spec.isPresent() && deck().current.containsCategory(spec.get().getName()));
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
        final CategoryPanel newCategory = new CategoryPanel(deck().current, spec.getName(), this);
        // When a card is selected in a category, the others should deselect
        TableSelectionListener listener = new TableSelectionListener(parent, newCategory.table, deck().current.getCategoryList(newCategory.getCategoryName()));
        newCategory.table.addMouseListener(listener);
        newCategory.table.getSelectionModel().addListSelectionListener(listener);
        // Add the behavior for the edit category button
        newCategory.editButton.addActionListener((e) -> editCategory(newCategory.getCategoryName()));
        // Add the behavior for the remove category button
        newCategory.removeButton.addActionListener((e) -> removeCategory(newCategory.getCategoryName()));
        // Add the behavior for the color edit button
        newCategory.colorButton.addActionListener((e) -> {
            final Color newColor = JColorChooser.showDialog(this, "Choose a Color", newCategory.colorButton.color());
            if (newColor != null)
            {
                final Color oldColor = deck().current.getCategorySpec(newCategory.getCategoryName()).getColor();
                final String name = newCategory.getCategoryName();
                performAction(() -> {
                    CategorySpec mod = deck().current.getCategorySpec(name);
                    mod.setColor(newColor);
                    deck().current.updateCategory(newCategory.getCategoryName(), mod);
                    return true;
                }, () -> {
                    CategorySpec mod = deck().current.getCategorySpec(name);
                    mod.setColor(oldColor);
                    deck().current.updateCategory(newCategory.getCategoryName(), mod);
                    return true;
                });
            }
        });
        // Add the behavior for double-clicking the category title
        newCategory.addMouseListener(new ChangeTitleListener(newCategory, (title) -> {
            final String oldName = newCategory.getCategoryName();
            if (!title.equals(oldName))
            {
                performAction(() -> {
                    CategorySpec mod = deck().current.getCategorySpec(oldName);
                    mod.setName(title);
                    deck().current.updateCategory(oldName, mod);
                    newCategory.setCategoryName(title);
                    updateCategoryPanel();
                    return true;
                }, () -> {
                    CategorySpec mod = deck().current.getCategorySpec(title);
                    mod.setName(oldName);
                    deck().current.updateCategory(title, mod);
                    newCategory.setCategoryName(oldName);
                    updateCategoryPanel();
                    return true;
                });
            }
        }));
        // Add behavior for the rank box
        newCategory.rankBox.addActionListener((e) -> {
            if (newCategory.rankBox.isPopupVisible())
            {
                final String name = newCategory.getCategoryName();
                final int old = deck().current.getCategoryRank(newCategory.getCategoryName());
                final int target = newCategory.rankBox.getSelectedIndex();
                performAction(() -> {
                    deck().current.swapCategoryRanks(name, target);
                    for (CategoryPanel panel : categoryPanels)
                        panel.rankBox.setSelectedIndex(deck().current.getCategoryRank(panel.getCategoryName()));
                    listTabs.setSelectedIndex(CATEGORIES);
                    updateCategoryPanel();
                    return true;
                }, () -> {
                    deck().current.swapCategoryRanks(name, old);
                    for (CategoryPanel panel : categoryPanels)
                        panel.rankBox.setSelectedIndex(deck().current.getCategoryRank(panel.getCategoryName()));
                    listTabs.setSelectedIndex(CATEGORIES);
                    updateCategoryPanel();
                    return true;
                });
            }
        });

        newCategory.table.setTransferHandler(new EditorTableTransferHandler(this, MAIN_DECK));
        newCategory.table.setDragEnabled(true);
        newCategory.table.setDropMode(DropMode.ON);

        // Add the behavior for clicking on the category's table
        // Table popup menu
        JPopupMenu tableMenu = new JPopupMenu();
        newCategory.table.addMouseListener(new TableMouseAdapter(newCategory.table, tableMenu));

        // Cut, copy, paste
        CCPItems cardCCP = new CCPItems(deck().table, true);
        tableMenu.add(cardCCP.cut);
        tableMenu.add(cardCCP.copy);
        tableMenu.add(cardCCP.paste);
        tableMenu.add(new JSeparator());
        
        CardMenuItems tableMenuCardItems = new CardMenuItems(() -> Optional.of(this), parent::getSelectedCards, true);
        tableMenuCardItems.addAddItems(tableMenu);
        tableMenu.add(new JSeparator());
        tableMenuCardItems.addRemoveItems(tableMenu);

        JSeparator categoriesSeparator = new JSeparator();
        tableMenu.add(categoriesSeparator);

        // Quick edit categories
        JMenu addToCategoryMenu = new JMenu("Include in");
        tableMenu.add(addToCategoryMenu);
        JMenuItem removeFromCategoryItem = new JMenuItem("Exclude from " + spec.getName());
        removeFromCategoryItem.addActionListener((e) -> modifyInclusion(Collections.<Card>emptyList(), newCategory.getSelectedCards(), deck().current.getCategorySpec(newCategory.getCategoryName())));
        tableMenu.add(removeFromCategoryItem);
        JMenu removeFromCategoryMenu = new JMenu("Exclude from");
        tableMenu.add(removeFromCategoryMenu);

        // Edit categories item
        JMenuItem editCategoriesItem = new JMenuItem("Edit Categories...");
        editCategoriesItem.addActionListener((e) -> {
            IncludeExcludePanel iePanel = new IncludeExcludePanel(deck().current.categories().stream().sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName())).collect(Collectors.toList()), parent.getSelectedCards());
            if (JOptionPane.showConfirmDialog(this, new JScrollPane(iePanel), "Set Categories", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
                editInclusion(iePanel.getIncluded(), iePanel.getExcluded());
        });
        tableMenu.add(editCategoriesItem);

        tableMenu.add(new JSeparator());

        // Edit tags item
        JMenuItem editTagsItem = new JMenuItem("Edit Tags...");
        editTagsItem.addActionListener((e) -> CardTagPanel.editTags(parent.getSelectedCards(), parent));
        tableMenu.add(editTagsItem);

        // Table menu popup listeners
        tableMenu.addPopupMenuListener(new TableCategoriesPopupListener(addToCategoryMenu, removeFromCategoryMenu,
                editCategoriesItem, categoriesSeparator, newCategory.table));
        tableMenu.addPopupMenuListener(PopupMenuListenerFactory.createVisibleListener((e) -> {
            cardCCP.cut.setEnabled(!parent.getSelectedCards().isEmpty());
            cardCCP.copy.setEnabled(!parent.getSelectedCards().isEmpty());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            cardCCP.paste.setEnabled(clipboard.isDataFlavorAvailable(DataFlavors.entryFlavor) || clipboard.isDataFlavorAvailable(DataFlavors.cardFlavor));

            removeFromCategoryItem.setText("Exclude from " + newCategory.getCategoryName());
            tableMenuCardItems.setEnabled(!parent.getSelectedCards().isEmpty());
            editTagsItem.setEnabled(!parent.getSelectedCards().isEmpty());
        }));

        newCategory.setTransferHandler(new CategoryListTransferHandler(
            () -> getCategory(newCategory.getCategoryName()),
            (c) -> containsCategory(c.getName()),
            this::addCategory,
            (c) -> removeCategory(c.getName())
        ));

        // Category popup menu
        JPopupMenu categoryMenu = new JPopupMenu();
        newCategory.setComponentPopupMenu(categoryMenu);

        // Cut, copy, paste
        CCPItems categoryCCP = new CCPItems(newCategory, false);
        categoryMenu.add(categoryCCP.cut);
        categoryMenu.add(categoryCCP.copy);
        categoryMenu.add(categoryCCP.paste);
        categoryMenu.add(new JSeparator());

        // Edit item
        JMenuItem editItem = new JMenuItem("Edit...");
        editItem.addActionListener((e) -> editCategory(newCategory.getCategoryName()));
        categoryMenu.add(editItem);

        // Delete item
        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener((e) -> deck().current.removeCategory(newCategory.getCategoryName()));
        categoryMenu.add(deleteItem);

        // Add to presets item
        JMenuItem addPresetItem = new JMenuItem("Add to presets");
        addPresetItem.addActionListener((e) -> {
            CategorySpec s = deck().current.getCategorySpec(newCategory.getCategoryName());
            if (!s.getWhitelist().isEmpty() || !s.getBlacklist().isEmpty())
            {
                if (JOptionPane.showConfirmDialog(this,
                        "Category "
                                + s.getName()
                                + " contains cards in its whitelist or blacklist which will not be included in the preset category."
                                + "  Make this category a preset category?",
                        "Add to Presets",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                    parent.addPreset(s);
            }
            else
                parent.addPreset(s);
        });
        categoryMenu.add(addPresetItem);

        categoryMenu.addPopupMenuListener(PopupMenuListenerFactory.createVisibleListener((e) -> {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            try
            {
                categoryCCP.paste.setEnabled(!containsCategory(((CategoryTransferData)clipboard.getData(DataFlavors.categoryFlavor)).data.getName()));
            }
            catch (UnsupportedFlavorException | IOException x)
            {
                // Technically using exceptions as control flow (as with unsupported flavors here) is bad
                // programming practice, but since the exception has to be caught here anyway it reduces
                // code size
                categoryCCP.paste.setEnabled(false);
            }
        }));

        newCategory.table.addMouseListener(new TableMouseAdapter(newCategory.table, tableMenu));

        return newCategory;
    }

    /**
     * Create a new extra, uncategorized, untracked list, which usually will be used for a
     * sideboard.
     * 
     * @param name name of the extra list, i.e. "Sideboard"; should be unique
     * @param id ID of the extra to create
     * @param index index of the tab to insert the new list at
     * @return <code>true</code> if the list was created, and <code>false</code> otherwise.
     * @throws IllegalArgumentException if a list with the given name already exists
     */
    private boolean createExtra(String name, final int id, int index)
    {
        if (id == 0)
            throw new IllegalArgumentException("only the main deck can have ID 0");
        else if (lists.size() > id && lists.get(id) != null)
            throw new IllegalArgumentException("extra already exists at ID " + id);
        else
        {
            if (extras().stream().anyMatch((l) -> l.name.get().equals(name)))
                throw new IllegalArgumentException("sideboard \"" + name + "\" already exists");

            DeckData newExtra = new DeckData(name);
            while (lists.size() <= id)
                lists.add(null);
            lists.set(id, newExtra);

            final EditablePanel panel = new EditablePanel(name, extrasPane);
            extrasPane.insertTab(name, null, initExtraList(id), null, index);
            extrasPane.setTabComponentAt(index, panel);
            extrasPane.setSelectedIndex(index);
            extrasPane.getTabComponentAt(extrasPane.getSelectedIndex()).requestFocus();

            extrasPanel.setVisible(!extras().isEmpty());
            emptyPanel.setVisible(extras().isEmpty());

            panel.addActionListener((e) -> {
                switch (e.getActionCommand())
                {
                case EditablePanel.CLOSE:
                    final String n = panel.getTitle();
                    final DeckData extra = new DeckData(lists.get(id));
                    final int i = extrasPane.indexOfTab(n);
                    performAction(() -> deleteExtra(id, i), () -> {
                        boolean success = createExtra(n, id, i);
                        success |= lists.get(id).current.addAll(extra.current);
                        success |= lists.get(id).original.addAll(extra.original);
                        return success;
                    });
                    break;
                case EditablePanel.EDIT:
                    final String current = panel.getTitle();
                    final String old = panel.getOldTitle();
                    if (current.isEmpty())
                        panel.setTitle(old);
                    else if (extras().stream().anyMatch((l) -> l.name.get().equals(current)))
                    {
                        panel.setTitle(old);
                        JOptionPane.showMessageDialog(EditorFrame.this, "Sideboard \"" + current + "\" already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    else if (!current.equals(old))
                    {
                        final int j = extrasPane.indexOfTab(old);
                        performAction(() -> {
                            newExtra.name = Optional.of(current);
                            ((EditablePanel)extrasPane.getTabComponentAt(j)).setTitle(current);
                            extrasPane.setTitleAt(j, current);
                            return true;
                        }, () -> {
                            newExtra.name = Optional.of(old);
                            ((EditablePanel)extrasPane.getTabComponentAt(j)).setTitle(old);
                            extrasPane.setTitleAt(j, old);
                            return true;
                        });
                    }
                    break;
                case EditablePanel.CANCEL:
                    break;
                }
            });

            return true;
        }
    }

    /**
     * @return The list corresponding to the main deck (ID 0).
     */
    private DeckData deck()
    {
        return lists.get(MAIN_DECK);
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
     * Delete an extra list. This just sets its index in the list of card lists to null,
     * so it can be reused later if this is undone.
     * 
     * @param id ID of the list to delete
     * @param index index of the tab containing the list
     * @return <code>true</code> if the list was successfully removed, and <code>false</code>
     * otherwise.
     * @throws IllegalArgumentException if the list with the given ID doesn't exist
     */
    private boolean deleteExtra(int id, int index)
    {
        if (lists.get(id) == null)
            throw new IllegalArgumentException("missing sideboard with ID " + id);

        lists.set(id, null);
        extrasPane.remove(index);
        if (index > 0)
        {
            extrasPane.setSelectedIndex(index - 1);
            extrasPane.getTabComponentAt(extrasPane.getSelectedIndex()).requestFocus();
        }
        extrasPanel.setVisible(!extras().isEmpty());
        emptyPanel.setVisible(extras().isEmpty());

        return true;
    }

    /**
     * Helper method for adding a category.
     * 
     * @param spec specification of the new category
     * @return <code>true</code> if the category was successfully added, and <code>false</code>
     * otherwise
     */
    private boolean do_addCategory(CategorySpec spec)
    {
        deck().current.addCategory(spec);

        CategoryPanel category = createCategoryPanel(spec);
        categoryPanels.add(category);

        for (CategoryPanel c : categoryPanels)
            if (c != category)
                c.rankBox.addItem(deck().current.categories().size() - 1);

        listTabs.setSelectedIndex(CATEGORIES);
        updateCategoryPanel();
        SwingUtilities.invokeLater(() -> {
            switchCategoryBox.setSelectedItem(category.getCategoryName());
            category.scrollRectToVisible(new Rectangle(category.getSize()));
            category.flash();
        });
        handCalculations.update();

        return true;
    }

    /**
     * Helper method for removing a category.
     * 
     * @param spec specification of the category to remove
     * @return <code>true</code> if the category was removed, and <code>false</code>
     * otherwise.
     */
    private boolean do_removeCategory(CategorySpec spec)
    {
        deck().current.removeCategory(spec);

        categoryPanels.remove(getCategoryPanel(spec.getName()).get());
        for (CategoryPanel panel : categoryPanels)
            panel.rankBox.removeItemAt(categoryPanels.size());

        listTabs.setSelectedIndex(CATEGORIES);
        updateCategoryPanel();
        handCalculations.update();

        return true;
    }

    /**
     * Open the category dialog to edit the category with the given
     * name, if there is one, and then update the undo buffer.
     *
     * @param name name of the category to edit
     * @return <code>true</code> if the category was edited, and <code>false</code>
     * otherwise.
     */
    public boolean editCategory(String name)
    {
        CategorySpec toEdit = deck().current.getCategorySpec(name);
        if (toEdit == null)
            JOptionPane.showMessageDialog(this, "Deck " + deckName() + " has no category named " + name + ".",
                    "Error", JOptionPane.ERROR_MESSAGE);
        return CategoryEditorPanel.showCategoryEditor(this, Optional.of(toEdit)).map((s) -> {
            final CategorySpec old = deck().current.getCategorySpec(name);
            return performAction(() -> {
                if (!deck().current.updateCategory(old.getName(), s).equals(old))
                    throw new RuntimeException("edited unexpected category");
                CategoryPanel panel = getCategoryPanel(old.getName()).get();
                panel.setCategoryName(s.getName());
                ((AbstractTableModel)panel.table.getModel()).fireTableDataChanged();
                updateCategoryPanel();
                return true;
            }, () -> {
                if (!deck().current.updateCategory(s.getName(), old).equals(s))
                    throw new RuntimeException("restored from unexpected category");
                CategoryPanel panel = getCategoryPanel(s.getName()).get();
                panel.setCategoryName(old.getName());
                ((AbstractTableModel)panel.table.getModel()).fireTableDataChanged();
                updateCategoryPanel();
                return true;
            });
        }).orElse(false);
    }

    /**
     * Change inclusion of cards in categories according to the given maps.
     *
     * @param included map of cards onto the set of categories they should become included in
     * @param excluded map of cards onto the set of categories they should become excluded from
     * @return <code>true</code> if any categories were modified, and <code>false</code>
     * otherwise.
     */
    public boolean editInclusion(final Map<Card, Set<CategorySpec>> included, final Map<Card, Set<CategorySpec>> excluded)
    {
        for (Card card : included.keySet())
        {
            included.compute(card, (k, v) -> {
                Set<CategorySpec> s = v.stream().filter((c) -> !c.includes(k)).collect(Collectors.toSet());
                return s.isEmpty() ? null : s;
            });
        }
        for (Card card : excluded.keySet())
        {
            excluded.compute(card, (k, v) -> {
                Set<CategorySpec> s = v.stream().filter((c) -> c.includes(k)).collect(Collectors.toSet());
                return s.isEmpty() ? null : s;
            });
        }
        if (included.isEmpty() && excluded.isEmpty())
            return false;
        else
        {
            return performAction(() -> {
                var mods = new HashMap<String, CategorySpec>();
                for (Card card : included.keySet())
                {
                    for (CategorySpec category : included.get(card))
                    {
                        if (deck().current.getCategorySpec(category.getName()).includes(card))
                            throw new IllegalArgumentException(card + " is already in " + category.getName());
                        mods.putIfAbsent(category.getName(), deck().current.getCategorySpec(category.getName()));
                        mods.get(category.getName()).include(card);
                    }
                }
                for (Card card : excluded.keySet())
                {
                    for (CategorySpec category : excluded.get(card))
                    {
                        if (!deck().current.getCategorySpec(category.getName()).includes(card))
                            throw new IllegalArgumentException(card + " is already not in " + category.getName());
                        mods.putIfAbsent(category.getName(), deck().current.getCategorySpec(category.getName()));
                        mods.get(category.getName()).exclude(card);
                    }
                }
                for (var mod : mods.entrySet())
                    deck().current.updateCategory(mod.getKey(), mod.getValue());
                return true;
            }, () -> {
                var mods = new HashMap<String, CategorySpec>();
                for (Card card : included.keySet())
                {
                    for (CategorySpec category : included.get(card))
                    {
                        if (!deck().current.getCategorySpec(category.getName()).includes(card))
                            throw new IllegalArgumentException("error undoing category edit: " + card + " is already not in " + category.getName());
                        mods.putIfAbsent(category.getName(), deck().current.getCategorySpec(category.getName()));
                        mods.get(category.getName()).exclude(card);
                    }
                }
                for (Card card : excluded.keySet())
                {
                    for (CategorySpec category : excluded.get(card))
                    {
                        if (deck().current.getCategorySpec(category.getName()).includes(card))
                            throw new IllegalArgumentException("error undoing category edit: " + card + " is already in " + category.getName());
                        mods.putIfAbsent(category.getName(), deck().current.getCategorySpec(category.getName()));
                        mods.get(category.getName()).include(card);
                    }
                }
                for (var mod : mods.entrySet())
                    deck().current.updateCategory(mod.getKey(), mod.getValue());
                return true;
            });
        }
    }

    /**
     * Exclude a card from a category.
     * 
     * @param card card to exclude
     * @param spec specification for the category to exclude it from; must be a category in the
     * main deck
     * @return <code>true</code> if the card was successfully excluded from the category, and
     * <code>false</code> otherwise (such as if the card already wasn't in the category).
     */
    public boolean excludeFrom(final Card card, CategorySpec spec)
    {
        return modifyInclusion(Collections.<Card>emptyList(), Arrays.asList(card), spec);
    }

    /**
     * Export the deck to a different format.
     *
     * @param format formatter to use for export
     * @param file file to export to
     * @param extraNames names of extra lists to include in the export
     * @throws UnsupportedEncodingException
     * @throws FileNotFoundException if the file can't be opened
     * @throws NoSuchElementException if any of the named extra lists aren't in the deck
     */
    public void export(CardListFormat format, Comparator<? super CardList.Entry> comp, List<String> extraNames, File file) throws UnsupportedEncodingException, FileNotFoundException, NoSuchElementException
    {
        try (PrintWriter wr = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file, false), "UTF8")))
        {
            Deck copy;

            if (format.hasHeader())
                wr.println(format.header());
            if (!deck().current.isEmpty())
            {
                copy = new Deck(deck().current);
                copy.sort(comp);
                wr.print(format.format(copy));
            }
            for (String extra : extraNames)
            {
                var list = extras().stream().filter((l) -> l.name.get().equals(extra)).findAny();
                if (!list.isPresent())
                    throw new NoSuchElementException("No extra list named " + extra);
                if (!list.get().current.isEmpty())
                {
                    copy = new Deck(list.get().current);
                    copy.sort(comp);
                    wr.println();
                    wr.println(extra);
                    wr.print(format.format(copy));
                }
            }
        }
    }

    /**
     * @return The extra lists, filtered for null entries.
     */
    private Collection<DeckData> extras()
    {
        return lists.stream().skip(1).filter((l) -> l != null).collect(Collectors.toList());
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
     * @return The names of the extra lists.
     */
    public List<String> getExtraNames()
    {
        return extras().stream().map((l) -> l.name.get()).collect(Collectors.toList());
    }

    /**
     * @return a copy of the extra list corresponding to the selected tab.
     */
    public CardList getSelectedExtra()
    {
        Deck copy = new Deck();
        copy.addAll(sideboard());
        return copy;
    }

    /**
     * Get the IDs of lists in the deck. ID 0 will always contain the main deck, and
     * IDs starting from 1 will contain extra lists. IDs do not have to be sequential,
     * but they will never be reused (unless list deletion is undone).
     * 
     * @return The list of IDs of card lists in the deck.
     */
    public int[] getListIDs()
    {
        return IntStream.range(0, lists.size()).filter((i) -> lists.get(i) != null).toArray();
    }

    /**
     * @return The ID of the extra list corresponding to the selected tab.
     */
    public Optional<Integer> getSelectedExtraID()
    {
        for (int i = 0; i < lists.size(); i++)
            if (lists.get(i) != null && getSelectedExtraName().equals(lists.get(i).name))
                return Optional.of(i);
        return Optional.empty();

    }

    /**
     * @return the name of the extra list corresponding to the selected tab.
     */
    public Optional<String> getSelectedExtraName()
    {
        if (extras().isEmpty())
            return Optional.empty();
        else
            return Optional.of(extrasPane.getTitleAt(extrasPane.getSelectedIndex()));
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
        if (t == deck().table)
            return deck().current.get(deck().table.convertRowIndexToModel(index));
        else
        {
            for (CategoryPanel panel : categoryPanels)
                if (t == panel.table)
                    return deck().current.getCategoryList(panel.getCategoryName()).get(panel.table.convertRowIndexToModel(index));
            throw new IllegalArgumentException("Table not in deck " + deckName());
        }
    }

    /**
     * @return The categories in the main deck.
     */
    public Collection<CategorySpec> getCategories()
    {
        return deck().current.categories();
    }

    public boolean containsCategory(String name)
    {
        return deck().current.containsCategory(name);
    }

    public CategorySpec getCategory(String name) throws IllegalArgumentException
    {
        return deck().current.getCategorySpec(name);
    }

    /**
     * Get the panel for the category with the specified name in the deck.
     *
     * @param name name of the category to search for
     * @return the panel for the category with the specified name, if there is none.
     */
    private Optional<CategoryPanel> getCategoryPanel(String name)
    {
        return categoryPanels.stream().filter((c) -> c.getCategoryName().equals(name)).findAny();
    }

    /**
     * @return a copy of the main deck.
     */
    public CardList getDeck()
    {
        return new Deck(deck().current);
    }

    /**
     * Get the cards in one of the deck lists. ID 0 corresponds to the main deck.
     *
     * @param id ID of the list to get
     * @return a copy of the list.
     * @throws ArrayIndexOutOfBoundsException if there is no list with the given ID.
     */
    public CardList getList(int id)
    {
        if (lists.get(id) == null)
            throw new ArrayIndexOutOfBoundsException(id);
        else
            return new Deck(lists.get(id).current);
    }

    /**
     * @return a {@link CardList} containing all of the cards in extra lists.
     */
    public CardList getExtraCards()
    {
        Deck sideboard = new Deck();
        for (DeckData extra : extras())
            sideboard.addAll(extra.current);
        return sideboard;
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
     * Get the selected cards from the currently-selected list (even if it isn't this editor).
     *
     * @return a list of cards representing the current table selection
     */
    public List<Card> getSelectedCards()
    {
        return parent.getSelectedCards();
    }

    /**
     * @param id ID of the list to search, with the empty string specifying the main deck
     * @param card card to search for
     * @return <code>true</code> if the specified list contains the specified card, and
     * <code>false</code> otherwise.
     * @see #getListIDs()
     * @throws ArrayIndexOutOfBoundsException if there is no list with the given ID.
     */
    public boolean hasCard(int id, Card card)
    {
        if (lists.get(id) == null)
            throw new ArrayIndexOutOfBoundsException(id);
        else
            return lists.get(id).current.contains(card);
    }

    /**
     * Check whether or not this editor has the table with the current selection.
     *
     * @return true if this editor has the table with the current selection and false otherwise.
     */
    public boolean hasSelectedCards()
    {
        return parent.getSelectedTable().map((t) -> {
            if (lists.stream().filter((l) -> l != null).anyMatch((l) -> l.table == t))
                return true;
            for (CategoryPanel panel : categoryPanels)
                if (t == panel.table)
                    return true;
            return false;
        }).orElse(false);
    }

    /**
     * Include a card in a category.
     * 
     * @param card card to include
     * @param spec specification for the category to include the card in; must be a category in
     * the deck
     * @return <code>true</code> if the card was sucessfully included in the category, and
     * <code>false</code> otherwise (such as if the card was already in the category).
     */
    public boolean includeIn(final Card card, CategorySpec spec)
    {
        return modifyInclusion(Arrays.asList(card), Collections.<Card>emptyList(), spec);
    }

    /**
     * Create and initialize the table, backing model, and menu items relating to a newly-created
     * extra list.
     * 
     * @param id ID of the new extra list
     * @return the pane that contains the table showing the extra list
     */
    public JScrollPane initExtraList(final int id)
    {
        // Extra list's models
        lists.get(id).model = new CardTableModel(this, lists.get(id).current, SettingsDialog.settings().editor.columns);
        lists.get(id).table = new CardTable(lists.get(id).model)
        {
            @Override
            public Dimension getPreferredScrollableViewportSize()
            {
                Dimension s = super.getPreferredScrollableViewportSize();
                return new Dimension(s.width, getRowHeight() * 5);
            }
        };
        lists.get(id).table.setStripeColor(SettingsDialog.settings().editor.stripe);
        // When a card is selected in a sideboard table, select it for adding
        TableSelectionListener listener = new TableSelectionListener(parent, lists.get(id).table, lists.get(id).current);
        lists.get(id).table.addMouseListener(listener);
        lists.get(id).table.getSelectionModel().addListSelectionListener(listener);
        for (int i = 0; i < lists.get(id).table.getColumnCount(); i++)
            if (lists.get(id).model.isCellEditable(0, i))
                lists.get(id).table.getColumn(lists.get(id).model.getColumnName(i)).setCellEditor(CardTable.createCellEditor(this, lists.get(id).model.getColumnData(i)));
        lists.get(id).table.setTransferHandler(new EditorTableTransferHandler(this, id));
        lists.get(id).table.setDragEnabled(true);
        lists.get(id).table.setDropMode(DropMode.ON);

        JScrollPane sideboardPane = new JScrollPane(lists.get(id).table);
        sideboardPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

        // Extra list's table menu
        JPopupMenu extraMenu = new JPopupMenu();
        lists.get(id).table.addMouseListener(new TableMouseAdapter(lists.get(id).table, extraMenu));

        // Cut, copy, paste
        CCPItems ccp = new CCPItems(() -> lists.get(id).table, true);
        extraMenu.add(ccp.cut);
        extraMenu.add(ccp.copy);
        extraMenu.add(ccp.paste);
        extraMenu.add(new JSeparator());

        // Add/remove cards from sideboard
        CardMenuItems sideboardMenuCardItems = new CardMenuItems(() -> Optional.of(this), parent::getSelectedCards, false);
        sideboardMenuCardItems.addAddItems(extraMenu);
        extraMenu.add(new JSeparator());
        sideboardMenuCardItems.addRemoveItems(extraMenu);
        extraMenu.add(new JSeparator());

        // Move cards to main deck
        JMenuItem moveToMainItem = new JMenuItem("Move to Main Deck");
        moveToMainItem.addActionListener((e) -> moveCards(id, MAIN_DECK, parent.getSelectedCards().stream().collect(Collectors.toMap(Function.identity(), (c) -> 1))));
        extraMenu.add(moveToMainItem);
        JMenuItem moveAllToMainItem = new JMenuItem("Move All to Main Deck");
        moveAllToMainItem.addActionListener((e) -> moveCards(id, MAIN_DECK, parent.getSelectedCards().stream().collect(Collectors.toMap(Function.identity(), (c) -> lists.get(id).current.getEntry(c).count()))));
        extraMenu.add(moveAllToMainItem);
        extraMenu.add(new JSeparator());

        // Edit card tags item in sideboard
        JMenuItem sBeditTagsItem = new JMenuItem("Edit Tags...");
        sBeditTagsItem.addActionListener((e) -> CardTagPanel.editTags(parent.getSelectedCards(), parent));
        extraMenu.add(sBeditTagsItem);

        extraMenu.addPopupMenuListener(PopupMenuListenerFactory.createVisibleListener((e) -> {
            ccp.cut.setEnabled(!parent.getSelectedCards().isEmpty());
            ccp.copy.setEnabled(!parent.getSelectedCards().isEmpty());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            ccp.paste.setEnabled(clipboard.isDataFlavorAvailable(DataFlavors.entryFlavor) || clipboard.isDataFlavorAvailable(DataFlavors.cardFlavor));
        }));

        return sideboardPane;
    }

    /**
     * Change the number of copies of cards in the deck, adding and removing entries as
     * needed.
     * 
     * @param id ID of the list to add to
     * @param changes map of card onto integer representing the number of copies of each card to
     * add (positive number) or remove (negative number)
     * @return <code>true</code> if the list deck changed as a result, or <code>false</code>
     * otherwise
     */
    public boolean modifyCards(final int id, final Map<Card, Integer> changes)
    {
        if (changes.isEmpty() || changes.values().stream().allMatch((n) -> n == 0))
            return false;
        else
        {
            var capped = changes.entrySet().stream().collect(Collectors.toMap(Map.Entry<Card, Integer>::getKey, (e) -> Math.max(e.getValue(), -lists.get(id).current.getEntry(e.getKey()).count())));
            return performAction(() -> {
                var selected = parent.getSelectedCards();
                boolean changed = capped.entrySet().stream().map((e) -> {
                    if (e.getValue() < 0)
                        return lists.get(id).current.remove(e.getKey(), -e.getValue()) > 0;
                    else if (e.getValue() > 0)
                        return lists.get(id).current.add(e.getKey(), e.getValue());
                    else
                        return false;
                }).reduce(false, (a, b) -> a || b);
                if (changed)
                    updateTables(selected);
                return changed;
            }, () -> {
                var selected = parent.getSelectedCards();
                boolean changed = capped.entrySet().stream().map((e) -> {
                    if (e.getValue() < 0)
                        return lists.get(id).current.add(e.getKey(), -e.getValue());
                    else if (e.getValue() > 0)
                        return lists.get(id).current.remove(e.getKey(), e.getValue()) > 0;
                    else
                        return false;
                }).reduce(false, (a, b) -> a || b);
                if (changed)
                    updateTables(selected);
                return changed;
            });
        }
    }

    /**
     * Modify the inclusion of cards in a category.
     * 
     * @param include cards to include in the category
     * @param exclude cards to exclude from the category
     * @param spec specification for the category to modify card inclusion for; must be part of
     * the deck
     * @return <code>true</code> if the category was modified, and <code>false</code>
     * otherwise (such as if the included cards already existed in the category and the
     * excluded cards didn't).
     */
    public boolean modifyInclusion(Collection<Card> include, Collection<Card> exclude, CategorySpec spec)
    {
        if (!deck().current.containsCategory(spec.getName()))
            throw new IllegalArgumentException("can't include a card in a category that doesn't exist");
        if (!deck().current.getCategorySpec(spec.getName()).equals(spec))
            throw new IllegalArgumentException("category name matches, but specification doesn't");

        include.removeIf(spec::includes);
        exclude.removeIf((c) -> !spec.includes(c));
        if (include.isEmpty() && exclude.isEmpty())
            return false;
        else
        {
            final String name = spec.getName();
            return performAction(() -> {
                CategorySpec mod = deck().current.getCategorySpec(name);
                for (Card c : include)
                {
                    if (mod.includes(c))
                        throw new IllegalArgumentException(mod.getName() + " already includes " + c);
                    mod.include(c);
                }
                for (Card c : exclude)
                {
                    if (!mod.includes(c))
                        throw new IllegalArgumentException(mod.getName() + " already doesn't include " + c);
                    mod.exclude(c);
                }
                deck().current.updateCategory(name, mod);
                for (CategoryPanel panel : categoryPanels)
                    if (panel.getCategoryName().equals(name))
                        ((AbstractTableModel)panel.table.getModel()).fireTableDataChanged();
                updateCategoryPanel();
                return true;
            }, () -> {
                CategorySpec mod = deck().current.getCategorySpec(name);
                for (Card c : include)
                {
                    if (!mod.includes(c))
                        throw new IllegalArgumentException("error undoing include: " + mod.getName() + " already doesn't include " + c);
                    mod.exclude(c);
                }
                for (Card c : exclude)
                {
                    if (mod.includes(c))
                        throw new IllegalArgumentException("error undoing exclude: " + mod.getName() + " already includes " + c);
                    mod.include(c);
                }
                deck().current.updateCategory(name, mod);
                for (CategoryPanel panel : categoryPanels)
                    if (panel.getCategoryName().equals(name))
                        ((AbstractTableModel)panel.table.getModel()).fireTableDataChanged();
                updateCategoryPanel();
                return true;
            });
        }
    }

    /**
     * Move cards between lists.
     * 
     * @param from ID of the list to move from
     * @param to ID of the list to move to
     * @param moves Cards and amounts to move
     * @return <code>true</code> if the cards were successfully moved and <code>false</code> otherwise.
     */
    public boolean moveCards(final int from, final int to, final Map<Card, Integer> moves)
    {
        if (lists.get(from) == null)
            throw new ArrayIndexOutOfBoundsException(from);
        if (lists.get(to) == null)
            throw new ArrayIndexOutOfBoundsException(to);

        return performAction(() -> {
            var selected = parent.getSelectedCards();
            boolean preserve = parent.getSelectedTable().filter((t) -> t == lists.get(from).table).isPresent() &&
                               moves.entrySet().stream().allMatch((e) -> lists.get(from).current.getEntry(e.getKey()).count() == e.getValue());
            if (!lists.get(from).current.removeAll(moves).equals(moves))
                throw new CardException(moves.keySet(), "error moving cards from list " + from);
            if (!lists.get(to).current.addAll(moves))
                throw new CardException(moves.keySet(), "could not move cards to list " + to);
            if (preserve)
                parent.setSelectedComponents(lists.get(to).table, lists.get(to).current);
            updateTables(selected);
            if (preserve)
                lists.get(to).table.scrollRectToVisible(lists.get(to).table.getCellRect(lists.get(to).table.getSelectedRow(), 0, true));
            return true;
        }, () -> {
            var selected = parent.getSelectedCards();
            boolean preserve = parent.getSelectedTable().filter((t) -> t == lists.get(to).table).isPresent() &&
                               moves.entrySet().stream().allMatch((e) -> lists.get(to).current.getEntry(e.getKey()).count() == e.getValue());
            if (!lists.get(from).current.addAll(moves))
                throw new CardException(moves.keySet(), "could not undo move from list " + from);
            if (!lists.get(to).current.removeAll(moves).equals(moves))
                throw new CardException(moves.keySet(), "error undoing move to list " + to);
            if (preserve)
                parent.setSelectedComponents(lists.get(from).table, lists.get(from).current);
            updateTables(selected);
            if (preserve)
                lists.get(from).table.scrollRectToVisible(lists.get(from).table.getCellRect(lists.get(from).table.getSelectedRow(), 0, true));
            return true;
        });
    }

    /**
     * Peform an action that can be undone.  Actions and their inverses should
     * return a boolean value indicating if they were successful.
     * 
     * @param action action to perform and its inverse
     * @return <code>true</code> if the action was successful, and <code>false</code>
     * otherwise.
     */
    private boolean performAction(UndoableAction<Boolean, Boolean> action)
    {
        redoBuffer.clear();
        undoBuffer.push(action);
        return action.redo();
    }

    /**
     * Peform an action that can be undone.  Actions and their inverses should
     * return a boolean value indicating if they were successful.
     * 
     * @param redo action to perform; this gets performed upon calling this method
     * and stored for later in case it needs to be redone
     * @param undo action to perform when undoing the action
     * @return <code>true</code> if the action was successful, and <code>false</code>
     * otherwise.
     */
    private boolean performAction(Supplier<Boolean> redo, Supplier<Boolean> undo)
    {
        return performAction(UndoableAction.createAction(() -> {
            boolean done = redo.get();
            setUnsaved();
            update();
            return done;
        }, () -> {
            boolean done = undo.get();
            setUnsaved();
            update();
            return done;
        }));
    }

    /**
     * Redo the last action that was undone, assuming nothing was done
     * between then and now.
     */
    public boolean redo()
    {
        if (!redoBuffer.isEmpty())
        {
            var action = redoBuffer.pop();
            if (action.redo())
            {
                undoBuffer.push(action);
                return true;
            }
            else
                throw new RuntimeException("error redoing action");
        }
        return false;
    }

    /**
     * Remove some copies of each of a collection of cards from the specified list.
     * 
     * @param id ID of the list to remove cards from
     * @param cards cards to remove
     * @param n number of copies to remove
     * @return <code>true</code> if any copies were removed, and <code>false</code>
     * otherwise.
     */
    public boolean removeCards(int id, Collection<Card> cards, int n)
    {
        return modifyCards(id, cards.stream().collect(Collectors.toMap(Function.identity(), (c) -> -n)));
    }

    /**
     * Remove a category from the deck.
     * 
     * @param name name of the category to remove
     * @return <code>true</code> if the category was removed, and <code>false</code>
     * otherwise.
     */
    public boolean removeCategory(final String name)
    {
        if (deck().current.containsCategory(name))
        {
            final CategorySpec spec = deck().current.getCategorySpec(name);
            return performAction(() -> do_removeCategory(spec), () -> {
                if (deck().current.containsCategory(name))
                    throw new RuntimeException("duplicate category " + name + " found when attempting to undo removal");
                else
                    return do_addCategory(spec);
            });
        }
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
        return file != null && save(file);
    }

    /**
     * Save the deck to the given file (like Save As).
     *
     * @param f file to save to
     * @return true if the file was successfully saved, and false otherwise.
     */
    public boolean save(File f)
    {
        String changes = deck().getChanges();
        if (!changes.isEmpty())
        {
            changelogArea.append("~~~~~" + DeckSerializer.CHANGELOG_DATE.format(new Date()) + "~~~~~\n");
            changelogArea.append(changes + "\n");
        }

        var sideboards = lists.stream().skip(1).filter((l) -> l != null).collect(Collectors.toMap((l) -> l.name.get(), (l) -> l.current));
        DeckSerializer manager = new DeckSerializer(deck().current, sideboards, changelogArea.getText());
        try
        {
            manager.save(f);
            deck().original = new Deck();
            deck().original.addAll(deck().current);
            unsaved = false;
            setFile(manager.file());
            return true;
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(parent, "Error saving " + f.getName() + ": " + e.getMessage() + ".", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
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
    }

    /**
     * Set the background color for the sample hand panel.
     *
     * @param col new background color for the sample hand panel.
     */
    public void setHandBackground(Color col)
    {
        imagePanel.setBackground(col);
        for (Component c : imagePanel.getComponents())
            c.setBackground(col);
        imagePane.getViewport().setBackground(col);
    }

    /**
     * Change the frame title to reflect an unsaved state.
     */
    private void setUnsaved()
    {
        if (!unsaved)
        {
            setTitle(getTitle() + " *");
            unsaved = true;
        }
    }

    /**
     * @return the Deck corresponding to the tab that's currently active in the sideboards
     * panel.
     */
    private CardList sideboard()
    {
        return getSelectedExtraID().map((id) -> lists.get(id).current).orElse(new Deck());
    }

    /**
     * Undo the last action that was performed on the deck.
     * 
     * @return <code>true</code> if the action was successfully undone, and
     * <code>false</code> otherwise.
     */
    public boolean undo()
    {
        if (!undoBuffer.isEmpty())
        {
            var action = undoBuffer.pop();
            if (action.undo())
            {
                redoBuffer.push(action);
                return true;
            }
            else
                throw new RuntimeException("error undoing action");
        }
        return false;
    }

    /**
     * Update the GUI to show the latest state of the deck.
     * XXX: Graphical errors could be attributed to this function
     */
    public void update()
    {
        revalidate();
        repaint();
        for (CategoryPanel panel : categoryPanels)
            panel.update();
    }

    /**
     * Update the categories combo box with all of the current categories.
     */
    public void updateCategoryPanel()
    {
        categoriesContainer.removeAll();
        switchCategoryModel.removeAllElements();

        if (deck().current.categories().isEmpty())
            switchCategoryBox.setEnabled(false);
        else
        {
            switchCategoryBox.setEnabled(true);
            var categories = new ArrayList<>(deck().current.categories());
            categories.sort((a, b) -> sortCategoriesBox.getItemAt(sortCategoriesBox.getSelectedIndex()).compare(deck().current, a, b));

            for (CategorySpec c : categories)
                categoriesContainer.add(getCategoryPanel(c.getName()).get());
            for (CategorySpec c : categories)
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
        countLabel.setText("Total cards: " + deck().current.total());
        landLabel.setText("Lands: " + deck().current.land());
        nonlandLabel.setText("Nonlands: " + deck().current.nonland());

        var cmc = deck().current.stream()
            .filter((c) -> !c.typeContains("land"))
            .flatMap((c) -> Collections.nCopies(deck().current.getEntry(c).count(), c.cmc().stream().min(Double::compare).orElse(0.0)).stream())
            .sorted()
            .collect(Collectors.toList());
        double avgCMC = cmc.stream().mapToDouble(Double::valueOf).average().orElse(0);
        if ((int)avgCMC == avgCMC)
            avgCMCLabel.setText("Average CMC: " + (int)avgCMC);
        else
            avgCMCLabel.setText(String.format("Average CMC: %.2f", avgCMC));

        double medCMC = 0.0;
        if (!cmc.isEmpty())
        {
            if (cmc.size() % 2 == 0)
                medCMC = (cmc.get(cmc.size()/2 - 1) + cmc.get(cmc.size()/2))/2;
            else
                medCMC = cmc.get(cmc.size()/2);
        }
        if ((int)medCMC == medCMC)
            medCMCLabel.setText("Median CMC: " + (int)medCMC);
        else
            medCMCLabel.setText(String.format("Median CMC: %.1f", medCMC));
    }

    /**
     * Update all of the tables and components with the contents of the cards in the
     * deck.
     * 
     * @param selected list of selected cards from <b>before</b> the change to the deck was made
     */
    private void updateTables(Collection<Card> selected)
    {
        updateStats();
        parent.updateCardsInDeck();
        lists.stream().filter((l) -> l != null).forEach((l) -> l.model.fireTableDataChanged());
        for (CategoryPanel c : categoryPanels)
            ((AbstractTableModel)c.table.getModel()).fireTableDataChanged();
        parent.getSelectedTable().ifPresent((t) -> {
            parent.getSelectedList().ifPresent((l) -> {
                for (Card c : selected)
                {
                    if (l.contains(c))
                    {
                        int row = t.convertRowIndexToView(l.indexOf(c));
                        t.addRowSelectionInterval(row, row);
                    }
                }
                if (t.isEditing())
                    t.getCellEditor().cancelCellEditing();
            });
        });
        hand.refresh();
        handCalculations.update();
    }
}
