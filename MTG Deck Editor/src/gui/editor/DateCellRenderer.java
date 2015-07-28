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
 * This class represents a renderer for drawing cells with Dates in them.  It will format the
 * date according to the format specified by @link{database.Deck#DATE_FORMAT}.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class DateCellRenderer extends DefaultTableCellRenderer
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
			datePanel.setForeground(c.getForeground());
			datePanel.setBackground(c.getBackground());
			datePanel.add(new JLabel(Deck.DATE_FORMAT.format((Date)value)));
			return datePanel;
		}
		else
			return c;
	}
}
