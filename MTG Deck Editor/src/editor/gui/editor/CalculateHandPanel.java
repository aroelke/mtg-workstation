package editor.gui.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import editor.collection.category.CategorySpec;
import editor.collection.deck.Deck;

/**
 * This class represents a panel that shows the probability of getting a certain
 * amount of cards that match a category in an initial hand and after a given
 * number of draws.
 * 
 * TODO: Decide if category changes here should reflect in the categories tab
 * TODO: Make double-clicking a category edit it and double-clicking empty space add a new one
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
		double p = nchoosek(count, n)*nchoosek(total - count, hand - n)/nchoosek(total, hand);
		return p;
	}
	
	/**
	 * Deck containing cards to draw from.
	 */
	private Deck deck;
	/**
	 * List of categories to display.
	 */
	private List<CategorySpec> categories;
	/**
	 * Number of cards in each category to display.
	 */
	private List<Integer> categorySizes;
	/**
	 * List of boxes displaying the desired number of cards in hand.
	 */
	private List<JComboBox<Integer>> desiredBoxes;
	/**
	 * List of combo boxes displaying relations to the desired numbers.
	 */
	private List<JComboBox<Relation>> relationBoxes;
	/**
	 * List of lists of probabilities for opening and hand each draw afterward
	 * for each category.
	 */
	private List<List<Double>> probabilities;
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
	 * Create a new CalculateHandPanel and populate it with its initial
	 * categories.
	 * 
	 * @param d Deck containing cards to draw
	 * @param stripeColor Color of alternating stripes in the table
	 */
	public CalculateHandPanel(Deck d, Color stripeColor)
	{
		super(new BorderLayout());
		
		// Parameter initialization
		deck = d;
		categories = new ArrayList<CategorySpec>();
		categorySizes = new ArrayList<Integer>();
		desiredBoxes = new ArrayList<JComboBox<Integer>>();
		relationBoxes = new ArrayList<JComboBox<Relation>>();
		probabilities = new ArrayList<List<Double>>();
		
		// Add/remove button panel
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		buttonPanel.add(Box.createVerticalGlue());
		JButton addButton = new JButton("+");
		buttonPanel.add(addButton);
		JButton removeButton = new JButton("−");
		buttonPanel.add(removeButton);
		buttonPanel.add(Box.createVerticalGlue());
		add(buttonPanel, BorderLayout.WEST);
		
		// Right panel containing table and settings
		JPanel tablePanel = new JPanel(new BorderLayout());
		add(tablePanel, BorderLayout.CENTER);
		
		// Panel containing settings
		JPanel northPanel = new JPanel();
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.X_AXIS));
		tablePanel.add(northPanel, BorderLayout.NORTH);
		
		// Spinners controlling draws to show and initial hand size
		JPanel labelPanel = new JPanel(new GridLayout(0, 1));
		labelPanel.add(new JLabel("Show Draws: "));
		labelPanel.add(new JLabel("Hand Size: "));
		labelPanel.setMaximumSize(new Dimension(labelPanel.getPreferredSize().width, Integer.MAX_VALUE));
		northPanel.add(labelPanel);
		JPanel spinnerPanel = new JPanel(new GridLayout(0, 1));
		spinnerPanel.add(drawsSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1)));
		spinnerPanel.add(handSpinner = new JSpinner(new SpinnerNumberModel(7, 0, Integer.MAX_VALUE, 1)));
		spinnerPanel.setMaximumSize(new Dimension(spinnerPanel.getPreferredSize().width, Integer.MAX_VALUE));
		northPanel.add(spinnerPanel);
		
		// Central table showing probabilities
		JTable table = new JTable(model = new CalculationTableModel())
		{
			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
			{
				Component c = super.prepareRenderer(renderer, row, column);
				if (!isRowSelected(row) || !getRowSelectionAllowed())
					c.setBackground(row%2 == 0 ? new Color(getBackground().getRGB()) : stripeColor);
				if (relationBoxes.get(row).getSelectedItem().equals(Relation.AT_LEAST) && desiredBoxes.get(row).getSelectedItem().equals(0))
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
				switch (column)
				{
				case DESIRED:
					return new DefaultCellEditor(desiredBoxes.get(row));
				case RELATION:
					return new DefaultCellEditor(relationBoxes.get(row));
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
		
		// Actions for buttons and spinners
		addButton.addActionListener((e) -> addCategory());
		removeButton.addActionListener((e) -> {
			int index = table.getSelectedRow();
			if (index > -1)
			{
				categories.remove(index);
				categorySizes.remove(index);
				desiredBoxes.remove(index);
				relationBoxes.remove(index);
				probabilities.remove(index);
				if (table.getCellEditor() != null)
					table.getCellEditor().cancelCellEditing();
				recalculate();
				table.repaint();
			}
		});
		drawsSpinner.addChangeListener((e) -> {
			model.fireTableDataChanged();
			model.fireTableStructureChanged();
			
			if (!categories.isEmpty())
			{
				int draws = (int)drawsSpinner.getValue();
				if (draws < probabilities.get(0).size() - 1)
				{
					for (List<Double> p: probabilities)
						while (p.size() - 1 > draws)
							p.remove(draws + 1);
				}
				else
				{
					for (List<Double> p: probabilities)
						for (int i = p.size() - 1; i < draws; i++)
							p.add(0.0);
				}
				recalculate();
			}
		});
		handSpinner.addChangeListener((e) -> recalculate());
		
		// Initial categories to show
		for (CategorySpec category: deck.categories())
			addCategory(category);
	}
	
	/**
	 * Add a new category to the table.
	 * 
	 * @param spec Specification of the category
	 */
	public void addCategory(CategorySpec spec)
	{
		categories.add(spec);
		int size = (int)deck.stream().filter(spec::includes).mapToInt(deck::count).reduce(0, Integer::sum);
		categorySizes.add(size);
		JComboBox<Integer> desiredBox = new JComboBox<Integer>();
		for (int i = 0; i <= size; i++)
			desiredBox.addItem(i);
		desiredBox.addActionListener((e) -> recalculate());
		desiredBoxes.add(desiredBox);
		JComboBox<Relation> relationBox = new JComboBox<Relation>(Relation.values());
		relationBox.addActionListener((e) -> recalculate());
		relationBoxes.add(relationBox);
		ArrayList<Double> p = new ArrayList<Double>();
		for (int i = 0; i < 1 + (int)drawsSpinner.getValue(); i++)
			p.add(0.0);
		probabilities.add(p);
		recalculate();
	}
	
	/**
	 * Add a new category to the table specified by the editor form.
	 */
	public void addCategory()
	{
		CategorySpec spec = CategoryEditorPanel.showCategoryEditor(this);
		if (spec != null)
			addCategory(spec);
	}
	
	/**
	 * Recalculate the probabilities of drawing the desired number of cards in
	 * each category in the initial hand and after each draw.
	 */
	public void recalculate()
	{
		for (int i = 0; i < categories.size(); i++)
		{
			Relation r = (Relation)relationBoxes.get(i).getSelectedItem();
			for (int j = 0; j < 1 + (int)drawsSpinner.getValue(); j++)
			{
				double p = 0.0;
				switch (r)
				{
				case AT_LEAST:
					for (int k = 0; k < (int)desiredBoxes.get(i).getSelectedItem(); k++)
						p += hypergeom(k,
								j + (int)handSpinner.getValue(),
								categorySizes.get(i),
								deck.total());
					p = 1.0 - p;
					break;
				case EXACTLY:
					p = hypergeom((int)desiredBoxes.get(i).getSelectedItem(),
							j + (int)handSpinner.getValue(),
							categorySizes.get(i),
							deck.total());
					break;
				case AT_MOST:
					for (int k = 0; k <= (int)desiredBoxes.get(i).getSelectedItem(); k++)
						p += hypergeom(k,
								j + (int)handSpinner.getValue(),
								categorySizes.get(i),
								deck.total());
					break;
				}
				probabilities.get(i).set(j, p);
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
			return categories.size();
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
			switch (columnIndex)
			{
			case CATEGORY:
				return categories.get(rowIndex).getName();
			case COUNT:
				return categorySizes.get(rowIndex);
			case DESIRED:
				return desiredBoxes.get(rowIndex).getSelectedItem();
			case RELATION:
				return relationBoxes.get(rowIndex).getSelectedItem();
			default:
				return String.format("%.2f%%", probabilities.get(rowIndex).get(columnIndex - (INFO_COLS - 1))*100.0);
			}
		}
	}
}
