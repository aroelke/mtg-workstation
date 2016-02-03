package editor.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import editor.collection.category.CategorySpec;
import editor.collection.deck.Deck;
import editor.database.Card;
import editor.database.characteristics.Loyalty;
import editor.database.characteristics.MTGColor;
import editor.database.characteristics.ManaCost;
import editor.database.characteristics.PowerToughness;
import editor.database.symbol.ColorSymbol;
import editor.database.symbol.Symbol;

/**
 * This class represents a table whose alternating occupied rows will be different
 * colors.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class CardTable extends JTable
{
	/**
	 * This class represents a renderer of ManaCosts in a table.
	 * 
	 * @author Alec
	 */
	private static class ManaCostCellRenderer extends DefaultTableCellRenderer
	{
		/**
		 * Create the Component that will display the contents of the specified cell.  If that cell contains
		 * a ManaCost, then rather than displaying text, a panel containing a series of labels whose icons
		 * are mana symbols will be displayed instead. 
		 * 
		 * @return The Component that should be used to render the cell.
		 */
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (value instanceof ManaCost.Tuple)
			{
				ManaCost.Tuple cost = (ManaCost.Tuple)value;
				JPanel costPanel = new JPanel();
				costPanel.setLayout(new BoxLayout(costPanel, BoxLayout.X_AXIS));
				if (hasFocus)
					costPanel.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
				else
					costPanel.setBorder(new EmptyBorder(0, 1, cost.size() == 1 ? -1 : 0, 0));
				costPanel.setForeground(c.getForeground());
				costPanel.setBackground(c.getBackground());
				for (int i = 0; i < cost.size(); i++)
				{
					if (!cost.get(i).isEmpty())
					{
						if (i > 0)
						{
							costPanel.add(Box.createHorizontalStrut(3));
							costPanel.add(new JLabel(Card.FACE_SEPARATOR));
							costPanel.add(Box.createHorizontalStrut(3));
						}
						for (Symbol sym: cost.get(i).symbols())
							costPanel.add(new JLabel(sym.getIcon(13)));
					}
				}
				return costPanel;
			}
			else
				return c;
		}
	}
	
	/**
	 * This class represents a renderer for a table cell that shows a tuple of colors.  Each
	 * color is represented by its corresponding mana symbol.
	 * 
	 * @author Alec
	 */
	private static class ColorRenderer extends DefaultTableCellRenderer
	{
		/**
		 * Get the component that is used to render the colors.  It consists of a panel
		 * containing some labels whose icons are each mana symbols laid out in a row.
		 * 
		 * @param table Table containing the value to render
		 * @param value Value to render
		 * @param isSelected Whether or not the cell is selected
		 * @param hasFocus Whether or not the cell has focus
		 * @param row Row of the cell being rendered
		 * @param column Column of the cell being rendered
		 * @return A JPanel containing JLabels showing the mana symbols of the colors to display.
		 */
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (value instanceof MTGColor.Tuple)
			{
				MTGColor.Tuple colors = (MTGColor.Tuple)value;
				JPanel colorPanel = new JPanel();
				colorPanel.setLayout(new BoxLayout(colorPanel, BoxLayout.X_AXIS));
				if (hasFocus)
					colorPanel.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
				else
					colorPanel.setBorder(new EmptyBorder(0, 1, -1, 0));
				for (MTGColor color: colors)
					colorPanel.add(new JLabel(ColorSymbol.get(color).getIcon(13)));
				colorPanel.setBackground(c.getBackground());
				colorPanel.setForeground(c.getForeground());
				return colorPanel;
			}
			else
				return c;
		}
	}
	
	/**
	 * This class represents a renderer that displays which categories a card is in.  Colored boxes
	 * correspond to the colors of the categories a card belongs to.
	 * 
	 * @author Alec Roelke
	 */
	private static class CategoriesCellRenderer extends DefaultTableCellRenderer
	{
		/**
		 * Create a panel that draws boxes whose colors correspond to the card in the given row.
		 * 
		 * @param table Table containing the element to draw
		 * @param value Value to draw
		 * @param isSelected Whether or not the cell is selected
		 * @param hasFocus Whether or not the cell has focus
		 * @param row Row of the cell being rendered
		 * @param column Column of the cell being rendered
		 * @return A component, which will be a JPanel containing several squares if the given value is a List<Deck.Category>.
		 */
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (value instanceof Collection)
			{
				List<CategorySpec> categories = ((Collection<?>)value).stream().filter((o) -> o instanceof CategorySpec).map((o) -> (CategorySpec)o).collect(Collectors.toList());
				JPanel panel = new JPanel()
				{
					@Override
					public void paintComponent(Graphics g)
					{
						super.paintComponent(g);
						int s = getHeight();
						for (int i = 0; i < categories.size(); i++)
						{
							int x = i*(s + 1) + 1;
							int y = 1;
							g.setColor(categories.get(i).getColor());
							g.fillRect(x, y, s - 3, s - 3);
							g.setColor(Color.BLACK);
							g.drawRect(x, y, s - 3, s - 3);
						}
					}
				};
				StringBuilder tooltip = new StringBuilder();
				tooltip.append("<html>Categories:<br>");
				for (CategorySpec category: categories)
					tooltip.append("• ").append(category.getName()).append("<br>");
				tooltip.append("</html>");
				panel.setToolTipText(tooltip.toString());
				if (hasFocus)
					panel.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
				panel.setForeground(c.getForeground());
				panel.setBackground(c.getBackground());
				return panel;
			}
			return c;
		}
	}
	
	/**
	 * This class represents a renderer for drawing cells with Dates in them.  It will format the
	 * date according to the format specified by @link{database.Deck#DATE_FORMAT}.
	 * 
	 * @author Alec Roelke
	 */
	private static class DateCellRenderer extends DefaultTableCellRenderer
	{
		/**
		 * If the specified value is a Date, create a JPanel containing a JLabel containing the date
		 * formatted according to @link{database.Deck#DATE_FORMAT}.
		 * 
		 * @param table Table containing the data to draw
		 * @param value Value to draw
		 * @param isSelected Whether or not the cell is selected
		 * @param hasFocus Whether or not the cell has focus
		 * @param row Row containing the cell
		 * @param column Column containing the cell
		 * @return The component containing the label with the formatted Date, if the value is a Date, and
		 * the default value otherwise.
		 */
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (value instanceof Date)
			{
				JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
				if (hasFocus)
					datePanel.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
				else
					datePanel.setBorder(new EmptyBorder(1, 1, 1, 1));
				datePanel.setForeground(c.getForeground());
				datePanel.setBackground(c.getBackground());
				datePanel.add(new JLabel(Deck.DATE_FORMAT.format((Date)value)));
				return datePanel;
			}
			else
				return c;
		}
	}
	
	/**
	 * This class represents a sorter that sorts a table column whose empty cells are invalid values.
	 * Currently this only applies for power and toughness columns.  Those cells are always placed
	 * last in the column.
	 * 
	 * @author Alec Roelke
	 */
	private static class EmptyTableRowSorter extends TableRowSorter<TableModel>
	{
		/**
		 * This EmptyTableRowSorter's model for data.
		 */
		private TableModel model;
		
		/**
		 * Create a new EmptyTableRowSorter.
		 * 
		 * @param m Model for data.
		 */
		public EmptyTableRowSorter(TableModel m)
		{
			super(m);
			model = m;
		}
		
		/**
		 * @param column column being sorted
		 * @return A Comparator that should be used to sort the column.  For any data except
		 * power, toughness, or loyalty, the natural ordering is used.  For those,
		 * empty rows are placed last and then the natural ordering is used.
		 */
		@Override
		public Comparator<?> getComparator(int column)
		{
			boolean ascending = getSortKeys().get(0).getSortOrder() == SortOrder.ASCENDING;
			if (model.getColumnClass(column).equals(PowerToughness.Tuple.class))
				return (a, b) -> {
					PowerToughness pt1 = ((PowerToughness.Tuple)a).stream().filter((pt) -> !Double.isNaN(pt.value)).findFirst().orElse(((PowerToughness.Tuple)a).get(0));
					PowerToughness pt2 = ((PowerToughness.Tuple)b).stream().filter((pt) -> !Double.isNaN(pt.value)).findFirst().orElse(((PowerToughness.Tuple)b).get(0));
					if (Double.isNaN(pt1.value) && Double.isNaN(pt2.value))
						return 0;
					else if (Double.isNaN(pt1.value))
						return ascending ? 1 : -1;
					else if (Double.isNaN(pt2.value))
						return ascending ? -1 : 1;
					else
						return pt1.compareTo(pt2);
				};
			else if (model.getColumnClass(column).equals(Loyalty.Tuple.class))
				return (a, b) -> {
					Loyalty l1 = ((Loyalty.Tuple)a).stream().filter((l) -> l.value > 0).findFirst().orElse(((Loyalty.Tuple)a).get(0));
					Loyalty l2 = ((Loyalty.Tuple)b).stream().filter((l) -> l.value > 0).findFirst().orElse(((Loyalty.Tuple)b).get(0));
					if (l1.value < 1 && l2.value < 1)
						return 0;
					else if (l1.value < 1)
						return ascending ? 1 : -1;
					else if (l2.value < 1)
						return ascending ? -1 : 1;
					else
						return l1.compareTo(l2);
				};
			else
				return super.getComparator(column);
		}
	}
	
	/**
	 * Color of the alternate rows.
	 */
	private Color stripeColor;
	
	/**
	 * Create a new CardTable with the default color.
	 */
	public CardTable()
	{
		super();
		init();
	}
	
	/**
	 * Create a new CardTable with the default color and given model.
	 * 
	 * @param model Model for the new table.
	 */
	public CardTable(TableModel model)
	{
		super(model);
		init();
	}
	
	/**
	 * Initialize the table.
	 */
	private void init()
	{
		stripeColor = Color.LIGHT_GRAY;

		setFillsViewportHeight(true);
		setShowGrid(false);
		
		setDefaultRenderer(ManaCost.Tuple.class, new ManaCostCellRenderer());
		setDefaultRenderer(MTGColor.Tuple.class, new ColorRenderer());
		setDefaultRenderer(Set.class, new CategoriesCellRenderer());
		setDefaultRenderer(Date.class, new DateCellRenderer());
		
		setRowSorter(new EmptyTableRowSorter(getModel()));
	}
	
	/**
	 * Set the model backing the data for this table.
	 * 
	 * @param model Model that gives this table data to show
	 */
	@Override
	public void setModel(TableModel model)
	{
		super.setModel(model);
		setRowSorter(new EmptyTableRowSorter(model));
	}
	
	/**
	 * Set the color for the stripes of this CardTable.
	 * 
	 * @param col New stripe color
	 */
	public void setStripeColor(Color col)
	{
		stripeColor = col;
		repaint();
	}
	
	/**
	 * The table will track the viewport width if the viewport is larger than its preferred size.
	 */
	@Override
	public boolean getScrollableTracksViewportWidth()
	{
		return getPreferredSize().width < getParent().getWidth();
	}
	
	/**
	 * Prepares the renderer.  Changes its background according to the striping color.
	 * @see JTable#prepareRenderer(TableCellRenderer, int, int)
	 */
	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
	{
		Component c = super.prepareRenderer(renderer, row, column);
		if (!isRowSelected(row) || !getRowSelectionAllowed())
			c.setBackground(row%2 == 0 ? new Color(getBackground().getRGB()) : stripeColor);
		return c;
	}
	
	/**
	 * If the contents of a cell are too big to fit in the cell, they (in their text form)
	 * can be viewed as the cell's tooltip.
	 * 
	 * @param e MouseEvent that should be used to determine what tooltip to display.
	 */
	@Override
	public String getToolTipText(MouseEvent e)
	{
		String tooltip = super.getToolTipText(e);
		if (tooltip == null)
		{
			Point p = e.getPoint();
			int col = columnAtPoint(p);
			int row = rowAtPoint(p);
			if (col >= 0 && row >= 0)
			{
				Rectangle bounds = getCellRect(row, col, false);
				JComponent c = (JComponent)prepareRenderer(getCellRenderer(row, col), row, col);
				if (c.getPreferredSize().width > bounds.width)
					tooltip = "<html>" + String.valueOf(getValueAt(row, col)) + "</html>";
			}
		}
		return tooltip;
	}
}
