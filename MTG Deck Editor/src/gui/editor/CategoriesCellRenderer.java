package gui.editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

import database.Deck;

/**
 * This class represents a renderer that displays which categories a card is in.  Colored boxes
 * correspond to the colors of the categories a card belongs to.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class CategoriesCellRenderer extends DefaultTableCellRenderer
{
	/**
	 * Create a panel that draws boxes whose colors correspond to the card in the given row.
	 * 
	 * @param table Table containing the element to draw
	 * @param value Value to draw
	 * @param isSelected Whether or not the cell is selected
	 * @param hasFocus Whether or not the cell has focus
	 * @param row Row of the cell being rendered
	 * @param column Column of the cell being rendered
	 * @return A component, which will be a JPanel containing several squares if the given value is a List<Deck.Category>.
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (value instanceof List)
		{
			List<Deck.Category> categories = ((List<?>)value).stream().filter((o) -> o instanceof Deck.Category).map((o) -> (Deck.Category)o).collect(Collectors.toList());
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
						g.setColor(categories.get(i).color());
						g.fillRect(x, y, s - 2, s - 2);
						g.setColor(Color.BLACK);
						g.drawRect(x, y, s - 2, s - 2);
					}
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
