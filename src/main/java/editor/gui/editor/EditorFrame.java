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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;
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
import javax.swing.OverlayLayout;
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
import editor.collection.deck.CategorySpec;
import editor.collection.deck.Deck;
import editor.collection.deck.Hand;
import editor.collection.export.CardListFormat;
import editor.database.card.Card;
import editor.database.characteristics.CardAttribute;
import editor.gui.CardTagPanel;
import editor.gui.MainFrame;
import editor.gui.display.CardImagePanel;
import editor.gui.display.CardTable;
import editor.gui.display.CardTableModel;
import editor.gui.generic.CardMenuItems;
import editor.gui.generic.ChangeTitleListener;
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

        /**
         * Create a copy of a DeckData.
         * 
         * @Param d DeckData to copy
         */
        public DeckData(DeckData d)
        {
            current = new Deck();
            current.addAll(d.current);
            original = new Deck();
            original.addAll(d.original);
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
                    var data = (Map<Card, Integer>)supp.getTransferable().getTransferData(CardList.entryFlavor);
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
                    final var data = Arrays.stream((Card[])supp.getTransferable().getTransferData(Card.cardFlavor)).collect(Collectors.toSet());
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

                    for (CategorySpec category : deck.current.categories())
                    {
                        if (!category.includes(card))
                        {
                            JMenuItem categoryItem = new JMenuItem(category.getName());
                            categoryItem.addActionListener((e2) -> includeIn(card, category));
                            addToCategoryMenu.add(categoryItem);
                        }
                    }
                    addToCategoryMenu.setVisible(addToCategoryMenu.getItemCount() > 0);

                    for (CategorySpec category : deck.current.categories())
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
     * Panel containing components to display when there are no extra lists.
     */
    private JPanel emptyPanel;
    /**
     * Extra list deck data (e.g. sideboard).
     */
    private Map<String, DeckData> extras;
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

        deck = new DeckData(manager.deck());
        extras = new LinkedHashMap<>();

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

        deck.model = new CardTableModel(this, deck.current, SettingsDialog.settings().editor.columns);
        deck.table = new CardTable(deck.model);
        deck.table.setStripeColor(SettingsDialog.settings().editor.stripe);
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
        deckButtons.get("X").addActionListener((e) -> removeCards("",  parent.getSelectedCards(), parent.getSelectedCards().stream().mapToInt((c) -> deck.current.getEntry(c).count()).reduce(0, Math::max)));
        mainDeckPanel.add(deckButtons, BorderLayout.WEST);
        mainPanel.add(mainDeckPanel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel();
        southPanel.setLayout(new OverlayLayout(southPanel));
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        extrasPanel = new JPanel(new BorderLayout());
        extrasPanel.setVisible(false);
        southPanel.add(extrasPanel, BorderLayout.SOUTH);

        VerticalButtonList extrasButtons = new VerticalButtonList("+", String.valueOf(UnicodeSymbols.MINUS), "X");
        extrasButtons.get("+").addActionListener((e) -> getSelectedExtraName().ifPresent((x) -> addCards(x, parent.getSelectedCards(), 1)));
        extrasButtons.get(String.valueOf(UnicodeSymbols.MINUS)).addActionListener((e) -> {
            getSelectedExtraName().ifPresent((x) -> removeCards(x, parent.getSelectedCards(), 1));
        });
        extrasButtons.get("X").addActionListener((e) -> getSelectedExtraName().ifPresent((x) -> {
            removeCards(x, parent.getSelectedCards(), parent.getSelectedCards().stream().mapToInt((c) -> sideboard().getEntry(c).count()).reduce(0, Math::max));
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
        deck.table.addMouseListener(new TableMouseAdapter(deck.table, tableMenu));

        // Add/remove cards
        CardMenuItems tableMenuCardItems = new CardMenuItems(() -> Optional.of(this), parent::getSelectedCards, true);
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
        editTagsItem.addActionListener((e) -> CardTagPanel.editTags(parent.getSelectedCards(), parent));
        tableMenu.add(editTagsItem);

        // Table memu popup listeners
        tableMenu.addPopupMenuListener(new TableCategoriesPopupListener(addToCategoryMenu, removeFromCategoryMenu,
                editCategoriesItem, categoriesSeparator, deck.table));
        tableMenu.addPopupMenuListener(PopupMenuListenerFactory.createVisibleListener((e) -> {
            for (JMenuItem item : tableMenuCardItems)
                item.setEnabled(!parent.getSelectedCards().isEmpty());
            moveToMenu.setEnabled(!extras.isEmpty());
            moveAllToMenu.setEnabled(!extras.isEmpty());
            addToCategoryMenu.setEnabled(!categoryPanels.isEmpty());
            removeFromCategoryMenu.setEnabled(!categoryPanels.isEmpty());
            editCategoriesItem.setEnabled(!categoryPanels.isEmpty());
            editTagsItem.setEnabled(!parent.getSelectedCards().isEmpty());

            moveToMenu.removeAll();
            moveAllToMenu.removeAll();
            for (final var extra : extras.entrySet())
            {
                JMenuItem moveToItem = new JMenuItem(extra.getKey());
                moveToItem.addActionListener((e2) -> {
                    final var selected = new HashSet<>(parent.getSelectedCards());
                    performAction(() -> {
                        if (!deck.current.removeAll(selected).equals(selected))
                            throw new CardException(selected, "error moving cards from main deck");
                        if (!extra.getValue().current.addAll(selected))
                            throw new CardException(selected, "could not move cards to list \"" + extra.getKey() + '"');
                        updateTables();
                        return true;
                    }, () -> {
                        if (!deck.current.addAll(selected))
                            throw new CardException(selected, "could not undo move from main deck");
                        if (!extra.getValue().current.removeAll(selected).equals(selected))
                            throw new CardException(selected, "error undoing move to list \"" + extra.getKey() + '"');
                        updateTables();
                        return true;
                    });
                });
                moveToMenu.add(moveToItem);
                JMenuItem moveAllToItem = new JMenuItem(extra.getKey());
                moveAllToItem.addActionListener((e2) -> {
                    final var moves = parent.getSelectedCards().stream().collect(Collectors.toMap(Function.identity(), (c) -> deck.current.getEntry(c).count()));
                    performAction(() -> {
                        if (!deck.current.removeAll(moves).equals(moves))
                            throw new CardException(moves.keySet(), "error moving cards from main deck");
                        if (!extra.getValue().current.addAll(moves))
                            throw new CardException(moves.keySet(), "could not move cards to list \"" + extra.getKey() + '"');
                        updateTables();
                        return true;
                    }, () -> {
                        if (!deck.current.addAll(moves))
                            throw new CardException(moves.keySet(), "could not undo move from main deck");
                        if (!extra.getValue().current.removeAll(moves).equals(moves))
                            throw new CardException(moves.keySet(), "error undoing move to list \"" + extra.getKey() + '"');
                        updateTables();
                        return true;
                    });
                });
                moveAllToMenu.add(moveAllToItem);
            }
        }));

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
        categoryButtons.get("X").addActionListener((e) -> removeCards("", parent.getSelectedCards(), parent.getSelectedCards().stream().mapToInt((c) -> deck.current.getEntry(c).count()).reduce(0, Math::max)));
        categoriesPanel.add(categoryButtons, BorderLayout.WEST);

        // Sample hands
        JPanel handPanel = new JPanel(new BorderLayout());

        // Table showing the cards in hand
        hand = new Hand(deck.current);

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
            if (hand.size() < deck.current.total())
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
        JButton excludeButton = new JButton("Exclude...");
        excludeButton.addActionListener((e) -> {
            JPanel excludePanel = new JPanel();
            excludePanel.setLayout(new BoxLayout(excludePanel, BoxLayout.X_AXIS));

            var excludeModel = new DefaultListModel<Card>();
            var exclude = new JList<>(excludeModel);
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
            excludeTable.setStripeColor(SettingsDialog.settings().editor.stripe);
            excludePanel.add(new JScrollPane(excludeTable));

            addExclusionButton.addActionListener((a) -> {
                for (Card c : Arrays.stream(excludeTable.getSelectedRows()).mapToObj((r) -> deck.current.get(excludeTable.convertRowIndexToModel(r))).collect(Collectors.toList()))
                {
                    int n = 0;
                    for (int i = 0; i < excludeModel.size(); i++)
                        if (excludeModel.elementAt(i).equals(c))
                            n++;
                    if (n < deck.current.getEntry(c).count())
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
                changelogArea.setText("");
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
        extrasPane.addTab("+", null);
        for (var extra : manager.sideboards().entrySet())
        {
            createExtra(extra.getKey(), extrasPane.getTabCount() - 1);
            extras.get(extra.getKey()).current.addAll(extra.getValue());
            extras.get(extra.getKey()).original.addAll(extra.getValue());
        }
        extrasPane.setSelectedIndex(0);
        Consumer<MouseEvent> addSideboard = (e) -> {
            int index = extrasPanel.isVisible() ? extrasPane.indexAtLocation(e.getX(), e.getY()) : 0;
            int last = extrasPane.getTabCount() - 1;
            if (index == last)
            {
                int i = extrasPane.getTabCount();
                while (extras.containsKey("Sideboard " + i))
                    i++;
                String name = "Sideboard " + i;
                performAction(() -> createExtra(name, last), () -> deleteExtra(name, last));
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
     * @param name name of the list to add to.  An empty string specifies the main deck
     * @param cards cards to add
     * @param n number of copies of each card to add
     * @return <code>true</code> if the cards were added, and <code>false</code> otherwise.
     */
    public boolean addCards(String name, Collection<Card> cards, int n)
    {
        return modifyCards(name, cards.stream().collect(Collectors.toMap(Function.identity(), (c) -> n)));
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
        var columns = SettingsDialog.settings().editor.columns;
        Color stripe = SettingsDialog.settings().editor.stripe;
        deck.model.setColumns(columns);
        deck.table.setStripeColor(stripe);
        for (int i = 0; i < deck.table.getColumnCount(); i++)
            if (deck.model.isCellEditable(0, i))
                deck.table.getColumn(deck.model.getColumnName(i)).setCellEditor(CardTable.createCellEditor(this, deck.model.getColumnData(i)));
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
    public Optional<CategorySpec> createCategory()
    {
        Optional<CategorySpec> spec = Optional.empty();
        do
        {
            (spec = CategoryEditorPanel.showCategoryEditor(this, spec)).ifPresent((s) -> {
            if (deck.current.containsCategory(s.getName()))
                JOptionPane.showMessageDialog(this, "Categories must have unique names.", "Error", JOptionPane.ERROR_MESSAGE);
            });
        } while (spec.isPresent() && deck.current.containsCategory(spec.get().getName()));
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
                parent.setSelectedCards(newCategory.table, deck.current.getCategoryList(newCategory.getCategoryName()));
        });
        // Add the behavior for the edit category button
        newCategory.editButton.addActionListener((e) -> editCategory(newCategory.getCategoryName()));
        // Add the behavior for the remove category button
        newCategory.removeButton.addActionListener((e) -> removeCategory(newCategory.getCategoryName()));
        // Add the behavior for the color edit button
        newCategory.colorButton.addActionListener((e) -> {
            final Color newColor = JColorChooser.showDialog(this, "Choose a Color", newCategory.colorButton.color());
            if (newColor != null)
            {
                final Color oldColor = deck.current.getCategorySpec(newCategory.getCategoryName()).getColor();
                performAction(() -> {
                    CategorySpec mod = deck.current.getCategorySpec(newCategory.getCategoryName());
                    mod.setColor(newColor);
                    deck.current.updateCategory(newCategory.getCategoryName(), mod);
                    return true;
                }, () -> {
                    CategorySpec mod = deck.current.getCategorySpec(newCategory.getCategoryName());
                    mod.setColor(oldColor);
                    deck.current.updateCategory(newCategory.getCategoryName(), mod);
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
                    CategorySpec mod = deck.current.getCategorySpec(newCategory.getCategoryName());
                    mod.setName(title);
                    deck.current.updateCategory(newCategory.getCategoryName(), mod);
                    newCategory.setCategoryName(title);
                    updateCategoryPanel();
                    return true;
                }, () -> {
                    CategorySpec mod = deck.current.getCategorySpec(newCategory.getCategoryName());
                    mod.setName(oldName);
                    deck.current.updateCategory(newCategory.getCategoryName(), mod);
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

        newCategory.table.setTransferHandler(new EditorTableTransferHandler(deck));
        newCategory.table.setDragEnabled(true);

        // Add the behavior for clicking on the category's table
        // Table popup menu
        JPopupMenu tableMenu = new JPopupMenu();
        newCategory.table.addMouseListener(new TableMouseAdapter(newCategory.table, tableMenu));

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
        removeFromCategoryItem.addActionListener((e) -> modifyInclusion(Collections.<Card>emptyList(), newCategory.getSelectedCards(), deck.current.getCategorySpec(newCategory.getCategoryName())));
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
        editTagsItem.addActionListener((e) -> CardTagPanel.editTags(parent.getSelectedCards(), parent));
        tableMenu.add(editTagsItem);

        // Table menu popup listeners
        tableMenu.addPopupMenuListener(new TableCategoriesPopupListener(addToCategoryMenu, removeFromCategoryMenu,
                editCategoriesItem, categoriesSeparator, newCategory.table));
        tableMenu.addPopupMenuListener(PopupMenuListenerFactory.createVisibleListener((e) -> {
            removeFromCategoryItem.setText("Exclude from " + newCategory.getCategoryName());

            for (JMenuItem item : tableMenuCardItems)
                item.setEnabled(!parent.getSelectedCards().isEmpty());
                editTagsItem.setEnabled(!parent.getSelectedCards().isEmpty());
        }));

        
        // Category popup menu
        JPopupMenu categoryMenu = new JPopupMenu();
        newCategory.setComponentPopupMenu(categoryMenu);

        // Cut item
        JMenuItem cutItem = new JMenuItem("Cut");
        cutItem.setEnabled(false);
        categoryMenu.add(cutItem);

        // Copy item
        JMenuItem copyItem = new JMenuItem("Copy");
        copyItem.setEnabled(false);
        categoryMenu.add(copyItem);

        // Paste item
        JMenuItem pasteItem = new JMenuItem("Paste");
        pasteItem.setEnabled(false);
        categoryMenu.add(pasteItem);

        // Edit item
        JMenuItem editItem = new JMenuItem("Edit...");
        editItem.addActionListener((e) -> editCategory(newCategory.getCategoryName()));
        categoryMenu.add(editItem);

        // Delete item
        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener((e) -> deck.current.removeCategory(newCategory.getCategoryName()));
        categoryMenu.add(deleteItem);

        // Add to presets item
        JMenuItem addPresetItem = new JMenuItem("Add to presets");
        addPresetItem.addActionListener((e) -> {
            CategorySpec s = deck.current.getCategorySpec(newCategory.getCategoryName());
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

        newCategory.table.addMouseListener(new TableMouseAdapter(newCategory.table, tableMenu));

        return newCategory;
    }

    /**
     * Create a new extra, uncategorized, untracked list, which usually will be used for a
     * sideboard.
     * 
     * @param name name of the extra list, i.e. "Sideboard"; should be unique
     * @param index index of the tab to insert the new list at
     * @return <code>true</code> if the list was successfully added, and <code>false</code>
     * otherwise.
     */
    private boolean createExtra(String name, int index)
    {
        if (extras.containsKey(name))
            throw new IllegalArgumentException("sideboard \"" + name + "\" already exists");

        if (extras.put(name, new DeckData()) != null)
            return false;
        else
        {
            final EditablePanel panel = new EditablePanel(name, extrasPane);
            extrasPane.insertTab(name, null, initExtraList(name, extras.get(name)), null, index);
            extrasPane.setTabComponentAt(index, panel);
            extrasPane.setSelectedIndex(index);
            extrasPane.getTabComponentAt(extrasPane.getSelectedIndex()).requestFocus();

            extrasPanel.setVisible(!extras.isEmpty());
            emptyPanel.setVisible(extras.isEmpty());

            panel.addActionListener((e) -> {
                switch (e.getActionCommand())
                {
                case EditablePanel.CLOSE:
                    final String n = panel.getTitle();
                    final DeckData extra = new DeckData(extras.get(n));
                    final int i = extrasPane.indexOfTab(n);
                    performAction(() -> deleteExtra(n, i), () -> {
                        boolean success = createExtra(n, i);
                        success |= extras.get(n).current.addAll(extra.current);
                        success |= extras.get(n).original.addAll(extra.original);
                        return success;
                    });
                    break;
                case EditablePanel.EDIT:
                    final String current = panel.getTitle();
                    final String old = panel.getOldTitle();
                    if (current.isEmpty())
                        panel.setTitle(old);
                    else if (extras.containsKey(current))
                    {
                        panel.setTitle(old);
                        JOptionPane.showMessageDialog(EditorFrame.this, "Sideboard \"" + current + "\" already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    else if (!current.equals(old))
                    {
                        final int j = extrasPane.indexOfTab(old);
                        performAction(() -> {
                            extras.put(current, extras.remove(old));
                            ((EditablePanel)extrasPane.getTabComponentAt(j)).setTitle(current);
                            extrasPane.setTitleAt(j, current);
                            return true;
                        }, () -> {
                            extras.put(old, extras.remove(current));
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
     * Delete an extra list.
     * 
     * @param name name of the list to delete
     * @param index index of the tab containing the list
     * @return <code>true</code> if the list was successfully removed, and <code>false</code>
     * otherwise.
     */
    private boolean deleteExtra(String name, int index)
    {
        if (!extras.containsKey(name))
            throw new IllegalArgumentException("missing sideboard \"" + name + '"');

        if (extras.remove(name) == null)
            return false;
        else
        {
            extrasPane.remove(index);
            if (index > 0)
            {
                extrasPane.setSelectedIndex(index - 1);
                extrasPane.getTabComponentAt(extrasPane.getSelectedIndex()).requestFocus();
            }
            extrasPanel.setVisible(!extras.isEmpty());
            emptyPanel.setVisible(extras.isEmpty());

            return true;
        }
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
     * Helper method for removing a category.
     * 
     * @param spec specification of the category to remove
     * @return <code>true</code> if the category was removed, and <code>false</code>
     * otherwise.
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
     * @return <code>true</code> if the category was edited, and <code>false</code>
     * otherwise.
     */
    public boolean editCategory(String name)
    {
        CategorySpec toEdit = deck.current.getCategorySpec(name);
        if (toEdit == null)
            JOptionPane.showMessageDialog(this, "Deck " + deckName() + " has no category named " + name + ".",
                    "Error", JOptionPane.ERROR_MESSAGE);
        return CategoryEditorPanel.showCategoryEditor(this, Optional.of(toEdit)).map((s) -> {
            final CategorySpec old = deck.current.getCategorySpec(name);
            return performAction(() -> {
                if (!deck.current.updateCategory(old.getName(), s).equals(old))
                    throw new RuntimeException("edited unexpected category");
                CategoryPanel panel = getCategory(old.getName());
                panel.setCategoryName(s.getName());
                ((AbstractTableModel)panel.table.getModel()).fireTableDataChanged();
                updateCategoryPanel();
                return true;
            }, () -> {
                if (!deck.current.updateCategory(s.getName(), old).equals(s))
                    throw new RuntimeException("restored from unexpected category");
                CategoryPanel panel = getCategory(s.getName());
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
                        if (deck.current.getCategorySpec(category.getName()).includes(card))
                            throw new IllegalArgumentException(card + " is already in " + category.getName());
                        mods.putIfAbsent(category.getName(), deck.current.getCategorySpec(category.getName()));
                        mods.get(category.getName()).include(card);
                    }
                }
                for (Card card : excluded.keySet())
                {
                    for (CategorySpec category : excluded.get(card))
                    {
                        if (!deck.current.getCategorySpec(category.getName()).includes(card))
                            throw new IllegalArgumentException(card + " is already not in " + category.getName());
                        mods.putIfAbsent(category.getName(), deck.current.getCategorySpec(category.getName()));
                        mods.get(category.getName()).exclude(card);
                    }
                }
                for (var mod : mods.entrySet())
                    deck.current.updateCategory(mod.getKey(), mod.getValue());
                return true;
            }, () -> {
                var mods = new HashMap<String, CategorySpec>();
                for (Card card : included.keySet())
                {
                    for (CategorySpec category : included.get(card))
                    {
                        if (!deck.current.getCategorySpec(category.getName()).includes(card))
                            throw new IllegalArgumentException("error undoing category edit: " + card + " is already not in " + category.getName());
                        mods.putIfAbsent(category.getName(), deck.current.getCategorySpec(category.getName()));
                        mods.get(category.getName()).exclude(card);
                    }
                }
                for (Card card : excluded.keySet())
                {
                    for (CategorySpec category : excluded.get(card))
                    {
                        if (deck.current.getCategorySpec(category.getName()).includes(card))
                            throw new IllegalArgumentException("error undoing category edit: " + card + " is already in " + category.getName());
                        mods.putIfAbsent(category.getName(), deck.current.getCategorySpec(category.getName()));
                        mods.get(category.getName()).include(card);
                    }
                }
                for (var mod : mods.entrySet())
                    deck.current.updateCategory(mod.getKey(), mod.getValue());
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
            for (var extra : extras.entrySet())
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
     * @return a copy of the extra list corresponding to the selected tab.
     */
    public CardList getSelectedExtra()
    {
        Deck copy = new Deck();
        copy.addAll(sideboard());
        return copy;
    }

    /**
     * @return the name of the extra list corresponding to the selected tab.
     */
    public Optional<String> getSelectedExtraName()
    {
        if (extras.isEmpty())
            return Optional.empty();
        else
            return Optional.of(extrasPane.getTitleAt(extrasPane.getSelectedIndex()));
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
     * @return a copy of the main deck.
     */
    public CardList getDeck()
    {
        Deck copy = new Deck();
        copy.addAll(deck.current);
        return copy;
    }

    /**
     * Get the cards in one of the extra lists. Using an empty string (<code>""</code>)
     * returns the main deck.
     *
     * @param name name of the list to get
     * @return a copy of the extra list.
     */
    public CardList getList(String name)
    {
        if (name.isEmpty())
            return getDeck();
        else
        {
            Deck copy = new Deck();
            copy.addAll(extras.get(name).current);
            return copy;
        }
    }

    /**
     * @return a {@link CardList} containing all of the cards in extra lists.
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
     * @param name name of the list to search, with the empty string specifying the main deck
     * @param card card to search for
     * @return <code>true</code> if the specified list contains the specified card, and
     * <code>false</code> otherwise.
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
        return parent.getSelectedTable().map((t) -> {
            if (t == deck.table)
                return true;
            for (DeckData extra : extras.values())
                if (t == extra.table)
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
     * @param name name of the new extra list
     * @param extra data containing cards in the extra list
     * @return the pane that contains the table showing the extra list
     */
    public JScrollPane initExtraList(String name, DeckData extra)
    {
        // Extra list's models
        extra.model = new CardTableModel(this, extra.current, SettingsDialog.settings().editor.columns);
        extra.table = new CardTable(extra.model)
        {
            @Override
            public Dimension getPreferredScrollableViewportSize()
            {
                Dimension s = super.getPreferredScrollableViewportSize();
                return new Dimension(s.width, getRowHeight() * 5);
            }
        };
        extra.table.setStripeColor(SettingsDialog.settings().editor.stripe);
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
        CardMenuItems sideboardMenuCardItems = new CardMenuItems(() -> Optional.of(this), parent::getSelectedCards, false);
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
            final Map<Card, Integer> moves = parent.getSelectedCards().stream().collect(Collectors.toMap(Function.identity(), (c) -> extra.current.getEntry(c).count()));
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
        sBeditTagsItem.addActionListener((e) -> CardTagPanel.editTags(parent.getSelectedCards(), parent));
        extraMenu.add(sBeditTagsItem);

        return sideboardPane;
    }

    /**
     * Change the number of copies of cards in the deck, adding and removing entries as
     * needed.
     * 
     * @param name name of the list to add to, with the empty string representing the main deck
     * @param changes map of card onto integer representing the number of copies of each card to
     * add (positive number) or remove (negative number)
     * @return <code>true</code> if the list deck changed as a result, or <code>false</code>
     * otherwise
     */
    public boolean modifyCards(final String name, final Map<Card, Integer> changes)
    {
        if (changes.isEmpty() || changes.values().stream().allMatch((n) -> n == 0))
            return false;
        else
        {
            var capped = changes.entrySet().stream().collect(Collectors.toMap(Map.Entry<Card, Integer>::getKey, (e) -> Math.max(e.getValue(), -(name.isEmpty() ? deck : extras.get(name)).current.getEntry(e.getKey()).count())));
            return performAction(() -> {
                DeckData target = name.isEmpty() ? deck : extras.get(name);
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
                DeckData target = name.isEmpty() ? deck : extras.get(name);
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
        if (!deck.current.containsCategory(spec.getName()))
            throw new IllegalArgumentException("can't include a card in a category that doesn't exist");
        if (!deck.current.getCategorySpec(spec.getName()).equals(spec))
            throw new IllegalArgumentException("category name matches, but specification doesn't");

        include.removeIf(spec::includes);
        exclude.removeIf((c) -> !spec.includes(c));
        if (include.isEmpty() && exclude.isEmpty())
            return false;
        else
        {
            final String name = spec.getName();
            return performAction(() -> {
                CategorySpec mod = deck.current.getCategorySpec(name);
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
                deck.current.updateCategory(name, mod);
                for (CategoryPanel panel : categoryPanels)
                    if (panel.getCategoryName().equals(name))
                        ((AbstractTableModel)panel.table.getModel()).fireTableDataChanged();
                updateCategoryPanel();
                return true;
            }, () -> {
                CategorySpec mod = deck.current.getCategorySpec(name);
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
                deck.current.updateCategory(name, mod);
                for (CategoryPanel panel : categoryPanels)
                    if (panel.getCategoryName().equals(name))
                        ((AbstractTableModel)panel.table.getModel()).fireTableDataChanged();
                updateCategoryPanel();
                return true;
            });
        }
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
    public boolean performAction(Supplier<Boolean> redo, Supplier<Boolean> undo)
    {
        var action = new UndoableAction<>(() -> {
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
     * @param name name of the list to remove cards from
     * @param cards cards to remove
     * @param n number of copies to remove
     * @return <code>true</code> if any copies were removed, and <code>false</code>
     * otherwise.
     */
    public boolean removeCards(String name, Collection<Card> cards, int n)
    {
        return modifyCards(name, cards.stream().collect(Collectors.toMap(Function.identity(), (c) -> -n)));
    }

    /**
     * Remove a category from the deck.
     * 
     * @param name name of the category to remove
     * @return <code>true</code> if the category was removed, and <code>false</code>
     * otherwise.
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

        var sideboards = new LinkedHashMap<String, Deck>();
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
        return extras.get(getSelectedExtraName().get()).current;
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

        if (deck.current.categories().isEmpty())
            switchCategoryBox.setEnabled(false);
        else
        {
            switchCategoryBox.setEnabled(true);
            var categories = new ArrayList<>(deck.current.categories());
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
                avgCMC += card.minCmc() * deck.current.getEntry(card).count();
        if (deck.current.nonland() > 0)
            avgCMC /= deck.current.nonland();
        if ((int)avgCMC == avgCMC)
            avgCMCLabel.setText("Average CMC: " + (int)avgCMC);
        else
            avgCMCLabel.setText(String.format("Average CMC: %.2f", avgCMC));

        double medCMC = 0.0;
        var cmc = new ArrayList<Double>();
        for (Card card : deck.current)
            if (!card.typeContains("land"))
                for (int i = 0; i < deck.current.getEntry(card).count(); i++)
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
     * Update all of the tables and components with the contents of the cards in the
     * deck.
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
            parent.getSelectedList().ifPresent((l) -> {
                if (l.contains(c))
                {
                    int row = parent.getSelectedTable().get().convertRowIndexToView(l.indexOf(c));
                    parent.getSelectedTable().get().addRowSelectionInterval(row, row);
                }
            });
        }
        parent.getSelectedTable().ifPresent((t) -> {
            if (t.isEditing())
                t.getCellEditor().cancelCellEditing();
        });

        hand.refresh();
        handCalculations.update();
    }
}
