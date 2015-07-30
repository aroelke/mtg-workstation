package gui;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

import database.Card;
import database.ManaCost;
import database.symbol.Symbol;

/**
 * This class represents a renderer of ManaCosts in a table.
 * 
 * @author Alec
 */
@SuppressWarnings("serial")
public class ManaCostCellRenderer extends DefaultTableCellRenderer
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
		if (value instanceof ManaCost[])
		{
			ManaCost[] cost = (ManaCost[])value;
			JPanel costPanel = new JPanel();
			for (int i = 0; i < cost.length; i++)
			{
				costPanel.setLayout(new BoxLayout(costPanel, BoxLayout.X_AXIS));
				if (hasFocus)
					costPanel.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
				else
					costPanel.setBorder(new EmptyBorder(0, 1, -1, 0));
				costPanel.setForeground(c.getForeground());
				costPanel.setBackground(c.getBackground());
				for (Symbol sym: cost[i].symbols())
					costPanel.add(new JLabel(sym.getIcon(13)));
				if (i < cost.length - 1)
				{
					costPanel.add(Box.createHorizontalStrut(3));
					costPanel.add(new JLabel(Card.FACE_SEPARATOR));
					costPanel.add(Box.createHorizontalStrut(3));
				}
			}
			return costPanel;
		}
		else
			return c;
	}
}
