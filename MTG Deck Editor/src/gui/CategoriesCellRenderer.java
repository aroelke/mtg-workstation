package gui;

import java.awt.Component;
import java.awt.Graphics;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

@SuppressWarnings("serial")
public class CategoriesCellRenderer extends DefaultTableCellRenderer
{
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (value instanceof List)
		{
			JPanel panel = new JPanel()
			{
				@Override
				public void paintComponent(Graphics g)
				{
					super.paintComponent(g);
				}
			};
			if (hasFocus)
				panel.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
			panel.setForeground(c.getForeground());
			panel.setBackground(c.getBackground());
			return panel;
		}
		return c;
	}
}
