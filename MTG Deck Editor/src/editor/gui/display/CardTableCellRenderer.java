package editor.gui.display;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;

import editor.collection.category.CategorySpec;
import editor.collection.deck.Deck;
import editor.database.card.Card;
import editor.database.characteristics.CombatStat;
import editor.database.characteristics.Loyalty;
import editor.database.characteristics.ManaCost;
import editor.database.characteristics.ManaType;
import editor.database.symbol.ColorSymbol;
import editor.database.symbol.Symbol;
import editor.util.CollectionUtils;
import editor.util.UnicodeSymbols;

/**
 * This class represents a cell renderer for a {@link CardTable}.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class CardTableCellRenderer extends DefaultTableCellRenderer
{
	/**
	 * {@inheritDoc}
	 * Several types of data get special renderings:
	 * <ul>
	 * <li>Mana costs are displayed using their symbols
	 * <li>Converted mana costs and combat stats are displayed using special delimiters
	 * <li>Colors and color identity are displayed using the mana symbols corresponding to the colors
	 * <li>Deck categories are displayed using colored squares
	 * <li>Date added is displayed according to {@link Deck#DATE_FORMATTER}
	 * </ul>
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (table.getModel() instanceof CardTableModel)
		{
			JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			Border border = BorderFactory.createEmptyBorder(1, 1, 1, 1);
			StringJoiner join = new StringJoiner(" " + Card.FACE_SEPARATOR + " ");
			switch (((CardTableModel)table.getModel()).getColumnData(column))
			{
			case MANA_COST:
				panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
				List<ManaCost> cost = CollectionUtils.convertToList(value, ManaCost.class);
				border = BorderFactory.createEmptyBorder(0, 1, cost.size() == 1 ? -1 : 0, 0);
				for (int i = 0; i < cost.size(); i++)
				{
					if (!cost.get(i).isEmpty())
					{
						if (i > 0)
						{
							panel.add(Box.createHorizontalStrut(3));
							panel.add(new JLabel(Card.FACE_SEPARATOR));
							panel.add(Box.createHorizontalStrut(3));
						}
						for (Symbol sym: cost.get(i))
							panel.add(new JLabel(sym.getIcon(13)));
					}
				}
				break;
			case CMC:
				panel.add(new JLabel(CollectionUtils.join(join, CollectionUtils.convertToList(value, Double.class))));
				break;
			case COLORS: case COLOR_IDENTITY:
				panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
				for (ManaType color: CollectionUtils.convertToList(value, ManaType.class))
					panel.add(new JLabel(ColorSymbol.SYMBOLS.get(color).getIcon(13)));
				break;
			case POWER: case TOUGHNESS:
				panel.add(new JLabel(CollectionUtils.join(join, CollectionUtils.convertToList(value, CombatStat.class))));
				break;
			case LOYALTY:
				panel.add(new JLabel(CollectionUtils.join(join, CollectionUtils.convertToList(value, Loyalty.class))));
				break;
			case CATEGORIES:
				List<CategorySpec> categories = new ArrayList<CategorySpec>(CollectionUtils.convertToSet(value, CategorySpec.class));
				Collections.sort(categories, (a, b) -> a.getName().compareTo(b.getName()));
				panel = new JPanel()
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
							g.setColor(categories.get(i).getColor());
							g.fillRect(x, y, s - 3, s - 3);
							g.setColor(Color.BLACK);
							g.drawRect(x, y, s - 3, s - 3);
						}
					}
				};
				if (!categories.isEmpty())
				{
					StringBuilder tooltip = new StringBuilder();
					tooltip.append("<html>Categories:<br>");
					for (CategorySpec category: categories)
						tooltip.append(String.valueOf(UnicodeSymbols.BULLET) + " ").append(category.getName()).append("<br>");
					tooltip.append("</html>");
					panel.setToolTipText(tooltip.toString());
				}
				break;
			case DATE_ADDED:
				panel.add(new JLabel(Deck.DATE_FORMATTER.format((LocalDate)value)));
				break;
			default:
				return c;
			}
			if (hasFocus)
				panel.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
			else
				panel.setBorder(border);
			panel.setForeground(c.getForeground());
			panel.setBackground(c.getBackground());
			c = panel;
		}
		return c;
	}
}
