package editor.gui.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import editor.collection.category.CategorySpec;
import editor.collection.deck.Deck;
import editor.gui.SettingsDialog;

/**
 * This class represents a panel that shows the probability of getting a certain
 * amount of cards that match a category in an initial hand and after a given
 * number of draws.
 * 
 * TODO: Make this work with exclusion
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class CalculateHandPanel extends JPanel
{
	/**
	 * Column in the table for the category.
	 */
	private static final int CATEGORY = 0;
	/**
	 * Column in the table for the number of cards in that category.
	 */
	private static final int COUNT = 1;
	/**
	 * Column in the table showing the relation with the desired number.
	 */
	private static final int RELATION = 2;
	/**
	 * Column in the table showing the desired number of cards in hand.
	 */
	private static final int DESIRED = 3;
	/**
	 * Column in the table showing the probability for the initial hand.
	 */
	private static final int INITIAL = 4;
	/**
	 * Number of columns before draw columns.
	 */
	private static final int INFO_COLS = 5;
	
	/**
	 * Calculate the exact value of n!, as long as it will fit into a double.
	 * 
	 * @param n Parameter for factorial
	 * @return The factorial of n, or n!
	 */
	public static double factorial(int n)
	{
		double f = 1.0;
		for (int i = 1; i <= n; i++)
			f *= i;
		return f;
	}
	
	/**
	 * Calculate n choose k based on the {@link CalculateHandPanel#factorial(int)}
	 * function.
	 * 
	 * @param n Number of items to choose from
	 * @param k Number of items to choose
	 * @return The number of ways to choose k out of n items.
	 */
	public static double nchoosek(int n, int k)
	{
		return factorial(n)/(factorial(n - k)*factorial(k));
	}
	
	/**
	 * Calculate a hypergeometric distribution for drawing the given number
	 * of cards in a hand of the given size from a deck of the given size,
	 * when the given number of cards are successes.
	 * 
	 * @param n Number of desired cards
	 * @param hand Size of hand drawn
	 * @param count Number of successful cards in deck
	 * @param total Number of cards in the deck
	 * @return The hypergeometric distribution with parameters hand, count, and
	 * total and argument n.
	 */
	public static double hypergeom(int n, int hand, int count, int total)
	{
		return nchoosek(count, n)*nchoosek(total - count, hand - n)/nchoosek(total, hand);
	}
	
	/**
	 * Deck containing cards to draw from.
	 */
	private Deck deck;
	/**
	 * List of boxes displaying the desired number of cards in hand.
	 */
	private Map<String, JComboBox<Integer>> desiredBoxes;
	/**
	 * List of combo boxes displaying relations to the desired numbers.
	 */
	private Map<String, JComboBox<Relation>> relationBoxes;
	/**
	 * List of lists of probabilities for opening and hand each draw afterward
	 * for each category.
	 */
	private Map<String, List<Double>> probabilities;
	/**
	 * Spinner controlling the number of drawn cards to show.
	 */
	private JSpinner drawsSpinner;
	/**
	 * Data model for the table showing probabilities.
	 */
	private CalculationTableModel model;
	/**
	 * Spinner controlling the number of cards in the initial hand.
	 */
	private JSpinner handSpinner;
	/**
	 * Table showing probabilities of fulfilling category requirements.
	 */
	private JTable table;
	
	/**
	 * Create a new CalculateHandPanel and populate it with its initial
	 * categories.
	 * 
	 * @param d Deck containing cards to draw
	 * @param stripeColor Color of alternating stripes in the table
	 */
	public CalculateHandPanel(Deck d)
	{
		super(new BorderLayout());
		
		// Parameter initialization
		deck = d;
		desiredBoxes = new HashMap<String, JComboBox<Integer>>();
		relationBoxes = new HashMap<String, JComboBox<Relation>>();
		probabilities = new HashMap<String, List<Double>>();
		
		// Right panel containing table and settings
		JPanel tablePanel = new JPanel(new BorderLayout());
		add(tablePanel, BorderLayout.CENTER);
		
		// Spinners controlling draws to show and initial hand size
		JPanel northPanel = new JPanel();
		northPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		northPanel.setBorder(new EmptyBorder(2, 5, 2, 5));
		tablePanel.add(northPanel, BorderLayout.NORTH);
		northPanel.add(new JLabel("Hand Size: "));
		northPanel.add(handSpinner = new JSpinner(new SpinnerNumberModel(7, 0, Integer.MAX_VALUE, 1)));
		northPanel.add(Box.createHorizontalStrut(15));
		northPanel.add(new JLabel("Show Draws: "));
		northPanel.add(drawsSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1)));
		
		table = new JTable(model = new CalculationTableModel())
		{
			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
			{
				Component c = super.prepareRenderer(renderer, row, column);
				if (!isRowSelected(row) || !getRowSelectionAllowed())
					c.setBackground(row%2 == 0 ? new Color(getBackground().getRGB()) : SettingsDialog.getAsColor(SettingsDialog.EDITOR_STRIPE));
				if (model.getValueAt(row, RELATION).equals(Relation.AT_LEAST) && model.getValueAt(row, DESIRED).equals(0))
				{
					c.setForeground(c.getBackground().darker());
					c.setFont(new Font(c.getFont().getFontName(), Font.ITALIC, c.getFont().getSize()));
				}
				else
				{
					c.setForeground(Color.BLACK);
				}
				return c;
			}
			
			@Override
			public boolean getScrollableTracksViewportWidth()
			{
				return getPreferredSize().width < getParent().getWidth();
			}
			
			@Override
			public TableCellEditor getCellEditor(int row, int column)
			{
				String category = deck.categories().stream().map(CategorySpec::getName).sorted().collect(Collectors.toList()).get(row);
				
				switch (column)
				{
				case DESIRED:
					return new DefaultCellEditor(desiredBoxes.get(category));
				case RELATION:
					return new DefaultCellEditor(relationBoxes.get(category));
				default:
					return super.getCellEditor(row, column);
				}
			}
			
			@Override
			public boolean isCellEditable(int row, int column)
			{
				return column == DESIRED || column == RELATION;
			}
		};
		table.setFillsViewportHeight(true);
		table.setShowGrid(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		DefaultTableCellRenderer intRenderer = new DefaultTableCellRenderer();
		intRenderer.setHorizontalAlignment(DefaultTableCellRenderer.LEFT);
		table.setDefaultRenderer(Integer.class, intRenderer);
		tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
		
		// Actions
		drawsSpinner.addChangeListener((e) -> {
			recalculate();
			model.fireTableStructureChanged();
		});
		handSpinner.addChangeListener((e) -> recalculate());
	}
	
	public void update()
	{
		List<String> categories = deck.categories().stream().map(CategorySpec::getName).sorted().collect(Collectors.toList());
		
		Map<String, Integer> oldDesired = desiredBoxes.entrySet().stream().collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue().getSelectedIndex()));
		Map<String, Relation> oldRelations = relationBoxes.entrySet().stream().collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue().getItemAt(e.getValue().getSelectedIndex())));
		
		desiredBoxes.clear();
		relationBoxes.clear();
		probabilities.clear();
		
		for (String category: categories)
		{
			JComboBox<Integer> desiredBox = new JComboBox<Integer>();
			for (int i = 0; i <= deck.total(category); i++)
				desiredBox.addItem(i);
			if (oldDesired.containsKey(category) && oldDesired.get(category) < deck.total(category))
				desiredBox.setSelectedIndex(oldDesired.get(category));
			desiredBox.addActionListener((e) -> recalculate());
			desiredBoxes.put(category, desiredBox);
			
			JComboBox<Relation> relationBox = new JComboBox<Relation>(Relation.values());
			if (oldRelations.containsKey(category))
				relationBox.setSelectedItem(oldRelations.get(category));
			relationBox.addActionListener((e) -> recalculate());
			relationBoxes.put(category, relationBox);
		}
		
		recalculate();
		model.fireTableStructureChanged();
	}
	
	/**
	 * Recalculate the probabilities of drawing the desired number of cards in
	 * each category in the initial hand and after each draw.
	 */
	public void recalculate()
	{
		List<String> categories = deck.categories().stream().map(CategorySpec::getName).sorted().collect(Collectors.toList());
		
		probabilities.clear();
		int hand = (int)handSpinner.getValue();
		int draws = (int)drawsSpinner.getValue();
		
		for (String category: categories)
		{
			probabilities.put(category, new ArrayList<Double>(Collections.nCopies(1 + draws, 0.0)));
			Relation r = (Relation)relationBoxes.get(category).getSelectedItem();
			for (int j = 0; j < probabilities.get(category).size(); j++)
			{
				double p = 0.0;
				switch (r)
				{
				case AT_LEAST:
					for (int k = 0; k < desiredBoxes.get(category).getSelectedIndex(); k++)
						p += hypergeom(k, hand + j, deck.total(category), deck.total());
					p = 1.0 - p;
					break;
				case EXACTLY:
					p = hypergeom(desiredBoxes.get(category).getSelectedIndex(), hand + j, deck.total(category), deck.total());
					break;
				case AT_MOST:
					for (int k = 0; k <= desiredBoxes.get(category).getSelectedIndex(); k++)
						p += hypergeom(k, hand + j, deck.total(category), deck.total());
					break;
				}
				probabilities.get(category).set(j, p);
			}
		}
		model.fireTableDataChanged();
	}
	
	/**
	 * This enum represents the three relations to the desired number of cards
	 * in a category to draw.
	 * 
	 * @author Alec Roelke
	 */
	private enum Relation
	{
		AT_LEAST("At least"),
		EXACTLY("Exactly"),
		AT_MOST("At most");
		
		/**
		 * String representation to display in a combo box.
		 */
		private final String relation;
		
		/**
		 * Create a new Relation.
		 * 
		 * @param r String representation of the new Relation
		 */
		private Relation(String r)
		{
			relation = r;
		}
		
		/**
		 * @return The String representation of this Relation.
		 */
		@Override
		public String toString()
		{
			return relation;
		}
	}
	
	/**
	 * This class represents the data model backing the main table showing
	 * probabilities.
	 * 
	 * @author Alec Roelke
	 */
	private class CalculationTableModel extends AbstractTableModel
	{
		/**
		 * @return The number of rows in the table, which is the same as the number
		 * of categories to display.
		 */
		@Override
		public int getRowCount()
		{
			return deck.numCategories();
		}

		/**
		 * @return The number of columns in the table, which is the number of "info"
		 * columns plus one for each card draw to display.
		 */
		@Override
		public int getColumnCount()
		{
			return (int)drawsSpinner.getValue() + INFO_COLS;
		}

		/**
		 * @param column Index of the column to get the name of
		 * @return The name of the given column.
		 */
		@Override
		public String getColumnName(int column)
		{
			switch (column)
			{
			case CATEGORY:
				return "Kind of Card";
			case COUNT:
				return "Count";
			case DESIRED:
				return "Desired";
			case RELATION:
				return "Relation";
			case INITIAL:
				return "Initial Hand";
			default:
				return "Draw " + (column - (INFO_COLS - 1));
			}
		}
		
		/**
		 * @param column Index of the column to get the class of
		 * @return The class of the information contained in the given column.
		 */
		@Override
		public Class<?> getColumnClass(int column)
		{
			switch (column)
			{
			case CATEGORY:
				return String.class;
			case COUNT: case DESIRED:
				return Integer.class;
			case RELATION:
				return Relation.class;
			default:
				return String.class;
			}
		}
		
		/**
		 * @param rowIndex Row of the cell to get the value of
		 * @param columnIndex Column of the cell to get the value of
		 * @return The value of the cell at the given row and column.
		 */
		@Override
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			String category = deck.categories().stream().map(CategorySpec::getName).sorted().collect(Collectors.toList()).get(rowIndex);
			
			switch (columnIndex)
			{
			case CATEGORY:
				return category;
			case COUNT:
				return deck.total(category);
			case DESIRED:
				return desiredBoxes.get(category).getSelectedItem();
			case RELATION:
				return relationBoxes.get(category).getSelectedItem();
			default:
				return String.format("%.2f%%", probabilities.get(category).get(columnIndex - (INFO_COLS - 1))*100.0);
			}
		}
	}
}
