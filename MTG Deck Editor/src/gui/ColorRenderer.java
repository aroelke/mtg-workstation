package gui;

import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

import database.characteristics.MTGColor;
import database.symbol.ColorSymbol;

/**
 * TODO: Comment this
 * @author Alec
 */
@SuppressWarnings("serial")
public class ColorRenderer extends DefaultTableCellRenderer
{
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		if (value instanceof MTGColor.Tuple)
		{
			MTGColor.Tuple colors = (MTGColor.Tuple)value;
			JPanel colorPanel = new JPanel();
			colorPanel.setLayout(new BoxLayout(colorPanel, BoxLayout.X_AXIS));
			colorPanel.setBorder(new EmptyBorder(0, 1, -1, 0));
			for (MTGColor color: colors)
				colorPanel.add(new JLabel(ColorSymbol.SYMBOLS.get(color).getIcon(13)));
			if (isSelected)
			{
				colorPanel.setBackground(table.getSelectionBackground());
				colorPanel.setForeground(table.getSelectionForeground());
			}
			else
			{
				colorPanel.setBackground(table.getBackground());
				colorPanel.setForeground(table.getForeground());
			}
			return colorPanel;
		}
		else
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	}
}
