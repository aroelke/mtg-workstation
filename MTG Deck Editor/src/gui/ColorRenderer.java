package gui;

import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

import database.characteristics.MTGColor;
import database.symbol.ColorSymbol;

/**
 * This class represents a renderer for a table cell that shows a tuple of colors.  Each
 * color is represented by its corresponding mana symbol.
 * 
 * @author Alec
 */
@SuppressWarnings("serial")
public class ColorRenderer extends DefaultTableCellRenderer
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
				colorPanel.add(new JLabel(ColorSymbol.SYMBOLS.get(color).getIcon(13)));
			colorPanel.setBackground(c.getBackground());
			colorPanel.setForeground(c.getForeground());
			return colorPanel;
		}
		else
			return c;
	}
}
