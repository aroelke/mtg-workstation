package gui.editor;

import java.awt.Component;
import java.awt.FlowLayout;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

import database.Deck;

/**
 * TODO: Comment this
 * @author Alec
 *
 */
@SuppressWarnings("serial")
public class DateCellRenderer extends DefaultTableCellRenderer
{
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (value instanceof Date)
		{
			JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			if (hasFocus)
				datePanel.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
			datePanel.setForeground(c.getForeground());
			datePanel.setBackground(c.getBackground());
			datePanel.add(new JLabel(Deck.DATE_FORMAT.format((Date)value)));
			return datePanel;
		}
		else
			return c;
	}
}
