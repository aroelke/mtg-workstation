package editor.gui.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;
import java.util.function.Supplier;
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
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
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
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
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
import editor.collection.export.CardListFormat;
import editor.database.card.Card;
import editor.database.characteristics.CardAttribute;
import editor.gui.MainFrame;
import editor.gui.SettingsDialog;
import editor.gui.display.CardImagePanel;
import editor.gui.display.CardTable;
import editor.gui.display.CardTableModel;
import editor.gui.generic.CardMenuItems;
import editor.gui.generic.EditablePanel;
import editor.gui.generic.ScrollablePanel;
import editor.gui.generic.TableMouseAdapter;
import editor.gui.generic.VerticalButtonList;
import editor.util.PopupMenuListenerFactory;
import editor.util.UndoableAction;
import editor.util.UnicodeSymbols;

/**
 * This class represents an internal frame for editing a deck.  It contains a table that shows all cards
 * and their counts in the deck as well as zero or more tables for categories within it.  It can add cards
 * to a deck and add, edit, and delete categories.  It is contained within the main frame, which has the
 * inventory from which cards can be added.
 * <p>
 * TODO: Add a filter bar to the main tab just like the inventory has
 * TODO: Add something for calculating probability for multiple categories at once
 * TODO: Instead of a single dedicated extra list for sideboard, etc., start with none and allow adding arbitrary extras in tabs
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
                int had = original.contains(c) ? original.getData(c).count() : 0;
                int has = current.contains(c) ? current.getData(c).count() : 0;
                if (has < had)
                    changes.append("-").append(had - has).append("x ").append(c.unifiedName()).append(" (").append(c.expansion().name).append(")\n");
            }
            for (Card c : current)
            {
                int had = original.contains(c) ? original.getData(c).count() : 0;
                int has = current.contains(c) ? current.getData(c).count() : 0;
                if (had < has)
                    changes.append("+").append(has - had).append("x ").append(c.unifiedName()).append(" (").append(c.expansion().name).append(")\n");
            }
            return changes.toString();
        }

        /**
         * Create a new DeckData using the given Deck.  The original deck
         * will be a copy of it.
         * 
         * @param deck Deck to use as backing data
         */
        public DeckData(Deck deck)
        {
            current = deck;
            original = new Deck();
            original.addAll(deck);
        }

        /**
         * Create a new, empty DeckData.
         */
        public DeckData()
        {
            this(new Deck());
        }
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
         * List to make changes to
         */
        protected DeckData source;

        /**
         * Create a new EditorImportHandler that imports from the given list.
         *
         * @param s list to make changes to
         */
        public EditorImportHandler(DeckData s)
        {
            super();
            source = s;
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
                        if (!source.current.addAll(data))
                            throw new CardException(data.keySet(), "unable to copy cards");
                        updateTables();
                        return true;
                    }, () -> {
                        if (!source.current.removeAll(data).equals(data))
                            throw new CardException(data.keySet(), "unable to undo copy of cards");
                        updateTables();
                        return true;
                    });
                }
                else if (supp.isDataFlavorSupported(Card.cardFlavor))
                {
                    // TODO: Account for multiples
                    final Set<Card> data = Arrays.stream((Card[])supp.getTransferable().getTransferData(Card.cardFlavor)).collect(Collectors.toSet());
                    return performAction(() -> {
                        if (!source.current.addAll(data))
                            throw new CardException(data, "unable to copy cards");
                        updateTables();
                        return true;
                    }, () -> {
                        if (!source.current.removeAll(data).equals(data))
                            throw new CardException(data, "unable to undo copy of cards");
                        updateTables();
                        return true;
                    });
                }
                else
                    return false;
            }
            catch (UnsupportedFlavorException | IOException e)
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
         * Create a new EditorTableTransferHandler that handles transfers to or from
         * the main deck or extra lists.
         *
         * @param s list to make changes to
         */
        public EditorTableTransferHandler(DeckData s)
        {
            super(s);
        }

        @Override
        public Transferable createTransferable(JComponent c)
        {
            return new Deck.TransferData(source.current, parent.getSelectedCards());
        }

        @Override
        public void exportDone(JComponent c, Transferable t, int action)
        {
            if (action == TransferHandler.MOVE)
                source.current.removeAll(parent.getSelectedCards().stream().collect(Collectors.toMap(Function.identity(), (k) -> Integer.MAX_VALUE)));
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
         * @param add    submenu for adding cards
         * @param remove submenu for removing cards
         * @param edit   item for editing card categories
         * @param t      table which will contain the popup
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
            if (parent.getSelectedTable() == table)
            {
                if (parent.getSelectedCards().size() == 1)
                {
                    Card card = parent.getSelectedCard();

                    for (CategorySpec category : deck.current.categories())
                    {
                        if (!category.includes(card))
                        {
                            JMenuItem categoryItem = new JMenuItem(category.getName());
                            categoryItem.addActionListener((e2) -> category.include(card));
                            addToCategoryMenu.add(categoryItem);
                        }
                    }
                    addToCategoryMenu.setVisible(addToCategoryMenu.getItemCount() > 0);

                    for (CategorySpec category : deck.current.categories())
                    {
                        if (category.includes(card))
                        {
                            JMenuItem categoryItem = new JMenuItem(category.getName());
                            categoryItem.addActionListener((e2) -> category.exclude(card));
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

                editCategoriesItem.setVisible(!parent.getSelectedCards().isEmpty() && !deck.current.categories().isEmpty());

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
     * Main deck data.
     */
    private DeckData deck;
    /**
     * Extra list deck data (e.g. sideboard).
     */
    private Map<String, DeckData> extras;
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
     * TODO
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
     * TODO
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

        deck = new DeckData(manager.deck());
        extras = new LinkedHashMap<>();
        for (Map.Entry<String, Deck> sideboard : manager.sideboards().entrySet())
            extras.put(sideboard.getKey(), new DeckData(sideboard.getValue()));
        if (extras.isEmpty())
            extras.put(SettingsDialog.getAsString(SettingsDialog.DEFAULT_SIDEBOARD), new DeckData());

        parent = p;
        unsaved = false;
        undoBuffer = new Stack<>();
        redoBuffer = new Stack<>();
        startingHandSize = SettingsDialog.getAsInt(SettingsDialog.HAND_SIZE);
        if (manager.canSaveFile())
            setFile(manager.file());
        else
            setUnsaved();

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
                    parent.setSelectedCards(deck.table, deck.current);
            }
        });
        for (int i = 0; i < deck.table.getColumnCount(); i++)
            if (deck.model.isCellEditable(0, i))
                deck.table.getColumn(deck.model.getColumnName(i)).setCellEditor(CardTable.createCellEditor(this, deck.model.getColumnData(i)));
        deck.table.setTransferHandler(new EditorTableTransferHandler(deck));
        deck.table.setDragEnabled(true);
        deck.table.setDropMode(DropMode.ON);

        JScrollPane mainDeckPane = new JScrollPane(deck.table);
        mainDeckPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        JPanel mainDeckPanel = new JPanel(new BorderLayout());
        mainDeckPanel.add(mainDeckPane, BorderLayout.CENTER);

        VerticalButtonList deckButtons = new VerticalButtonList("+", String.valueOf(UnicodeSymbols.MINUS), "X");
        deckButtons.get("+").addActionListener((e) -> addCards("", parent.getSelectedCards(), 1));
        deckButtons.get(String.valueOf(UnicodeSymbols.MINUS)).addActionListener((e) -> removeCards("",  parent.getSelectedCards(), 1));
        deckButtons.get("X").addActionListener((e) -> removeCards("",  parent.getSelectedCards(), parent.getSelectedCards().stream().mapToInt((c) -> deck.current.getData(c).count()).reduce(0, Math::max)));
        mainDeckPanel.add(deckButtons, BorderLayout.WEST);
        mainPanel.add(mainDeckPanel, BorderLayout.CENTER);

        JPanel extrasPanel = new JPanel(new BorderLayout());
        mainPanel.add(extrasPanel, BorderLayout.SOUTH);

        VerticalButtonList extrasButtons = new VerticalButtonList("+", String.valueOf(UnicodeSymbols.MINUS), "X");
        extrasButtons.get("+").addActionListener((e) -> addCards(getActiveExtraName(), parent.getSelectedCards(), 1));
        extrasButtons.get(String.valueOf(UnicodeSymbols.MINUS)).addActionListener((e) -> {
            removeCards(getActiveExtraName(), parent.getSelectedCards(), 1);
        });
        extrasButtons.get("X").addActionListener((e) -> removeCards(getActiveExtraName(), parent.getSelectedCards(), parent.getSelectedCards().stream().mapToInt((c) -> sideboard().getData(c).count()).reduce(0, Math::max)));
        extrasPanel.add(extrasButtons, BorderLayout.WEST);

        extrasPane = new JTabbedPane();
        extrasPanel.add(extrasPane, BorderLayout.CENTER);

        listTabs.addTab("Cards", mainPanel);

        // Main table popup menu
        JPopupMenu tableMenu = new JPopupMenu();
        deck.table.addMouseListener(new TableMouseAdapter(deck.table, tableMenu));

        // Add/remove cards
        CardMenuItems tableMenuCardItems = new CardMenuItems(() -> this, parent::getSelectedCards, true);
        tableMenuCardItems.addAddItems(tableMenu);
        tableMenu.add(new JSeparator());
        tableMenuCardItems.addRemoveItems(tableMenu);
        tableMenu.add(new JSeparator());

        // Move cards to sideboard
        tableMenu.add(moveToMenu = new JMenu("Move to"));
        tableMenu.add(moveAllToMenu = new JMenu("Move all to"));
        tableMenu.add(new JSeparator());

        // Quick edit categories
        JMenu addToCategoryMenu = new JMenu("Include in");
        tableMenu.add(addToCategoryMenu);
        JMenu removeFromCategoryMenu = new JMenu("Exclude from");
        tableMenu.add(removeFromCategoryMenu);

        // Edit categories item
        JMenuItem editCategoriesItem = new JMenuItem("Edit Categories...");
        editCategoriesItem.addActionListener((e) -> {
            IncludeExcludePanel iePanel = new IncludeExcludePanel(deck.current.categories().stream().sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName())).collect(Collectors.toList()), parent.getSelectedCards());
            if (JOptionPane.showConfirmDialog(this, new JScrollPane(iePanel), "Set Categories", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
                editInclusion(iePanel.getIncluded(), iePanel.getExcluded());
        });
        tableMenu.add(editCategoriesItem);

        JSeparator categoriesSeparator = new JSeparator();
        tableMenu.add(categoriesSeparator);

        // Edit card tags item
        JMenuItem editTagsItem = new JMenuItem("Edit Tags...");
        editTagsItem.addActionListener((e) -> parent.editTags(parent.getSelectedCards()));
        tableMenu.add(editTagsItem);

        tableMenu.addPopupMenuListener(new TableCategoriesPopupListener(addToCategoryMenu, removeFromCategoryMenu,
                editCategoriesItem, categoriesSeparator, deck.table));

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
        addCategoryButton.addActionListener((e) -> {
            CategorySpec spec = createCategory();
            if (spec != null)
                addCategory(spec);
        });
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
        categoryPanels = new ArrayList<>();

        // The category panel is a vertically-scrollable panel that contains all categories stacked vertically
        // The categories should have a constant height, but fit the container horizontally
        categoriesSuperContainer.add(categoriesContainer, BorderLayout.NORTH);
        categoriesMainPanel.add(new JScrollPane(categoriesSuperContainer, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

        VerticalButtonList categoryButtons = new VerticalButtonList("+", String.valueOf(UnicodeSymbols.MINUS), "X");
        categoryButtons.get("+").addActionListener((e) -> addCards("", parent.getSelectedCards(), 1));
        categoryButtons.get(String.valueOf(UnicodeSymbols.MINUS)).addActionListener((e) -> removeCards("", parent.getSelectedCards(), 1));
        categoryButtons.get("X").addActionListener((e) -> removeCards("", parent.getSelectedCards(), parent.getSelectedCards().stream().mapToInt((c) -> deck.current.getData(c).count()).reduce(0, Math::max)));
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
            for (Card c : hand)
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
            for (Card c : hand)
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

            DefaultListModel<Card> excludeModel = new DefaultListModel<>();
            JList<Card> exclude = new JList<>(excludeModel);
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

            CardTableModel excludeTableModel = new CardTableModel(deck.current, List.of(CardAttribute.NAME, CardAttribute.COUNT));
            CardTable excludeTable = new CardTable(excludeTableModel);
            excludeTable.setStripeColor(SettingsDialog.getAsColor(SettingsDialog.EDITOR_STRIPE));
            excludePanel.add(new JScrollPane(excludeTable));

            addExclusionButton.addActionListener((a) -> {
                for (Card c : Arrays.stream(excludeTable.getSelectedRows()).mapToObj((r) -> deck.current.get(excludeTable.convertRowIndexToModel(r))).collect(Collectors.toList()))
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
                for (Card c : Arrays.stream(exclude.getSelectedIndices()).mapToObj(excludeModel::getElementAt).collect(Collectors.toList()))
                    excludeModel.removeElement(c);
            });

            for (Card c : hand.excluded())
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
        hand.refresh();

        // TODO: Add tabs for deck analysis
        // - category pie chart
        // - mana curve
        // - color distribution (cards/devotion[max,avg,total])
        // - mana production distribution
        // - notes

        // Panel to show the stats of the deck
        JPanel bottomPanel = new JPanel();
        GridBagLayout bottomLayout = new GridBagLayout();
        bottomLayout.columnWidths = new int[]{0, 0};
        bottomLayout.columnWeights = new double[]{1.0, 1.0};
        bottomLayout.rowHeights = new int[]{0};
        bottomLayout.rowWeights = new double[]{1.0};
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
        changelogArea = new JTextArea(manager.changelog());
        changelogArea.setEditable(false);
        changelogPanel.add(new JScrollPane(changelogArea), BorderLayout.CENTER);
        JPanel clearLogPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton clearLogButton = new JButton("Clear Change Log");
        clearLogButton.addActionListener((e) -> {
            if (!changelogArea.getText().isEmpty()
                    && JOptionPane.showConfirmDialog(EditorFrame.this, "This change is permanent.  Clear change log?", "Clear Change Log?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
            {
                changelogArea.setText(""); // TODO: Make this undoable somehow
                setUnsaved();
            }
        });
        clearLogPanel.add(clearLogButton);
        changelogPanel.add(clearLogPanel, BorderLayout.SOUTH);
        listTabs.addTab("Change Log", changelogPanel);

        changelogArea.setText(manager.changelog());

        setTransferHandler(new EditorImportHandler(deck));

        for (CategorySpec spec: deck.current.categories())
            categoryPanels.add(createCategoryPanel(spec));
        updateCategoryPanel();
        handCalculations.update();

        // Initialize extra lists
        for (Map.Entry<String, DeckData> extra : extras.entrySet())
        {
            extrasPane.addTab(extra.getKey(), initExtraList(extra.getKey(), extra.getValue()));
            extrasPane.setTabComponentAt(extrasPane.getTabCount() - 1, new EditablePanel(extra.getKey(), extrasPane));
        }
        extrasPane.addTab("+", null);
        extrasPane.addChangeListener((e) -> {
            int last = extrasPane.getTabCount() - 1;
            if (extrasPane.getSelectedIndex() == last)
            {
                String name = "Sideboard " + extrasPane.getTabCount();
                extrasPane.setTitleAt(last, name);
                extrasPane.setTabComponentAt(last, new EditablePanel(name, extrasPane));
                extras.put(name, new DeckData());
                extrasPane.setComponentAt(last, initExtraList(name, extras.get(name)));
                extrasPane.addTab("+", null);
            }
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
     * TODO
     */
    public boolean addCards(String name, Collection<Card> cards, int n)
    {
        return modifyCards(name, cards.stream().collect(Collectors.toMap(Function.identity(), (c) -> n)));
    }

    /**
     * TODO
     */
    public boolean addCategory(CategorySpec spec)
    {
        if (deck.current.containsCategory(spec.getName()))
            return false;
        else
        {
            return performAction(() -> {
                if (deck.current.containsCategory(spec.getName()))
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
        List<CardAttribute> columns = SettingsDialog.getAsCharacteristics(SettingsDialog.EDITOR_COLUMNS);
        Color stripe = SettingsDialog.getAsColor(SettingsDialog.EDITOR_STRIPE);
        deck.model.setColumns(columns);
        deck.table.setStripeColor(stripe);
        for (int i = 0; i < deck.table.getColumnCount(); i++)
            if (deck.model.isCellEditable(0, i))
                deck.table.getColumn(deck.model.getColumnName(i)).setCellEditor(CardTable.createCellEditor(this, deck.model.getColumnData(i)));
        for (CategoryPanel category : categoryPanels)
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
        for (DeckData extra : extras.values())
            if (extra.table != except)
                extra.table.clearSelection();
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
        final CategoryPanel newCategory = new CategoryPanel(deck.current, spec.getName(), this);
        // When a card is selected in a category, the others should deselect
        newCategory.table.getSelectionModel().addListSelectionListener((e) -> {
            ListSelectionModel lsm = (ListSelectionModel)e.getSource();
            if (!lsm.isSelectionEmpty() && !e.getValueIsAdjusting())
                parent.setSelectedCards(newCategory.table, deck.current.getCategoryList(spec.getName()));
        });
        // Add the behavior for the edit category button
        newCategory.rankBox.addActionListener((e) -> {
            if (newCategory.rankBox.isPopupVisible())
            {
                final int old = deck.current.getCategoryRank(newCategory.getCategoryName());
                final int target = newCategory.rankBox.getSelectedIndex();
                performAction(() -> {
                    deck.current.swapCategoryRanks(newCategory.getCategoryName(), target);

                    for (CategoryPanel panel : categoryPanels)
                        panel.rankBox.setSelectedIndex(deck.current.getCategoryRank(panel.getCategoryName()));
                    listTabs.setSelectedIndex(CATEGORIES);
                    updateCategoryPanel();
                    return true;
                }, () -> {
                    deck.current.swapCategoryRanks(newCategory.getCategoryName(), old);

                    for (CategoryPanel panel : categoryPanels)
                        panel.rankBox.setSelectedIndex(deck.current.getCategoryRank(panel.getCategoryName()));
                    listTabs.setSelectedIndex(CATEGORIES);
                    updateCategoryPanel();
                    return true;
                });
            }
        });
        newCategory.editButton.addActionListener((e) -> editCategory(spec.getName()));
        // Add the behavior for the remove category button
        newCategory.removeButton.addActionListener((e) -> removeCategory(spec.getName()));
        // Add the behavior for the color edit button
        newCategory.colorButton.addActionListener((e) -> {
            final Color newColor = JColorChooser.showDialog(this, "Choose a Color", newCategory.colorButton.color());
            if (newColor != null)
            {
                final Color oldColor = spec.getColor();
                performAction(() -> {
                    spec.setColor(newColor);
                    newCategory.colorButton.setColor(newColor);
                    return true;
                }, () -> {
                    spec.setColor(oldColor);
                    newCategory.colorButton.setColor(oldColor);
                    return true;
                });
            }
        });

        newCategory.table.setTransferHandler(new EditorTableTransferHandler(deck));
        newCategory.table.setDragEnabled(true);

        // Add the behavior for clicking on the category's table
        // Table popup menu
        JPopupMenu tableMenu = new JPopupMenu();
        newCategory.table.addMouseListener(new TableMouseAdapter(newCategory.table, tableMenu));

        CardMenuItems tableMenuCardItems = new CardMenuItems(() -> this, parent::getSelectedCards, true);
        tableMenuCardItems.addAddItems(tableMenu);
        tableMenu.add(new JSeparator());
        tableMenuCardItems.addRemoveItems(tableMenu);

        JSeparator categoriesSeparator = new JSeparator();
        tableMenu.add(categoriesSeparator);

        // Quick edit categories
        JMenu addToCategoryMenu = new JMenu("Include in");
        tableMenu.add(addToCategoryMenu);
        JMenuItem removeFromCategoryItem = new JMenuItem("Exclude from " + spec.getName());
        removeFromCategoryItem.addActionListener((e) -> {
            for (Card c : newCategory.getSelectedCards())
                spec.exclude(c);
            ((AbstractTableModel)newCategory.table.getModel()).fireTableDataChanged();
        });
        tableMenu.add(removeFromCategoryItem);
        JMenu removeFromCategoryMenu = new JMenu("Exclude from");
        tableMenu.add(removeFromCategoryMenu);

        // Edit categories item
        JMenuItem editCategoriesItem = new JMenuItem("Edit Categories...");
        editCategoriesItem.addActionListener((e) -> {
            IncludeExcludePanel iePanel = new IncludeExcludePanel(deck.current.categories().stream().sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName())).collect(Collectors.toList()), parent.getSelectedCards());
            if (JOptionPane.showConfirmDialog(this, new JScrollPane(iePanel), "Set Categories", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
                editInclusion(iePanel.getIncluded(), iePanel.getExcluded());
        });
        tableMenu.add(editCategoriesItem);

        tableMenu.add(new JSeparator());

        // Edit tags item
        JMenuItem editTagsItem = new JMenuItem("Edit Tags...");
        editTagsItem.addActionListener((e) -> parent.editTags(parent.getSelectedCards()));
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
        deleteItem.addActionListener((e) -> deck.current.removeCategory(spec));
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
                    parent.addPreset(spec);
            }
            else
                parent.addPreset(spec);
        });
        categoryMenu.add(addPresetItem);

        newCategory.table.addMouseListener(new TableMouseAdapter(newCategory.table, tableMenu));

        return newCategory;
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
     * TODO
     */
    private boolean do_addCategory(CategorySpec spec)
    {
        deck.current.addCategory(spec);

        CategoryPanel category = createCategoryPanel(spec);
        categoryPanels.add(category);

        for (CategoryPanel c : categoryPanels)
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

        return true;
    }

    /**
     * TODO
     */
    private boolean do_removeCategory(CategorySpec spec)
    {
        deck.current.removeCategory(spec);

        categoryPanels.remove(getCategory(spec.getName()));
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
     * TODO
     */
    public boolean editCategory(String name)
    {
        CategorySpec toEdit = deck.current.getCategorySpec(name);
        if (toEdit == null)
            JOptionPane.showMessageDialog(this, "Deck " + deckName() + " has no category named " + name + ".",
                    "Error", JOptionPane.ERROR_MESSAGE);
        else
        {
            final CategorySpec spec = CategoryEditorPanel.showCategoryEditor(this, toEdit);
            if (spec != null)
            {
                final CategorySpec old = deck.current.getCategorySpec(name);
                return performAction(() -> {
                    deck.current.updateCategory(name, spec);
                    return true;
                }, () -> {
                    deck.current.updateCategory(spec.getName(), old);
                    return true;
                });
            }
        }
        return false;
    }

    /**
     * Change inclusion of cards in categories according to the given maps.
     *
     * @param included map of cards onto the set of categories they should become included in
     * @param excluded map of cards onto the set of categories they should become excluded from
     */
    public void editInclusion(Map<Card, Set<CategorySpec>> included, Map<Card, Set<CategorySpec>> excluded)
    {
        // TODO: Make this all one action
        for (Card card : included.keySet())
            for (CategorySpec category : included.get(card))
                category.include(card);
        for (Card card : excluded.keySet())
            for (CategorySpec category : excluded.get(card))
                category.exclude(card);
    }

    /**
     * Export the deck to a different format.
     *
     * @param format formatter to use for export
     * @param file   file to export to
     * @throws UnsupportedEncodingException
     * @throws FileNotFoundException
     */
    public void export(CardListFormat format, File file) throws UnsupportedEncodingException, FileNotFoundException
    {
        try (PrintWriter wr = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file, false), "UTF8")))
        {
            if (format.hasHeader())
                wr.println(format.header());
            if (!deck.current.isEmpty())
                wr.print(format.format(deck.current));
            for (Map.Entry<String, DeckData> extra : extras.entrySet())
            {
                wr.println();
                wr.println(extra.getKey());
                wr.print(format.format(extra.getValue().current));
            }
        }
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
     * TODO
     */
    public CardList getActiveExtra()
    {
        Deck copy = new Deck();
        copy.addAll(sideboard());
        return copy;
    }

    /**
     * TODO
     */
    public String getActiveExtraName()
    {
        return extrasPane.getTitleAt(extrasPane.getSelectedIndex());
    }

    /**
     * Get the card at the given index in the given table.
     *
     * @param t     table to get the card from
     * @param index index into the given table to get a card from
     * @return the card in the deck at the given index in the given table, if the table is in this EditorFrame.
     */
    public Card getCardAt(CardTable t, int index)
    {
        if (t == deck.table)
            return deck.current.get(deck.table.convertRowIndexToModel(index));
        else
        {
            for (CategoryPanel panel : categoryPanels)
                if (t == panel.table)
                    return deck.current.getCategoryList(panel.getCategoryName()).get(panel.table.convertRowIndexToModel(index));
            throw new IllegalArgumentException("Table not in deck " + deckName());
        }
    }

    public Collection<CategorySpec> getCategories()
    {
        return deck.current.categories();
    }

    /**
     * Get the panel for the category with the specified name in the deck.
     *
     * @param name name of the category to search for
     * @return the panel for the category with the specified name, or null if there is none.
     */
    private CategoryPanel getCategory(String name)
    {
        for (CategoryPanel category : categoryPanels)
            if (category.getCategoryName().equals(name))
                return category;
        return null;
    }

    /**
     * TODO
     */
    public CardList getDeck()
    {
        Deck copy = new Deck();
        copy.addAll(deck.current);
        return copy;
    }

    /**
     * TODO
     */
    public CardList getExtraCards()
    {
        Deck sideboard = new Deck();
        for (DeckData extra : extras.values())
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
     * TODO
     */
    public boolean hasCard(String name, Card card)
    {
        if (name.isEmpty())
            return deck.current.contains(card);
        else
            return extras.get(name).current.contains(card);
    }

    /**
     * Check whether or not this editor has the table with the current selection.
     *
     * @return true if this editor has the table with the current selection and false otherwise.
     */
    public boolean hasSelectedCards()
    {
        CardTable selectedTable = parent.getSelectedTable();
        if (selectedTable == deck.table)
            return true;
        for (DeckData extra : extras.values())
            if (selectedTable == extra.table)
                return true;
        for (CategoryPanel panel : categoryPanels)
            if (selectedTable == panel.table)
                return true;
        return false;
    }

    /**
     * Create and initialize the table, backing model, and menu items relating to a newly-created
     * extra list.
     * 
     * @param name name of the new extra list
     * @param extra data containing cards in the extra list
     * @return the pane that contains the table showing the extra list
     */
    public JScrollPane initExtraList(String name, DeckData extra)
    {
        // Move cards to sideboard
        JMenuItem moveToItem = new JMenuItem(name);
        moveToItem.addActionListener((e) -> {
            final Set<Card> selected = new HashSet<>(parent.getSelectedCards());
            performAction(() -> {
                if (!deck.current.removeAll(selected).equals(selected))
                    throw new CardException(selected, "error moving cards from main deck");
                if (!extra.current.addAll(selected))
                    throw new CardException(selected, "could not move cards to list \"" + name + '"');
                    updateTables();
                return true;
            }, () -> {
                if (!deck.current.addAll(selected))
                    throw new CardException(selected, "could not undo move from main deck");
                if (!extra.current.removeAll(selected).equals(selected))
                    throw new CardException(selected, "error undoing move to list \"" + name + '"');
                updateTables();
                return true;
            });
        });
        moveToMenu.add(moveToItem);
        JMenuItem moveAllToItem = new JMenuItem(name);
        moveAllToItem.addActionListener((e) -> {
            final Map<Card, Integer> moves = parent.getSelectedCards().stream().collect(Collectors.toMap(Function.identity(), (c) -> deck.current.getData(c).count()));
            performAction(() -> {
                if (!deck.current.removeAll(moves).equals(moves))
                    throw new CardException(moves.keySet(), "error moving cards from main deck");
                if (!extra.current.addAll(moves))
                    throw new CardException(moves.keySet(), "could not move cards to list \"" + name + '"');
                updateTables();
                return true;
            }, () -> {
                if (!deck.current.addAll(moves))
                    throw new CardException(moves.keySet(), "could not undo move from main deck");
                if (!extra.current.removeAll(moves).equals(moves))
                    throw new CardException(moves.keySet(), "error undoing move to list \"" + name + '"');
                updateTables();
                return true;
            });
        });
        moveAllToMenu.add(moveAllToItem);

        // Extra list's models
        extra.model = new CardTableModel(this, extra.current, SettingsDialog.getAsCharacteristics(SettingsDialog.EDITOR_COLUMNS));
        extra.table = new CardTable(extra.model)
        {
            @Override
            public Dimension getPreferredScrollableViewportSize()
            {
                Dimension s = super.getPreferredScrollableViewportSize();
                return new Dimension(s.width, getRowHeight() * 5);
            }
        };
        extra.table.setStripeColor(SettingsDialog.getAsColor(SettingsDialog.EDITOR_STRIPE));
        // When a card is selected in a sideboard table, select it for adding
        extra.table.getSelectionModel().addListSelectionListener((e) -> {
            if (!e.getValueIsAdjusting())
            {
                ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                if (!lsm.isSelectionEmpty())
                    parent.setSelectedCards(extra.table, extra.current);
            }
        });
        for (int i = 0; i < extra.table.getColumnCount(); i++)
            if (extra.model.isCellEditable(0, i))
                extra.table.getColumn(extra.model.getColumnName(i)).setCellEditor(CardTable.createCellEditor(this, extra.model.getColumnData(i)));
        extra.table.setTransferHandler(new EditorTableTransferHandler(extra));
        extra.table.setDragEnabled(true);
        extra.table.setDropMode(DropMode.ON);

        JScrollPane sideboardPane = new JScrollPane(extra.table);
        sideboardPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

        // Extra list's table menu
        JPopupMenu extraMenu = new JPopupMenu();
        extra.table.addMouseListener(new TableMouseAdapter(extra.table, extraMenu));

        // Add/remove cards from sideboard
        CardMenuItems sideboardMenuCardItems = new CardMenuItems(() -> this, parent::getSelectedCards, false);
        sideboardMenuCardItems.addAddItems(extraMenu);
        extraMenu.add(new JSeparator());
        sideboardMenuCardItems.addRemoveItems(extraMenu);
        extraMenu.add(new JSeparator());

        // Move cards to main deck
        JMenuItem moveToMainItem = new JMenuItem("Move to Main Deck");
        moveToMainItem.addActionListener((e) -> {
            final Set<Card> selected = new HashSet<>(parent.getSelectedCards());
            performAction(() -> {
                if (!extra.current.removeAll(selected).equals(selected))
                    throw new CardException(selected, "error moving cards from list \"" + name + '"');
                if (!deck.current.addAll(selected))
                    throw new CardException(selected, "could not move cards to main deck");
                updateTables();
                return true;
            }, () -> {
                if (!extra.current.addAll(selected))
                    throw new CardException(selected, "could not undo move from list \"" + name + '"');
                if (!deck.current.removeAll(selected).equals(selected))
                    throw new CardException(selected, "error moving cards to main deck");
                updateTables();
                return true;
            });
        });
        extraMenu.add(moveToMainItem);
        JMenuItem moveAllToMainItem = new JMenuItem("Move All to Main Deck");
        moveAllToMainItem.addActionListener((e) -> {
            final Map<Card, Integer> moves = parent.getSelectedCards().stream().collect(Collectors.toMap(Function.identity(), (c) -> extra.current.getData(c).count()));
            performAction(() -> {
                for (Map.Entry<Card, Integer> move : moves.entrySet())
                {
                    final int actual = extra.current.remove(move.getKey(), move.getValue());
                    if (actual != move.getValue())
                        throw new CardException(move.getKey(), String.format("could only remove %d/%d copies from list \"%s\"", actual, move.getValue(), name));
                    if (!deck.current.add(move.getKey(), move.getValue()))
                        throw new CardException(move.getKey(), String.format("could not add %d copies to main deck", move.getValue()));
                }
                updateTables();
                return true;
            }, () -> {
                for (Map.Entry<Card, Integer> move : moves.entrySet())
                {
                    if (!extra.current.add(move.getKey(), move.getValue()))
                        throw new CardException(move.getKey(), String.format("could undo removal of %d copies to list \"%s\"", move.getValue(), name));
                    int actual = deck.current.remove(move.getKey(), move.getValue());
                    if (actual != move.getValue())
                        throw new CardException(move.getKey(), String.format("could only undo addition of %d/%d copies to main deck", actual, move.getValue()));
                }
                updateTables();
                return true;
            });
        });
        extraMenu.add(moveAllToMainItem);
        extraMenu.add(new JSeparator());

        // Edit card tags item in sideboard
        JMenuItem sBeditTagsItem = new JMenuItem("Edit Tags...");
        sBeditTagsItem.addActionListener((e) -> parent.editTags(parent.getSelectedCards()));
        extraMenu.add(sBeditTagsItem);

/*
        extra.current.addDeckListener((e) -> {
            if (e.cardsChanged())
            {
                updateStats();
                parent.updateCardsInDeck();
                extra.model.fireTableDataChanged();
                for (Card c : parent.getSelectedCards())
                {
                    if (parent.getSelectedList().contains(c))
                    {
                        int row = parent.getSelectedTable().convertRowIndexToView(parent.getSelectedList().indexOf(c));
                        parent.getSelectedTable().addRowSelectionInterval(row, row);
                    }
                }
                if (parent.getSelectedTable().isEditing())
                    parent.getSelectedTable().getCellEditor().cancelCellEditing();
            }

            setUnsaved();
            update();

            if (!undoing)
            {
                redoBuffer.clear();
                undoBuffer.push(e);
            }
        });
*/

        return sideboardPane;
    }

    public boolean modifyCards(final String name, final Map<Card, Integer> changes)
    {
        if (changes.isEmpty() || changes.values().stream().allMatch((n) -> n == 0))
            return false;
        else
        {
            final DeckData target = name.isEmpty() ? deck : extras.get(name);
            final Map<Card, Integer> capped = changes.entrySet().stream().collect(Collectors.toMap(Map.Entry<Card, Integer>::getKey, (e) -> Math.max(e.getValue(), -target.current.getData(e.getKey()).count())));
            return performAction(() -> {
                boolean changed = capped.entrySet().stream().map((e) -> {
                    if (e.getValue() < 0)
                        return target.current.remove(e.getKey(), -e.getValue()) > 0;
                    else if (e.getValue() > 0)
                        return target.current.add(e.getKey(), e.getValue());
                    else
                        return false;
                }).reduce(false, (a, b) -> a || b);
                if (changed)
                    updateTables();
                return changed;
            }, () -> {
                boolean changed = capped.entrySet().stream().map((e) -> {
                    if (e.getValue() < 0)
                        return target.current.add(e.getKey(), -e.getValue());
                    else if (e.getValue() > 0)
                        return target.current.remove(e.getKey(), e.getValue()) > 0;
                    else
                        return false;
                }).reduce(false, (a, b) -> a || b);
                if (changed)
                    updateTables();
                return changed;
            });
        }
    }

    public boolean performAction(Supplier<Boolean> redo, Supplier<Boolean> undo)
    {
        UndoableAction<Boolean, Boolean> action = new UndoableAction<>(() -> {
            boolean done = redo.get();
            setUnsaved();
            update();
            return done;
        }, () -> {
            boolean done = undo.get();
            setUnsaved();
            update();
            return done;
        });
        redoBuffer.clear();
        undoBuffer.push(action);
        return action.redo();
    }

    /**
     * Redo the last action that was undone, assuming nothing was done
     * between then and now.
     */
    public boolean redo()
    {
        if (!redoBuffer.isEmpty())
        {
            UndoableAction<Boolean, Boolean> action = redoBuffer.pop();
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
     * TODO
     */
    public boolean removeCards(String name, Collection<Card> cards, int n)
    {
        return modifyCards(name, cards.stream().collect(Collectors.toMap(Function.identity(), (c) -> -n)));
    }

    /**
     * TODO
     */
    public boolean removeCategory(String name)
    {
        if (deck.current.containsCategory(name))
        {
            final CategorySpec spec = deck.current.getCategorySpec(name);
            return performAction(() -> do_removeCategory(spec), () -> {
                if (deck.current.containsCategory(name))
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
        String changes = deck.getChanges();
        if (!changes.isEmpty())
        {
            changelogArea.append("~~~~~" + DeckSerializer.CHANGELOG_DATE.format(new Date()) + "~~~~~\n");
            changelogArea.append(changes + "\n");
        }

        Map<String, Deck> sideboards = new LinkedHashMap<>();
        for (int i = 0; i < extrasPane.getTabCount() - 1; i++)
            sideboards.put(extrasPane.getTitleAt(i), extras.get(extrasPane.getTitleAt(i)).current);
        DeckSerializer manager = new DeckSerializer(deck.current, sideboards, changelogArea.getText());
        try
        {
            manager.save(f);
            deck.original = new Deck();
            deck.original.addAll(deck.current);
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
        return extras.get(getActiveExtraName()).current;
    }

    /**
     * Undo the last action that was performed on the deck.
     * TODO
     */
    public boolean undo()
    {
        if (!undoBuffer.isEmpty())
        {
            UndoableAction<Boolean, Boolean> action = undoBuffer.pop();
            if (action.undo())
            {
                redoBuffer.push(action);
                return true;
            }
            else
                throw new RuntimeException("error undoing action");
        }
        return false;
/*
        if (!undoBuffer.isEmpty())
        {
            undoing = true;

            Deck.Event action = undoBuffer.pop();
            if (action.cardsChanged())
            {
                action.getSource().addAll(action.cardsRemoved());
                action.getSource().removeAll(action.cardsAdded());
            }
            if (action.categoryRemoved())
            {
                for (DeckData extra : extras.values())
                    if (action.getSource() == extra.current)
                        throw new IllegalStateException("side lists can't have categories");
                deck.current.addCategory(action.removedCategory());
            }
            if (action.categoryAdded())
            {
                for (DeckData extra : extras.values())
                    if (action.getSource() == extra.current)
                        throw new IllegalStateException("side lists can't have categories");
                deck.current.removeCategory(action.addedCategory());
            }
            if (action.categoryChanged())
            {
                for (DeckData extra : extras.values())
                    if (action.getSource() == extra.current)
                        throw new IllegalStateException("side lists can't have categories");
                action.categoryChanges().getSource().copy(action.categoryChanges().oldSpec());
            }
            if (action.ranksChanged())
            {
                for (DeckData extra : extras.values())
                    if (action.getSource() == extra.current)
                        throw new IllegalStateException("side lists can't have categories");
                List<String> categories = new ArrayList<>(action.oldRanks().keySet());
                action.getSource().swapCategoryRanks(categories.get(0), action.oldRanks().get(categories.get(0)));
            }
            redoBuffer.push(action);

            undoing = false;
        }
*/
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

        if (deck.current.categories().isEmpty())
            switchCategoryBox.setEnabled(false);
        else
        {
            switchCategoryBox.setEnabled(true);
            List<CategorySpec> categories = new ArrayList<>(deck.current.categories());
            categories.sort((a, b) -> sortCategoriesBox.getItemAt(sortCategoriesBox.getSelectedIndex()).compare(deck.current, a, b));

            for (CategorySpec c : categories)
                categoriesContainer.add(getCategory(c.getName()));
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
        countLabel.setText("Total cards: " + deck.current.total());
        landLabel.setText("Lands: " + deck.current.land());
        nonlandLabel.setText("Nonlands: " + deck.current.nonland());

        double avgCMC = 0.0;
        for (Card card : deck.current)
            if (!card.typeContains("land"))
                avgCMC += card.minCmc() * deck.current.getData(card).count();
        if (deck.current.nonland() > 0)
            avgCMC /= deck.current.nonland();
        if ((int)avgCMC == avgCMC)
            avgCMCLabel.setText("Average CMC: " + (int)avgCMC);
        else
            avgCMCLabel.setText(String.format("Average CMC: %.2f", avgCMC));

        double medCMC = 0.0;
        List<Double> cmc = new ArrayList<>();
        for (Card card : deck.current)
            if (!card.typeContains("land"))
                for (int i = 0; i < deck.current.getData(card).count(); i++)
                    cmc.add(card.minCmc());
        Collections.sort(cmc);
        if (!cmc.isEmpty())
        {
            if (cmc.size() % 2 == 0)
                medCMC = (cmc.get(cmc.size() / 2 - 1) + cmc.get(cmc.size() / 2)) / 2;
            else
                medCMC = cmc.get(cmc.size() / 2);
        }
        if ((int)medCMC == medCMC)
            medCMCLabel.setText("Median CMC: " + (int)medCMC);
        else
            medCMCLabel.setText(String.format("Median CMC: %.1f", medCMC));
    }

    /**
     * TODO
     */
    public void updateTables()
    {
        updateStats();
        parent.updateCardsInDeck();
        deck.model.fireTableDataChanged();
        for (CategoryPanel c : categoryPanels)
            ((AbstractTableModel)c.table.getModel()).fireTableDataChanged();
        for (DeckData data : extras.values())
            data.model.fireTableDataChanged();
        for (Card c : parent.getSelectedCards())
        {
            if (parent.getSelectedList().contains(c))
            {
                int row = parent.getSelectedTable().convertRowIndexToView(parent.getSelectedList().indexOf(c));
                parent.getSelectedTable().addRowSelectionInterval(row, row);
            }
        }
        if (parent.getSelectedTable().isEditing())
            parent.getSelectedTable().getCellEditor().cancelCellEditing();

        hand.refresh();
        handCalculations.update();
    }
}
