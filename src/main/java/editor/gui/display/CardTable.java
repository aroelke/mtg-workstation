package editor.gui.display;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import editor.database.attributes.CardAttribute;
import editor.database.attributes.ManaCost;
import editor.database.attributes.OptionalAttribute;
import editor.database.card.Card;
import editor.gui.editor.EditorFrame;
import editor.gui.editor.InclusionCellEditor;
import editor.gui.generic.SpinnerCellEditor;
import editor.util.CollectionUtils;

/**
 * This class represents a table whose alternating occupied rows will be different
 * colors.
 *
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class CardTable extends JTable
{
    /**
     * Set of CardAttribute that should not use toString to convert non-comparable data.
     */
    private static final Set<CardAttribute> NO_STRING = Set.of(CardAttribute.MANA_COST,
                                                               CardAttribute.CMC,
                                                               CardAttribute.COLORS,
                                                               CardAttribute.COLOR_IDENTITY,
                                                               CardAttribute.POWER,
                                                               CardAttribute.TOUGHNESS,
                                                               CardAttribute.LOYALTY,
                                                               CardAttribute.CATEGORIES);

    /**
     * This class represents a sorter that sorts a table column whose empty cells are invalid values.
     * Currently this only applies for power and toughness columns.  Those cells are always placed
     * last in the column.
     *
     * @author Alec Roelke
     */
    private static class EmptyTableRowSorter extends TableRowSorter<TableModel>
    {
        /**
         * This EmptyTableRowSorter's model for data.
         */
        private TableModel model;

        /**
         * Create a new EmptyTableRowSorter.
         *
         * @param m Model for data.
         */
        public EmptyTableRowSorter(TableModel m)
        {
            super(m);
            model = m;
        }

        /**
         * {@inheritDoc}
         * Empty cells are always sorted last.
         */
        @Override
        public Comparator<?> getComparator(int column)
        {
            if (model instanceof CardTableModel)
            {
                boolean ascending = getSortKeys().get(0).getSortOrder() == SortOrder.ASCENDING;
                CardAttribute attribute = ((CardTableModel)model).getColumnData(column);
                // Have to special-case P/T/L so they are always last if missing
                return switch (attribute) {
                    case POWER, TOUGHNESS, LOYALTY -> (a, b) -> {
                        OptionalAttribute first = CollectionUtils.convertToList(a, OptionalAttribute.class).stream().filter(OptionalAttribute::exists).findFirst().orElse(OptionalAttribute.empty());
                        OptionalAttribute second = CollectionUtils.convertToList(b, OptionalAttribute.class).stream().filter(OptionalAttribute::exists).findFirst().orElse(OptionalAttribute.empty());
                        if (!first.exists() && !second.exists())
                            return 0;
                        else if (!first.exists())
                            return ascending ? 1 : -1;
                        else if (!second.exists())
                            return ascending ? -1 : 1;
                        else
                            return attribute.compare(a, b);
                    };
                    default -> attribute;
                };
            }
            else
                return super.getComparator(column);
        }

        /**
         * {@inheritDoc}
         * Don't convert to a string if the data type is part of {@link #NO_STRING}.
         */
        @Override
        protected boolean useToString(int column)
        {
            return !(model instanceof CardTableModel &&
                    NO_STRING.contains(((CardTableModel)model).getColumnData(column))) && super.useToString(column);
        }
    }

    /**
     * Create an instance of the editor for cells containing the given type of CardAttribute.
     *
     * @param frame frame containing the table with the cell to edit
     * @param type  type of data to edit
     * @return an instance of the editor for the given type of data
     * @throws IllegalArgumentException if the given type of CardAttribute can't be edited
     */
    public static TableCellEditor createCellEditor(EditorFrame frame, CardAttribute type) throws IllegalArgumentException
    {
        return switch (type) {
            case COUNT -> new SpinnerCellEditor();
            case CATEGORIES -> new InclusionCellEditor(frame);
            default -> throw new IllegalArgumentException("CardAttribute type " + type + " can't be edited.");
        };
    }

    /**
     * Color of the alternate rows.
     */
    private Color stripeColor;

    /**
     * Create a new CardTable with the default color.
     */
    public CardTable()
    {
        super();
        init();
    }

    /**
     * Create a new CardTable with the default color and given model.
     *
     * @param model model for the new table.
     */
    public CardTable(TableModel model)
    {
        super(model);
        init();
    }

    /**
     * Get the background color of the specified row.
     *
     * @param row row to get the color of
     * @return the background color of the row at the given index of this CardTable.
     */
    public Color getRowColor(int row)
    {
        return row % 2 == 0 ? new Color(getBackground().getRGB()) : stripeColor;
    }

    @Override
    public boolean getScrollableTracksViewportWidth()
    {
        return getPreferredSize().width < getParent().getWidth();
    }

    /**
     * {@inheritDoc}
     * Tooltips are only available for cells whose contents don't fit.
     */
    @Override
    public String getToolTipText(MouseEvent e)
    {
        String tooltip = super.getToolTipText(e);
        if (tooltip == null)
        {
            Point p = e.getPoint();
            int col = columnAtPoint(p);
            int row = rowAtPoint(p);
            if (col >= 0 && row >= 0)
            {
                Rectangle bounds = getCellRect(row, col, false);
                JComponent c = (JComponent)prepareRenderer(getCellRenderer(row, col), row, col);
                if (c.getPreferredSize().width > bounds.width)
                {
                    if (((CardTableModel)getModel()).getColumnData(col) == CardAttribute.MANA_COST)
                        tooltip = "<html>" + String.join(Card.FACE_SEPARATOR, CollectionUtils.convertToList(getValueAt(row, col), ManaCost.class).stream().map(ManaCost::toHTMLString).collect(Collectors.toList())) + "</html>";
                    else
                        tooltip = "<html>" + String.valueOf(getValueAt(row, col)) + "</html>";
                }
            }
        }
        return tooltip;
    }

    /**
     * Initialize the table.
     */
    private void init()
    {
        stripeColor = Color.LIGHT_GRAY;

        setFillsViewportHeight(true);
        setShowGrid(false);

        TableCellRenderer renderer = new CardTableCellRenderer();
        for (CardAttribute type : CardAttribute.displayableValues())
            setDefaultRenderer(type.dataType(), renderer);
        setRowSorter(new EmptyTableRowSorter(getModel()));
    }

    /**
     * {@inheritDoc}
     * Changes the renderer's background according to the stripe color.
     */
    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
    {
        Component c = super.prepareRenderer(renderer, row, column);
        if (!isRowSelected(row) || !getRowSelectionAllowed())
            c.setBackground(getRowColor(row));
        return c;
    }

    @Override
    public void setModel(TableModel model)
    {
        super.setModel(model);
        setRowSorter(new EmptyTableRowSorter(model));
    }

    /**
     * Set the color for the stripes of this CardTable.
     *
     * @param col new stripe color
     */
    public void setStripeColor(Color col)
    {
        stripeColor = col;
        repaint();
    }
}
