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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.CancellationException;
import java.util.function.Function;
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

import com.jidesoft.plaf.LookAndFeelFactory;

import editor.collection.CardList;
import editor.collection.Inventory;
import editor.collection.category.CategorySpec;
import editor.collection.export.CardListFormat;
import editor.collection.export.DelimitedCardListFormat;
import editor.collection.export.TextCardListFormat;
import editor.database.card.Card;
import editor.database.characteristics.CardData;
import editor.database.characteristics.Expansion;
import editor.database.characteristics.Rarity;
import editor.database.symbol.Symbol;
import editor.filter.FilterFactory;
import editor.filter.leaf.TextFilter;
import editor.gui.display.CardImagePanel;
import editor.gui.display.CardTable;
import editor.gui.display.CardTableCellRenderer;
import editor.gui.display.CardTableModel;
import editor.gui.editor.EditorFrame;
import editor.gui.filter.FilterGroupPanel;
import editor.gui.generic.CardMenuItems;
import editor.gui.generic.ComponentUtils;
import editor.gui.generic.DocumentChangeListener;
import editor.gui.generic.OverwriteFileChooser;
import editor.gui.generic.ScrollablePanel;
import editor.gui.generic.TableMouseAdapter;
import editor.gui.generic.VerticalButtonList;
import editor.gui.generic.WizardDialog;
import editor.gui.inventory.InventoryDownloadDialog;
import editor.gui.inventory.InventoryLoadDialog;
import editor.util.MouseListenerFactory;
import editor.util.UnicodeSymbols;

/**
 * This class represents the main frame of the editor.  It contains several tabs that display information
 * about decks.
 *
 * The frame is divided into three sections:  On the left side is a database of all cards that can be
 * added to decks with a window below it that displays the Oracle text of the currently-selected card.  On
 * the right side is a pane which contains internal frames that allow the user to open, close, and edit
 * multiple decks at once.  See #EditorFrame for details on the editor frames.
 *
 * TODO: Create a diff frame that shows differences between two (or more, potentially) decks
 * TODO: Add a File->Print... dialog (for the editor frame)
 * TODO: Right-click column header = create filter for that column
 * TODO: Remove documentation from inherited methods and use inheritDoc for ones that need to be clarified.
 * TODO: Optionally get card images from magiccards.info rather than locally
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
		 * @param table #JTable to render for
		 * @param value value being rendered
		 * @param isSelected whether or not the cell is selected
		 * @param hasFocus whether or not the table has focus
		 * @param row row of the cell being rendered
		 * @param column column of the cell being rendered
		 * @return The #Component responsible for rendering the table cell.
		 */
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (selectedFrame != null)
			{
				if (selectedFrame.deck().contains(inventory.get(table.convertRowIndexToModel(row))))
					ComponentUtils.changeFontRecursive(c, c.getFont().deriveFont(Font.BOLD));
				else if (selectedFrame.sideboard().contains(inventory.get(table.convertRowIndexToModel(row))))
					ComponentUtils.changeFontRecursive(c, c.getFont().deriveFont(Font.ITALIC));
			}
			return c;
		}
	}
	
	/**
	 * Size of the text in oracle text and rulings tabs.
	 */
	public static final int TEXT_SIZE = UIManager.getFont("Label.font").getSize();
	/**
	 * Default height for displaying card images.
	 */
	public static final double DEFAULT_CARD_HEIGHT = 1.0/3.0;
	/**
	 * Maximum height that the advanced filter editor panel can attain before scrolling.
	 */
	public static final int MAX_FILTER_HEIGHT = 300;
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
	 * TODO: Add copy/paste mechanics
	 * TODO: Try to reduce memory footprint.
	 *
	 * @param args arguments to the program
	 */
	public static void main(String[] args)
	{
		LookAndFeelFactory.setDefaultStyle(LookAndFeelFactory.VSNET_STYLE_WITHOUT_MENU);
		LookAndFeelFactory.installDefaultLookAndFeel();
		LookAndFeelFactory.installJideExtension();

		try
		{
			Object tristateIcon = UIManager.get("TristateCheckBox.icon");
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager.put("TristateCheckBox.icon", tristateIcon);
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
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
	private String newestVersion;
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
	 * Currently-selected cards.  Should never be null, but can be empty.
	 */
	private List<Card> selectedCards;
	/**
	 * Table containing the currently-selected cards.  Can be null if there is no
	 * selection.
	 */
	private CardTable selectedTable;
	/**
	 * List backing the table containing the currently-selected cards.  Can be null
	 * if there is no selection.
	 */
	private CardList selectedList;

	/**
	 * Create a new MainFrame.
	 */
	public MainFrame(List<File> files)
	{
		super();

		selectedCard = null;
		selectedCards = Collections.emptyList();
		selectedTable = null;
		selectedList = null;
		untitled = 0;
		selectedFrame = null;
		editors = new ArrayList<EditorFrame>();
		recentItems = new LinkedList<JMenuItem>();
		recents = new HashMap<JMenuItem, File>();

		// Initialize properties to their default values, then load the current values
		// from the properties file
		try
		{
			SettingsDialog.load();
		}
		catch (IOException e)
		{
			JOptionPane.showMessageDialog(this, "Error opening " + SettingsDialog.PROPERTIES_FILE + ": " + e.getMessage() + ".", "Warning", JOptionPane.WARNING_MESSAGE);
		}
		try
		{
			versionSite = new URL(SettingsDialog.getAsString(SettingsDialog.INVENTORY_SOURCE) + SettingsDialog.getAsString(SettingsDialog.VERSION_FILE));
		}
		catch (MalformedURLException e)
		{
			JOptionPane.showMessageDialog(this, "Bad version URL: " + SettingsDialog.getAsString(SettingsDialog.INVENTORY_SOURCE) + SettingsDialog.getAsString(SettingsDialog.VERSION_FILE), "Warning", JOptionPane.WARNING_MESSAGE);
		}
		try
		{
			inventorySite = new URL(SettingsDialog.getAsString(SettingsDialog.INVENTORY_SOURCE) + SettingsDialog.getAsString(SettingsDialog.INVENTORY_FILE));
		}
		catch (MalformedURLException e)
		{
			JOptionPane.showMessageDialog(this, "Bad file URL: " + SettingsDialog.getAsString(SettingsDialog.INVENTORY_SOURCE) + SettingsDialog.getAsString(SettingsDialog.INVENTORY_FILE), "Warning", JOptionPane.WARNING_MESSAGE);
		}
		inventoryFile = new File(SettingsDialog.getAsString(SettingsDialog.INVENTORY_LOCATION) + File.separator + SettingsDialog.getAsString(SettingsDialog.INVENTORY_FILE));
		recentCount = Integer.valueOf(SettingsDialog.getAsString(SettingsDialog.RECENT_COUNT));
		newestVersion = SettingsDialog.getAsString(SettingsDialog.VERSION);

		// TODO: Pick a title and icon
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
		newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		newItem.addActionListener((e) -> newEditor());
		fileMenu.add(newItem);

		// Open file menu item
		JMenuItem openItem = new JMenuItem("Open...");
		openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		openItem.addActionListener((e) -> open());
		fileMenu.add(openItem);

		// Close file menu item
		JMenuItem closeItem = new JMenuItem("Close");
		closeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK));
		closeItem.addActionListener((e) -> {if (selectedFrame != null) close(selectedFrame); else exit();});
		fileMenu.add(closeItem);

		// Close all files menu item
		JMenuItem closeAllItem = new JMenuItem("Close All");
		closeAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
		closeAllItem.addActionListener((e) -> closeAll());
		fileMenu.add(closeAllItem);

		fileMenu.add(new JSeparator());

		// Save file menu item
		JMenuItem saveItem = new JMenuItem("Save");
		saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		saveItem.addActionListener((e) -> {if (selectedFrame != null) save(selectedFrame);});
		fileMenu.add(saveItem);

		// Save file as menu item
		JMenuItem saveAsItem = new JMenuItem("Save As...");
		saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));
		saveAsItem.addActionListener((e) -> {if (selectedFrame != null) saveAs(selectedFrame);});
		fileMenu.add(saveAsItem);

		// Save all files menu item
		JMenuItem saveAllItem = new JMenuItem("Save All");
		saveAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
		saveAllItem.addActionListener((e) -> saveAll());
		fileMenu.add(saveAllItem);

		// Recent files menu
		recentsMenu = new JMenu("Open Recent");
		recentsMenu.setEnabled(false);
		if (!SettingsDialog.getAsString(SettingsDialog.RECENT_FILES).isEmpty())
			for (String fname: SettingsDialog.getAsString(SettingsDialog.RECENT_FILES).split("\\|"))
				updateRecents(new File(fname));
		fileMenu.add(recentsMenu);

		fileMenu.add(new JSeparator());

		// Import and export items
		final FileNameExtensionFilter text = new FileNameExtensionFilter("Text (*.txt)", "txt");
		final FileNameExtensionFilter delimited = new FileNameExtensionFilter("Delimited (*.txt, *.csv)", "txt", "csv");
		JMenuItem importItem = new JMenuItem("Import...");
		importItem.addActionListener((e) -> {
			JFileChooser importChooser = new JFileChooser();
			importChooser.setAcceptAllFileFilterUsed(false);
			importChooser.addChoosableFileFilter(text);
			importChooser.addChoosableFileFilter(delimited);
			importChooser.setDialogTitle("Import");
			importChooser.setCurrentDirectory(fileChooser.getCurrentDirectory());
			switch (importChooser.showOpenDialog(this))
			{
			case JFileChooser.APPROVE_OPTION:
				CardListFormat format;
				if (importChooser.getFileFilter() == text)
				{
					format = new TextCardListFormat("");
				}
				else if (importChooser.getFileFilter() == delimited)
				{
					JPanel dataPanel = new JPanel(new BorderLayout());
					JPanel optionsPanel = new JPanel(new FlowLayout(0));
					optionsPanel.add(new JLabel("Delimiter: "));
					JComboBox<String> delimiterBox = new JComboBox<String>(DelimitedCardListFormat.DELIMITERS);
					delimiterBox.setEditable(true);						
					optionsPanel.add(delimiterBox);
					JCheckBox includeCheckBox = new JCheckBox("Read Headers");
					includeCheckBox.setSelected(true);
					optionsPanel.add(includeCheckBox);
					dataPanel.add(optionsPanel, BorderLayout.NORTH);
					JList<CardData> headersList = new JList<CardData>(CardData.values());
					headersList.setEnabled(!includeCheckBox.isSelected());
					JScrollPane headersPane = new JScrollPane(headersList);
					JPanel headersPanel = new JPanel();
					headersPanel.setLayout(new BoxLayout(headersPanel, BoxLayout.X_AXIS));
					headersPanel.setBorder(BorderFactory.createTitledBorder("Column Data:"));
					VerticalButtonList rearrangeButtons = new VerticalButtonList(String.valueOf(UnicodeSymbols.UP_ARROW), String.valueOf(UnicodeSymbols.DOWN_ARROW));
					for (JButton rearrange: rearrangeButtons)
						rearrange.setEnabled(!includeCheckBox.isSelected());
					headersPanel.add(rearrangeButtons);
					headersPanel.add(Box.createHorizontalStrut(5));
					DefaultListModel<CardData> selectedHeadersModel = new DefaultListModel<CardData>();
					selectedHeadersModel.addElement(CardData.NAME);
					selectedHeadersModel.addElement(CardData.EXPANSION_NAME);
					selectedHeadersModel.addElement(CardData.CARD_NUMBER);
					selectedHeadersModel.addElement(CardData.COUNT);
					selectedHeadersModel.addElement(CardData.DATE_ADDED);
					JList<CardData> selectedHeadersList = new JList<CardData>(selectedHeadersModel);
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
					for (JButton move: moveButtons)
						move.setEnabled(!includeCheckBox.isSelected());
					headersPanel.add(moveButtons);
					headersPanel.add(Box.createHorizontalStrut(5));
					headersPanel.add(headersPane);
					dataPanel.add(headersPanel, BorderLayout.CENTER);
					rearrangeButtons.get(String.valueOf(UnicodeSymbols.UP_ARROW)).addActionListener((v) -> {
						List<CardData> selected = selectedHeadersList.getSelectedValuesList();
						int ignore = 0;
						for (int index: selectedHeadersList.getSelectedIndices())
						{
							if (index == ignore)
							{
								ignore++;
								continue;
							}
							CardData temp = selectedHeadersModel.getElementAt(index - 1);
							selectedHeadersModel.setElementAt(selectedHeadersModel.getElementAt(index), index - 1);
							selectedHeadersModel.setElementAt(temp, index);
						}
						selectedHeadersList.clearSelection();
						for (CardData type: selected)
						{
							int index = selectedHeadersModel.indexOf(type);
							selectedHeadersList.addSelectionInterval(index, index);
						}
					});
					rearrangeButtons.get(String.valueOf(UnicodeSymbols.DOWN_ARROW)).addActionListener((v) -> {
						List<CardData> selected = selectedHeadersList.getSelectedValuesList();
						List<Integer> indices = Arrays.stream(selectedHeadersList.getSelectedIndices()).boxed().collect(Collectors.toList());
						Collections.reverse(indices);
						int ignore = selectedHeadersModel.size() - 1;
						for (int index: indices)
						{
							if (index == ignore)
							{
								ignore--;
								continue;
							}
							CardData temp = selectedHeadersModel.getElementAt(index + 1);
							selectedHeadersModel.setElementAt(selectedHeadersModel.getElementAt(index), index + 1);
							selectedHeadersModel.setElementAt(temp, index);
						}
						selectedHeadersList.clearSelection();
						for (CardData type: selected)
						{
							int index = selectedHeadersModel.indexOf(type);
							selectedHeadersList.addSelectionInterval(index, index);
						}
					});
					moveButtons.get(String.valueOf(UnicodeSymbols.LEFT_ARROW)).addActionListener((v) -> {
						for (CardData selected: headersList.getSelectedValuesList())
							if (!selectedHeadersModel.contains(selected))
								selectedHeadersModel.addElement(selected);
						headersList.clearSelection();
					});
					moveButtons.get(String.valueOf(UnicodeSymbols.RIGHT_ARROW)).addActionListener((v) -> {
						for (CardData selected: new ArrayList<CardData>(selectedHeadersList.getSelectedValuesList()))
							selectedHeadersModel.removeElement(selected);
					});
					includeCheckBox.addActionListener((v) -> {
						headersList.setEnabled(!includeCheckBox.isSelected());
						selectedHeadersList.setEnabled(!includeCheckBox.isSelected());
						for (JButton rearrange: rearrangeButtons)
							rearrange.setEnabled(!includeCheckBox.isSelected());
						for (JButton move: moveButtons)
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
							List<String> lines = Files.readAllLines(importChooser.getSelectedFile().toPath());
							if (includeCheckBox.isSelected())
							{
								String[] columns = lines.remove(0).split(delimiterBox.getSelectedItem().toString());
								String[][] data = lines.stream().map((s) -> DelimitedCardListFormat.split(delimiterBox.getSelectedItem().toString(), s)).toArray(String[][]::new);
								model.setDataVector(data, columns);
							}
							else
							{
								CardData[] columns = new CardData[selectedHeadersModel.size()];
								for (int i = 0; i < selectedHeadersModel.size(); i++)
									columns[i] = selectedHeadersModel.getElementAt(i);
								String[][] data = lines.stream().map((s) -> DelimitedCardListFormat.split(delimiterBox.getSelectedItem().toString(), s)).toArray(String[][]::new);
								model.setDataVector(data, columns);
							}
							previewTable.setModel(model);
						}
						catch (IOException x)
						{
							JOptionPane.showMessageDialog(this, "Could not import " + importChooser.getSelectedFile() + ": "  + x.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
						}
					};
					delimiterBox.addActionListener(updateTable);
					includeCheckBox.addActionListener(updateTable);
					for (JButton rearrange: rearrangeButtons)
						rearrange.addActionListener(updateTable);
					for (JButton move: moveButtons)
						move.addActionListener(updateTable);
					updateTable.actionPerformed(null);
					
					if (WizardDialog.showWizardDialog(this, "Import Wizard", dataPanel, previewPanel) == WizardDialog.FINISH_OPTION)
					{
						List<CardData> selected = new ArrayList<CardData>(selectedHeadersModel.size());
						for (int i = 0; i < selectedHeadersModel.size(); i++)
							selected.add(selectedHeadersModel.getElementAt(i));
						format = new DelimitedCardListFormat(delimiterBox.getSelectedItem().toString(), selected, !includeCheckBox.isSelected());
					}
					else
						return;
				}
				else
				{
					JOptionPane.showMessageDialog(this, "Could not import " + importChooser.getSelectedFile() + '.', "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				newEditor();
				try
				{
					selectedFrame.importList(format, importChooser.getSelectedFile());
				}
				catch (IllegalStateException | IOException | ParseException x)
				{
					JOptionPane.showMessageDialog(this, "Could not import " + importChooser.getSelectedFile() + ": "  + x.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
		exportItem.addActionListener((e) -> {
			if (selectedFrame != null)
			{
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
					if (exportChooser.getFileFilter() == text)
					{
						JPanel wizardPanel = new JPanel(new BorderLayout());
						JPanel fieldPanel = new JPanel(new BorderLayout());
						fieldPanel.setBorder(BorderFactory.createTitledBorder("List Format:"));
						JTextField formatField = new JTextField(TextCardListFormat.DEFAULT_FORMAT);
						formatField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, formatField.getFont().getSize()));
						formatField.setColumns(50);
						fieldPanel.add(formatField, BorderLayout.CENTER);
						JPanel addDataPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
						addDataPanel.add(new JLabel("Add Data: "));
						JComboBox<CardData> addDataBox = new JComboBox<CardData>(CardData.values());
						addDataPanel.add(addDataBox);
						fieldPanel.add(addDataPanel, BorderLayout.SOUTH);
						wizardPanel.add(fieldPanel, BorderLayout.NORTH);
						
						if (selectedFrame.deck().total() > 0 || selectedFrame.sideboard().total() > 0)
						{
							JPanel previewPanel = new JPanel(new BorderLayout());
							previewPanel.setBorder(BorderFactory.createTitledBorder("Preview:"));
							JTextArea previewArea = new JTextArea();
							JScrollPane previewPane = new JScrollPane(previewArea);
							previewArea.setText(new TextCardListFormat(formatField.getText())
									.format(selectedFrame.deck().total() > 0 ? selectedFrame.deck() : selectedFrame.sideboard()));
							previewArea.setRows(1);
							previewArea.setCaretPosition(0);
							previewPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
							previewPanel.add(previewPane, BorderLayout.CENTER);
							wizardPanel.add(previewPanel);
							
							addDataBox.addActionListener((v) -> {
								int pos = formatField.getCaretPosition();
								String data = '{' + addDataBox.getSelectedItem().toString().toLowerCase() + '}';
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
											.format(selectedFrame.deck().total() > 0 ? selectedFrame.deck() : selectedFrame.sideboard()));
									previewArea.setCaretPosition(0);
								}
							});
						}
						
						if (WizardDialog.showWizardDialog(this, "Export Wizard", wizardPanel) == WizardDialog.FINISH_OPTION)
							format = new TextCardListFormat(formatField.getText());
						else
							return;
					}
					else if (exportChooser.getFileFilter() == delimited)
					{
						JPanel wizardPanel = new JPanel(new BorderLayout());
						JList<CardData> headersList = new JList<CardData>(CardData.values());
						JScrollPane headersPane = new JScrollPane(headersList);
						JPanel headersPanel = new JPanel();
						headersPanel.setLayout(new BoxLayout(headersPanel, BoxLayout.X_AXIS));
						headersPanel.setBorder(BorderFactory.createTitledBorder("Column Data:"));
						VerticalButtonList rearrangeButtons = new VerticalButtonList(String.valueOf(UnicodeSymbols.UP_ARROW), String.valueOf(UnicodeSymbols.DOWN_ARROW));
						headersPanel.add(rearrangeButtons);
						headersPanel.add(Box.createHorizontalStrut(5));
						DefaultListModel<CardData> selectedHeadersModel = new DefaultListModel<CardData>();
						selectedHeadersModel.addElement(CardData.NAME);
						selectedHeadersModel.addElement(CardData.EXPANSION_NAME);
						selectedHeadersModel.addElement(CardData.CARD_NUMBER);
						selectedHeadersModel.addElement(CardData.COUNT);
						selectedHeadersModel.addElement(CardData.DATE_ADDED);
						JList<CardData> selectedHeadersList = new JList<CardData>(selectedHeadersModel);
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
						wizardPanel.add(headersPanel, BorderLayout.CENTER);
						
						rearrangeButtons.get(String.valueOf(UnicodeSymbols.UP_ARROW)).addActionListener((v) -> {
							List<CardData> selected = selectedHeadersList.getSelectedValuesList();
							int ignore = 0;
							for (int index: selectedHeadersList.getSelectedIndices())
							{
								if (index == ignore)
								{
									ignore++;
									continue;
								}
								CardData temp = selectedHeadersModel.getElementAt(index - 1);
								selectedHeadersModel.setElementAt(selectedHeadersModel.getElementAt(index), index - 1);
								selectedHeadersModel.setElementAt(temp, index);
							}
							selectedHeadersList.clearSelection();
							for (CardData type: selected)
							{
								int index = selectedHeadersModel.indexOf(type);
								selectedHeadersList.addSelectionInterval(index, index);
							}
						});
						rearrangeButtons.get(String.valueOf(UnicodeSymbols.DOWN_ARROW)).addActionListener((v) -> {
							List<CardData> selected = selectedHeadersList.getSelectedValuesList();
							List<Integer> indices = Arrays.stream(selectedHeadersList.getSelectedIndices()).boxed().collect(Collectors.toList());
							Collections.reverse(indices);
							int ignore = selectedHeadersModel.size() - 1;
							for (int index: indices)
							{
								if (index == ignore)
								{
									ignore--;
									continue;
								}
								CardData temp = selectedHeadersModel.getElementAt(index + 1);
								selectedHeadersModel.setElementAt(selectedHeadersModel.getElementAt(index), index + 1);
								selectedHeadersModel.setElementAt(temp, index);
							}
							selectedHeadersList.clearSelection();
							for (CardData type: selected)
							{
								int index = selectedHeadersModel.indexOf(type);
								selectedHeadersList.addSelectionInterval(index, index);
							}
						});
						moveButtons.get(String.valueOf(UnicodeSymbols.LEFT_ARROW)).addActionListener((v) -> {
							for (CardData selected: headersList.getSelectedValuesList())
								if (!selectedHeadersModel.contains(selected))
									selectedHeadersModel.addElement(selected);
							headersList.clearSelection();
						});
						moveButtons.get(String.valueOf(UnicodeSymbols.RIGHT_ARROW)).addActionListener((v) -> {
							for (CardData selected: new ArrayList<CardData>(selectedHeadersList.getSelectedValuesList()))
								selectedHeadersModel.removeElement(selected);
						});
						
						JPanel optionsPanel = new JPanel(new FlowLayout(0));
						optionsPanel.add(new JLabel("Delimiter: "));
						JComboBox<String> delimiterBox = new JComboBox<String>(DelimitedCardListFormat.DELIMITERS);
						delimiterBox.setEditable(true);						
						optionsPanel.add(delimiterBox);
						JCheckBox includeCheckBox = new JCheckBox("Include Headers");
						includeCheckBox.setSelected(true);
						optionsPanel.add(includeCheckBox);
						wizardPanel.add(optionsPanel, BorderLayout.SOUTH);
						
						if (WizardDialog.showWizardDialog(this, "Export Wizard", wizardPanel) == WizardDialog.FINISH_OPTION)
						{
							List<CardData> selected = new ArrayList<CardData>(selectedHeadersModel.size());
							for (int i = 0; i < selectedHeadersModel.size(); i++)
								selected.add(selectedHeadersModel.getElementAt(i));
							format = new DelimitedCardListFormat(delimiterBox.getSelectedItem().toString(), selected, includeCheckBox.isSelected());
						}
						else
							return;
					}
					else
					{
						JOptionPane.showMessageDialog(this, "Could not export " + selectedFrame.deckName() + '.', "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					// TODO: Add file extension if it's missing.
					try
					{
						selectedFrame.export(format, exportChooser.getSelectedFile());
					}
					catch (UnsupportedEncodingException | FileNotFoundException x)
					{
						JOptionPane.showMessageDialog(this, "Could not export " + selectedFrame.deckName() + ": " + x.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
					break;
				case JFileChooser.CANCEL_OPTION:
					break;
				case JFileChooser.ERROR_OPTION:
					JOptionPane.showMessageDialog(this, "Could not export " + selectedFrame.deckName() + '.', "Error", JOptionPane.ERROR_MESSAGE);
					break;
				}
			}
		});
		fileMenu.add(exportItem);
		
		fileMenu.add(new JSeparator());
		
		// Exit menu item
		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener((e) -> exit());
		exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_MASK));
		fileMenu.add(exitItem);

		// Edit menu
		JMenu editMenu = new JMenu("Edit");
		menuBar.add(editMenu);

		// Undo menu item
		JMenuItem undoItem = new JMenuItem("Undo");
		undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
		undoItem.addActionListener((e) -> {if (selectedFrame != null) selectedFrame.undo();});
		editMenu.add(undoItem);

		// Redo menu item
		JMenuItem redoItem = new JMenuItem("Redo");
		redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
		redoItem.addActionListener((e) -> {if (selectedFrame != null) selectedFrame.redo();});
		editMenu.add(redoItem);

		editMenu.add(new JSeparator());

		// Preferences menu item
		JMenuItem preferencesItem = new JMenuItem("Preferences...");
		preferencesItem.addActionListener((e) -> {
			SettingsDialog settings = new SettingsDialog(this);
			settings.setVisible(true);
		});
		editMenu.add(preferencesItem);

		deckMenu = new JMenu("Deck");
		deckMenu.setEnabled(false);
		menuBar.add(deckMenu);

		// Add/Remove card menus
		JMenu addMenu = new JMenu("Add Cards");
		deckMenu.add(addMenu);
		JMenu removeMenu = new JMenu("Remove Cards");
		deckMenu.add(removeMenu);
		CardMenuItems deckMenuCardItems = new CardMenuItems(this,
				(n) -> {
					if (selectedFrame != null)
						selectedFrame.deck().addAll(getSelectedCards().stream().collect(Collectors.toMap(Function.identity(), (c) -> n)));
				},
				() -> {
					if (selectedFrame != null)
						selectedFrame.deck().addAll(getSelectedCards().stream().collect(Collectors.toMap(Function.identity(), (c) -> 4 - selectedFrame.deck().getData(c).count())));
					},
				(n) -> {
					if (selectedFrame != null)
						selectedFrame.deck().removeAll(getSelectedCards().stream().collect(Collectors.toMap(Function.identity(), (c) -> n)));
				});
		addMenu.add(deckMenuCardItems.get(CardMenuItems.ADD_SINGLE));
		addMenu.add(deckMenuCardItems.get(CardMenuItems.FILL_PLAYSET));
		addMenu.add(deckMenuCardItems.get(CardMenuItems.ADD_N));
		removeMenu.add(deckMenuCardItems.get(CardMenuItems.REMOVE_SINGLE));
		removeMenu.add(deckMenuCardItems.get(CardMenuItems.REMOVE_ALL));
		removeMenu.add(deckMenuCardItems.get(CardMenuItems.REMOVE_N));

		// Sideboard menu
		JMenu sideboardMenu = new JMenu("Sideboard");
		deckMenu.add(sideboardMenu);
		CardMenuItems sideboardMenuItems = new CardMenuItems(this,
				(n) -> {
					if (selectedFrame != null)
						selectedFrame.sideboard().addAll(getSelectedCards().stream().collect(Collectors.toMap(Function.identity(), (c) -> n)));
				},
				null,
				(n) -> {
					if (selectedFrame != null)
						selectedFrame.sideboard().removeAll(getSelectedCards().stream().collect(Collectors.toMap(Function.identity(), (c) -> n)));
				});
		sideboardMenu.add(sideboardMenuItems.get(CardMenuItems.ADD_SINGLE));
		sideboardMenu.add(sideboardMenuItems.get(CardMenuItems.ADD_N));
		sideboardMenu.add(sideboardMenuItems.get(CardMenuItems.REMOVE_SINGLE));
		sideboardMenu.add(sideboardMenuItems.get(CardMenuItems.REMOVE_ALL));
		
		// Category menu
		JMenu categoryMenu = new JMenu("Category");
		deckMenu.add(categoryMenu);

		// Add category item
		JMenuItem addCategoryItem = new JMenuItem("Add...");
		addCategoryItem.addActionListener((e) -> {
			if (selectedFrame != null)
				selectedFrame.deck().addCategory(selectedFrame.createCategory());
			});
		categoryMenu.add(addCategoryItem);

		// Edit category item
		JMenuItem editCategoryItem = new JMenuItem("Edit...");
		editCategoryItem.addActionListener((e) -> {
			if (selectedFrame != null)
			{
				JPanel contentPanel = new JPanel(new BorderLayout());
				contentPanel.add(new JLabel("Choose a category to edit:"), BorderLayout.NORTH);
				JList<String> categories = new JList<String>(selectedFrame.deck().categories().stream().map(CategorySpec::getName).sorted().toArray(String[]::new));
				categories.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				contentPanel.add(new JScrollPane(categories), BorderLayout.CENTER);
				if (JOptionPane.showConfirmDialog(this, contentPanel, "Edit Category", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
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
				JList<String> categories = new JList<String>(selectedFrame.deck().categories().stream().map(CategorySpec::getName).sorted().toArray(String[]::new));
				categories.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				contentPanel.add(new JScrollPane(categories), BorderLayout.CENTER);
				if (JOptionPane.showConfirmDialog(this, contentPanel, "Edit Category", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
					selectedFrame.deck().remove(categories.getSelectedValue());
			}
		});
		categoryMenu.add(removeCategoryItem);

		// Preset categories menu
		presetMenu = new JMenu("Add Preset");
		categoryMenu.add(presetMenu);
		for (CategorySpec spec: SettingsDialog.getPresetCategories())
		{
			JMenuItem categoryItem = new JMenuItem(spec.getName());
			categoryItem.addActionListener((e) -> {
				if (selectedFrame != null && !selectedFrame.deck().containsCategory(spec.getName()))
					selectedFrame.deck().addCategory(spec);
			});
			presetMenu.add(categoryItem);
		}

		// Help menu
		JMenu helpMenu = new JMenu("Help");
		menuBar.add(helpMenu);
		// TODO: Add a help dialog

		// Inventory update item
		JMenuItem updateInventoryItem = new JMenuItem("Check for inventory update...");
		updateInventoryItem.addActionListener((e) -> {
			switch (checkForUpdate())
			{
			case UPDATE_NEEDED:
				if (updateInventory())
				{
					SettingsDialog.set(SettingsDialog.VERSION, newestVersion);
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
				@Override
				public int getColumnCount()
				{
					return 5;
				}

				@Override
				public String getColumnName(int index)
				{
					switch (index)
					{
					case 0:
						return "Expansion";
					case 1:
						return "Block";
					case 2:
						return "Code";
					case 3:
						return "magiccards.info";
					case 4:
						return "Gatherer";
					default:
						return null;
					}
				}

				@Override
				public int getRowCount()
				{
					return Expansion.expansions.length;
				}

				@Override
				public Object getValueAt(int rowIndex, int columnIndex)
				{
					switch (columnIndex)
					{
					case 0:
						return Expansion.expansions[rowIndex].name;
					case 1:
						return Expansion.expansions[rowIndex].block;
					case 2:
						return Expansion.expansions[rowIndex].code;
					case 3:
						return Expansion.expansions[rowIndex].magicCardsInfoCode;
					case 4:
						return Expansion.expansions[rowIndex].gathererCode;
					default:
						return null;
					}
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

		JTabbedPane cardPane = new JTabbedPane();

		// Panel showing the image of the currently-selected card
		cardPane.addTab("Image", imagePanel = new CardImagePanel());
		setImageBackground(SettingsDialog.getAsColor(SettingsDialog.IMAGE_BGCOLOR));

		// Pane displaying the Oracle text
		oracleTextPane = new JTextPane();
		oracleTextPane.setEditable(false);
		oracleTextPane.setCursor(new Cursor(Cursor.TEXT_CURSOR));
		cardPane.addTab("Oracle Text", new JScrollPane(oracleTextPane));

		rulingsPane = new JTextPane();
		rulingsPane.setEditable(false);
		rulingsPane.setCursor(new Cursor(Cursor.TEXT_CURSOR));
		cardPane.addTab("Rulings", new JScrollPane(rulingsPane));

		// Oracle text pane popup menu
		JPopupMenu oraclePopupMenu = new JPopupMenu();
		oracleTextPane.setComponentPopupMenu(oraclePopupMenu);
		imagePanel.setComponentPopupMenu(oraclePopupMenu);
		
		// Add the card to the main deck
		CardMenuItems oracleMenuCardItems = new CardMenuItems(this,
				(n) -> {
					if (selectedFrame != null && selectedCard != null)
						selectedFrame.deck().add(selectedCard, n);
				},
				() -> {
					if (selectedFrame != null && selectedCard != null)
						selectedFrame.deck().add(selectedCard, 4 - selectedFrame.deck().getData(selectedCard).count());
				},
				(n) -> {
					if (selectedFrame != null && selectedCard != null)
						selectedFrame.deck().remove(selectedCard, n);
				});
		oraclePopupMenu.add(oracleMenuCardItems.get(CardMenuItems.ADD_SINGLE));
		oraclePopupMenu.add(oracleMenuCardItems.get(CardMenuItems.FILL_PLAYSET));
		oraclePopupMenu.add(oracleMenuCardItems.get(CardMenuItems.ADD_N));
		oraclePopupMenu.add(new JSeparator());
		oraclePopupMenu.add(oracleMenuCardItems.get(CardMenuItems.REMOVE_SINGLE));
		oraclePopupMenu.add(oracleMenuCardItems.get(CardMenuItems.REMOVE_ALL));
		oraclePopupMenu.add(oracleMenuCardItems.get(CardMenuItems.REMOVE_N));
		oraclePopupMenu.add(new JSeparator());
		
		// Add the card to the sideboard
		CardMenuItems oracleMenuSBCardItems = new CardMenuItems(this,
				(n) -> {
					if (selectedFrame != null && selectedCard != null)
						selectedFrame.sideboard().add(selectedCard, n);
				},
				() -> {
					if (selectedFrame != null && selectedCard != null)
						selectedFrame.sideboard().add(selectedCard, 4 - selectedFrame.deck().getData(selectedCard).count());
				},
				(n) -> {
					if (selectedFrame != null && selectedCard != null)
						selectedFrame.sideboard().remove(selectedCard, n);
				});
		oracleMenuSBCardItems.get(CardMenuItems.ADD_SINGLE).setText("Add to Sideboard");
		oraclePopupMenu.add(oracleMenuSBCardItems.get(CardMenuItems.ADD_SINGLE));
		oracleMenuSBCardItems.get(CardMenuItems.ADD_N).setText("Add to Sideboard...");
		oraclePopupMenu.add(oracleMenuSBCardItems.get(CardMenuItems.ADD_N));
		oracleMenuSBCardItems.get(CardMenuItems.REMOVE_SINGLE).setText("Remove from Sideboard");
		oraclePopupMenu.add(oracleMenuSBCardItems.get(CardMenuItems.REMOVE_SINGLE));
		oracleMenuSBCardItems.get(CardMenuItems.REMOVE_ALL).setText("Remove All from Sideboard");
		oraclePopupMenu.add(oracleMenuSBCardItems.get(CardMenuItems.REMOVE_ALL));
		oraclePopupMenu.add(new JSeparator());

		JMenuItem oracleEditTagsItem = new JMenuItem("Edit Tags...");
		oracleEditTagsItem.addActionListener((e) -> editTags(getSelectedCards()));
		oraclePopupMenu.add(oracleEditTagsItem);

		// Panel containing inventory and image of currently-selected card
		JPanel inventoryPanel = new JPanel(new BorderLayout(0, 0));
		inventoryPanel.setPreferredSize(new Dimension(getWidth()/4, getHeight()*3/4));

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

		// Create the inventory and put it in the table
		inventoryTable = new CardTable();
		inventoryTable.setDefaultRenderer(String.class, new InventoryTableCellRenderer());
		inventoryTable.setDefaultRenderer(Integer.class, new InventoryTableCellRenderer());
		inventoryTable.setDefaultRenderer(Rarity.class, new InventoryTableCellRenderer());
		inventoryTable.setDefaultRenderer(List.class, new InventoryTableCellRenderer());
		inventoryTable.setStripeColor(SettingsDialog.getAsColor(SettingsDialog.INVENTORY_STRIPE));
		inventoryTable.addMouseListener(MouseListenerFactory.createClickListener((e) -> {
			if (e.getClickCount()%2 == 0 && selectedFrame != null)
				selectedFrame.deck().addAll(new HashSet<Card>(getSelectedCards()));
		}));
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
				return new Inventory.TransferData(getSelectedCards());
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
		
		// Add cards to the main deck
		CardMenuItems inventoryMenuCardItems = new CardMenuItems(this,
				(n) -> {
					if (selectedFrame != null)
						selectedFrame.deck().addAll(getSelectedCards().stream().collect(Collectors.toMap(Function.identity(), (c) -> n)));
				},
				() -> {
					if (selectedFrame != null)
					{
						Map<Card, Integer> toAdd = new HashMap<Card, Integer>();
						for (Card c: getSelectedCards())
						{
							if (selectedFrame.deck().contains(c))
								toAdd.put(c, 4 - selectedFrame.deck().getData(c).count());
							else
								toAdd.put(c, 4);
						}
						selectedFrame.deck().addAll(toAdd);
					}
				},
				(n) -> {
					if (selectedFrame != null)
						selectedFrame.deck().removeAll(getSelectedCards().stream().collect(Collectors.toMap(Function.identity(), (c) -> n)));
				});
		inventoryMenu.add(inventoryMenuCardItems.get(CardMenuItems.ADD_SINGLE));
		inventoryMenu.add(inventoryMenuCardItems.get(CardMenuItems.FILL_PLAYSET));
		inventoryMenu.add(inventoryMenuCardItems.get(CardMenuItems.ADD_N));
		inventoryMenu.add(new JSeparator());
		inventoryMenu.add(inventoryMenuCardItems.get(CardMenuItems.REMOVE_SINGLE));
		inventoryMenu.add(inventoryMenuCardItems.get(CardMenuItems.REMOVE_ALL));
		inventoryMenu.add(inventoryMenuCardItems.get(CardMenuItems.REMOVE_N));
		inventoryMenu.add(new JSeparator());

		// Add cards to the sideboard
		CardMenuItems inventoryMenuSBItems = new CardMenuItems(this,
				(n) -> {
					if (selectedFrame != null)
						selectedFrame.sideboard().addAll(getSelectedCards().stream().collect(Collectors.toMap(Function.identity(), (c) -> n)));
				},
				() -> {
					if (selectedFrame != null)
						selectedFrame.sideboard().addAll(getSelectedCards().stream().collect(Collectors.toMap(Function.identity(), (c) -> 4 - selectedFrame.sideboard().getData(c).count())));
				},
				(n) -> {
					if (selectedFrame != null)
						selectedFrame.sideboard().removeAll(getSelectedCards().stream().collect(Collectors.toMap(Function.identity(), (c) -> n)));
				});
		inventoryMenuSBItems.get(CardMenuItems.ADD_SINGLE).setText("Add to Sideboard");
		inventoryMenu.add(inventoryMenuSBItems.get(CardMenuItems.ADD_SINGLE));
		inventoryMenuSBItems.get(CardMenuItems.ADD_N).setText("Add to Sideboard...");
		inventoryMenu.add(inventoryMenuSBItems.get(CardMenuItems.ADD_N));
		inventoryMenuSBItems.get(CardMenuItems.REMOVE_SINGLE).setText("Remove from Sideboard");
		inventoryMenu.add(inventoryMenuSBItems.get(CardMenuItems.REMOVE_SINGLE));
		inventoryMenuSBItems.get(CardMenuItems.REMOVE_ALL).setText("Remove All from Sideboard");
		inventoryMenu.add(inventoryMenuSBItems.get(CardMenuItems.REMOVE_ALL));
		inventoryMenu.add(new JSeparator());
		
		// Edit tags item
		JMenuItem editTagsItem = new JMenuItem("Edit Tags...");
		editTagsItem.addActionListener((e) -> editTags(getSelectedCards()));
		inventoryMenu.add(editTagsItem);

		// Action to be taken when the user presses the Enter key after entering text into the quick-filter
		// bar
		nameFilterField.addActionListener((e) -> {
			inventory.updateFilter(TextFilter.createQuickFilter(FilterFactory.NAME, nameFilterField.getText().toLowerCase()));
			inventoryModel.fireTableDataChanged();
		});

		// Action to be taken when the clear button is pressed (reset the filter)
		clearButton.addActionListener((e) -> {
			nameFilterField.setText("");
			inventory.updateFilter(FilterFactory.createFilter(FilterFactory.ALL));
			inventoryModel.fireTableDataChanged();
		});

		// Action to be taken when the advanced filter button is pressed (show the advanced filter
		// dialog)
		advancedFilterButton.addActionListener((e) -> {
			FilterGroupPanel panel = new FilterGroupPanel();
			if (inventory.getFilter().equals(FilterFactory.createFilter(FilterFactory.ALL)))
				panel.setContents(FilterFactory.createFilter(FilterFactory.NAME));
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

		// Action to be taken when a selection is made in the inventory table (update the relevant
		// panels)
		inventoryTable.getSelectionModel().addListSelectionListener((e) -> {
			if (!e.getValueIsAdjusting())
			{
				ListSelectionModel lsm = (ListSelectionModel)e.getSource();
				if (!lsm.isSelectionEmpty())
					setSelectedCards(inventoryTable, inventory);
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
		fileChooser = new JFileChooser(SettingsDialog.getAsString(SettingsDialog.INITIALDIR));
		fileChooser.setMultiSelectionEnabled(false);

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
				if ((SettingsDialog.getAsBoolean(SettingsDialog.INITIAL_CHECK) || !inventoryFile.exists())
						&& (checkForUpdate() == UPDATE_NEEDED && updateInventory()))
					SettingsDialog.set(SettingsDialog.VERSION, newestVersion);
				loadInventory();
				if (!inventory.isEmpty())
					for (File f: files)
						open(f);
			}
		});
	}

	/**
	 * Add a new preset category to the preset categories list.
	 *
	 * @param category new preset category to add
	 */
	public void addPreset(String category)
	{
		SettingsDialog.addPresetCategory(category);

		CategorySpec spec = new CategorySpec(category);
		JMenuItem categoryItem = new JMenuItem(spec.getName());
		categoryItem.addActionListener((e) -> {
			if (selectedFrame != null)
				selectedFrame.deck().addCategory(spec);
		});
		presetMenu.add(categoryItem);
	}

	/**
	 * Apply the global settings.
	 */
	public void applySettings()
	{
		try
		{
			inventorySite = new URL(SettingsDialog.getAsString(SettingsDialog.INVENTORY_SOURCE) + SettingsDialog.getAsString(SettingsDialog.INVENTORY_FILE));
		}
		catch (MalformedURLException e)
		{
			JOptionPane.showMessageDialog(this, "Bad file URL: " + SettingsDialog.getAsString(SettingsDialog.INVENTORY_SOURCE) + SettingsDialog.getAsString(SettingsDialog.INVENTORY_FILE), "Warning", JOptionPane.WARNING_MESSAGE);
		}
		inventoryFile = new File(SettingsDialog.getAsString(SettingsDialog.INVENTORY_LOCATION) + '\\' + SettingsDialog.getAsString(SettingsDialog.INVENTORY_FILE));
		recentCount = SettingsDialog.getAsInt(SettingsDialog.RECENT_COUNT);
		if (SettingsDialog.getAsString(SettingsDialog.INVENTORY_COLUMNS).isEmpty())
			SettingsDialog.set(SettingsDialog.INVENTORY_COLUMNS, "Name,Expansion,Mana Cost,Type");
		inventoryModel.setColumns(Arrays.stream(SettingsDialog.getAsString(SettingsDialog.INVENTORY_COLUMNS).split(",")).map(CardData::parseCardData).collect(Collectors.toList()));
		inventoryTable.setStripeColor(SettingsDialog.getAsColor(SettingsDialog.INVENTORY_STRIPE));
		if (SettingsDialog.getAsString(SettingsDialog.EDITOR_COLUMNS).isEmpty())
			SettingsDialog.set(SettingsDialog.EDITOR_COLUMNS, "Name,Count,Mana Cost,Type,Expansion,Rarity");
		for (EditorFrame frame: editors)
			frame.applySettings();
		presetMenu.removeAll();
		for (CategorySpec spec: SettingsDialog.getPresetCategories())
		{
			JMenuItem categoryItem = new JMenuItem(spec.getName());
			categoryItem.addActionListener((e) -> {
				if (selectedFrame != null)
					selectedFrame.deck().addCategory(spec);
			});
			presetMenu.add(categoryItem);
		}
		setImageBackground(SettingsDialog.getAsColor(SettingsDialog.IMAGE_BGCOLOR));
		setHandBackground(SettingsDialog.getAsColor(SettingsDialog.HAND_BGCOLOR));

		revalidate();
		repaint();
	}

	/**
	 * Check to see if the inventory needs to be updated.  If it does, ask the user if it should be.
	 *
	 * TODO: Add a timeout
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
			JOptionPane.showMessageDialog(this, inventoryFile.getName() + " not found.  It will be downloaded.", "Update", JOptionPane.WARNING_MESSAGE);
			try (BufferedReader in = new BufferedReader(new InputStreamReader(versionSite.openStream())))
			{
				newestVersion = in.readLine();
				newestVersion = newestVersion.substring(1, newestVersion.length() - 1);
			}
			catch (IOException e)
			{
				JOptionPane.showMessageDialog(this, "Error connecting to server: " + e.getMessage() + ".", "Connection Error", JOptionPane.ERROR_MESSAGE);
				return NO_UPDATE;
			}
			return UPDATE_NEEDED;
		}
		else
		{
			try (BufferedReader in = new BufferedReader(new InputStreamReader(versionSite.openStream())))
			{
				newestVersion = in.readLine();
				newestVersion = newestVersion.substring(1, newestVersion.length() - 1);
				if (!newestVersion.equals(SettingsDialog.getAsString(SettingsDialog.VERSION)))
				{
					if (JOptionPane.showConfirmDialog(this, "Inventory is out of date.  Download update?", "Update", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
						return UPDATE_NEEDED;
					else
						return UPDATE_CANCELLED;
				}
				else
					return NO_UPDATE;
			}
			catch (IOException e)
			{
				JOptionPane.showMessageDialog(this, "Error connecting to server: " + e.getMessage() + ".", "Connection Error", JOptionPane.ERROR_MESSAGE);
				return NO_UPDATE;
			}
		}
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
				selectedCards = Collections.emptyList();
				selectedTable = null;
				selectedList = null;
			}
			editors.remove(frame);
			if (editors.size() > 0)
				selectFrame(editors.get(0));
			else
			{
				selectedFrame = null;
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
		List<EditorFrame> e = new ArrayList<EditorFrame>(editors);
		boolean closedAll = true;
		for (EditorFrame editor: e)
			closedAll &= close(editor);
		return closedAll;
	}

	/**
	 * Show a dialog allowing editing of the tags of the given cards and adding
	 * new tags.
	 *
	 * @param cards cards whose tags should be edited
	 */
	public void editTags(List<Card> cards)
	{
		JPanel contentPanel = new JPanel(new BorderLayout());
		CardTagPanel cardTagPanel = new CardTagPanel(cards);
		contentPanel.add(new JScrollPane(cardTagPanel), BorderLayout.CENTER);
		JPanel lowerPanel = new JPanel();
		lowerPanel.setLayout(new BoxLayout(lowerPanel, BoxLayout.X_AXIS));
		JTextField newTagField = new JTextField();
		lowerPanel.add(newTagField);
		JButton newTagButton = new JButton("Add");

		ActionListener addListener = (e) -> {
			if (!newTagField.getText().isEmpty())
			{
				if (cardTagPanel.addTag(newTagField.getText()))
				{
					newTagField.setText("");
					cardTagPanel.revalidate();
					cardTagPanel.repaint();
					SwingUtilities.getWindowAncestor(cardTagPanel).pack();
				}
			}
		};
		newTagButton.addActionListener(addListener);
		newTagField.addActionListener(addListener);
		lowerPanel.add(newTagButton);
		contentPanel.add(lowerPanel, BorderLayout.SOUTH);
		if (JOptionPane.showConfirmDialog(this, contentPanel, "Edit Card Tags", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
		{
			for (Map.Entry<Card, Set<String>> entry: cardTagPanel.getTagged().entrySet())
				Card.tags.compute(entry.getKey(), (k, v) -> {
					if (v == null)
						v = new HashSet<String>();
					v.addAll(entry.getValue());
					return v;
				});
			for (Map.Entry<Card, Set<String>> entry: cardTagPanel.getUntagged().entrySet())
				Card.tags.compute(entry.getKey(), (k, v) -> {
					if (v != null)
					{
						v.removeAll(entry.getValue());
						if (v.isEmpty())
							v = null;
					}
					return v;
				});
		}
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
	 * @param id UID of the #Card to look for
	 * @return the #Card with the given UID.
	 */
	public Card getCard(String id)
	{
		return inventory.get(id);
	}

	/**
	 * Get the #Card being shown in the image window.
	 * 
	 * @return the card currently being shown in the card image window.
	 */
	public Card getSelectedCard()
	{
		return selectedCard;
	}

	/**
	 * Get the currently-selected card(s).
	 * 
	 * @return a List containing each currently-selected card in the inventory table.
	 */
	public List<Card> getSelectedCards()
	{
		if (selectedCards.isEmpty() && selectedCard != null)
			return Arrays.asList(selectedCard);
		return selectedCards;
	}
	
	/**
	 * Get the list corresponding to the table with the currently-selected cards.
	 * 
	 * @return the list containing the currently selected cards
	 */
	public CardList getSelectedList()
	{
		return selectedList;
	}
	
	/**
	 * Get the table containing the currently selected cards.
	 * 
	 * @return the table with the selected cards
	 */
	public CardTable getSelectedTable()
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
		return selectedTable == inventoryTable;
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
		inventoryModel = new CardTableModel(inventory, SettingsDialog.getAsCharacteristics(SettingsDialog.INVENTORY_COLUMNS));
		inventoryTable.setModel(inventoryModel);
		setCursor(Cursor.getDefaultCursor());
	}

	/**
	 * Create a new editor frame.
	 * @see EditorFrame
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
	 * @param f #File to open.
	 */
	public void open(File f)
	{
		EditorFrame frame = null;
		for (EditorFrame e: editors)
		{
			if (e.file() != null && e.file().equals(f))
			{
				frame = e;
				break;
			}
		}
		try
		{
			if (frame == null)
			{
				frame = new EditorFrame(f, ++untitled, this);
				frame.setVisible(true);
				editors.add(frame);
				decklistDesktop.add(frame);
			}
			SettingsDialog.set(SettingsDialog.INITIALDIR, fileChooser.getCurrentDirectory().getPath());
			selectFrame(frame);
		}
		catch (CancellationException e)
		{}
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
		for (EditorFrame editor: editors)
			save(editor);
	}

	/**
	 * Save the specified editor frame to a file chosen from a {@link JFileChooser}.
	 *
	 * @param frame frame to save.
	 */
	public void saveAs(EditorFrame frame)
	{
		// If the file exists, let the user choose whether or not to overwrite.  If he or she chooses not to,
		// ask for a new file.  If he or she cancels at any point, stop asking and don't open a file.
		
		// TODO: Make this an OverwriteFileChooser
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
					int option = JOptionPane.showConfirmDialog(this, "File " + f.getName() + " already exists.  Overwrite?", "Warning", JOptionPane.YES_NO_CANCEL_OPTION);
					write = (option == JOptionPane.YES_OPTION);
					done = (option != JOptionPane.NO_OPTION);
				}
				else
				{
					write = true;
					done = true;
				}
				if (write)
				{
					frame.save(f);
					updateRecents(f);
				}
				break;
			case JFileChooser.CANCEL_OPTION: case JFileChooser.ERROR_OPTION:
				done = true;
				break;
			default:
				break;
			}
		} while (!done);
		SettingsDialog.set(SettingsDialog.INITIALDIR, fileChooser.getCurrentDirectory().getPath());
	}

	/**
	 * Write the latest values of the settings to the settings file.
	 */
	public void saveSettings()
	{
		StringJoiner str = new StringJoiner("|");
		for (JMenuItem recent: recentItems)
			str.add(recents.get(recent).getPath());
		SettingsDialog.set(SettingsDialog.RECENT_FILES, str.toString());
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
	 * Update the currently-selected card.
	 *
	 * @param card #Card to select
	 */
	public void selectCard(Card card)
	{
		if (selectedCard == null || !selectedCard.equals(card))
		{
			selectedCard = card;

			oracleTextPane.setText("");
			StyledDocument oracleDocument = (StyledDocument)oracleTextPane.getDocument();
			Style oracleTextStyle = oracleDocument.addStyle("text", null);
			StyleConstants.setFontFamily(oracleTextStyle, UIManager.getFont("Label.font").getFamily());
			StyleConstants.setFontSize(oracleTextStyle, TEXT_SIZE);
			Style reminderStyle = oracleDocument.addStyle("reminder", oracleTextStyle);
			StyleConstants.setItalic(reminderStyle, true);
			selectedCard.formatDocument(oracleDocument);
			oracleTextPane.setCaretPosition(0);

			rulingsPane.setText("");
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			StyledDocument rulingsDocument = (StyledDocument)rulingsPane.getDocument();
			Style rulingStyle = oracleDocument.addStyle("ruling", null);
			StyleConstants.setFontFamily(rulingStyle, UIManager.getFont("Label.font").getFamily());
			StyleConstants.setFontSize(rulingStyle, TEXT_SIZE);
			Style dateStyle = rulingsDocument.addStyle("date", rulingStyle);
			StyleConstants.setBold(dateStyle, true);
			if (!selectedCard.rulings().isEmpty())
			{
				try
				{
					for (Date date: selectedCard.rulings().keySet())
					{
						for (String ruling: selectedCard.rulings().get(date))
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
									Symbol symbol = Symbol.tryParseSymbol(ruling.substring(start, i));
									if (symbol == null)
									{
										System.err.println("Unexpected symbol {" + ruling.substring(start, i) + "} in ruling for " + selectedCard.unifiedName() + ".");
										rulingsDocument.insertString(rulingsDocument.getLength(), ruling.substring(start, i), rulingStyle);
									}
									else
									{
										Style symbolStyle = rulingsDocument.addStyle(symbol.toString(), null);
										StyleConstants.setIcon(symbolStyle, symbol.getIcon(MainFrame.TEXT_SIZE));
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
			imagePanel.setCard(selectedCard);
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
			deckMenu.setEnabled(true);
			selectedFrame = frame;
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
		for (EditorFrame frame: editors)
			frame.setHandBackground(col);
	}
	
	/**
	 * Set the selected cards from the given table backed by the given card list.  Make sure
	 * the list and the table represent the same set of cards!
	 * 
	 * @param table table with a selection to get the selected cards from
	 * @param list list backing the given table
	 */
	public void setSelectedCards(CardTable table, CardList list)
	{
		selectedTable = table;
		selectedList = list;
		selectedCards = Collections.unmodifiableList(Arrays.stream(table.getSelectedRows())
				.mapToObj((r) -> list.get(table.convertRowIndexToModel(r)))
				.collect(Collectors.toList()));

		if (!selectedCards.isEmpty())
			selectCard(selectedCards.get(0));
		
		if (table != inventoryTable)
			inventoryTable.clearSelection();
		for (EditorFrame editor: editors)
			editor.clearTableSelections(table);
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
		inventoryModel.fireTableDataChanged();
	}

	/**
	 * Download the latest list of cards from the inventory site (default mtgjson.com).  If the
	 * download is taking a while, a progress bar will appear.
	 *
	 * @return true if the download was successful, and false otherwise.
	 */
	public boolean updateInventory()
	{
		InventoryDownloadDialog downloadDialog = new InventoryDownloadDialog(this);
		downloadDialog.setLocationRelativeTo(this);
		return downloadDialog.downloadInventory(inventorySite, inventoryFile);
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
