package editor.gui.display;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;

import editor.collection.deck.Category;
import editor.collection.deck.Deck;
import editor.database.attributes.CombatStat;
import editor.database.attributes.Loyalty;
import editor.database.attributes.ManaCost;
import editor.database.attributes.ManaType;
import editor.database.card.Card;
import editor.database.symbol.ColorSymbol;
import editor.util.CollectionUtils;
import editor.util.UnicodeSymbols;

/**
 * This class represents a cell renderer for a {@link CardTable}.
 *
 * @author Alec Roelke
 */
public class CardTableCellRenderer extends DefaultTableCellRenderer
{
    /** Internal cache of icon sets to speed up resizing. */
    private Map<List<ManaCost>, Icon[][]> cache;

    /**
     * Create a new CardTableCellRenderer.
     */
    public CardTableCellRenderer()
    {
        super();
        cache = new HashMap<>();
    }

    /**
     * {@inheritDoc}
     * Several types of data get special renderings:
     * <ul>
     * <li>Mana costs are displayed using their symbols
     * <li>Mana values and combat stats are displayed using special delimiters
     * <li>Colors and color identity are displayed using the mana symbols corresponding to the colors
     * <li>Deck categories are displayed using colored squares
     * <li>Date added is displayed according to {@link Deck#DATE_FORMATTER}
     * </ul>
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (table.getModel() instanceof CardTableModel m)
        {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            Border border = BorderFactory.createEmptyBorder(1, 1, 1, 1);
            StringJoiner join = new StringJoiner(Card.FACE_SEPARATOR);
            switch (m.columns().apply(column))
            {
            case MANA_COST:
                panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
                var icons = cache.computeIfAbsent(
                    CollectionUtils.convertToList(value, ManaCost.class),
                    (cost) -> cost.stream().map((l) -> l.stream().map((s) -> s.getIcon(13)).toArray(Icon[]::new)).toArray(Icon[][]::new)
                );
                border = BorderFactory.createEmptyBorder(0, 1, icons.length == 1 ? -1 : 0, 0);
                for (int i = 0; i < icons.length; i++)
                {
                    if (icons[i].length > 0)
                    {
                        if (i > 0)
                            panel.add(new JLabel(Card.FACE_SEPARATOR));
                        for (int j = 0; j < icons[i].length; j++)
                            panel.add(new JLabel(icons[i][j]));
                    }
                }
                break;
            case MANA_VALUE:
                double manaValue = value == null ? 0 : (Double)value;
                panel.add(new JLabel(manaValue == (int)manaValue ? Integer.toString((int)manaValue) : Double.toString(manaValue)));
                break;
            case COLORS:
            case COLOR_IDENTITY:
                panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
                for (ManaType color : CollectionUtils.convertToList(value, ManaType.class))
                    panel.add(new JLabel(ColorSymbol.SYMBOLS.get(color).getIcon(13)));
                break;
            case POWER:
            case TOUGHNESS:
                panel.add(new JLabel(CollectionUtils.join(join, CollectionUtils.convertToList(value, CombatStat.class))));
                break;
            case LOYALTY:
                panel.add(new JLabel(CollectionUtils.join(join, CollectionUtils.convertToList(value, Loyalty.class))));
                break;
            case CATEGORIES:
                var categories = new ArrayList<>(CollectionUtils.convertToSet(value, Category.class));
                categories.sort(Comparator.comparing(Category::getName));
                panel = new JPanel()
                {
                    @Override
                    public void paintComponent(Graphics g)
                    {
                        super.paintComponent(g);
                        int s = getHeight();
                        for (int i = 0; i < categories.size(); i++)
                        {
                            int x = i * (s + 1) + 1;
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
                    for (Category category : categories)
                        tooltip.append(UnicodeSymbols.BULLET).append(" ").append(category.getName()).append("<br>");
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
