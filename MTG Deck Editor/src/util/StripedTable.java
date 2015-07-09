package util;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 * This class represents a table whose alternating occupied rows will be different
 * colors.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class StripedTable extends JTable
{
	/**
	 * Color of the alternate rows.
	 */
	private Color stripeColor;
	
	/**
	 * Create a new StripedTable with the default color.
	 */
	public StripedTable()
	{
		super();
		stripeColor = Color.LIGHT_GRAY;
	}
	
	/**
	 * Create a new StripedTable with the default color and given model.
	 * 
	 * @param model Model for the new table.
	 */
	public StripedTable(TableModel model)
	{
		super(model);
		stripeColor = Color.LIGHT_GRAY;
	}
	
	/**
	 * Set the color for the stripes of this StripedTable.
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
		if (!isRowSelected(row))
			c.setBackground(row%2 == 0 ? getBackground() : stripeColor);
		return c;
	}
}
