package util;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
public class StripedTable extends JTable
{
	private Color stripeColor;
	
	public StripedTable()
	{
		super();
		stripeColor = Color.LIGHT_GRAY;
	}
	
	public StripedTable(TableModel model)
	{
		super(model);
		stripeColor = Color.LIGHT_GRAY;
	}
	
	public void setStripeColor(Color col)
	{
		stripeColor = col;
		repaint();
	}
	
	@Override
	public boolean getScrollableTracksViewportWidth()
	{
		return getPreferredSize().width < getParent().getWidth();
	}
	
	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
	{
		Component c = super.prepareRenderer(renderer, row, column);
		if (!isRowSelected(row))
			c.setBackground(row%2 == 0 ? getBackground() : stripeColor);
		return c;
	}
}
