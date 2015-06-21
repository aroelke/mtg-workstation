package gui.editor;

import gui.MainFrame;
import gui.ManaCostRenderer;
import gui.ScrollablePanel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ProgressMonitorInputStream;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import database.Card;
import database.Deck;
import database.ManaCost;
import database.characteristics.CardCharacteristic;

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
	private JTable table;
	/**
	 * CardListTableModel for showing the deck list.
	 */
	private DeckTableModel model;
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
	 * JDialog for creating and editing categories.
	 */
	protected CategoryDialog categoryCreator;
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
		setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);

		parent = p;
		deck = new Deck();
		file = null;
		unsaved = false;
		undoBuffer = new Stack<DeckAction>();
		redoBuffer = new Stack<DeckAction>();

		// Panel for showing buttons to add and remove cards
		// The buttons are concentrated in the middle of the panel
		JPanel buttonPanel = new JPanel();
		getContentPane().add(buttonPanel, BorderLayout.WEST);
		GridBagLayout buttonLayout = new GridBagLayout();
		buttonLayout.columnWidths = new int[] {0};
		buttonLayout.rowHeights = new int[] {0, 0, 0, 0, 0};
		buttonLayout.columnWeights = new double[] {1.0};
		buttonLayout.rowWeights = new double[] {1.0, 0.0, 0.0, 0.0, 1.0};
		buttonPanel.setLayout(buttonLayout);

		// Add button to add one copy of the currently-selected card to the deck
		JButton addButton = new JButton("+");
		addButton.addActionListener((e) -> addCards(parent.getTableSelection(), 1));
		GridBagConstraints addConstraints = new GridBagConstraints();
		addConstraints.fill = GridBagConstraints.HORIZONTAL;
		addConstraints.gridy = 1;
		buttonPanel.add(addButton, addConstraints);

		// Remove button to remove one copy of each selected card from the deck
		JButton removeButton = new JButton("\u2013");
		removeButton.addActionListener((e) -> removeSelectedCards(1));
		GridBagConstraints removeConstraints = new GridBagConstraints();
		removeConstraints.fill = GridBagConstraints.HORIZONTAL;
		removeConstraints.gridy = 2;
		buttonPanel.add(removeButton, removeConstraints);

		// Delete button to remove all copies of each selected card from the deck
		JButton deleteButton = new JButton("X");
		deleteButton.addActionListener((e) -> removeSelectedCards(Integer.MAX_VALUE));
		GridBagConstraints deleteConstraints = new GridBagConstraints();
		deleteConstraints.fill = GridBagConstraints.HORIZONTAL;
		deleteConstraints.gridy = 3;
		buttonPanel.add(deleteButton, deleteConstraints);

		// The first tab is the master list tab, and the second tab is the categories tab
		listTabs = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(listTabs, BorderLayout.CENTER);

		model = new DeckTableModel(deck, Arrays.asList(CardCharacteristic.NAME, CardCharacteristic.COUNT,
				CardCharacteristic.MANA_COST, CardCharacteristic.TYPE_LINE,
				CardCharacteristic.EXPANSION_NAME, CardCharacteristic.RARITY));

		// Create the table so that it resizes if the window is too big but not if it's too small
		// TODO: Allow the user to add cards to a category
		table = new JTable(model)
		{
			@Override
			public boolean getScrollableTracksViewportWidth()
			{
				return getPreferredSize().width < getParent().getWidth();
			}
		};
		table.setFillsViewportHeight(true);
		table.setAutoCreateRowSorter(true);
		table.setDefaultRenderer(ManaCost.class, new ManaCostRenderer());
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setShowGrid(false);
		// When a card is selected in the master list table, select it for adding
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				if (!e.getValueIsAdjusting())
				{
					ListSelectionModel lsm = (ListSelectionModel)e.getSource();
					if (!lsm.isSelectionEmpty())
					{
						parent.selectCard(deck.get(table.convertRowIndexToModel(lsm.getMinSelectionIndex())));
						for (CategoryPanel c: categories)
							c.clearSelection();
					}
				}
			}
		});
		listTabs.addTab("Cards", new JScrollPane(table));

		JPanel categoriesPanel = new JPanel(new BorderLayout());

		// Button to add a new category
		JButton addCategoryButton = new JButton("Add");
		addCategoryButton.addActionListener((e) -> createCategory());
		categoriesPanel.add(addCategoryButton, BorderLayout.NORTH);

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
		categoriesContainer = new JPanel(new GridLayout(0, 1));
		categories = new ArrayList<CategoryPanel>();
		// TODO: Make it so that the categories are resizable (perhaps with splitpanes)

		// The category panel is a vertically-scrollable panel that contains all categories stacked vertically
		// The categories should have a constant height, but fit the container horizontally
		categoriesSuperContainer.add(categoriesContainer, BorderLayout.NORTH);
		categoriesPanel.add(new JScrollPane(categoriesSuperContainer, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
		listTabs.addTab("Categories", categoriesPanel);

		// Panel to show the stats of the deck
		JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		getContentPane().add(statsPanel, BorderLayout.SOUTH);

		// Label to show the number of cards in a deck
		countLabel = new JLabel();
		updateCount();
		statsPanel.add(countLabel);

		categoryCreator = new CategoryDialog(this);
		categoryCreator.setLocationRelativeTo(parent);

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
		try (BufferedReader rd = new BufferedReader(new InputStreamReader(new ProgressMonitorInputStream(p, "Opening " + f.getName(), new FileInputStream(f)))))
		{
			int cards = Integer.valueOf(rd.readLine().trim());
			for (int i = 0; i < cards; i++)
			{
				String[] card = rd.readLine().trim().split("\t");
				deck.add(p.inventory().get(card[0]), Integer.valueOf(card[1]));
			}
			int categories = Integer.valueOf(rd.readLine().trim());
			for (int i = 0; i < categories; i++)
			{
				try
				{
					String repr = rd.readLine().trim();
					categoryCreator.initializeFromString(repr);
					addCategory(new CategoryPanel(categoryCreator.name(), repr, deck, categoryCreator.getFilter()));
					categoryCreator.reset();
				}
				catch (Exception e)
				{
					JOptionPane.showMessageDialog(null, "Error parsing " + f.getName() + ": " + e.getMessage() + ".", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(null, "Error opening " + f.getName() + ": " + e.getMessage() + ".", "Error", JOptionPane.ERROR_MESSAGE);
			deck.clear();
			categoryCreator.reset();
		}
		finally
		{
			updateCount();
			unsaved = false;
			setFile(f);
			undoBuffer.clear();
			redoBuffer.clear();
		}
	}

	/**
	 * @return The names of all the categories in the deck, sorted
	 * alphabetically.
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
	 * Open the dialog to create a new category for the deck and then
	 * add it.
	 */
	public void createCategory()
	{
		addCategory(categoryCreator.createNewCategory());
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
			newCategory.addListSelectionListener((e) -> {
				ListSelectionModel lsm = (ListSelectionModel)e.getSource();
				if (!lsm.isSelectionEmpty())
				{
					if (!e.getValueIsAdjusting())
						parent.selectCard(deck.getCategory(newCategory.name()).get(newCategory.convertRowIndexToModel(lsm.getMinSelectionIndex())));
					for (CategoryPanel c: categories)
						if (newCategory != c)
							c.clearSelection();
					table.clearSelection();
				}
			});
			// Add the behavior for the edit category button
			newCategory.addEditButtonListener((e) -> {
				String oldName = newCategory.name();
				String oldRepr = newCategory.toString();
				Predicate<Card> oldFilter = newCategory.filter();
				if (categoryCreator.editCategory(newCategory))
				{
					setUnsaved();
					undoBuffer.push(new EditCategoryAction(this, oldName, oldRepr, oldFilter, newCategory.name(), newCategory.toString(), newCategory.filter()));
					redoBuffer.clear();
				}
			});
			// Add the behavior for the remove category button
			newCategory.addRemoveButtonListener((e) -> removeCategory(newCategory));
			revalidate();
			repaint();
			setUnsaved();
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
		{
			String deckName;
			if (unsaved)
				deckName = getTitle().substring(0, getTitle().length() - 2);
			else
				deckName = getTitle();
			JOptionPane.showMessageDialog(null, "Deck " + deckName + " has no category named " + name + ".", "Error", JOptionPane.ERROR_MESSAGE);
		}
		else
		{
			String oldRepr = toEdit.toString();
			Predicate<Card> oldFilter = toEdit.filter();
			categoryCreator.editCategory(toEdit);
			revalidate();
			repaint();
			setUnsaved();
			undoBuffer.push(new EditCategoryAction(this, name, oldRepr, oldFilter, toEdit.name(), toEdit.toString(), toEdit.filter()));
			redoBuffer.clear();
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
			revalidate();
			repaint();
			setUnsaved();
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
			String deckName;
			if (unsaved)
				deckName = getTitle().substring(0, getTitle().length() - 2);
			else
				deckName = getTitle();
			JOptionPane.showMessageDialog(null, "Deck " + deckName + " has no category named " + name + ".", "Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}
	
	/**
	 * Update the card counter to reflect the total number of cards in the deck.
	 */
	public void updateCount()
	{
		countLabel.setText("Total cards: " + deck.total());
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
			deck.addAll(toAdd, n);

			switch (listTabs.getSelectedIndex())
			{
			case 0:
				// Maintain the selection in the master list
				int[] selectedRows = table.getSelectedRows();
				model.fireTableDataChanged();
				for (int row: selectedRows)
					table.addRowSelectionInterval(row, row);
				break;
			case 1:
				// Maintain the selection in each category
				for (CategoryPanel c: categories)
				{
					selectedRows = c.getSelectedRows();
					c.update();
					for (int row: selectedRows)
						c.addRowSelectionInterval(row, row);
				}
				categoriesContainer.revalidate();
				categoriesContainer.repaint();
				break;
			default:
				break;
			}
			parent.selectCard(toAdd.get(0));
			updateCount();
			setUnsaved();
			revalidate();
			repaint();
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
	 * Remove a number of copies of the specified Cards from the deck.  The current selections
	 * for any cards remaining in them in the category and main tables are maintained.  Then
	 * update the undo buffer.
	 * 
	 * @param toRemove List of cards to remove
	 * @param n Number of copies to remove
	 * @return <code>true</code> if the deck was changed as a result, and <code>false</code>
	 * otherwise.
	 */
	public boolean removeCards(List<Card> toRemove, int n)
	{
		if (removeCardsUnbuffered(toRemove, n))
		{
			undoBuffer.push(new RemoveCardsAction(this, toRemove, n));
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
	 * @return <code>true</code> if the deck was changed as a result, and <code>false</code>
	 * otherwise.
	 */
	protected boolean removeCardsUnbuffered(List<Card> toRemove, int n)
	{
		if (toRemove.isEmpty())
			return false;
		else
		{
			boolean changed = true;

			switch (listTabs.getSelectedIndex())
			{
			case 0:
				// Get the selected cards first
				List<Card> selectedCards = new ArrayList<Card>();
				for (int row: table.getSelectedRows())
					selectedCards.add(deck.get(table.convertRowIndexToModel(row)));
				// Remove cards from the deck
				for (Card c: toRemove)
					changed &= deck.remove(c, n);
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
				break;
			case 1:
				// Get all of the selected cards from each category (only one category should
				// have selected cards, but just in case)
				Map<CategoryPanel, List<Card>> selectedCardsMap = new HashMap<CategoryPanel, List<Card>>();
				for (CategoryPanel category: categories)
				{
					List<Card> categorySelectedCards = new ArrayList<Card>();
					if (category.getSelectedRows().length > 0)
						for (int row: category.getSelectedRows())
							categorySelectedCards.add(deck.getCategory(category.name()).get(category.convertRowIndexToModel(row)));
					selectedCardsMap.put(category, categorySelectedCards);
				}
				// Remove cards from the deck
				for (Card c: toRemove)
					changed &= deck.remove(c, n);
				// Update each category panel and then restore the selection as much as possible
				for (CategoryPanel category: categories)
				{
					category.update();
					for (Card c: selectedCardsMap.get(category))
					{
						if (deck.getCategory(category.name()).contains(c))
						{
							int row = category.convertRowIndexToView(deck.getCategory(category.name()).indexOf(c));
							category.addRowSelectionInterval(row, row);
						}
					}
				}
				categoriesContainer.revalidate();
				categoriesContainer.repaint();
				break;
			default:
				break;
			}

			if (changed)
			{
				updateCount();
				setUnsaved();
			}
			revalidate();
			repaint();
			return changed;
		}
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
		case 0:
			selectedCards = Arrays.stream(table.getSelectedRows())
			.mapToObj((r) -> deck.get(table.convertRowIndexToModel(r)))
			.collect(Collectors.toList());
			break;
		case 1:
			selectedCards = new ArrayList<Card>();
			for (CategoryPanel category: categories)
				selectedCards.addAll(Arrays.stream(category.getSelectedRows())
						.mapToObj((r) -> deck.getCategory(category.name()).get(category.convertRowIndexToModel(r)))
						.collect(Collectors.toList()));
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
			return (save(file));
	}

	// TODO: Cause the tab to change to the category tab when adding or removing categories
	// and to the main tab when adding or removing cards not in a category
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
}
