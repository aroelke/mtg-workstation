package editor.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CancellationException;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
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
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import editor.collection.CardList;
import editor.collection.Inventory;
import editor.collection.deck.CategorySpec;
import editor.collection.deck.Deck;
import editor.collection.export.CardListFormat;
import editor.collection.export.DelimitedCardListFormat;
import editor.collection.export.TextCardListFormat;
import editor.database.attributes.CardAttribute;
import editor.database.attributes.Expansion;
import editor.database.attributes.Rarity;
import editor.database.card.Card;
import editor.database.symbol.Symbol;
import editor.database.version.DatabaseVersion;
import editor.database.version.UpdateFrequency;
import editor.filter.Filter;
import editor.filter.leaf.TextFilter;
import editor.gui.ccp.DataFlavors;
import editor.gui.ccp.InventoryTransferData;
import editor.gui.display.CardImagePanel;
import editor.gui.display.CardTable;
import editor.gui.display.CardTableCellRenderer;
import editor.gui.display.CardTableModel;
import editor.gui.editor.DeckLoadException;
import editor.gui.editor.DeckSerializer;
import editor.gui.editor.EditorFrame;
import editor.gui.filter.FilterGroupPanel;
import editor.gui.generic.CardMenuItems;
import editor.gui.generic.ComponentUtils;
import editor.gui.generic.DocumentChangeListener;
import editor.gui.generic.OverwriteFileChooser;
import editor.gui.generic.ScrollablePanel;
import editor.gui.generic.TableMouseAdapter;
import editor.gui.generic.TristateCheckBox;
import editor.gui.generic.VerticalButtonList;
import editor.gui.generic.WizardDialog;
import editor.gui.inventory.InventoryDownloader;
import editor.gui.inventory.InventoryLoader;
import editor.gui.settings.SettingsDialog;
import editor.serialization.AttributeAdapter;
import editor.serialization.CardAdapter;
import editor.serialization.CategoryAdapter;
import editor.serialization.DeckAdapter;
import editor.serialization.FilterAdapter;
import editor.serialization.VersionAdapter;
import editor.serialization.legacy.DeckDeserializer;
import editor.util.ColorAdapter;
import editor.util.MenuListenerFactory;
import editor.util.MouseListenerFactory;
import editor.util.PopupMenuListenerFactory;
import editor.util.UnicodeSymbols;

/**
 * This class represents the main frame of the editor.  It contains several tabs that display information
 * about decks.
 * <p>
 * The frame is divided into three sections:  On the left side is a database of all cards that can be
 * added to decks with a window below it that displays the Oracle text of the currently-selected card.  On
 * the right side is a pane which contains internal frames that allow the user to open, close, and edit
 * multiple decks at once.  See #EditorFrame for details on the editor frames.
 *
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class MainFrame extends JFrame
{
    /**
     * This class represents a renderer for rendering table cells that display text.  If
     * the cell contains text and the card at the row is in the currently-active deck,
     * the cell is rendered bold.
     *
     * @author Alec Roelke
     */
    private class InventoryTableCellRenderer extends CardTableCellRenderer
    {
        /**
         * Create a new CardTableCellRenderer.
         */
        public InventoryTableCellRenderer()
        {
            super();
        }

        /**
         * If the cell is rendered using a JLabel, make that JLabel bold.  Otherwise, just use
         * the default renderer.
         *
         * @param table {@link JTable} to render for
         * @param value value being rendered
         * @param isSelected whether or not the cell is selected
         * @param hasFocus whether or not the table has focus
         * @param row row of the cell being rendered
         * @param column column of the cell being rendered
         * @return The {@link Component} responsible for rendering the table cell.
         */
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            Card card = inventory.get(table.convertRowIndexToModel(row));
            boolean main = selectedFrame.map((f) -> f.hasCard(EditorFrame.MAIN_DECK, card)).orElse(false);
            boolean extra = selectedFrame.map((f) -> f.getExtraCards().contains(card)).orElse(false);
            if (main && extra)
                ComponentUtils.changeFontRecursive(c, c.getFont().deriveFont(Font.BOLD | Font.ITALIC));
            else if (main)
                ComponentUtils.changeFontRecursive(c, c.getFont().deriveFont(Font.BOLD));
            else if (extra)
                ComponentUtils.changeFontRecursive(c, c.getFont().deriveFont(Font.ITALIC));
            return c;
        }
    }

    /**
     * Default height for displaying card images.
     */
    public static final double DEFAULT_CARD_HEIGHT = 1.0/3.0;
    /**
     * Maximum height that the advanced filter editor panel can attain before scrolling.
     */
    public static final int MAX_FILTER_HEIGHT = 300;
    /**
     * Serializer for saving and loading external information.
     */
    public static final Gson SERIALIZER = new GsonBuilder()
        .registerTypeAdapter(CategorySpec.class, new CategoryAdapter())
        .registerTypeHierarchyAdapter(Filter.class, new FilterAdapter())
        .registerTypeAdapter(Color.class, new ColorAdapter())
        .registerTypeHierarchyAdapter(Card.class, new CardAdapter())
        .registerTypeAdapter(CardAttribute.class, new AttributeAdapter())
        .registerTypeAdapter(Deck.class, new DeckAdapter())
        .registerTypeAdapter(DeckSerializer.class, new DeckSerializer())
        .registerTypeAdapter(DatabaseVersion.class, new VersionAdapter())
        .setPrettyPrinting()
        .create();
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
    private static Inventory inventory;

    /**
     * @return The inventory.
     */
    public static Inventory inventory()
    {
        return inventory;
    }

    /**
     * Entry point for the program. All it does is set the look and feel to the
     * system one and create the GUI.
     *
     * @param args arguments to the program
     */
    public static void main(String[] args)
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e)
        {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new MainFrame(Arrays.stream(args).map(File::new).filter(File::exists).collect(Collectors.toList())).setVisible(true));
    }

    /**
     * Table displaying the inventory of all cards.
     */
    private CardTable inventoryTable;
    /**
     * Model for the table displaying the inventory of all cards.
     */
    private CardTableModel inventoryModel;
    /**
     * Pane for showing the Oracle text of the currently-selected card.
     */
    private JTextPane oracleTextPane;
    /**
     * Pane for showing the printed text of the currently-selected card.
     */
    private JTextPane printedTextPane;
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
    private Optional<EditorFrame> selectedFrame;
    /**
     * List of open editor frames.
     */
    private List<EditorFrame> editors;
    /**
     * File chooser for opening and saving.
     */
    private OverwriteFileChooser fileChooser;
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
     * Newest version number of the inventory.
     */
    private DatabaseVersion newestVersion;
    /**
     * Menu showing preset categories.
     */
    private JMenu presetMenu;
    /**
     * Panel displaying the image for the currently selected card.
     */
    private CardImagePanel imagePanel;
    /**
     * Pane displaying the currently-selected card's rulings.
     */
    private JTextPane rulingsPane;
    /**
     * Top menu allowing editing of cards and categories in the selected deck.
     */
    private JMenu deckMenu;
    /**
     * Table containing the currently-selected cards.  Can be null if there is no
     * selection.
     */
    private Optional<CardTable> selectedTable;
    /**
     * List backing the table containing the currently-selected cards.  Can be null
     * if there is no selection.
     */
    private Optional<CardList> selectedList;

    /**
     * Create a new MainFrame.
     */
    public MainFrame(List<File> files)
    {
        super();

        selectedTable = Optional.empty();
        selectedList = Optional.empty();
        untitled = 0;
        selectedFrame = Optional.empty();
        editors = new ArrayList<>();
        recentItems = new LinkedList<>();
        recents = new HashMap<>();

        // Initialize properties to their default values, then load the current values
        // from the properties file
        try
        {
            SettingsDialog.load();
        }
        catch (IOException | JsonParseException e)
        {
            Throwable ex = e;
            while (ex.getCause() != null)
                ex = ex.getCause();
            JOptionPane.showMessageDialog(this, "Error opening " + SettingsDialog.PROPERTIES_FILE + ": " + ex.getMessage() + ".", "Warning", JOptionPane.WARNING_MESSAGE);
            SettingsDialog.resetDefaultSettings();
        }
        try
        {
            versionSite = new URL(SettingsDialog.settings().inventory.versionSite());
        }
        catch (MalformedURLException e)
        {
            JOptionPane.showMessageDialog(this, "Bad version URL: " + SettingsDialog.settings().inventory.versionSite(), "Warning", JOptionPane.WARNING_MESSAGE);
        }
        try
        {
            inventorySite = new URL(SettingsDialog.settings().inventory.url() + ".zip");
        }
        catch (MalformedURLException e)
        {
            JOptionPane.showMessageDialog(this, "Bad file URL: " + SettingsDialog.settings().inventory.url() + ".zip", "Warning", JOptionPane.WARNING_MESSAGE);
        }
        inventoryFile = new File(SettingsDialog.settings().inventory.path());
        recentCount = SettingsDialog.settings().editor.recents.count;
        newestVersion = SettingsDialog.settings().inventory.version;

        setTitle("MTG Deck Editor");
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        Dimension screenRes = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(50, 50, screenRes.width - 100, screenRes.height - 100);

        /* MENU BAR */
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // File menu
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        // New file menu item
        JMenuItem newItem = new JMenuItem("New");
        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        newItem.addActionListener((e) -> selectFrame(newEditor()));
        fileMenu.add(newItem);

        // Open file menu item
        JMenuItem openItem = new JMenuItem("Open...");
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        openItem.addActionListener((e) -> open());
        fileMenu.add(openItem);

        // Close file menu item
        JMenuItem closeItem = new JMenuItem("Close");
        closeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));
        closeItem.addActionListener((e) -> selectedFrame.ifPresentOrElse((f) -> close(f), () -> exit()));
        fileMenu.add(closeItem);

        // Close all files menu item
        JMenuItem closeAllItem = new JMenuItem("Close All");
        closeAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK|InputEvent.SHIFT_DOWN_MASK));
        closeAllItem.addActionListener((e) -> closeAll());
        fileMenu.add(closeAllItem);

        fileMenu.add(new JSeparator());

        // Save file menu item
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        saveItem.addActionListener((e) -> selectedFrame.ifPresent((f) -> save(f)));
        fileMenu.add(saveItem);

        // Save file as menu item
        JMenuItem saveAsItem = new JMenuItem("Save As...");
        saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));
        saveAsItem.addActionListener((e) -> selectedFrame.ifPresent((f) -> saveAs(f)));
        fileMenu.add(saveAsItem);

        // Save all files menu item
        JMenuItem saveAllItem = new JMenuItem("Save All");
        saveAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK|InputEvent.SHIFT_DOWN_MASK));
        saveAllItem.addActionListener((e) -> saveAll());
        fileMenu.add(saveAllItem);

        // Recent files menu
        recentsMenu = new JMenu("Open Recent");
        recentsMenu.setEnabled(false);
        for (String fname : SettingsDialog.settings().editor.recents.files)
            updateRecents(new File(fname));
        fileMenu.add(recentsMenu);

        fileMenu.add(new JSeparator());

        // Import and export items
        final FileNameExtensionFilter text = new FileNameExtensionFilter("Text (*.txt)", "txt");
        final FileNameExtensionFilter delimited = new FileNameExtensionFilter("Delimited (*.csv, *.txt)", "csv", "txt");
        final FileNameExtensionFilter legacy = new FileNameExtensionFilter("Deck from v0.1 or older (*." + DeckDeserializer.EXTENSION + ')', DeckDeserializer.EXTENSION);
        JMenuItem importItem = new JMenuItem("Import...");
        importItem.addActionListener((e) -> {
            JFileChooser importChooser = new JFileChooser();
            importChooser.setAcceptAllFileFilterUsed(false);
            importChooser.addChoosableFileFilter(text);
            importChooser.addChoosableFileFilter(delimited);
            importChooser.addChoosableFileFilter(legacy);
            importChooser.setDialogTitle("Import");
            importChooser.setCurrentDirectory(fileChooser.getCurrentDirectory());
            switch (importChooser.showOpenDialog(this))
            {
            case JFileChooser.APPROVE_OPTION:
                if (importChooser.getFileFilter() == legacy)
                {
                    try
                    {
                        DeckSerializer manager = new DeckSerializer();
                        manager.importLegacy(importChooser.getSelectedFile(), this);
                        selectFrame(newEditor(manager));
                    }
                    catch (DeckLoadException x)
                    {
                        x.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Error opening " + importChooser.getSelectedFile().getName() + ": " + x.getMessage() + ".", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                else
                {
                    CardListFormat format;
                    if (importChooser.getFileFilter() == text)
                    {
                        format = new TextCardListFormat("");
                    }
                    else if (importChooser.getFileFilter() == delimited)
                    {
                        JPanel dataPanel = new JPanel(new BorderLayout());
                        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                        optionsPanel.add(new JLabel("Delimiter: "));
                        var delimiterBox = new JComboBox<>(DelimitedCardListFormat.DELIMITERS);
                        delimiterBox.setEditable(true);
                        optionsPanel.add(delimiterBox);
                        JCheckBox includeCheckBox = new JCheckBox("Read Headers");
                        includeCheckBox.setSelected(true);
                        optionsPanel.add(includeCheckBox);
                        dataPanel.add(optionsPanel, BorderLayout.NORTH);
                        var headersList = new JList<>(CardAttribute.displayableValues());
                        headersList.setEnabled(!includeCheckBox.isSelected());
                        JScrollPane headersPane = new JScrollPane(headersList);
                        Box headersPanel = new Box(BoxLayout.X_AXIS);
                        headersPanel.setBorder(BorderFactory.createTitledBorder("Column Data:"));
                        VerticalButtonList rearrangeButtons = new VerticalButtonList(String.valueOf(UnicodeSymbols.UP_ARROW), String.valueOf(UnicodeSymbols.DOWN_ARROW));
                        for (JButton rearrange : rearrangeButtons)
                            rearrange.setEnabled(!includeCheckBox.isSelected());
                        headersPanel.add(rearrangeButtons);
                        headersPanel.add(Box.createHorizontalStrut(5));
                        var selectedHeadersModel = new DefaultListModel<CardAttribute>();
                        selectedHeadersModel.addElement(CardAttribute.NAME);
                        selectedHeadersModel.addElement(CardAttribute.EXPANSION);
                        selectedHeadersModel.addElement(CardAttribute.CARD_NUMBER);
                        selectedHeadersModel.addElement(CardAttribute.COUNT);
                        selectedHeadersModel.addElement(CardAttribute.DATE_ADDED);
                        var selectedHeadersList = new JList<>(selectedHeadersModel);
                        selectedHeadersList.setEnabled(!includeCheckBox.isSelected());
                        headersPanel.add(new JScrollPane(selectedHeadersList)
                        {
                            @Override
                            public Dimension getPreferredSize()
                            {
                                return headersPane.getPreferredSize();
                            }
                        });
                        headersPanel.add(Box.createHorizontalStrut(5));
                        VerticalButtonList moveButtons = new VerticalButtonList(String.valueOf(UnicodeSymbols.LEFT_ARROW), String.valueOf(UnicodeSymbols.RIGHT_ARROW));
                        for (JButton move : moveButtons)
                            move.setEnabled(!includeCheckBox.isSelected());
                        headersPanel.add(moveButtons);
                        headersPanel.add(Box.createHorizontalStrut(5));
                        headersPanel.add(headersPane);
                        dataPanel.add(headersPanel, BorderLayout.CENTER);
                        rearrangeButtons.get(String.valueOf(UnicodeSymbols.UP_ARROW)).addActionListener((v) -> {
                            var selected = selectedHeadersList.getSelectedValuesList();
                            int ignore = 0;
                            for (int index : selectedHeadersList.getSelectedIndices())
                            {
                                if (index == ignore)
                                {
                                    ignore++;
                                    continue;
                                }
                                CardAttribute temp = selectedHeadersModel.getElementAt(index - 1);
                                selectedHeadersModel.setElementAt(selectedHeadersModel.getElementAt(index), index - 1);
                                selectedHeadersModel.setElementAt(temp, index);
                            }
                            selectedHeadersList.clearSelection();
                            for (CardAttribute type : selected)
                            {
                                int index = selectedHeadersModel.indexOf(type);
                                selectedHeadersList.addSelectionInterval(index, index);
                            }
                        });
                        rearrangeButtons.get(String.valueOf(UnicodeSymbols.DOWN_ARROW)).addActionListener((v) -> {
                            var selected = selectedHeadersList.getSelectedValuesList();
                            var indices = Arrays.stream(selectedHeadersList.getSelectedIndices()).boxed().collect(Collectors.toList());
                            Collections.reverse(indices);
                            int ignore = selectedHeadersModel.size() - 1;
                            for (int index : indices)
                            {
                                if (index == ignore)
                                {
                                    ignore--;
                                    continue;
                                }
                                CardAttribute temp = selectedHeadersModel.getElementAt(index + 1);
                                selectedHeadersModel.setElementAt(selectedHeadersModel.getElementAt(index), index + 1);
                                selectedHeadersModel.setElementAt(temp, index);
                            }
                            selectedHeadersList.clearSelection();
                            for (CardAttribute type : selected)
                            {
                                int index = selectedHeadersModel.indexOf(type);
                                selectedHeadersList.addSelectionInterval(index, index);
                            }
                        });
                        moveButtons.get(String.valueOf(UnicodeSymbols.LEFT_ARROW)).addActionListener((v) -> {
                            for (CardAttribute selected : headersList.getSelectedValuesList())
                                if (!selectedHeadersModel.contains(selected))
                                    selectedHeadersModel.addElement(selected);
                            headersList.clearSelection();
                        });
                        moveButtons.get(String.valueOf(UnicodeSymbols.RIGHT_ARROW)).addActionListener((v) -> {
                            for (CardAttribute selected : new ArrayList<>(selectedHeadersList.getSelectedValuesList()))
                                selectedHeadersModel.removeElement(selected);
                        });
                        includeCheckBox.addActionListener((v) -> {
                            headersList.setEnabled(!includeCheckBox.isSelected());
                            selectedHeadersList.setEnabled(!includeCheckBox.isSelected());
                            for (JButton rearrange : rearrangeButtons)
                                rearrange.setEnabled(!includeCheckBox.isSelected());
                            for (JButton move : moveButtons)
                                move.setEnabled(!includeCheckBox.isSelected());
                        });

                        JPanel previewPanel = new JPanel(new BorderLayout());
                        previewPanel.setBorder(BorderFactory.createTitledBorder("Data to Import:"));
                        JTable previewTable = new JTable()
                        {
                            @Override
                            public Dimension getPreferredScrollableViewportSize()
                            {
                                return new Dimension(0, 0);
                            }

                            @Override
                            public boolean getScrollableTracksViewportWidth()
                            {
                                return getPreferredSize().width < getParent().getWidth();
                            }
                        };
                        previewTable.setAutoCreateRowSorter(true);
                        previewPanel.add(new JScrollPane(previewTable));

                        ActionListener updateTable = (v) -> {
                            try
                            {
                                DefaultTableModel model = new DefaultTableModel();
                                var lines = Files.readAllLines(importChooser.getSelectedFile().toPath());
                                if (includeCheckBox.isSelected())
                                {
                                    String[] columns = lines.remove(0).split(String.valueOf(delimiterBox.getSelectedItem()));
                                    String[][] data = lines.stream().map((s) -> DelimitedCardListFormat.split(delimiterBox.getSelectedItem().toString(), s)).toArray(String[][]::new);
                                    model.setDataVector(data, columns);
                                }
                                else
                                {
                                    CardAttribute[] columns = new CardAttribute[selectedHeadersModel.size()];
                                    for (int i = 0; i < selectedHeadersModel.size(); i++)
                                        columns[i] = selectedHeadersModel.getElementAt(i);
                                    String[][] data = lines.stream().map((s) -> DelimitedCardListFormat.split(String.valueOf(delimiterBox.getSelectedItem()), s)).toArray(String[][]::new);
                                    model.setDataVector(data, columns);
                                }
                                previewTable.setModel(model);
                            }
                            catch (IOException x)
                            {
                                JOptionPane.showMessageDialog(this, "Could not import " + importChooser.getSelectedFile() + ": " + x.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        };
                        delimiterBox.addActionListener(updateTable);
                        includeCheckBox.addActionListener(updateTable);
                        for (JButton rearrange : rearrangeButtons)
                            rearrange.addActionListener(updateTable);
                        for (JButton move : moveButtons)
                            move.addActionListener(updateTable);
                        updateTable.actionPerformed(null);

                        if (WizardDialog.showWizardDialog(this, "Import Wizard", dataPanel, previewPanel) == WizardDialog.FINISH_OPTION)
                        {
                            var selected = new ArrayList<CardAttribute>(selectedHeadersModel.size());
                            for (int i = 0; i < selectedHeadersModel.size(); i++)
                                selected.add(selectedHeadersModel.getElementAt(i));
                            format = new DelimitedCardListFormat(String.valueOf(delimiterBox.getSelectedItem()), selected, !includeCheckBox.isSelected());
                        }
                        else
                            return;
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(this, "Could not import " + importChooser.getSelectedFile() + '.', "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    DeckSerializer manager = new DeckSerializer();
                    try
                    {
                        manager.importList(format, importChooser.getSelectedFile(), this);
                    }
                    catch (DeckLoadException x)
                    {
                        JOptionPane.showMessageDialog(this, "Could not import " + importChooser.getSelectedFile() + ": " + x.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    finally
                    {
                        selectFrame(newEditor(manager));
                    }
                }
                break;
            case JFileChooser.CANCEL_OPTION:
                break;
            case JFileChooser.ERROR_OPTION:
                JOptionPane.showMessageDialog(this, "Could not import " + importChooser.getSelectedFile() + '.', "Error", JOptionPane.ERROR_MESSAGE);
                break;
            }
        });
        fileMenu.add(importItem);
        JMenuItem exportItem = new JMenuItem("Export...");
        exportItem.addActionListener((e) -> selectedFrame.ifPresent((f) -> {
            JFileChooser exportChooser = new OverwriteFileChooser();
            exportChooser.setAcceptAllFileFilterUsed(false);
            exportChooser.addChoosableFileFilter(text);
            exportChooser.addChoosableFileFilter(delimited);
            exportChooser.setDialogTitle("Export");
            exportChooser.setCurrentDirectory(fileChooser.getCurrentDirectory());
            switch (exportChooser.showSaveDialog(this))
            {
            case JFileChooser.APPROVE_OPTION:
                CardListFormat format;

                // Common pieces of the wizard
                JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                JCheckBox sortCheck = new JCheckBox("Sort by:", true);
                sortPanel.add(sortCheck);
                var sortBox = new JComboBox<>(CardAttribute.displayableValues());
                sortBox.setSelectedItem(CardAttribute.NAME);
                sortCheck.addItemListener((v) -> sortBox.setEnabled(sortCheck.isSelected()));
                sortPanel.add(sortBox);

                Map<String, Boolean> extras = new LinkedHashMap<>();
                for (String extra : f.getExtraNames())
                    extras.put(extra, true);
                JPanel extrasPanel = new JPanel(new BorderLayout());
                extrasPanel.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, extrasPanel.getBackground()));
                TristateCheckBox includeExtras = new TristateCheckBox("Include additional lists:", TristateCheckBox.State.SELECTED);
                extrasPanel.add(includeExtras, BorderLayout.NORTH);
                extrasPanel.setBackground(UIManager.getColor("List.background"));
                Box extrasList = new Box(BoxLayout.Y_AXIS);
                extrasList.setBorder(BorderFactory.createLineBorder(UIManager.getColor("List.dropLineColor")));
                for (String extra : extras.keySet())
                {
                    JCheckBox extraBox = new JCheckBox(extra, extras.get(extra));
                    extraBox.setBackground(extrasPanel.getBackground());
                    extraBox.addActionListener((v) -> {
                        extras.put(extra, extraBox.isSelected());
                        long n = extras.values().stream().filter((b) -> b).count();
                        if (n == 0)
                            includeExtras.setSelected(false);
                        else if (n < extras.size())
                            includeExtras.setState(TristateCheckBox.State.INDETERMINATE);
                        else // n == extra.size()
                            includeExtras.setSelected(true);
                        SwingUtilities.invokeLater(() -> includeExtras.repaint());
                    });
                    includeExtras.addActionListener((v) -> {
                        extraBox.setSelected(includeExtras.getState() == TristateCheckBox.State.SELECTED);
                        extras.put(extra, extraBox.isSelected());
                        SwingUtilities.invokeLater(() -> extraBox.repaint());
                    });
                    extrasList.add(extraBox);
                }
                extrasPanel.add(extrasList, BorderLayout.CENTER);

                // File-format-specific pieces of the wizard
                if (exportChooser.getFileFilter() == text)
                {
                    Box wizardPanel = new Box(BoxLayout.Y_AXIS);
                    Box fieldPanel = new Box(BoxLayout.Y_AXIS);
                    fieldPanel.setBorder(BorderFactory.createTitledBorder("List Format:"));
                    JTextField formatField = new JTextField(TextCardListFormat.DEFAULT_FORMAT);
                    formatField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, formatField.getFont().getSize()));
                    formatField.setColumns(50);
                    fieldPanel.add(formatField);
                    JPanel addDataPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    addDataPanel.add(new JLabel("Add Data: "));
                    var addDataBox = new JComboBox<>(CardAttribute.displayableValues());
                    fieldPanel.add(addDataPanel);
                    addDataPanel.add(addDataBox);
                    wizardPanel.add(fieldPanel);

                    if (f.getDeck().total() > 0 || f.getExtraCards().total() > 0)
                    {
                        JPanel previewPanel = new JPanel(new BorderLayout());
                        previewPanel.setBorder(BorderFactory.createTitledBorder("Preview:"));
                        JTextArea previewArea = new JTextArea();
                        JScrollPane previewPane = new JScrollPane(previewArea);
                        previewArea.setText(new TextCardListFormat(formatField.getText())
                                .format(f.getDeck().total() > 0 ? f.getDeck() : f.getExtraCards()));
                        previewArea.setRows(1);
                        previewArea.setCaretPosition(0);
                        previewPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                        previewPanel.add(previewPane, BorderLayout.CENTER);
                        wizardPanel.add(previewPanel);

                        addDataBox.addActionListener((v) -> {
                            int pos = formatField.getCaretPosition();
                            String data = '{' + String.valueOf(addDataBox.getSelectedItem()).toLowerCase() + '}';
                            String t = formatField.getText().substring(0, pos) + data;
                            if (pos < formatField.getText().length())
                                t += formatField.getText().substring(formatField.getCaretPosition());
                            formatField.setText(t);
                            formatField.setCaretPosition(pos + data.length());
                            formatField.requestFocusInWindow();
                        });

                        formatField.getDocument().addDocumentListener(new DocumentChangeListener()
                        {
                            @Override
                            public void update(DocumentEvent e)
                            {
                                previewArea.setText(new TextCardListFormat(formatField.getText())
                                        .format(f.getDeck().total() > 0 ? f.getDeck() : f.getExtraCards()));
                                previewArea.setCaretPosition(0);
                            }
                        });
                    }

                    if (!extras.isEmpty())
                        wizardPanel.add(extrasPanel);

                    if (f.getDeck().total() > 0 || f.getExtraCards().total() > 0)
                        wizardPanel.add(sortPanel);

                    if (WizardDialog.showWizardDialog(this, "Export Wizard", wizardPanel) == WizardDialog.FINISH_OPTION)
                        format = new TextCardListFormat(formatField.getText());
                    else
                        return;
                }
                else if (exportChooser.getFileFilter() == delimited)
                {
                    var panels = new ArrayList<JComponent>();
                    Box dataPanel = new Box(BoxLayout.Y_AXIS);
                    var headersList = new JList<>(CardAttribute.displayableValues());
                    JScrollPane headersPane = new JScrollPane(headersList);
                    Box headersPanel = new Box(BoxLayout.X_AXIS);
                    headersPanel.setBorder(BorderFactory.createTitledBorder("Column Data:"));
                    VerticalButtonList rearrangeButtons = new VerticalButtonList(String.valueOf(UnicodeSymbols.UP_ARROW), String.valueOf(UnicodeSymbols.DOWN_ARROW));
                    headersPanel.add(rearrangeButtons);
                    headersPanel.add(Box.createHorizontalStrut(5));
                    var selectedHeadersModel = new DefaultListModel<CardAttribute>();
                    selectedHeadersModel.addElement(CardAttribute.NAME);
                    selectedHeadersModel.addElement(CardAttribute.EXPANSION);
                    selectedHeadersModel.addElement(CardAttribute.CARD_NUMBER);
                    selectedHeadersModel.addElement(CardAttribute.COUNT);
                    selectedHeadersModel.addElement(CardAttribute.DATE_ADDED);
                    var selectedHeadersList = new JList<>(selectedHeadersModel);
                    headersPanel.add(new JScrollPane(selectedHeadersList)
                    {
                        @Override
                        public Dimension getPreferredSize()
                        {
                            return headersPane.getPreferredSize();
                        }
                    });
                    headersPanel.add(Box.createHorizontalStrut(5));
                    VerticalButtonList moveButtons = new VerticalButtonList(String.valueOf(UnicodeSymbols.LEFT_ARROW), String.valueOf(UnicodeSymbols.RIGHT_ARROW));
                    headersPanel.add(moveButtons);
                    headersPanel.add(Box.createHorizontalStrut(5));
                    headersPanel.add(headersPane);
                    dataPanel.add(headersPanel);

                    rearrangeButtons.get(String.valueOf(UnicodeSymbols.UP_ARROW)).addActionListener((v) -> {
                        var selected = selectedHeadersList.getSelectedValuesList();
                        int ignore = 0;
                        for (int index : selectedHeadersList.getSelectedIndices())
                        {
                            if (index == ignore)
                            {
                                ignore++;
                                continue;
                            }
                            CardAttribute temp = selectedHeadersModel.getElementAt(index - 1);
                            selectedHeadersModel.setElementAt(selectedHeadersModel.getElementAt(index), index - 1);
                            selectedHeadersModel.setElementAt(temp, index);
                        }
                        selectedHeadersList.clearSelection();
                        for (CardAttribute type : selected)
                        {
                            int index = selectedHeadersModel.indexOf(type);
                            selectedHeadersList.addSelectionInterval(index, index);
                        }
                    });
                    rearrangeButtons.get(String.valueOf(UnicodeSymbols.DOWN_ARROW)).addActionListener((v) -> {
                        var selected = selectedHeadersList.getSelectedValuesList();
                        var indices = Arrays.stream(selectedHeadersList.getSelectedIndices()).boxed().collect(Collectors.toList());
                        Collections.reverse(indices);
                        int ignore = selectedHeadersModel.size() - 1;
                        for (int index : indices)
                        {
                            if (index == ignore)
                            {
                                ignore--;
                                continue;
                            }
                            CardAttribute temp = selectedHeadersModel.getElementAt(index + 1);
                            selectedHeadersModel.setElementAt(selectedHeadersModel.getElementAt(index), index + 1);
                            selectedHeadersModel.setElementAt(temp, index);
                        }
                        selectedHeadersList.clearSelection();
                        for (CardAttribute type : selected)
                        {
                            int index = selectedHeadersModel.indexOf(type);
                            selectedHeadersList.addSelectionInterval(index, index);
                        }
                    });
                    moveButtons.get(String.valueOf(UnicodeSymbols.LEFT_ARROW)).addActionListener((v) -> {
                        for (CardAttribute selected : headersList.getSelectedValuesList())
                            if (!selectedHeadersModel.contains(selected))
                                selectedHeadersModel.addElement(selected);
                        headersList.clearSelection();
                    });
                    moveButtons.get(String.valueOf(UnicodeSymbols.RIGHT_ARROW)).addActionListener((v) -> {
                        for (CardAttribute selected : new ArrayList<>(selectedHeadersList.getSelectedValuesList()))
                            selectedHeadersModel.removeElement(selected);
                    });

                    JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    optionsPanel.add(new JLabel("Delimiter: "));
                    var delimiterBox = new JComboBox<>(DelimitedCardListFormat.DELIMITERS);
                    delimiterBox.setEditable(true);
                    optionsPanel.add(delimiterBox);
                    JCheckBox includeCheckBox = new JCheckBox("Include Headers");
                    includeCheckBox.setSelected(true);
                    optionsPanel.add(includeCheckBox);
                    dataPanel.add(optionsPanel);

                    dataPanel.add(sortPanel);
                    panels.add(dataPanel);

                    if (!extras.isEmpty())
                        panels.add(extrasPanel);

                    if (WizardDialog.showWizardDialog(this, "Export Wizard", panels) == WizardDialog.FINISH_OPTION)
                    {
                        var selected = new ArrayList<CardAttribute>(selectedHeadersModel.size());
                        for (int i = 0; i < selectedHeadersModel.size(); i++)
                            selected.add(selectedHeadersModel.getElementAt(i));
                        format = new DelimitedCardListFormat(String.valueOf(delimiterBox.getSelectedItem()), selected, includeCheckBox.isSelected());
                    }
                    else
                        return;
                }
                else
                {
                    JOptionPane.showMessageDialog(this, "Could not export " + f.deckName() + '.', "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try
                {
                    f.export(
                        format,
                        sortCheck.isSelected() ? sortBox.getItemAt(sortBox.getSelectedIndex()).comparingCard() : (a, b) -> 0,
                        extras.entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).collect(Collectors.toList()),
                        exportChooser.getSelectedFile()
                    );
                }
                catch (UnsupportedEncodingException | FileNotFoundException x)
                {
                    JOptionPane.showMessageDialog(this, "Could not export " + f.deckName() + ": " + x.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                break;
            case JFileChooser.CANCEL_OPTION:
                break;
            case JFileChooser.ERROR_OPTION:
                JOptionPane.showMessageDialog(this, "Could not export " + f.deckName() + '.', "Error", JOptionPane.ERROR_MESSAGE);
                break;
            }
        }));
        fileMenu.add(exportItem);

        fileMenu.add(new JSeparator());

        // Exit menu item
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener((e) -> exit());
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK));
        fileMenu.add(exitItem);

        // File menu listener
        fileMenu.addMenuListener(MenuListenerFactory.createSelectedListener((e) -> {
            closeItem.setEnabled(selectedFrame.isPresent());
            closeAllItem.setEnabled(!editors.isEmpty());
            saveItem.setEnabled(selectedFrame.map(EditorFrame::getUnsaved).orElse(false));
            saveAsItem.setEnabled(selectedFrame.isPresent());
            saveAllItem.setEnabled(editors.stream().map(EditorFrame::getUnsaved).reduce(false, (a, b) -> a || b));
            exportItem.setEnabled(selectedFrame.isPresent());
        }));
        // Items are enabled while hidden so their listeners can be used
        fileMenu.addMenuListener(MenuListenerFactory.createDeselectedListener((e) -> {
            closeItem.setEnabled(true);
            closeAllItem.setEnabled(true);
            saveItem.setEnabled(true);
            saveAsItem.setEnabled(true);
            saveAllItem.setEnabled(true);
            exportItem.setEnabled(true);
        }));

        // Edit menu
        JMenu editMenu = new JMenu("Edit");
        menuBar.add(editMenu);

        // Cut, copy, paste
        JMenuItem editCutItem = new JMenuItem("Cut");
        editCutItem.addActionListener((e) -> selectedTable.ifPresent((t) -> TransferHandler.getCutAction().actionPerformed(new ActionEvent(t, ActionEvent.ACTION_PERFORMED, null))));
        editCutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        editMenu.add(editCutItem);
        JMenuItem editCopyItem = new JMenuItem("Copy");
        editCopyItem.addActionListener((e) -> selectedTable.ifPresent((t) -> TransferHandler.getCopyAction().actionPerformed(new ActionEvent(t, ActionEvent.ACTION_PERFORMED, null))));
        editCopyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        editMenu.add(editCopyItem);
        JMenuItem editPasteItem = new JMenuItem("Paste");
        editPasteItem.addActionListener((e) -> selectedTable.ifPresent((t) -> TransferHandler.getPasteAction().actionPerformed(new ActionEvent(t, ActionEvent.ACTION_PERFORMED, null))));
        editPasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        editMenu.add(editPasteItem);
        editMenu.add(new JSeparator());

        // Undo menu item
        JMenuItem undoItem = new JMenuItem("Undo");
        undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
        undoItem.addActionListener((e) -> selectedFrame.ifPresent(EditorFrame::undo));
        editMenu.add(undoItem);

        // Redo menu item
        JMenuItem redoItem = new JMenuItem("Redo");
        redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
        redoItem.addActionListener((e) -> selectedFrame.ifPresent(EditorFrame::redo));
        editMenu.add(redoItem);

        editMenu.add(new JSeparator());

        // Preferences menu item
        JMenuItem preferencesItem = new JMenuItem("Preferences...");
        preferencesItem.addActionListener((e) -> {
            SettingsDialog settings = new SettingsDialog(this);
            settings.setVisible(true);
        });
        editMenu.add(preferencesItem);

        // Edit menu listener
        editMenu.addMenuListener(MenuListenerFactory.createSelectedListener((e) -> {
            editCutItem.setEnabled(
                selectedList.filter((l) -> l == inventory).isEmpty() && !getSelectedCards().isEmpty()
            );
            editCopyItem.setEnabled(!getSelectedCards().isEmpty());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            editPasteItem.setEnabled(clipboard.isDataFlavorAvailable(DataFlavors.entryFlavor) || clipboard.isDataFlavorAvailable(DataFlavors.cardFlavor));

            undoItem.setEnabled(selectedFrame.isPresent());
            redoItem.setEnabled(selectedFrame.isPresent());
        }));
        // Items are enabled while hidden so their listeners can be used
        editMenu.addMenuListener(MenuListenerFactory.createDeselectedListener((e) -> {
            undoItem.setEnabled(true);
            redoItem.setEnabled(true);
        }));

        // Deck menu
        deckMenu = new JMenu("Deck");
        deckMenu.setEnabled(false);
        menuBar.add(deckMenu);

        // Add/Remove card menus
        JMenu addMenu = new JMenu("Add Cards");
        deckMenu.add(addMenu);
        JMenu removeMenu = new JMenu("Remove Cards");
        deckMenu.add(removeMenu);
        CardMenuItems deckMenuCardItems = new CardMenuItems(() -> selectedFrame, this::getSelectedCards, true);
        deckMenuCardItems.addAddItems(addMenu);
        deckMenuCardItems.addRemoveItems(removeMenu);
        deckMenuCardItems.addSingle().setAccelerator(KeyStroke.getKeyStroke('+'));
        deckMenuCardItems.fillPlayset().setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
        deckMenuCardItems.removeSingle().setAccelerator(KeyStroke.getKeyStroke('-'));
        deckMenuCardItems.removeAll().setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK));

        // Sideboard menu
        JMenu sideboardMenu = new JMenu("Sideboard");
        deckMenu.add(sideboardMenu);
        CardMenuItems sideboardMenuItems = new CardMenuItems(() -> selectedFrame, this::getSelectedCards, false);
        sideboardMenu.add(sideboardMenuItems.addSingle());
        sideboardMenu.add(sideboardMenuItems.addN());
        sideboardMenu.add(sideboardMenuItems.removeSingle());
        sideboardMenu.add(sideboardMenuItems.removeAll());

        // Category menu
        JMenu categoryMenu = new JMenu("Category");
        deckMenu.add(categoryMenu);

        // Add category item
        JMenuItem addCategoryItem = new JMenuItem("Add...");
        addCategoryItem.addActionListener((e) -> selectedFrame.ifPresent((f) -> f.createCategory().ifPresent(f::addCategory)));
        categoryMenu.add(addCategoryItem);

        // Edit category item
        JMenuItem editCategoryItem = new JMenuItem("Edit...");
        editCategoryItem.addActionListener((e) -> selectedFrame.ifPresent((f) -> {
            JPanel contentPanel = new JPanel(new BorderLayout());
            contentPanel.add(new JLabel("Choose a category to edit:"), BorderLayout.NORTH);
            var categories = new JList<>(f.getCategories().stream().map(CategorySpec::getName).sorted().toArray(String[]::new));
            categories.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            contentPanel.add(new JScrollPane(categories), BorderLayout.CENTER);
            if (JOptionPane.showConfirmDialog(this, contentPanel, "Edit Category", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
                f.editCategory(categories.getSelectedValue());
        }));
        categoryMenu.add(editCategoryItem);

        // Remove category item
        JMenuItem removeCategoryItem = new JMenuItem("Remove...");
        removeCategoryItem.addActionListener((e) -> selectedFrame.ifPresent((f) -> {
            JPanel contentPanel = new JPanel(new BorderLayout());
            contentPanel.add(new JLabel("Choose a category to remove:"), BorderLayout.NORTH);
            var categories = new JList<>(f.getCategories().stream().map(CategorySpec::getName).sorted().toArray(String[]::new));
            categories.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            contentPanel.add(new JScrollPane(categories), BorderLayout.CENTER);
            if (JOptionPane.showConfirmDialog(this, contentPanel, "Edit Category", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
                f.removeCategory(categories.getSelectedValue());
        }));
        categoryMenu.add(removeCategoryItem);

        // Preset categories menu
        presetMenu = new JMenu("Add Preset");
        categoryMenu.add(presetMenu);

        // Deck menu listener
        deckMenu.addMenuListener(MenuListenerFactory.createSelectedListener((e) -> {
            addMenu.setEnabled(selectedFrame.isPresent() && !getSelectedCards().isEmpty());
            removeMenu.setEnabled(selectedFrame.isPresent() && !getSelectedCards().isEmpty());
            sideboardMenu.setEnabled(selectedFrame.map((f) -> f.getSelectedExtraName().isPresent()).orElse(false) && !getSelectedCards().isEmpty());
            presetMenu.setEnabled(presetMenu.getMenuComponentCount() > 0);
        }));
        // Items are enabled while hidden so their listeners can be used.
        deckMenu.addMenuListener(MenuListenerFactory.createDeselectedListener((e) -> {
            addMenu.setEnabled(true);
            removeMenu.setEnabled(true);
        }));

        // Help menu
        JMenu helpMenu = new JMenu("Help");
        menuBar.add(helpMenu);

        // Inventory update item
        JMenuItem updateInventoryItem = new JMenuItem("Check for inventory update...");
        updateInventoryItem.addActionListener((e) -> {
            switch (checkForUpdate(UpdateFrequency.DAILY))
            {
            case UPDATE_NEEDED:
                if (updateInventory())
                {
                    SettingsDialog.setInventoryVersion(newestVersion);
                    loadInventory();
                }
                break;
            case NO_UPDATE:
                JOptionPane.showMessageDialog(this, "Inventory is up to date.");
                break;
            case UPDATE_CANCELLED:
                break;
            default:
                break;
            }
        });
        helpMenu.add(updateInventoryItem);

        // Reload inventory item
        JMenuItem reloadInventoryItem = new JMenuItem("Reload inventory...");
        reloadInventoryItem.addActionListener((e) -> loadInventory());
        helpMenu.add(reloadInventoryItem);

        helpMenu.add(new JSeparator());

        // Show expansions item
        JMenuItem showExpansionsItem = new JMenuItem("Show Expansions...");
        showExpansionsItem.addActionListener((e) -> {
            TableModel expansionTableModel = new AbstractTableModel()
            {
                private final String[] columns = {
                    "Expansion",
                    "Block",
                    "Code",
                    "magiccards.info",
                    "Gatherer"
                };

                @Override
                public int getColumnCount()
                {
                    return 5;
                }

                @Override
                public String getColumnName(int index)
                {
                    return columns[index];
                }

                @Override
                public int getRowCount()
                {
                    return Expansion.expansions.length;
                }

                @Override
                public Object getValueAt(int rowIndex, int columnIndex)
                {
                    final Object[] values = {
                        Expansion.expansions[rowIndex].name,
                        Expansion.expansions[rowIndex].block,
                        Expansion.expansions[rowIndex].code,
                        Expansion.expansions[rowIndex].magicCardsInfoCode,
                        Expansion.expansions[rowIndex].gathererCode
                    };
                    return values[columnIndex];
                }
            };
            JTable expansionTable = new JTable(expansionTableModel);
            expansionTable.setShowGrid(false);
            expansionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            expansionTable.setAutoCreateRowSorter(true);
            expansionTable.setPreferredScrollableViewportSize(new Dimension(600, expansionTable.getPreferredScrollableViewportSize().height));

            JOptionPane.showMessageDialog(this, new JScrollPane(expansionTable), "Expansions", JOptionPane.PLAIN_MESSAGE);
        });
        helpMenu.add(showExpansionsItem);

        /* CONTENT PANE */
        // Panel containing all content
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_DOWN_MASK), "Next Frame");
        contentPane.getActionMap().put("Next Frame", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (!editors.isEmpty())
                    selectedFrame.ifPresentOrElse((f) -> selectFrame(editors.get((editors.indexOf(f) + 1)%editors.size())), () -> selectFrame(editors.get(editors.size() - 1)));
            }
        });
        contentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_DOWN_MASK), "Previous Frame");
        contentPane.getActionMap().put("Previous Frame", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (!editors.isEmpty())
                {
                    int next = selectedFrame.map((f) -> editors.indexOf(f) - 1).orElse(0);
                    selectFrame(editors.get(next < 0 ? editors.size() - 1 : next));
                }
            }
        });
        setContentPane(contentPane);

        // DesktopPane containing editor frames
        decklistDesktop = new JDesktopPane();
        decklistDesktop.setBackground(SystemColor.controlShadow);

        JTabbedPane cardPane = new JTabbedPane();

        // Panel showing the image of the currently-selected card
        cardPane.addTab("Image", imagePanel = new CardImagePanel());
        setImageBackground(SettingsDialog.settings().inventory.background);

        // Pane displaying the Oracle text
        oracleTextPane = new JTextPane();
        oracleTextPane.setEditable(false);
        oracleTextPane.setCursor(new Cursor(Cursor.TEXT_CURSOR));
        cardPane.addTab("Oracle Text", new JScrollPane(oracleTextPane));

        printedTextPane = new JTextPane();
        printedTextPane.setEditable(false);
        printedTextPane.setCursor(new Cursor(Cursor.TEXT_CURSOR));
        cardPane.addTab("Printed Text", new JScrollPane(printedTextPane));

        rulingsPane = new JTextPane();
        rulingsPane.setEditable(false);
        rulingsPane.setCursor(new Cursor(Cursor.TEXT_CURSOR));
        cardPane.addTab("Rulings", new JScrollPane(rulingsPane));

        // Oracle text pane popup menu
        JPopupMenu oraclePopupMenu = new JPopupMenu();
        oracleTextPane.setComponentPopupMenu(oraclePopupMenu);
        printedTextPane.setComponentPopupMenu(oraclePopupMenu);
        imagePanel.setComponentPopupMenu(oraclePopupMenu);

        // Copy
        JMenuItem oracleCopy = new JMenuItem("Copy");
        oracleCopy.addActionListener((e) -> TransferHandler.getCopyAction().actionPerformed(new ActionEvent(imagePanel, ActionEvent.ACTION_PERFORMED, null)));
        oraclePopupMenu.add(oracleCopy);
        oraclePopupMenu.add(new JSeparator());

        // Add the card to the main deck
        CardMenuItems oracleMenuCardItems = new CardMenuItems(() -> selectedFrame, () -> Arrays.asList(getSelectedCards().get(0)), true);
        JSeparator[] oracleMenuCardSeparators = new JSeparator[] { new JSeparator(), new JSeparator() };
        oracleMenuCardItems.addAddItems(oraclePopupMenu);
        oraclePopupMenu.add(oracleMenuCardSeparators[0]);
        oracleMenuCardItems.addRemoveItems(oraclePopupMenu);
        oraclePopupMenu.add(oracleMenuCardSeparators[1]);

        // Add the card to the sideboard
        CardMenuItems oracleMenuSBCardItems = new CardMenuItems(() -> selectedFrame, () -> Arrays.asList(getSelectedCards().get(0)), false);
        oracleMenuSBCardItems.addSingle().setText("Add to Sideboard");
        oraclePopupMenu.add(oracleMenuSBCardItems.addSingle());
        oracleMenuSBCardItems.addN().setText("Add to Sideboard...");
        oraclePopupMenu.add(oracleMenuSBCardItems.addN());
        oracleMenuSBCardItems.removeSingle().setText("Remove from Sideboard");
        oraclePopupMenu.add(oracleMenuSBCardItems.removeSingle());
        oracleMenuSBCardItems.removeAll().setText("Remove All from Sideboard");
        oraclePopupMenu.add(oracleMenuSBCardItems.removeAll());
        JSeparator oracleMenuSBSeparator = new JSeparator();
        oraclePopupMenu.add(oracleMenuSBSeparator);

        JMenuItem oracleEditTagsItem = new JMenuItem("Edit Tags...");
        oracleEditTagsItem.addActionListener((e) -> CardTagPanel.editTags(getSelectedCards(), this));
        oraclePopupMenu.add(oracleEditTagsItem);

        // Popup listener for oracle popup menu
        oraclePopupMenu.addPopupMenuListener(PopupMenuListenerFactory.createVisibleListener((e) -> {
            oracleCopy.setEnabled(!getSelectedCards().isEmpty());
            oracleMenuCardItems.setVisible(selectedFrame.isPresent() && !getSelectedCards().isEmpty());
            for (JSeparator sep : oracleMenuCardSeparators)
                sep.setVisible(selectedFrame.isPresent() && !getSelectedCards().isEmpty());
            oracleMenuSBCardItems.setVisible(selectedFrame.map((f) -> !f.getExtraNames().isEmpty()).orElse(false) && !getSelectedCards().isEmpty());
            oracleMenuSBSeparator.setVisible(selectedFrame.map((f) -> !f.getExtraNames().isEmpty()).orElse(false) && !getSelectedCards().isEmpty());
            oracleEditTagsItem.setEnabled(!getSelectedCards().isEmpty());
        }));

        // Copy handler for image panel
        imagePanel.setTransferHandler(new TransferHandler()
        {
            @Override
            public boolean canImport(TransferHandler.TransferSupport support)
            {
                return false;
            }

            @Override
            protected Transferable createTransferable(JComponent c)
            {
                return new InventoryTransferData(getSelectedCards().get(0));
            }

            @Override
            public int getSourceActions(JComponent c)
            {
                return TransferHandler.COPY;
            }
        });

        // Panel containing inventory and image of currently-selected card
        JPanel inventoryPanel = new JPanel(new BorderLayout(0, 0));
        inventoryPanel.setPreferredSize(new Dimension(getWidth()/4, getHeight()*3/4));

        // Panel containing the inventory and the quick-filter bar
        JPanel tablePanel = new JPanel(new BorderLayout(0, 0));
        inventoryPanel.add(tablePanel, BorderLayout.CENTER);

        // Panel containing the quick-filter bar
        Box filterPanel = new Box(BoxLayout.X_AXIS);

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

        // Create the inventory and put it in the table
        inventoryTable = new CardTable();
        inventoryTable.setDefaultRenderer(String.class, new InventoryTableCellRenderer());
        inventoryTable.setDefaultRenderer(Integer.class, new InventoryTableCellRenderer());
        inventoryTable.setDefaultRenderer(Rarity.class, new InventoryTableCellRenderer());
        inventoryTable.setDefaultRenderer(List.class, new InventoryTableCellRenderer());
        inventoryTable.setStripeColor(SettingsDialog.settings().inventory.stripe);
        inventoryTable.addMouseListener(MouseListenerFactory.createClickListener((e) -> selectedFrame.ifPresent((f) -> {
            if (e.getClickCount() % 2 == 0)
                f.addCards(EditorFrame.MAIN_DECK, getSelectedCards(), 1);
        })));
        inventoryTable.setTransferHandler(new TransferHandler()
        {
            @Override
            public boolean canImport(TransferHandler.TransferSupport support)
            {
                return false;
            }

            @Override
            protected Transferable createTransferable(JComponent c)
            {
                return new InventoryTransferData(getSelectedCards());
            }

            @Override
            public int getSourceActions(JComponent c)
            {
                return TransferHandler.COPY;
            }
        });
        inventoryTable.setDragEnabled(true);
        tablePanel.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);

        // Table popup menu
        JPopupMenu inventoryMenu = new JPopupMenu();
        inventoryTable.addMouseListener(new TableMouseAdapter(inventoryTable, inventoryMenu));

        // Copy
        JMenuItem inventoryCopy = new JMenuItem("Copy");
        inventoryCopy.addActionListener((e) -> TransferHandler.getCopyAction().actionPerformed(new ActionEvent(inventoryTable, ActionEvent.ACTION_PERFORMED, null)));
        inventoryCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        inventoryMenu.add(inventoryCopy);
        inventoryMenu.add(new JSeparator());

        // Add cards to the main deck
        CardMenuItems inventoryMenuCardItems = new CardMenuItems(() -> selectedFrame, this::getSelectedCards, true);
        JSeparator[] inventoryMenuCardSeparators = new JSeparator[] { new JSeparator(), new JSeparator() };
        inventoryMenuCardItems.addAddItems(inventoryMenu);
        inventoryMenu.add(inventoryMenuCardSeparators[0]);
        inventoryMenuCardItems.addRemoveItems(inventoryMenu);
        inventoryMenu.add(inventoryMenuCardSeparators[1]);

        // Add cards to the sideboard
        CardMenuItems inventoryMenuSBItems = new CardMenuItems(() -> selectedFrame, this::getSelectedCards, false);
        inventoryMenuSBItems.addSingle().setText("Add to Sideboard");
        inventoryMenu.add(inventoryMenuSBItems.addSingle());
        inventoryMenuSBItems.addN().setText("Add to Sideboard...");
        inventoryMenu.add(inventoryMenuSBItems.addN());
        inventoryMenuSBItems.removeSingle().setText("Remove from Sideboard");
        inventoryMenu.add(inventoryMenuSBItems.removeSingle());
        inventoryMenuSBItems.removeAll().setText("Remove All from Sideboard");
        inventoryMenu.add(inventoryMenuSBItems.removeAll());
        JSeparator inventoryMenuSBSeparator = new JSeparator();
        inventoryMenu.add(inventoryMenuSBSeparator);

        // Edit tags item
        JMenuItem editTagsItem = new JMenuItem("Edit Tags...");
        editTagsItem.addActionListener((e) -> CardTagPanel.editTags(getSelectedCards(), this));
        inventoryMenu.add(editTagsItem);

        // Inventory menu listener
        inventoryMenu.addPopupMenuListener(PopupMenuListenerFactory.createVisibleListener((e) -> {
            inventoryMenuCardItems.setVisible(selectedFrame.isPresent() && !getSelectedCards().isEmpty());
            for (JSeparator sep : inventoryMenuCardSeparators)
                sep.setVisible(selectedFrame.isPresent() && !getSelectedCards().isEmpty());
            inventoryMenuSBItems.setVisible(selectedFrame.map((f) -> !f.getExtraNames().isEmpty()).orElse(false) && !getSelectedCards().isEmpty());
            inventoryMenuSBSeparator.setVisible(selectedFrame.map((f) -> !f.getExtraNames().isEmpty()).orElse(false) && !getSelectedCards().isEmpty());
            editTagsItem.setEnabled(!getSelectedCards().isEmpty());
        }));

        // Action to be taken when the user presses the Enter key after entering text into the quick-filter
        // bar
        nameFilterField.addActionListener((e) -> {
            inventory.updateFilter(TextFilter.createQuickFilter(CardAttribute.NAME, nameFilterField.getText().toLowerCase()));
            inventoryModel.fireTableDataChanged();
        });

        // Action to be taken when the clear button is pressed (reset the filter)
        clearButton.addActionListener((e) -> {
            nameFilterField.setText("");
            inventory.updateFilter(CardAttribute.createFilter(CardAttribute.ANY));
            inventoryModel.fireTableDataChanged();
        });

        // Action to be taken when the advanced filter button is pressed (show the advanced filter
        // dialog)
        advancedFilterButton.addActionListener((e) -> {
            FilterGroupPanel panel = new FilterGroupPanel();
            if (inventory.getFilter().equals(CardAttribute.createFilter(CardAttribute.ANY)))
                panel.setContents(CardAttribute.createFilter(CardAttribute.NAME));
            else
                panel.setContents(inventory.getFilter());
            panel.addChangeListener((c) -> SwingUtilities.getWindowAncestor((Component)c.getSource()).pack());

            ScrollablePanel panelPanel = new ScrollablePanel(new BorderLayout(), ScrollablePanel.TRACK_WIDTH)
            {
                @Override
                public Dimension getPreferredScrollableViewportSize()
                {
                    Dimension size = panel.getPreferredSize();
                    size.height = Math.min(MAX_FILTER_HEIGHT, size.height);
                    return size;
                }
            };
            panelPanel.add(panel, BorderLayout.CENTER);

            JScrollPane panelPane = new JScrollPane(panelPanel);
            panelPane.setBorder(BorderFactory.createEmptyBorder());
            if (JOptionPane.showConfirmDialog(this, panelPane, "Advanced Filter", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
            {
                nameFilterField.setText("");
                inventory.updateFilter(panel.filter());
                inventoryModel.fireTableDataChanged();
            }
        });

        // Split panes dividing the panel into three sections.  They can be resized at will.
        JSplitPane inventorySplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, cardPane, inventoryPanel);
        inventorySplit.setOneTouchExpandable(true);
        inventorySplit.setContinuousLayout(true);
        SwingUtilities.invokeLater(() -> inventorySplit.setDividerLocation(DEFAULT_CARD_HEIGHT));
        JSplitPane editorSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inventorySplit, decklistDesktop);
        editorSplit.setOneTouchExpandable(true);
        editorSplit.setContinuousLayout(true);
        contentPane.add(editorSplit, BorderLayout.CENTER);

        // File chooser
        fileChooser = new OverwriteFileChooser(SettingsDialog.settings().cwd);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Deck (*.json)", "json"));
        fileChooser.setAcceptAllFileFilterUsed(true);

        // Handle what happens when the window tries to close and when it opens.
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                exit();
            }

            @Override
            public void windowOpened(WindowEvent e)
            {
                if (checkForUpdate(SettingsDialog.settings().inventory.update) == UPDATE_NEEDED && updateInventory())
                    SettingsDialog.setInventoryVersion(newestVersion);
                loadInventory();
                TableSelectionListener listener = new TableSelectionListener(MainFrame.this, inventoryTable, inventory);
                inventoryTable.addMouseListener(listener);
                inventoryTable.getSelectionModel().addListSelectionListener(listener);

                if (!inventory.isEmpty())
                {
                    for (CategorySpec spec : SettingsDialog.settings().editor.categories.presets)
                    {
                        JMenuItem categoryItem = new JMenuItem(spec.getName());
                        categoryItem.addActionListener((v) -> selectedFrame.ifPresent((f) -> f.addCategory(spec)));
                        presetMenu.add(categoryItem);
                    }
                    for (File f : files)
                        open(f);
                }
            }
        });
    }

    /**
     * Add a new preset category to the preset categories list.
     *
     * @param category new preset category to add
     */
    public void addPreset(CategorySpec category)
    {
        CategorySpec spec = new CategorySpec(category);
        spec.getBlacklist().clear();
        spec.getWhitelist().clear();
        SettingsDialog.addPresetCategory(spec);
        JMenuItem categoryItem = new JMenuItem(spec.getName());
        categoryItem.addActionListener((e) -> selectedFrame.ifPresent((f) -> f.addCategory(spec)));
        presetMenu.add(categoryItem);
    }

    /**
     * Apply the global settings.
     */
    public void applySettings()
    {
        try
        {
            inventorySite = new URL(SettingsDialog.settings().inventory.url() + ".zip");
        }
        catch (MalformedURLException e)
        {
            JOptionPane.showMessageDialog(this, "Bad file URL: " + SettingsDialog.settings().inventory.url() + ".zip", "Warning", JOptionPane.WARNING_MESSAGE);
        }
        inventoryFile = new File(SettingsDialog.settings().inventory.path());
        recentCount = SettingsDialog.settings().editor.recents.count;
        inventoryModel.setColumns(SettingsDialog.settings().inventory.columns);
        inventoryTable.setStripeColor(SettingsDialog.settings().inventory.stripe);
        for (EditorFrame frame : editors)
            frame.applySettings();
        presetMenu.removeAll();
        for (CategorySpec spec : SettingsDialog.settings().editor.categories.presets)
        {
            JMenuItem categoryItem = new JMenuItem(spec.getName());
            categoryItem.addActionListener((e) -> selectedFrame.ifPresent((f) -> f.addCategory(spec)));
            presetMenu.add(categoryItem);
        }
        setImageBackground(SettingsDialog.settings().inventory.background);
        setHandBackground(SettingsDialog.settings().editor.hand.background);

        revalidate();
        repaint();
    }

    /**
     * Check to see if the inventory needs to be updated.  If it does, ask the user if it should be.
     *
     * @param freq desired frequency for downloading updates
     * @return an integer value representing the state of the update.  It can be:
     * {@link #UPDATE_NEEDED}
     * {@link #NO_UPDATE}
     * {@link #UPDATE_CANCELLED}
     */
    public int checkForUpdate(UpdateFrequency freq)
    {
        try
        {
            if (!inventoryFile.exists())
            {
                JOptionPane.showMessageDialog(this, inventoryFile.getName() + " not found.  It will be downloaded.", "Update", JOptionPane.WARNING_MESSAGE);
                try (BufferedReader in = new BufferedReader(new InputStreamReader(versionSite.openStream())))
                {
                    newestVersion = new DatabaseVersion(new JsonParser().parse(in.lines().collect(Collectors.joining())).getAsJsonObject().get("version").getAsString());
                }
                return UPDATE_NEEDED;
            }
            else if (SettingsDialog.settings().inventory.update != UpdateFrequency.NEVER)
            {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(versionSite.openStream())))
                {
                    newestVersion = new DatabaseVersion(new JsonParser().parse(in.lines().collect(Collectors.joining())).getAsJsonObject().get("version").getAsString());
                }
                if (newestVersion.needsUpdate(SettingsDialog.settings().inventory.version, freq))
                {
                    int wantUpdate = JOptionPane.showConfirmDialog(
                        this,
                        "Inventory is out of date:\n" +
                        UnicodeSymbols.BULLET + " Current version: " + SettingsDialog.settings().inventory.version + "\n" +
                        UnicodeSymbols.BULLET + " Latest version: " + newestVersion + "\n" +
                        "\n" +
                        "Download update?",
                        "Update",
                        JOptionPane.YES_NO_OPTION
                    );
                    return wantUpdate == JOptionPane.YES_OPTION ? UPDATE_NEEDED : UPDATE_CANCELLED;
                }
            }
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(this, "Error connecting to server: " + e.getMessage() + ".", "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
        catch (ParseException e)
        {
            JOptionPane.showMessageDialog(this, "Could not parse version \"" + e.getMessage() + '"', "Error", JOptionPane.ERROR_MESSAGE);
        }
        return NO_UPDATE;
    }

    /**
     * Attempt to close the specified frame.
     *
     * @param frame frame to close
     * @return true if the frame was closed, and false otherwise.
     */
    public boolean close(EditorFrame frame)
    {
        if (!editors.contains(frame) || !frame.close())
            return false;
        else
        {
            if (frame.hasSelectedCards())
            {
                selectedTable = Optional.empty();
                selectedList = Optional.empty();
            }
            editors.remove(frame);
            if (editors.size() > 0)
                selectFrame(editors.get(0));
            else
            {
                selectedFrame = Optional.empty();
                deckMenu.setEnabled(false);
            }
            revalidate();
            repaint();
            return true;
        }
    }

    /**
     * Attempts to close all of the open editors.  If any can't be closed for
     * whatever reason, they will remain open, but the rest will still be closed.
     *
     * @return true if all open editors were successfully closed, and false otherwise.
     */
    public boolean closeAll()
    {
        var e = new ArrayList<>(editors);
        boolean closedAll = true;
        for (EditorFrame editor : e)
            closedAll &= close(editor);
        return closedAll;
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
     * @param id multiverseid of the #Card to look for
     * @return the #Card with the given multiverseid.
     */
    public Card getCard(long id)
    {
        return inventory.get(id);
    }

    /**
     * Get the currently-selected card(s).
     *
     * @return a List containing each currently-selected card in the inventory table.
     */
    public List<Card> getSelectedCards()
    {
        return selectedList.flatMap((l) -> selectedTable.map((t) -> Arrays.stream(t.getSelectedRows())
            .mapToObj((r) -> l.get(t.convertRowIndexToModel(r)))
            .collect(Collectors.toList()))).orElse(Collections.emptyList());
    }

    /**
     * Get the list corresponding to the table with the currently-selected cards.
     *
     * @return the list containing the currently selected cards
     */
    public Optional<CardList> getSelectedList()
    {
        return selectedList;
    }

    /**
     * Get the table containing the currently selected cards.
     *
     * @return the table with the selected cards
     */
    public Optional<CardTable> getSelectedTable()
    {
        return selectedTable;
    }

    /**
     * Check whether or not the inventory has a selection.
     *
     * @return true if the inventory has a selection, and false otherwise.
     */
    public boolean hasSelectedCards()
    {
        return selectedList.filter((l) -> l == inventory).isPresent();
    }

    /**
     * Load the inventory and initialize the inventory table.
     *
     * @see InventoryLoadDialog
     */
    public void loadInventory()
    {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        inventory = InventoryLoader.loadInventory(this, inventoryFile);
        inventory.sort(CardAttribute.NAME.comparingCard());
        inventoryModel = new CardTableModel(inventory, SettingsDialog.settings().inventory.columns);
        inventoryTable.setModel(inventoryModel);
        setCursor(Cursor.getDefaultCursor());
    }

    /**
     * Create a new editor frame.  It will not be visible or selected.
     *
     * @param manager file manager containing the deck to display
     * 
     * @see EditorFrame
     */
    public EditorFrame newEditor(DeckSerializer manager)
    {
        EditorFrame frame = new EditorFrame(this, ++untitled, manager);
        editors.add(frame);
        decklistDesktop.add(frame);
        return frame;
    }

    /**
     * Create a new editor frame.  It will not be visible or selected.
     * 
     * @see EditorFrame
     */
    public EditorFrame newEditor()
    {
        return newEditor(new DeckSerializer());
    }

    /**
     * Open the file chooser to select a file, and if a file was selected,
     * parse it and initialize a Deck from it.
     * 
     * @return the EditorFrame containing the opened deck
     */
    public EditorFrame open()
    {
        EditorFrame frame = null;
        switch (fileChooser.showOpenDialog(this))
        {
        case JFileChooser.APPROVE_OPTION:
            frame = open(fileChooser.getSelectedFile());
            if (frame != null)
                updateRecents(fileChooser.getSelectedFile());
            break;
        case JFileChooser.CANCEL_OPTION:
        case JFileChooser.ERROR_OPTION:
            break;
        default:
            break;
        }
        return frame;
    }

    /**
     * Open the specified file and create an editor for it.
     *
     * @return the EditorFrame containing the opened deck, or <code>null</code>
     * if opening was canceled.
     */
    public EditorFrame open(File f)
    {
        EditorFrame frame = null;
        for (EditorFrame e : editors)
        {
            if (e.file() != null && e.file().equals(f))
            {
                frame = e;
                break;
            }
        }
        boolean canceled = false;
        if (frame == null)
        {
            DeckSerializer manager = new DeckSerializer();
            try
            {
                manager.load(f, this);
            }
            catch (CancellationException e)
            {
                canceled = true;
            }
            catch (DeckLoadException e)
            {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error opening " + f.getName() + ": " + e.getMessage() + ".", "Error", JOptionPane.ERROR_MESSAGE);
            }
            finally
            {
                if (!canceled)
                    frame = newEditor(manager);
            }
        }
        if (!canceled)
        {
            SettingsDialog.setStartingDir(f.getParent());
            fileChooser.setCurrentDirectory(f.getParentFile());
            selectFrame(frame);
        }
        return frame;
    }

    /**
     * If specified editor frame has a file associated with it, save
     * it to that file.  Otherwise, open the file dialog and save it
     * to whatever is chosen (save as).
     *
     * @param frame #EditorFrame to save
     */
    public void save(EditorFrame frame)
    {
        if (!frame.save())
            saveAs(frame);
    }

    /**
     * Attempt to save all open editors.  For each that needs a file, ask for a file
     * to save to.
     */
    public void saveAll()
    {
        for (EditorFrame editor : editors)
            save(editor);
    }

    /**
     * Save the specified editor frame to a file chosen from a {@link JFileChooser}.
     *
     * @param frame frame to save.
     */
    public void saveAs(EditorFrame frame)
    {
        switch (fileChooser.showSaveDialog(this))
        {
        case JFileChooser.APPROVE_OPTION:
            File f = fileChooser.getSelectedFile();
            frame.save(f);
            updateRecents(f);
            break;
        case JFileChooser.CANCEL_OPTION:
            break;
        case JFileChooser.ERROR_OPTION:
            JOptionPane.showMessageDialog(this, "Could not save " + frame.deckName() + '.', "Error", JOptionPane.ERROR_MESSAGE);
            break;
        }
        SettingsDialog.setStartingDir(fileChooser.getCurrentDirectory().getPath());
    }

    /**
     * Write the latest values of the settings to the settings file.
     */
    public void saveSettings()
    {
        SettingsDialog.setRecents(recentItems.stream().map((i) -> recents.get(i).getPath()).collect(Collectors.toList()));
        try (FileOutputStream out = new FileOutputStream(SettingsDialog.PROPERTIES_FILE))
        {
            SettingsDialog.save();
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(this, "Error writing " + SettingsDialog.PROPERTIES_FILE + ": " + e.getMessage() + ".", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Set the currently-active frame.  This is the one that will be operated on
     * when single-deck actions are taken from the main frame, such as saving
     * and closing.
     *
     * @param frame #EditorFrame to operate on from now on
     */
    public void selectFrame(EditorFrame frame)
    {
        try
        {
            frame.setSelected(true);
            frame.setVisible(true);
            deckMenu.setEnabled(true);
            selectedFrame = Optional.of(frame);
            revalidate();
            repaint();
        }
        catch (PropertyVetoException e)
        {}
    }

    /**
     * Set the background color of the editor panels containing sample hands.
     *
     * @param col new color for sample hand panels.
     */
    public void setHandBackground(Color col)
    {
        for (EditorFrame frame : editors)
            frame.setHandBackground(col);
    }

    /**
     * Sets that there is no selected list, clearing the selection of the currently-
     * selected table if there is one.
     */
    public void clearSelectedList()
    {
        selectedTable.ifPresent(CardTable::clearSelection);
        selectedList = Optional.empty();
    }

    /**
     * Set the selected table and backing list.
     * 
     * @param table table that contains the selection
     * @param list list backing that table
     */
    public void setSelectedComponents(CardTable table, CardList list)
    {
        selectedList = Optional.of(list);
        selectedTable = Optional.of(table);
        if (table != inventoryTable)
            inventoryTable.clearSelection();
        for (EditorFrame editor : editors)
            editor.clearTableSelections(table);
    }

    /**
     * Set the card to display in the image panel, along with its information in the other tabs.
     *
     * @param card card to display
     */
    public void setDisplayedCard(final Card card)
    {
        Objects.requireNonNull(card);

        StyledDocument oracleDocument = (StyledDocument)oracleTextPane.getDocument();
        Style oracleTextStyle = oracleDocument.addStyle("text", null);
        StyleConstants.setFontFamily(oracleTextStyle, UIManager.getFont("Label.font").getFamily());
        StyleConstants.setFontSize(oracleTextStyle, ComponentUtils.TEXT_SIZE);
        Style reminderStyle = oracleDocument.addStyle("reminder", oracleTextStyle);
        StyleConstants.setItalic(reminderStyle, true);
        card.formatDocument(oracleDocument, false);
        oracleTextPane.setCaretPosition(0);

        StyledDocument printedDocument = (StyledDocument)printedTextPane.getDocument();
        Style printedTextStyle = printedDocument.addStyle("text", null);
        StyleConstants.setFontFamily(printedTextStyle, UIManager.getFont("Label.font").getFamily());
        StyleConstants.setFontSize(printedTextStyle, ComponentUtils.TEXT_SIZE);
        reminderStyle = printedDocument.addStyle("reminder", oracleTextStyle);
        StyleConstants.setItalic(reminderStyle, true);
        card.formatDocument(printedDocument, true);
        printedTextPane.setCaretPosition(0);

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        StyledDocument rulingsDocument = (StyledDocument)rulingsPane.getDocument();
        Style rulingStyle = oracleDocument.addStyle("ruling", null);
        StyleConstants.setFontFamily(rulingStyle, UIManager.getFont("Label.font").getFamily());
        StyleConstants.setFontSize(rulingStyle, ComponentUtils.TEXT_SIZE);
        Style dateStyle = rulingsDocument.addStyle("date", rulingStyle);
        StyleConstants.setBold(dateStyle, true);
        if (!card.rulings().isEmpty())
        {
            try
            {
                for (Date date : card.rulings().keySet())
                {
                    for (String ruling : card.rulings().get(date))
                    {
                        rulingsDocument.insertString(rulingsDocument.getLength(), String.valueOf(UnicodeSymbols.BULLET) + " ", rulingStyle);
                        rulingsDocument.insertString(rulingsDocument.getLength(), format.format(date), dateStyle);
                        rulingsDocument.insertString(rulingsDocument.getLength(), ": ", rulingStyle);
                        int start = 0;
                        for (int i = 0; i < ruling.length(); i++)
                        {
                            switch (ruling.charAt(i))
                            {
                            case '{':
                                rulingsDocument.insertString(rulingsDocument.getLength(), ruling.substring(start, i), rulingStyle);
                                start = i + 1;
                                break;
                            case '}':
                                var symbol = Symbol.tryParseSymbol(ruling.substring(start, i));
                                if (symbol.isEmpty())
                                {
                                    System.err.println("Unexpected symbol {" + ruling.substring(start, i) + "} in ruling for " + card.unifiedName() + ".");
                                    rulingsDocument.insertString(rulingsDocument.getLength(), ruling.substring(start, i), rulingStyle);
                                }
                                else
                                {
                                    Style symbolStyle = rulingsDocument.addStyle(symbol.get().toString(), null);
                                    StyleConstants.setIcon(symbolStyle, symbol.get().getIcon(ComponentUtils.TEXT_SIZE));
                                    rulingsDocument.insertString(rulingsDocument.getLength(), " ", symbolStyle);
                                }
                                start = i + 1;
                                break;
                            default:
                                break;
                            }
                            if (i == ruling.length() - 1 && ruling.charAt(i) != '}')
                                rulingsDocument.insertString(rulingsDocument.getLength(), ruling.substring(start, i + 1) + '\n', rulingStyle);
                        }
                    }
                }
            }
            catch (BadLocationException e)
            {
                e.printStackTrace();
            }
        }
        rulingsPane.setCaretPosition(0);
        imagePanel.setCard(card);
    }

    /**
     * Clear the card display panel.
     */
    public void clearSelectedCard()
    {
        oracleTextPane.setText("");
        printedTextPane.setText("");
        rulingsPane.setText("");
        imagePanel.clearCard();
    }

    /**
     * Set the background color of the panel containing the card image.
     *
     * @param col new color for the card image panel
     */
    public void setImageBackground(Color col)
    {
        imagePanel.setBackground(col);
    }

    /**
     * Update the inventory table to bold the cards that are in the currently-selected editor.
     */
    public void updateCardsInDeck()
    {
        inventoryTable.repaint();
    }

    /**
     * Download the latest list of cards from the inventory site (default mtgjson.com).  If the
     * download is taking a while, a progress bar will appear.
     *
     * @return true if the download was successful, and false otherwise.
     */
    public boolean updateInventory()
    {
        try
        {
            return InventoryDownloader.downloadInventory(this, inventorySite, inventoryFile);
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(this, "Error connecting to inventory site: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Update the recently-opened files to add the most recently-opened one, and delete
     * the oldest one if too many are there.
     *
     * @param f #File to add to the list
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
}
