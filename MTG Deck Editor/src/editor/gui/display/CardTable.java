package editor.gui.display;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import editor.database.characteristics.CardData;
import editor.database.characteristics.CombatStat;
import editor.database.characteristics.ManaCost;
import editor.gui.editor.EditorFrame;
import editor.gui.editor.InclusionCellEditor;
import editor.gui.generic.SpinnerCellEditor;
import editor.util.CollectionUtils;

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
	 * Set of CardData that should not use toString to convert non-comparable data.
	 */
	private static final Set<CardData> NO_STRING = Stream.of(CardData.CMC, CardData.POWER, CardData.TOUGHNESS, CardData.LOYALTY, CardData.CATEGORIES).collect(Collectors.toSet());
	
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
			CardData data;
			if (model instanceof CardTableModel)
			{
				data = ((CardTableModel)model).getColumnData(column);
				boolean ascending = getSortKeys().get(0).getSortOrder() == SortOrder.ASCENDING;
				switch (data)
				{
				case MANA_COST:
					return (a, b) -> CollectionUtils.convertToList(a, ManaCost.class).get(0).compareTo(CollectionUtils.convertToList(b, ManaCost.class).get(0));
				case CMC:
					return (a, b) -> Double.compare(Collections.min(CollectionUtils.convertToList(a, Double.class)), Collections.min(CollectionUtils.convertToList(b, Double.class)));
				case POWER: case TOUGHNESS:
					return (a, b) -> {
						CombatStat first = CollectionUtils.convertToList(a, CombatStat.class).stream().filter(CombatStat::exists).findFirst().orElse(CombatStat.NO_COMBAT);
						CombatStat second = CollectionUtils.convertToList(b, CombatStat.class).stream().filter(CombatStat::exists).findFirst().orElse(CombatStat.NO_COMBAT);
						if (!first.exists() && !second.exists())
							return 0;
						else if (!first.exists())
							return ascending ? 1 : -1;
						else if (!second.exists())
							return ascending ? -1 : 1;
						else
							return first.compareTo(second);
					};
				case LOYALTY:
					return (a, b) -> {
						int first = CollectionUtils.convertToList(a, Integer.class).stream().filter((l) -> l > 0).findFirst().orElse(0);
						int second = CollectionUtils.convertToList(b, Integer.class).stream().filter((l) -> l > 0).findFirst().orElse(0);
						if (first == 0 && second == 0)
							return 0;
						else if (first == 0)
							return ascending ? 1 : -1;
						else if (second == 0)
							return ascending ? -1 : 1;
						else
							return Integer.compare(first, second);
					};
				default:
					return super.getComparator(column);
				}
			}
			else
				return super.getComparator(column);
		}
		
		/**
		 * {@inheritDoc}
		 * Don't convert to a string if the data type is part of {@link #NO_STRING}.
		 */
		@Override
		protected boolean useToString(int column)
		{
			if (model instanceof CardTableModel &&
					!NO_STRING.contains(((CardTableModel)model).getColumnData(column)))
				return false;
			else
				return super.useToString(column);
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

		TableCellRenderer renderer = new CardTableCellRenderer();
		for (CardData type: CardData.values())
			setDefaultRenderer(type.dataType, renderer);
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
