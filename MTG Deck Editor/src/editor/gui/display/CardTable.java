package editor.gui.display;

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
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import editor.collection.category.CategorySpec;
import editor.collection.deck.Deck;
import editor.database.card.Card;
import editor.database.characteristics.CardData;
import editor.database.characteristics.Loyalty;
import editor.database.characteristics.ManaCost;
import editor.database.characteristics.ManaType;
import editor.database.characteristics.CombatStat;
import editor.database.symbol.ColorSymbol;
import editor.database.symbol.Symbol;
import editor.gui.editor.EditorFrame;
import editor.gui.editor.InclusionCellEditor;
import editor.gui.generic.SpinnerCellEditor;
import editor.util.UnicodeSymbols;

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
	 * This class represents a renderer that displays which categories a card is in.  Colored boxes
	 * correspond to the colors of the categories a card belongs to.
	 *
	 * @author Alec Roelke
	 */
	private static class CategoriesCellRenderer extends DefaultTableCellRenderer
	{
		/**
		 * {@inheritDoc}
		 * Display a series of squares filled with the colors of the categories to display.
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
				if (!categories.isEmpty())
				{
					StringBuilder tooltip = new StringBuilder();
					tooltip.append("<html>Categories:<br>");
					for (CategorySpec category: categories)
						tooltip.append(String.valueOf(UnicodeSymbols.BULLET) + " ").append(category.getName()).append("<br>");
					tooltip.append("</html>");
					panel.setToolTipText(tooltip.toString());
				}
				if (hasFocus)
					panel.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
				panel.setForeground(c.getForeground());
				panel.setBackground(c.getBackground());
				c = panel;
			}
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
		 * {@inheritDoc}
		 * Display a series of mana symbols corresponding to those colors.
		 */
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (value instanceof ManaType.Tuple)
			{
				ManaType.Tuple colors = (ManaType.Tuple)value;
				JPanel colorPanel = new JPanel();
				colorPanel.setLayout(new BoxLayout(colorPanel, BoxLayout.X_AXIS));
				if (hasFocus)
					colorPanel.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
				else
					colorPanel.setBorder(BorderFactory.createEmptyBorder(0, 1, -1, 0));
				for (ManaType color: colors)
					colorPanel.add(new JLabel(ColorSymbol.get(color).getIcon(13)));
				colorPanel.setBackground(c.getBackground());
				colorPanel.setForeground(c.getForeground());
				c = colorPanel;
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
		 * {@inheritDoc}
		 * Display a {@link Date} according to {@link Deck#DATE_FORMAT}.
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
					datePanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
				datePanel.setForeground(c.getForeground());
				datePanel.setBackground(c.getBackground());
				datePanel.add(new JLabel(Deck.DATE_FORMAT.format((Date)value)));
				c = datePanel;
			}
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
		 * {@inheritDoc}
		 * Empty cells are always sorted last.
		 */
		@Override
		public Comparator<?> getComparator(int column)
		{
			boolean ascending = getSortKeys().get(0).getSortOrder() == SortOrder.ASCENDING;
			if (model.getColumnClass(column).equals(CombatStat.Tuple.class))
				return (a, b) -> {
					CombatStat pt1 = ((CombatStat.Tuple)a).stream().filter((pt) -> !Double.isNaN(pt.value)).findFirst().orElse(((CombatStat.Tuple)a).get(0));
					CombatStat pt2 = ((CombatStat.Tuple)b).stream().filter((pt) -> !Double.isNaN(pt.value)).findFirst().orElse(((CombatStat.Tuple)b).get(0));
					if (!pt1.exists() && !pt2.exists())
						return 0;
					else if (!pt1.exists())
						return ascending ? 1 : -1;
					else if (pt2.exists())
						return ascending ? -1 : 1;
					else
						return pt1.compareTo(pt2);
				};
			else if (model.getColumnClass(column).equals(Loyalty.Tuple.class))
				return (a, b) -> {
					Loyalty l1 = ((Loyalty.Tuple)a).stream().filter((l) -> l.value > 0).findFirst().orElse(((Loyalty.Tuple)a).get(0));
					Loyalty l2 = ((Loyalty.Tuple)b).stream().filter((l) -> l.value > 0).findFirst().orElse(((Loyalty.Tuple)b).get(0));
					if (!l1.exists() && !l2.exists())
						return 0;
					else if (!l1.exists())
						return ascending ? 1 : -1;
					else if (!l2.exists())
						return ascending ? -1 : 1;
					else
						return l1.compareTo(l2);
				};
			else
				return super.getComparator(column);
		}
	}

	/**
	 * This class represents a renderer for lists in a table.  It only does anything
	 * special for lists of doubles, which uses different separators than the default.
	 *
	 * @author Alec Roelke
	 */
	private static class ListRenderer extends DefaultTableCellRenderer
	{
		/**
		 * {@inheritDoc}
		 * This displays lists as normal, except for doubles which it separates with {@link Card#FACE_SEPARATOR}.
		 */
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (value instanceof List)
			{
				List<?> values = (List<?>)value;
				if (!values.isEmpty() && values.get(0) instanceof Double)
				{
					List<Double> cmc = values.stream().map((o) -> (Double)o).collect(Collectors.toList());
					StringJoiner join = new StringJoiner(" " + Card.FACE_SEPARATOR + " ");
					for (Double cost: cmc)
						join.add(cost.toString());
					JPanel cmcPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
					if (hasFocus)
						cmcPanel.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
					else
						cmcPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
					cmcPanel.setForeground(c.getForeground());
					cmcPanel.setBackground(c.getBackground());
					cmcPanel.add(new JLabel(join.toString()));
					c = cmcPanel;
				}
			}
			return c;
		}
	}

	/**
	 * This class represents a renderer of {@link ManaCost}s in a table.
	 *
	 * @author Alec
	 */
	private static class ManaCostCellRenderer extends DefaultTableCellRenderer
	{
		/**
		 * {@inheritDoc}
		 * If the specified cell contains {@link ManaCost}, then rather than displaying text,
		 * a panel containing a series of labels whose icons are mana symbols will be displayed instead.
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
					costPanel.setBorder(BorderFactory.createEmptyBorder(0, 1, cost.size() == 1 ? -1 : 0, 0));
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
						for (Symbol sym: cost.get(i))
							costPanel.add(new JLabel(sym.getIcon(13)));
					}
				}
				c = costPanel;
			}
			return c;
		}
	}

	/**
	 * Create an instance of the editor for cells containing the given type of CardData.
	 * 
	 * @param frame frame containing the table with the cell to edit
	 * @param type type of data to edit
	 * @return an instance of the editor for the given type of data
	 * @throws IllegalArgumentException if the given type of CardData can't be edited
	 */
	public static TableCellEditor createCellEditor(EditorFrame frame, CardData type) throws IllegalArgumentException
	{
		switch (type)
		{
		case COUNT:
			return new SpinnerCellEditor();
		case CATEGORIES:
			return new InclusionCellEditor(frame);
		default:
			throw new IllegalArgumentException("CardData type " + type + " can't be edited.");
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
	 * @param model model for the new table.
	 */
	public CardTable(TableModel model)
	{
		super(model);
		init();
	}

	/**
	 * Get the background color of the specified row.
	 * 
	 * @param row row to get the color of
	 * @return the background color of the row at the given index of this CardTable.
	 */
	public Color getRowColor(int row)
	{
		return row%2 == 0 ? new Color(getBackground().getRGB()) : stripeColor;
	}

	@Override
	public boolean getScrollableTracksViewportWidth()
	{
		return getPreferredSize().width < getParent().getWidth();
	}

	/**
	 * {@inheritDoc}
	 * Tooltips are only available for cells whose contents don't fit.
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

	/**
	 * Initialize the table.
	 */
	private void init()
	{
		stripeColor = Color.LIGHT_GRAY;

		setFillsViewportHeight(true);
		setShowGrid(false);

		setDefaultRenderer(ManaCost.Tuple.class, new ManaCostCellRenderer());
		setDefaultRenderer(ManaType.Tuple.class, new ColorRenderer());
		setDefaultRenderer(Set.class, new CategoriesCellRenderer());
		setDefaultRenderer(Date.class, new DateCellRenderer());
		setDefaultRenderer(List.class, new ListRenderer());

		setRowSorter(new EmptyTableRowSorter(getModel()));
	}

	/**
	 * {@inheritDoc}
	 * Changes the renderer's background according to the stripe color.
	 */
	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
	{
		Component c = super.prepareRenderer(renderer, row, column);
		if (!isRowSelected(row) || !getRowSelectionAllowed())
			c.setBackground(getRowColor(row));
		return c;
	}

	@Override
	public void setModel(TableModel model)
	{
		super.setModel(model);
		setRowSorter(new EmptyTableRowSorter(model));
	}

	/**
	 * Set the color for the stripes of this CardTable.
	 *
	 * @param col new stripe color
	 */
	public void setStripeColor(Color col)
	{
		stripeColor = col;
		repaint();
	}
}
